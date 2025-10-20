alter table if exists medical_authorization add column diagnosis varchar(1000);
alter table if exists medical_authorization add column disease_id int8;
alter table if exists medical_authorization add constraint FKp5o6f7dbc51ocegsikpbget2j foreign key (disease_id) references icd10_disease;
alter table if exists medical_authorization_audit_log add column diagnosis varchar(1000);
alter table if exists medical_authorization_audit_log add column diagnosis_mod boolean;
alter table if exists medical_authorization_audit_log add column disease_id int8;
alter table if exists medical_authorization_audit_log add column disease_id_mod boolean;