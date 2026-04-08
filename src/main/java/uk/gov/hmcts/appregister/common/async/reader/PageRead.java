package uk.gov.hmcts.appregister.common.async.reader;

import uk.gov.hmcts.appregister.common.async.JobContext;

import java.io.IOException;
import java.util.List;

/**
 * Represents a
 */
@FunctionalInterface
public interface PageRead<T> {
    /**
     * imports the page data from the csv file and converts it to a list of objects.
     * @param relatedData The related data to import.
     * @return p
     */
    boolean readData(List<T> relatedData, JobContext jobContext) throws IOException;
}
