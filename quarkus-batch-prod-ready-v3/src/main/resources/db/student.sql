CREATE TABLE public.stg_student (
        awarding_organisation_id varchar(2) NOT NULL,
        batch_id int4 NULL,
        student_id varchar(6) NOT NULL,
        parent_centre_id varchar(6) NULL,
        load_timestamp timestamp(6) NOT NULL,
        student_status varchar(20) NULL,
        operational_name varchar(100) NULL,
        student_name varchar(255) NOT NULL,
        CONSTRAINT stg_student_pkey PRIMARY KEY (student_id)
);

CREATE TABLE public.student (
        awarding_organisation_id varchar(2) NOT NULL,
        batch_id int4 NULL,
        student_id varchar(6) NOT NULL,
        parent_centre_id varchar(6) NULL,
        load_timestamp timestamp(6) NOT NULL,
        student_status varchar(20) NULL,
        operational_name varchar(100) NULL,
        student_name varchar(255) NOT NULL,
        CONSTRAINT student_pkey PRIMARY KEY (student_id)
);
