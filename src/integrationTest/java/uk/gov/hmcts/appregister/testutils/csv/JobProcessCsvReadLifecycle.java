package uk.gov.hmcts.appregister.testutils.csv;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycleEvent;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationCodeRepository;
import uk.gov.hmcts.appregister.common.enumeration.YesOrNo;
import uk.gov.hmcts.appregister.data.ApplicationCodeTestData;

import java.io.IOException;
import java.time.LocalDate;

/**
 * A test class to convert a csv pojo {@link ApplicationCodeCsvPojo} to a set of
 * database {@link ApplicationCode} objects.
 */
@RequiredArgsConstructor
@Getter
public class JobProcessCsvReadLifecycle implements AsyncJobLifecycle<ApplicationCodeCsvPojo> {
    private int countProcessed = 0;

    private final ApplicationCodeRepository applicationCodeRepository;

    @Override
    public void completed(AsyncJobLifecycleEvent<ApplicationCodeCsvPojo> event) throws IOException {
        AsyncJobLifecycle.super.completed(event);
    }

    @Override
    public void failed(AsyncJobLifecycleEvent<ApplicationCodeCsvPojo> event) throws IOException {
        AsyncJobLifecycle.super.failed(event);
    }

    @Override
    public void received(AsyncJobLifecycleEvent<ApplicationCodeCsvPojo> event) throws IOException {
        AsyncJobLifecycle.super.received(event);
    }

    @Override
    public void processing(AsyncJobLifecycleEvent<ApplicationCodeCsvPojo> event) throws IOException {
        for (ApplicationCodeCsvPojo applicationCodeCsvPojo : event.getData()) {
            ApplicationCode applicationCode = new ApplicationCodeTestData().someComplete();
            applicationCode.setTitle(applicationCodeCsvPojo.getTitle());
            applicationCode.setCode(applicationCodeCsvPojo.getCode());
            applicationCode.setWording(applicationCodeCsvPojo.getWording());
            applicationCode.setFeeDue(YesOrNo.fromValue(applicationCodeCsvPojo.getFeedue()));
            applicationCode.setStartDate(LocalDate.now().minusDays(1));
            applicationCode.setEndDate(LocalDate.now().minusDays(1));

            // save the code
            applicationCodeRepository.save(applicationCode);
            countProcessed = countProcessed + 1;
        }
    }

    @Override
    public void validating(AsyncJobLifecycleEvent<ApplicationCodeCsvPojo> event) throws IOException {
        AsyncJobLifecycle.super.validating(event);
    }
}

