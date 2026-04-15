package uk.gov.hmcts.appregister.service;

import io.restassured.internal.common.assertion.Assertion;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import uk.gov.hmcts.appregister.common.async.AsyncJobService;
import uk.gov.hmcts.appregister.common.async.JobContext;
import uk.gov.hmcts.appregister.common.async.lifecycle.AbstractCsvWriterAsyncLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycleEvent;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.async.model.TrackJobStatusResponse;
import uk.gov.hmcts.appregister.common.async.reader.CsvReader;
import uk.gov.hmcts.appregister.common.async.reader.JpaDataReader;
import uk.gov.hmcts.appregister.common.async.reader.ReadPagePosition;
import uk.gov.hmcts.appregister.common.async.writer.CsvWriter;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode_;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationCodeRepository;
import uk.gov.hmcts.appregister.common.enumeration.YesOrNo;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.data.ApplicationCodeTestData;
import uk.gov.hmcts.appregister.generated.model.JobStatus1;
import uk.gov.hmcts.appregister.generated.model.JobType;
import uk.gov.hmcts.appregister.testutils.BaseIntegration;
import uk.gov.hmcts.appregister.testutils.csv.ApplicationCodeCsvPojo;
import uk.gov.hmcts.appregister.testutils.csv.JobProcessCsvReadLifecycle;
import uk.gov.hmcts.appregister.testutils.csv.JobProcessCsvWriteLifecycle;
import uk.gov.hmcts.appregister.testutils.token.TokenGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.when;


public class AsyncJobServiceImplTest  extends BaseIntegration {
    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ApplicationCodeRepository applicationCodeRepository;

    @BeforeEach
    public void setUp() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal())
            .thenReturn(TokenGenerator.builder().build().getJwtFromToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testRunAsyncJobReadingPagedCsv() throws Exception {
        JobTypeRequest request = JobTypeRequest.builder().jobType(JobType.FEES_REPORT)
            .userName(userProvider.getUserId()).build();
        CsvReader<ApplicationCodeCsvPojo> csvReaderForAppCode
            = new CsvReader<>(
            getClass().getResourceAsStream("/appcodes.csv"),
            ApplicationCodeCsvPojo.class
        );
        JobProcessCsvReadLifecycle jobProcessCsvReadLifecycle
            = new JobProcessCsvReadLifecycle(applicationCodeRepository);

        TrackJobStatusResponse response = asyncJobService.startJob(
            request,
            csvReaderForAppCode,
            jobProcessCsvReadLifecycle
        );

        Assertions.assertNotNull(response.getJobId());

        // wait for end of async job
        response.getFuture().get();

        ReadPagePosition position = new ReadPagePosition(0, 2);

        JobContext jobContext = new JobContext();

        // ensure we have processed all of the csv records
        Assertions.assertEquals(10, jobProcessCsvReadLifecycle.getCountProcessed());

        try (CsvReader<ApplicationCodeCsvPojo> csvReader = new CsvReader<>(
            getClass().getResourceAsStream("/appcodes.csv"),
            ApplicationCodeCsvPojo.class
        )) {
            // verify that all application codes were created in the database
            csvReader.readData(
                position,
                (data, ctxt) -> {
                    data.forEach(applicationCode -> {
                        List<ApplicationCode> csvBaseAppCodeLst
                            = applicationCodeRepository.findByCodeAndDate(
                            applicationCode.getCode(),
                            LocalDate.now()
                        );
                        Assertions.assertEquals(1, csvBaseAppCodeLst.size());
                    });
                }, jobContext
            );
        }

        // ensure we succeeded
        Assertions.assertEquals(
            JobStatus1.COMPLETED,
            asyncJobService.getJobStatus(response.getJobId()).get().getStatus()
        );
    }

    Function<Pageable, Page<ApplicationCode>> GET_APPLICATION_CODES_FUNCTION
        = (pageable) -> {
           return applicationCodeRepository.search("CODE", null, null,
                                             pageable);
    };

    @Test
    void testRunAsyncJobWritingPagedCsv() throws Exception {
        JpaDataReader<ApplicationCode> reader
            = new JpaDataReader<>(GET_APPLICATION_CODES_FUNCTION);

        JobProcessCsvWriteLifecycle csvWriterLifecycle = new JobProcessCsvWriteLifecycle(new CsvWriter<>(
                ApplicationCodeCsvPojo.class));

        JobTypeRequest request = JobTypeRequest.builder().jobType(JobType.FEES_REPORT)
            .userName(userProvider.getUserId()).build();


        // run the job writing the csv as we go eventually writing it to the database
        TrackJobStatusResponse response = asyncJobService.startJob(
            request,
            reader,
            csvWriterLifecycle
        );

        // wait for end of async job
        response.getFuture().get();
    }

    @Test
    void testRunAsyncJobFailValidationPagedCsv() throws Exception {

    }

}
