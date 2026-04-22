package uk.gov.hmcts.appregister.data;

import static org.instancio.Select.field;

import org.instancio.Instancio;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;
import uk.gov.hmcts.appregister.common.entity.AppListEntryResolution;

public class AppListEntryResolutionTestData
        implements uk.gov.hmcts.appregister.testutils.data.Persistable<
                AppListEntryResolution, AppListEntryResolution.AppListEntryResolutionBuilder> {

    public static final String WORDING_1 = "Wording 1";
    public static final String WORDING_2 = "Wording 2";

    @Override
    public AppListEntryResolution.AppListEntryResolutionBuilder someMinimal() {
        return AppListEntryResolution.builder().resolutionOfficer("Officer").version(1L);
    }

    @Override
    public AppListEntryResolution someComplete() {
        Settings settings = Settings.create().set(Keys.BEAN_VALIDATION_ENABLED, true);
        return Instancio.of(AppListEntryResolution.class)
                .ignore(field(AppListEntryResolution::getId))
                .ignore(field(AppListEntryResolution::getVersion))
                .withSettings(settings)
                .create();
    }
}
