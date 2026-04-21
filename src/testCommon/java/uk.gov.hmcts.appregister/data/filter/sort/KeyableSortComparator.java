package uk.gov.hmcts.appregister.data.filter.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.data.filter.courtlocation.CourtLocationSortEnum;
import uk.gov.hmcts.appregister.data.filter.meta.SortMetaDataDescriptor;

/**
 * Sorts a list of keyable objects based on the sort descriptor {@link SortMetaDataDescriptor}
 * column against a keyable.
 */
@Setter
public class KeyableSortComparator<T extends Keyable> implements Comparator<T> {

    private SortMetaDataDescriptor<T> sortDescriptor;
    private boolean descending;

    @Override
    public int compare(T o1, T o2) {
        if (o1 == o2) {
            return 0;
        }

        if (o1 == null) {
            return 1;
        }

        if (o2 == null) {
            return -1;
        }

        Object v1 = sortDescriptor.getSortableValueFunction().apply(o1);
        Object v2 = sortDescriptor.getSortableValueFunction().apply(o2);

        int result;

        if (v1.equals(v2)) {
            result = compareIds(o1, o2);
        } else {
            if (v1 instanceof String s1 && v2 instanceof String s2) {
                result = s1.compareTo(s2);
            } else if (v1 instanceof Comparable<?> c1 && v1.getClass().isInstance(v2)) {
                @SuppressWarnings("unchecked")
                Comparable<Object> cmp = (Comparable<Object>) c1;
                result = cmp.compareTo(v2);
            } else {
                result = 0;
            }
        }

        return descending ? -result : result;
    }

    private int compareIds(T o1, T o2) {
        return o1.getId().compareTo(o2.getId());
    }

    public void setSortDescriptor(SortMetaDataDescriptor<T> descriptor) {
        this.sortDescriptor = descriptor;
    }

    public static void main(String[] args) {

        NationalCourtHouse nationalCourtHouse = new NationalCourtHouse();
        nationalCourtHouse.setCourtLocationCode("uvWjlBWcC3");

        NationalCourtHouse nationalCourtHouse2 = new NationalCourtHouse();
        nationalCourtHouse2.setCourtLocationCode("g8Z7ZWZsz4");

        NationalCourtHouse nationalCourtHouse3 = new NationalCourtHouse();
        nationalCourtHouse3.setCourtLocationCode("TDCdi44Ih1");

        NationalCourtHouse nationalCourtHouse4 = new NationalCourtHouse();
        nationalCourtHouse4.setCourtLocationCode("CFM8NbuK92");

        // CFM8NbuK92
        //    TDCdi44Ih1
        // g8Z7ZWZsz4
        //    uvWjlBWcC3
        KeyableSortComparator comparator = new KeyableSortComparator<NationalCourtHouse>();
        comparator.setSortDescriptor(CourtLocationSortEnum.CODE.getDescriptor());

        List<NationalCourtHouse> list = new ArrayList<>();
        list.add(nationalCourtHouse);
        list.add(nationalCourtHouse2);
        list.add(nationalCourtHouse3);
        list.add(nationalCourtHouse4);

        list.sort(comparator);
        Assertions.assertTrue(comparator.compare(nationalCourtHouse, nationalCourtHouse2) > 0);
    }
}
