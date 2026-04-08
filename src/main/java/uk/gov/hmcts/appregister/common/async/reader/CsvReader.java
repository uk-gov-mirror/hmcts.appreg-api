package uk.gov.hmcts.appregister.common.async.reader;

import com.opencsv.bean.CsvToBean;

import com.opencsv.bean.CsvToBeanBuilder;

import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;

import com.opencsv.exceptions.CsvException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import org.springframework.web.multipart.MultipartFile;

import uk.gov.hmcts.appregister.common.async.CsvPojo;
import uk.gov.hmcts.appregister.common.async.JobContext;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.UUID;

/**
 * A csv importer that reads from a generic csv file and pages accordingly.
 */
@Getter
@Setter
public class CsvReader<T extends CsvPojo> implements DataReader<T>, Closeable {

    private File source;

    private Class<T> cls;

    private CsvToBean<T> csvToBean;

    private List<T> lastReadPage;

    private static final char DEFAULT_DELIMITER = '|';

    public CsvReader(MultipartFile file, Class<T> tClass) throws IOException {
        this(file.getInputStream(), tClass);
    }

    public CsvReader(File file, Class<T> tClass) throws IOException {
        this(new FileInputStream(file), tClass);
    }

    public CsvReader(InputStream is, Class<T> tClass) throws IOException {
        source = File.createTempFile(UUID.randomUUID().toString(), ".csv");
        FileOutputStream fileOutputStream = new FileOutputStream(source);
        IOUtils.copy(is, fileOutputStream);
        this.cls = tClass;
    }

    @Override
    public void close() throws IOException {
        source.delete();
    }

    @Override
    public void readData(ReadPagePosition position, PageRead<T> keyableFunction, JobContext context) throws IOException {
        List<T> listCsvData = readCsv(new FileReader(source), cls, position, context);

        // loop around all of the pages in the csv file
        while (!listCsvData.isEmpty()) {
            keyableFunction.readData(listCsvData, context);

            position.setStartOffset(position.getStartOffset() + position.getPageSize());
            listCsvData = readCsv(new FileReader(source), cls, position, context);
        }
    }

    /**
     * reads the csv file and returns the records.
     * @return The type of the records.
     */
    private <T> List<T> readCsv(Reader source, Class<T> clzz, ReadPagePosition position, JobContext context) throws IOException {
        try (Reader reader = source) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withType(clzz)
                .withSeparator(DEFAULT_DELIMITER)
                .withExceptionHandler(new ErrorToContext(context))
                .build();

            return csvToBean.stream()
                .skip(position.startOffset)
                .limit(position.pageSize)
                .toList();
        }
    }

    /**
     * gets the input stream that has built up each time a page is processed.
     * @return The input stream.
     */
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(source);
    }

    /**
     * Converts the csv exception to a job error and adds it to the job context for storage.
     */
    @RequiredArgsConstructor
    class ErrorToContext implements CsvExceptionHandler {
        private final JobContext jobContext;

        @Override
        public CsvException handleException(CsvException e) throws CsvException {
            if (jobContext != null) {
                jobContext.logError(e.getMessage());
                // ignore the exception from being thrown
                return null;
            } else {
                return e;
            }
        }
    }
}
