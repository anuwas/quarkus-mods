-- =====================================================================
-- STG seed data — 30 representative PENDING records for demo / testing
-- =====================================================================
INSERT INTO public.staging_synch_log
(id, table_name, table_reference, status, created_at, updated_at, retry_count, processed_at, node_id, notes, error_message)
VALUES
    (1, 'stg_centres', '1', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (2, 'stg_centres', '2', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (3, 'stg_centres', '3', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (4, 'stg_centres', '4', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (5, 'stg_centres', '5', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (6, 'stg_centres', '6', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (7, 'stg_centres', '7', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (8, 'stg_centres', '8', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (9, 'stg_centres', '9', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (10, 'stg_centres', '10', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (11, 'stg_centres', '11', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (12, 'stg_centres', '12', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (13, 'stg_centres', '13', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (14, 'stg_centres', '14', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (15, 'stg_centres', '15', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (16, 'stg_centres', '16', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (17, 'stg_centres', '17', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (18, 'stg_centres', '18', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (19, 'stg_centres', '19', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (20, 'stg_centres', '20', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (21, 'stg_centres', '21', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (22, 'stg_centres', '22', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (23, 'stg_centres', '23', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (24, 'stg_centres', '24', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (25, 'stg_centres', '25', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (26, 'stg_centres', '26', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (27, 'stg_centres', '27', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (28, 'stg_centres', '28', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (29, 'stg_centres', '29', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (30, 'stg_centres', '30', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (31, 'stg_student', '1', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (32, 'stg_student', '2', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (33, 'stg_student', '3', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (34, 'stg_student', '4', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (35, 'stg_student', '5', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (36, 'stg_student', '6', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (37, 'stg_student', '7', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (38, 'stg_student', '8', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (39, 'stg_student', '9', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (40, 'stg_student', '10', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (41, 'stg_student', '11', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (42, 'stg_student', '12', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (43, 'stg_student', '13', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (44, 'stg_student', '14', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (45, 'stg_student', '15', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (46, 'stg_student', '16', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (47, 'stg_student', '17', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (48, 'stg_student', '18', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (49, 'stg_student', '19', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (50, 'stg_student', '20', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (51, 'stg_student', '21', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (52, 'stg_student', '22', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (53, 'stg_student', '23', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (54, 'stg_student', '24', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (55, 'stg_student', '25', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (56, 'stg_student', '26', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (57, 'stg_student', '27', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (58, 'stg_student', '28', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (59, 'stg_student', '29', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (60, 'stg_student', '30', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL);

-- =====================================================================
-- STG seed data — 30 stg_centres records corresponding to the above
-- =====================================================================
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '1', '', now(), '', '', 'Oakridge Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '2', '', now(), '', '', 'Westfield Institute');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '3', '', now(), '', '', 'Thornberry College');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '4', '', now(), '', '', 'Maplewood Learning Hub');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '5', '', now(), '', '', 'Greenhill Centre');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '6', '', now(), '', '', 'Bridgewater School');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '7', '', now(), '', '', 'Kensington Prep');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '8', '', now(), '', '', 'Silverstone Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '9', '', now(), '', '', 'Pinecrest Institute');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '10', '', now(), '', '', 'Harborview College');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '11', '', now(), '', '', 'Redwood Training Centre');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '12', '', now(), '', '', 'Lakeview Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '13', '', now(), '', '', 'Foxborough School');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '14', '', now(), '', '', 'Elmwood Institute');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '15', '', now(), '', '', 'Northgate College');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '16', '', now(), '', '', 'Brookside Centre');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '17', '', now(), '', '', 'Willowbank Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '18', '', now(), '', '', 'Stonebridge Prep');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '19', '', now(), '', '', 'Cedarwood Institute');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '20', '', now(), '', '', 'Highland Learning Hub');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '21', '', now(), '', '', 'Ashford Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '22', '', now(), '', '', 'Riverview School');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '23', '', now(), '', '', 'Summitdale College');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '24', '', now(), '', '', 'Birchwood Centre');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '25', '', now(), '', '', 'Crestview Institute');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '26', '', now(), '', '', 'Meadowbrook Academy');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '27', '', now(), '', '', 'Thornfield Prep');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '28', '', now(), '', '', 'Glendale School');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('01', 0, '29', '', now(), '', '', 'Hawthorne College');
INSERT INTO public.stg_centres (awarding_organisation_id, batch_id, centre_id, parent_centre_id, load_timestamp, centre_status, operational_name, centre_name) VALUES('02', 0, '30', '', now(), '', '', 'Fernwood Learning Centre');

-- =====================================================================
-- STG seed data — 30 stg_student records for demo / testing
-- =====================================================================
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 12, '1', '', now(), '', '', 'James Carter');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 7, '2', '', now(), '', '', 'Emily Watson');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 23, '3', '', now(), '', '', 'Oliver Bennett');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 5, '4', '', now(), '', '', 'Sophia Mitchell');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 18, '5', '', now(), '', '', 'Liam Henderson');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 31, '6', '', now(), '', '', 'Ava Richardson');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 9, '7', '', now(), '', '', 'Noah Chambers');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 14, '8', '', now(), '', '', 'Isabella Foster');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 27, '9', '', now(), '', '', 'Ethan Brooks');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 3, '10', '', now(), '', '', 'Mia Sullivan');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 21, '11', '', now(), '', '', 'Lucas Perry');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 16, '12', '', now(), '', '', 'Charlotte Morgan');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 8, '13', '', now(), '', '', 'Alexander Reed');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 29, '14', '', now(), '', '', 'Amelia Cooper');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 11, '15', '', now(), '', '', 'Benjamin Hayes');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 4, '16', '', now(), '', '', 'Harper Griffin');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 25, '17', '', now(), '', '', 'Daniel Murphy');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 19, '18', '', now(), '', '', 'Evelyn Howard');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 6, '19', '', now(), '', '', 'Henry Barnes');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 33, '20', '', now(), '', '', 'Ella Patterson');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 2, '21', '', now(), '', '', 'Sebastian Ward');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 15, '22', '', now(), '', '', 'Grace Simmons');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 22, '23', '', now(), '', '', 'Jack Russell');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 10, '24', '', now(), '', '', 'Chloe Fisher');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 28, '25', '', now(), '', '', 'William Turner');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 1, '26', '', now(), '', '', 'Lily Edwards');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 17, '27', '', now(), '', '', 'Owen Collins');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 30, '28', '', now(), '', '', 'Zoe Stewart');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 13, '29', '', now(), '', '', 'Samuel Morris');
INSERT INTO public.stg_student (awarding_organisation_id, batch_id, student_id, parent_centre_id, load_timestamp, student_status, operational_name, student_name) VALUES('01', 20, '30', '', now(), '', '', 'Hannah Price');

