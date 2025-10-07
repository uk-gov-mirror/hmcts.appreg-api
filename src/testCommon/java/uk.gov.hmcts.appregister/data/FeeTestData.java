package uk.gov.hmcts.appregister.data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import uk.gov.hmcts.appregister.common.entity.Fee;
import uk.gov.hmcts.appregister.util.StringUtil;

public class FeeTestData
        implements uk.gov.hmcts.appregister.testutils.data.Persistable<Fee, Fee.FeeBuilder> {
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
