package uk.gov.hmcts.appregister.testutils.data;

import java.time.LocalDate;
import java.util.UUID;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.testutils.StringUtil;

public class NationalCourtHouseData
        implements Persistable<NationalCourtHouse.NationalCourtHouseBuilder> {

    @Override
    public NationalCourtHouse.NationalCourtHouseBuilder someMinimal() {
        UUID id = UUID.randomUUID();
        var data = NationalCourtHouse.builder();
        data.courtLocationCode(StringUtil.stripToMax(id.toString(), 10))
                .name(StringUtil.stripToMax("name " + id, 100))
                .startDate(LocalDate.now())
                .courtType("CHOA");

        return data;
    }

    @Override
    public NationalCourtHouse.NationalCourtHouseBuilder someMaximal() {
        return Persistable.super.someMaximal();
    }
}
