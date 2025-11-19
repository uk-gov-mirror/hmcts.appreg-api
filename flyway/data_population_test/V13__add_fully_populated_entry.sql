INSERT INTO application_lists (al_id, application_list_status, application_list_date, application_list_time, courthouse_code, other_courthouse, list_description, version, changed_by, changed_date, user_name, courthouse_name, duration_hour, duration_minute, cja_cja_id)
VALUES
  (11,'OPEN', '2024-04-21', '2024-04-21:10:00:00', 'RCJ001', 'other', 'Morning list for Family Court', 1, 0, CURRENT_DATE, '12345678-aaaa-bbbb-cccc-000000000001', 'Royal Courts of Justice', 6, 3, 1);


INSERT INTO application_list_entries (ale_id, al_al_id, sa_sa_id, ac_ac_id, a_na_id, r_na_id, number_of_bulk_respondents, application_list_entry_wording, case_reference, entry_rescheduled, notes, version, changed_by, changed_date, bulk_upload, sequence_number, lodgement_date) VALUES
                                                                                                                                                                                                                                                                                       (10, 11, 2, 1, 4, 2, 0,'Electronic document request with 3 respondents', '232323232', 'Y', 'Rescheduled due to missing docs',1, 1,CURRENT_TIMESTAMP, 'Y', '1', TIMESTAMP '2025-05-01 00:00:00');

INSERT INTO app_list_entry_resolutions (aler_id, rc_rc_id, ale_ale_id, al_entry_resolution_wording, al_entry_resolution_officer, version, changed_by, changed_date, user_name)  VALUES
                                                                                                                                                                                  (3,1,10,'Application granted in full.','Magistrate Jane Doe',1,1,CURRENT_TIMESTAMP, NULL);
