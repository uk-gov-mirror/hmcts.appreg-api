package uk.gov.hmcts.appregister.common.async.writer;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderNameBaseMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;

import com.opencsv.bean.StatefulBeanToCsvBuilder;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.extern.slf4j.Slf4j;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import org.aspectj.apache.bcel.generic.RET;

import uk.gov.hmcts.appregister.common.async.CsvPojo;
import uk.gov.hmcts.appregister.common.async.JobContext;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.reader.ReadPagePosition;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CsvWriter<T extends CsvPojo> implements PageWrite<T> {
    private File file;

    private static final char DEFAULT_DELIMITER = '|';

    NoHeaderStrategy<T> noHeaderStrategy;

    private Class<T> tClass;

    public CsvWriter(Class<T> tClass) throws IOException {
        this.file = File.createTempFile(UUID.randomUUID().toString(), ".csv");
        this.tClass = tClass;
        noHeaderStrategy = new NoHeaderStrategy<>(file);
        noHeaderStrategy.setType(tClass);
    }

    @Override
    public void close() throws IOException {
        file.delete();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    /**
     * writes the csv to the file.
     * @param csv The records to write
     * @return The true or false if the write was successful.
     */
    public boolean write(List<T> csv, JobContext jobContext) throws IOException {
        try (FileWriter writer = new FileWriter(file, true)) {
            StatefulBeanToCsv<T> beanToCsv =
                new StatefulBeanToCsvBuilder<T>(writer)
                    .withApplyQuotesToAll(false)
                    .withSeparator(DEFAULT_DELIMITER)
                    .withMappingStrategy(noHeaderStrategy)
                    .build();

            try {
                beanToCsv.write(csv); // must pass a collection
            } catch (CsvDataTypeMismatchException dataTypeMismatchException) {
                jobContext.logError(dataTypeMismatchException.getMessage());
            } catch (CsvRequiredFieldEmptyException csvRequiredFieldEmptyException) {
                jobContext.logError(csvRequiredFieldEmptyException.getMessage());
            }
        }
        return false;
    }

    /**
     * Do not add a header if it has already been written.
     */
    public class NoHeaderStrategy<T> extends HeaderNameBaseMappingStrategy<T> {

        private final File file;

        public NoHeaderStrategy(File file) {
            this.file = file;
        }

        @Override
        public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
            if (file.length() != 0) {
                return new String[0]; // <-- prevents header from being written
            }
            return super.generateHeader(bean);
        }


    }
}
