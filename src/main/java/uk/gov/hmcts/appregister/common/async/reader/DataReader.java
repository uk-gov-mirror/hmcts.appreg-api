package uk.gov.hmcts.appregister.common.async.reader;

import uk.gov.hmcts.appregister.common.async.JobContext;

import java.io.Closeable;
import java.io.IOException;

/**
 * Allows us to import the job.
 */
public interface DataReader<T> extends Closeable {


    /**
     * reads the data from the reader and converts it to a list of objects.
      * @return The list of objects.
     */
    void readData(ReadPagePosition position, PageRead<T> pageImporter, JobContext jobContext) throws IOException;
}

