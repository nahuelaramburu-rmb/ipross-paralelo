alter table medical_authorization alter encrypted_qr_key type varchar(255);
alter table if exists medical_authorization_audit_log add column rating_id int8;
alter table if exists medical_authorization_audit_log add column rating_mod boolean;
alter table medical_authorization drop constraint ukdqkvrgkfohwjsjck6vb8bpm0e;

