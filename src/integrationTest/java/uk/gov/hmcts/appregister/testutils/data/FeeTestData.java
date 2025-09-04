package uk.gov.hmcts.appregister.testutils.data;

import uk.gov.hmcts.appregister.common.entity.Fee;
import uk.gov.hmcts.appregister.testutils.StringUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class FeeTestData implements Persistable <Fee.FeeBuilder> {
    @Override
    public Fee.FeeBuilder someMinimal() {
        UUID uniqueId = UUID.randomUUID();
        Fee.FeeBuilder data = Fee.builder();
        data.reference(StringUtil.stripToMax(uniqueId.toString(), 12))
            .description("description" + uniqueId)
            .startDate(OffsetDateTime.now(ZoneOffset.UTC))
            .amount(20D)
            .build();

        return data;
    }
}
