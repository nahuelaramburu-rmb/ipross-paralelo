alter table if exists medical_authorization add column messages jsonb;
update medical_authorization set messages = '[]' where messages is null;
alter table if exists medical_coverage_item add column audit_required boolean DEFAULT false;
alter table if exists medical_coverage_item_audit_log add column audit_required boolean;
alter table if exists medical_coverage_item_audit_log add column audit_required_mod boolean;
alter table if exists audit_tray add column city_id int8;
alter table if exists audit_tray add constraint FKf4af7tf2hv9ewyqh96ofai20w foreign key (city_id) references city;