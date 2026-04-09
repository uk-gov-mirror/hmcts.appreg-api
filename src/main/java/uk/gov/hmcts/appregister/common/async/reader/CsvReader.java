package uk.gov.hmcts.appregister.common.async.reader;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;
import com.opencsv.exceptions.CsvException;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.appregister.common.async.CsvPojo;
import uk.gov.hmcts.appregister.common.async.JobContext;

/**
 * A csv reader that reads pages of data from a generic csv file and pages the content according.
 * This class takes a copy of the csv file. As such, any use of this reader should use within a try
 * resources to ensure the file is deleted.
 */
@Getter
@Setter
public class CsvReader<T extends CsvPojo> implements DataReader<T>, Closeable {

    /** The file to read from. */
    private File source;

    /** The class to unmarshal into. */
    private Class<T> cls;

    /** The default csv delimiter to read. */
    private static final char DEFAULT_DELIMITER = '|';

    public CsvReader(MultipartFile file, Class<T> cls) throws IOException {
        this(file.getInputStream(), cls);
    }

    public CsvReader(File file, Class<T> cls) throws IOException {
        this(new FileInputStream(file), cls);
    }

    public CsvReader(InputStream is, Class<T> cls) throws IOException {
        source = File.createTempFile(UUID.randomUUID().toString(), ".csv");
        FileOutputStream fileOutputStream = new FileOutputStream(source);
        IOUtils.copy(is, fileOutputStream);
        this.cls = cls;
    }

    @Override
    public void close() throws IOException {
        source.delete();
    }

    @Override
    public void readData(
            ReadPagePosition position, PageReader<T> keyableFunction, JobContext context)
            throws IOException {
        List<T> listCsvData = readCsv(new FileReader(source), cls, position, context);

        // loop around all of the pages in the csv file reading them appropriately
        while (!listCsvData.isEmpty()) {
            keyableFunction.readData(listCsvData, context);

            position.setStartOffset(position.getStartOffset() + position.getPageSize());
            listCsvData = readCsv(new FileReader(source), cls, position, context);
        }
    }

    /**
     * reads the csv file and returns the records.
     *
     * @param source The reader to read from
     * @param clzz The class to unmarshal the records into.
     * @param position The position of the page to read.
     * @param context The context to log any errors.
     * @return The type of the records.
     */
    private <T> List<T> readCsv(
            Reader source, Class<T> clzz, ReadPagePosition position, JobContext context)
            throws IOException {
        try (Reader reader = source) {
            CsvToBean<T> csvToBean =
                    new CsvToBeanBuilder<T>(reader)
                            .withType(clzz)
                            .withSeparator(DEFAULT_DELIMITER)
                            .withExceptionHandler(new ErrorToJobContext(context))
                            .build();

            return csvToBean.stream().skip(position.startOffset).limit(position.pageSize).toList();
        }
    }

    /**
     * gets the input stream that has built up each time a page is processed.
     *
     * @return The input stream.
     */
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(source);
    }

    /** Converts any csv exception to a job error and adds it to the job context for storage. */
    @RequiredArgsConstructor
    class ErrorToJobContext implements CsvExceptionHandler {
        private final JobContext jobContext;

        @Override
        public CsvException handleException(CsvException e) throws CsvException {
            // add the csv error to the job context
            if (jobContext != null) {
                jobContext.logFailure(e.getMessage());
                // ignore the exception from being thrown
                return null;
            } else {
                return e;
            }
        }
    }
}
