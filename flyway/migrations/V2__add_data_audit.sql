-- Add data_audit table

-- Version Control
-- V1.0  	Matthew Harman  12/08/2025	Initial Version
--
--

SET client_encoding TO 'UTF8';

SET check_function_bodies = false;

CREATE TABLE data_audit (
	data_id NUMERIC NOT NULL,
	schema_name varchar(32) NOT NULL,
	table_name varchar(32) NOT NULL,
	column_name varchar(32) NOT NULL,
	old_value varchar(4000),
	new_value varchar(4000),
	user_id varchar(32),
	link varchar(100),
	created_date timestamp NOT NULL,
	old_clob_value text,
	new_clob_value text,
	related_key numeric(30),
	update_type varchar(1) NOT NULL,
	data_type varchar(1000),
	case_id NUMERIC,
	related_items_identifier varchar(30),
	related_items_identifier_index varchar(30),
	event_name varchar(100),
	user_name varchar(250)
) ;
ALTER TABLE data_audit ADD CONSTRAINT data_audit_pk PRIMARY KEY (data_id);

DROP SEQUENCE IF EXISTS add_dataaudit_event;
CREATE SEQUENCE add_dataaudit_event INCREMENT 1 MINVALUE 1 NO MAXVALUE START 807035 CACHE 20;
