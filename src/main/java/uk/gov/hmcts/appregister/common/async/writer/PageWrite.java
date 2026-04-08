package uk.gov.hmcts.appregister.common.async.writer;

import uk.gov.hmcts.appregister.common.async.JobContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PageWrite<T> extends Closeable {
    public boolean write(List<T> csv, JobContext context) throws IOException;

    InputStream getInputStream() throws IOException;
}
