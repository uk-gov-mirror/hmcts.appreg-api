package uk.gov.hmcts.appregister.testutils.csv;

import uk.gov.hmcts.appregister.common.async.lifecycle.AbstractCsvWriterAsyncLifecycle;
import uk.gov.hmcts.appregister.common.async.writer.CsvWriter;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;

import java.io.IOException;

/**
 * A test class to convert a jpa object {@link ApplicationCode}
 * to a csv pojo {@link ApplicationCodeCsvPojo} and write the data to a csv file.
 * The csv file is applied to the job that the events were source from.
 */
public class JobProcessCsvWriteLifecycle extends AbstractCsvWriterAsyncLifecycle<ApplicationCode,
    ApplicationCodeCsvPojo> {

    public JobProcessCsvWriteLifecycle(CsvWriter<ApplicationCodeCsvPojo> csvWriter) {
        super(csvWriter);
    }

    @Override
    protected ApplicationCodeCsvPojo convert(ApplicationCode data) throws IOException {
        ApplicationCodeCsvPojo pojo = new ApplicationCodeCsvPojo();
        pojo.setCode(data.getCode());
        pojo.setTitle(data.getTitle());
        pojo.setWording(data.getWording());
        pojo.setFeedue(data.getFeeDue().getValue());

        return pojo;
    }
}
