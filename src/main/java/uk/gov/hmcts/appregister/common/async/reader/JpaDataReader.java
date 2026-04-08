package uk.gov.hmcts.appregister.common.async.reader;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import uk.gov.hmcts.appregister.common.async.JobContext;

import java.io.IOException;
import java.util.function.Function;

public class JpaDataReader<T> implements DataReader<T>  {

    private Function<Pageable, Page<T>> getEntitySupplier;

    public JpaDataReader(Function<Pageable, Page<T>> getEntitySupplier) {
        this.getEntitySupplier = getEntitySupplier;
    }

    @Override
    public void readData(ReadPagePosition position, PageRead<T> pageImporter, JobContext jobContext) throws IOException {
        Page<T> page = getEntitySupplier.apply(convertToPageable(position));

        // loop through all pages
        while (!page.getContent().isEmpty()) {
            pageImporter.readData(page.getContent(), jobContext);
            position.setStartOffset(position.getStartOffset() + position.getPageSize());
            page = getEntitySupplier.apply(convertToPageable(position));
        }
    }

    private Pageable convertToPageable(ReadPagePosition position) {
        return Pageable.ofSize(position.getPageSize()).withPage(position.getStartOffset()
                                                                                       / position.getPageSize());
    }

    @Override
    public void close() throws IOException {

    }
}

