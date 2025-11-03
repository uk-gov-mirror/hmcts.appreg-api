package uk.gov.hmcts.appregister.audit.listener.diff;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;
import uk.gov.hmcts.appregister.common.entity.TableNames;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.data.AppListTestData;
import uk.gov.hmcts.appregister.data.CriminalJusticeTestData;

public class JaVersDifferentiatorTest {
    private AuditDifferentiator getAuditDifferentiator(boolean recurseNestedObjects) {

        return new JaversDifferentiator(recurseNestedObjects);
    }

    @Test
    public void testOldWithoutNewDiff() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.id = 20L;

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.CREATE, test, null);
        Assertions.assertEquals(4, differenceList.size());

        assertOneOfDiffsExist(
                new Difference("test_entity", "adr_id", test.id.toString(), "null"),
                getDifference("test_entity", "adr_id", differenceList));

        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));

        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        test.resolutionWording,
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    private List<Difference> getDifference(
            String table, String field, List<Difference> differenceList) {
        List<Difference> differences = new ArrayList<>();
        for (Difference difference : differenceList) {
            if (difference.getTableName().equals(table)
                    && difference.getFieldName().equals(field)) {
                differences.add(difference);
            }
        }

        return differences;
    }

    private boolean assertOneOfDiffsExist(
            Difference diffToMatch, List<Difference> actualDifferences) {
        for (Difference difference : actualDifferences) {
            if (difference.equals(diffToMatch)) {
                return true;
            }
        }
        throw new AssertionFailure("Should be at least one diff found");
    }

    @Test
    public void testNewWithoutOldDiff() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.id = 20L;

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.CREATE, null, test);
        Assertions.assertEquals(4, differenceList.size());

        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "adr_id",
                        ReflectiveAuditDifferentiator.EMPTY_VALUE,
                        test.id.toString()),
                getDifference("test_entity", "adr_id", differenceList));

        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        ReflectiveAuditDifferentiator.EMPTY_VALUE,
                        test.criminalJusticeArea.getCode()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        ReflectiveAuditDifferentiator.EMPTY_VALUE,
                        test.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));

        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        ReflectiveAuditDifferentiator.EMPTY_VALUE,
                        test.resolutionWording),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testNewAndOldFailOnIdDiff() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.id = 20L;

        TestEntityAuditable test1 = new TestEntityAuditable();
        test1.id = 201L;

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        Assertions.assertThrows(
                AppRegistryException.class,
                () -> reflectiveAuditDifferentiator.diff(CrudEnum.CREATE, test, test1));
    }

    @Test
    public void testNewAndOldNull() {
        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        Assertions.assertEquals(
                0, reflectiveAuditDifferentiator.diff(CrudEnum.CREATE, null, null).size());
    }

    @Test
    public void testChangesNoDiffWithNestedParsingEnabled() {
        AppListTestData appListTestData = new AppListTestData();
        ApplicationList list = appListTestData.someComplete();
        list.setId(20L);

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, list, list);

        Assertions.assertEquals(0, differenceList.size());
    }

    @Test
    public void testChangesCustomComplexDiff() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.id = 20L;

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        TestEntityAuditable test1 = new TestEntityAuditable();
        test1.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test1.id = 20L;

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        ;
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, test, test1);

        Assertions.assertEquals(3, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        test1.criminalJusticeArea.getCode()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        test1.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        test.getResolutionWording(),
                        "null"),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testChangesCustomOldNullWithNew() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, null, test);

        Assertions.assertEquals(3, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        "null",
                        test.criminalJusticeArea.getCode()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        "null",
                        test.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        "null",
                        test.getResolutionWording()),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testChangesDiffContainingNewWithNullComplex() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.id = 23L;
        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        TestEntityAuditable test1 = new TestEntityAuditable();
        test1.id = 23L;
        test1.resolutionWording = "3254";
        test1.name = "my_name";
        test1.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test1.criminalJusticeArea.setCode(null);

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, test, test1);

        Assertions.assertEquals(4, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        "null"),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        test1.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference("test_entity", "al_entry_resolution_wording", "32", "3254"),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
        assertOneOfDiffsExist(
                new Difference("test_entity", "myname", "null", test1.name),
                getDifference("test_entity", "myname", differenceList));
    }

    @Test
    public void testChangesDiffComplexOnlyNew() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.resolutionWording = "32";
        test.name = "myname";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList = reflectiveAuditDifferentiator.diff(CrudEnum.READ, test);
        Assertions.assertEquals(4, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        "null",
                        test.criminalJusticeArea.getCode()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        "null",
                        test.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        "null",
                        test.getResolutionWording()),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
        assertOneOfDiffsExist(
                new Difference("test_entity", "myname", "null", test.name),
                getDifference("test_entity", "myname", differenceList));
    }

    @Test
    public void testNewComplexRecursionAndCollectionRecursionOff() {
        TestEntityAuditable test = new TestEntityAuditable();
        test.resolutionWording = "32";
        test.name = "myname";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(false);
        List<Difference> differenceList = reflectiveAuditDifferentiator.diff(CrudEnum.READ, test);

        Assertions.assertEquals(2, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        "null",
                        test.getResolutionWording()),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
        Assertions.assertEquals(
                new Difference("test_entity", "myname", "null", test.name), differenceList.get(1));
    }

    @Test
    public void testNewAndOldParsingWithRecursionParsingDisabledAndInfiniteRecursionDetection() {
        TestEntityAuditable test = new TestEntityAuditable();

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test.id = 123L;

        ListEntity listEntity = new ListEntity();
        listEntity.setName("ee");
        listEntity.setId(3L);

        test.entry.add(listEntity);

        TestEntityAuditable test2 = new TestEntityAuditable();
        test2.id = 123L;
        ListEntity listEntitya = new ListEntity();
        listEntitya.setName("ee1");
        listEntitya.setId(6L);

        ListEntity listEntity2 = new ListEntity();

        // simulate recursion
        listEntity2.setEntity(test2);
        listEntity2.setName("e8");
        listEntity2.setId(3L);

        test2.entry.add(listEntitya);
        test2.entry.add(listEntity2);

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, test, test2);

        Assertions.assertEquals(3, differenceList.size());
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        "null"),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        "null"),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference("test_entity", "al_entry_resolution_wording", "32", "null"),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testNewObjectWithComplexAndBasicListWithRecursionOffForComplexObjects() {
        TestEntityAuditable test = new TestEntityAuditable();

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test.id = 123L;

        ListEntity listEntity2 = new ListEntity();
        listEntity2.setName("e8");
        listEntity2.setId(3L);

        test.entry.add(listEntity2);
        test.entryStrings.addAll(List.of("test string", "test string2"));

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(false);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, null, test);

        Assertions.assertEquals(2, differenceList.size());

        assertOneOfDiffsExist(
                new Difference("test_entity", "adr_id", "null", "123"),
                getDifference("test_entity", "adr_id", differenceList));
        assertOneOfDiffsExist(
                new Difference("test_entity", "al_entry_resolution_wording", "null", "32"),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testNewAndOldWithComplexAndBasicList() {
        TestEntityAuditable test = new TestEntityAuditable();

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test.id = 123L;

        ListEntity listEntity = new ListEntity();
        listEntity.setName("e8");
        listEntity.setId(3L);

        test.entry.add(listEntity);
        test.entryStrings.addAll(List.of("test string", "test string another"));

        TestEntityAuditable test1 = new TestEntityAuditable();

        test1.resolutionWording = "32544";
        test1.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test1.id = 123L;

        ListEntity listEntity1 = new ListEntity();
        listEntity1.setId(5L);
        listEntity1.setName("e832");

        test1.entry.add(listEntity1);
        test1.entryStrings.addAll(List.of("test string", "test string another 2"));

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, test, test1);

        Assertions.assertEquals(3, differenceList.size());

        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        test1.criminalJusticeArea.getCode()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        test1.criminalJusticeArea.getDescription()),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        test.resolutionWording,
                        test1.resolutionWording),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testOldWithComplexAndBasicList() {
        TestEntityAuditable test = new TestEntityAuditable();

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test.id = 123L;

        ListEntity listEntity = new ListEntity();
        listEntity.setName("e8");
        listEntity.setId(3L);

        test.entry.add(listEntity);
        test.entryStrings.addAll(List.of("test string", "test string another"));

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(true);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.READ, test, null);

        Assertions.assertEquals(4, differenceList.size());

        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "adr_id",
                        test.id.toString(),
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference("test_entity", "adr_id", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_code",
                        test.criminalJusticeArea.getCode(),
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_code", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        TableNames.CRIMINAL_JUSTICE_AREA,
                        "cja_description",
                        test.criminalJusticeArea.getDescription(),
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference(TableNames.CRIMINAL_JUSTICE_AREA, "cja_description", differenceList));
        assertOneOfDiffsExist(
                new Difference(
                        "test_entity",
                        "al_entry_resolution_wording",
                        test.resolutionWording,
                        ReflectiveAuditDifferentiator.EMPTY_VALUE),
                getDifference("test_entity", "al_entry_resolution_wording", differenceList));
    }

    @Test
    public void testAuditForDeleteWithAnnotation() {
        TestEntityAuditable test = new TestEntityAuditable();

        test.resolutionWording = "32";
        test.criminalJusticeArea = new CriminalJusticeTestData().someComplete();
        test.id = 123L;
        test.name = "random name";

        ListEntity listEntity2 = new ListEntity();
        listEntity2.setName("e8");
        listEntity2.setId(3L);
        listEntity2.name = "my name";

        test.entry.add(listEntity2);
        test.entryStrings.addAll(List.of("test string", "test string2"));

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(false);
        List<Difference> differenceList = reflectiveAuditDifferentiator.diff(CrudEnum.DELETE, test);
        Assertions.assertEquals(1, differenceList.size());
        assertOneOfDiffsExist(
                new Difference("test_entity", "myname", "null", "random name"),
                getDifference("test_entity", "myname", differenceList));
    }

    @Test
    public void testOldAndNewAppListEntityWithSuperFields() {
        ApplicationList oldAppLst = new AppListTestData().someComplete();
        oldAppLst.setId(123L);

        ApplicationList newAppLst = new AppListTestData().someComplete();
        newAppLst.setId(123L);

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(false);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.DELETE, oldAppLst, newAppLst);
        Assertions.assertNotNull(findByField("changed_date", differenceList));
        Assertions.assertNotNull(findByField("changed_by", differenceList));
        Assertions.assertNotNull(
                oldAppLst.getDescription(),
                findByField("list_description", differenceList).getOldValue());
        Assertions.assertNotNull(
                newAppLst.getDescription(),
                findByField("list_description", differenceList).getNewValue());

        Assertions.assertEquals(
                Short.valueOf(newAppLst.getDurationMinutes()).toString(),
                findByField("duration_minute", differenceList).getNewValue());
        Assertions.assertEquals(
                Short.valueOf(oldAppLst.getDurationMinutes()).toString(),
                findByField("duration_minute", differenceList).getOldValue());

        Assertions.assertEquals(
                Short.valueOf(newAppLst.getDurationHours()).toString(),
                findByField("duration_hour", differenceList).getNewValue());
        Assertions.assertEquals(
                Short.valueOf(oldAppLst.getDurationHours()).toString(),
                findByField("duration_hour", differenceList).getOldValue());

        Assertions.assertNotNull(
                newAppLst.getTime(),
                findByField("application_list_time", differenceList).getNewValue());
        Assertions.assertNotNull(
                oldAppLst.getTime(),
                findByField("application_list_time", differenceList).getNewValue());

        Assertions.assertNotNull(
                newAppLst.getDate(),
                findByField("application_list_date", differenceList).getNewValue());
        Assertions.assertNotNull(
                oldAppLst.getDate(),
                findByField("application_list_date", differenceList).getNewValue());
    }

    @Test
    public void testNewAppListEntityWithSuperFieldsAndRecursionOff() {
        ApplicationList newAppLst = new AppListTestData().someComplete();

        // when a zero value is set it should be audited as well but it is not diffed
        // newAppLst.setDurationMinutes(Short.valueOf("0"));

        AuditDifferentiator reflectiveAuditDifferentiator = getAuditDifferentiator(false);
        List<Difference> differenceList =
                reflectiveAuditDifferentiator.diff(CrudEnum.DELETE, newAppLst);
        Assertions.assertNotNull(findByField("changed_date", differenceList));
        Assertions.assertNotNull(findByField("changed_by", differenceList));
        Assertions.assertNotNull(
                newAppLst.getDescription(),
                findByField("list_description", differenceList).getNewValue());

        Assertions.assertEquals(
                Short.valueOf(newAppLst.getDurationMinutes()).toString(),
                findByField("duration_minute", differenceList).getNewValue());

        Assertions.assertEquals(
                Short.valueOf(newAppLst.getDurationHours()).toString(),
                findByField("duration_hour", differenceList).getNewValue());

        Assertions.assertEquals(
                newAppLst.getTime().toString(),
                findByField("application_list_time", differenceList).getNewValue());

        Assertions.assertEquals(
                newAppLst.getDate().toString(),
                findByField("application_list_date", differenceList).getNewValue());
    }

    private Difference findByField(String fieldName, List<Difference> differences) {
        for (Difference difference : differences) {
            if (difference.getFieldName().equals(fieldName)) {
                return difference;
            }
        }
        return null;
    }
}
