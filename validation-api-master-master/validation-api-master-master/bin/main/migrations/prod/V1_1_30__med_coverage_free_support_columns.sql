alter table if exists medical_coverage_item add column free_max_days int4;
alter table if exists medical_coverage_item add column free_max_quantity int4;
alter table if exists medical_coverage_item_audit_log add column free_max_days int4;
alter table if exists medical_coverage_item_audit_log add column free_max_days_mod boolean;
alter table if exists medical_coverage_item_audit_log add column free_max_quantity int4;
alter table if exists medical_coverage_item_audit_log add column free_max_quantity_mod boolean;
