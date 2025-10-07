package uk.gov.hmcts.appregister.data;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;

public class AppListData
        implements uk.gov.hmcts.appregister.testutils.data.Persistable<
                ApplicationList, ApplicationList.ApplicationListBuilder> {
    @Override
    public ApplicationList.ApplicationListBuilder someMinimal() {
        UUID uniqueId = UUID.randomUUID();
        return ApplicationList.builder()
                .date(OffsetDateTime.now(ZoneId.of("UTC")))
                .time(OffsetDateTime.now(ZoneId.of("UTC")))
                .listDescription("Description" + uniqueId);
    }
}
