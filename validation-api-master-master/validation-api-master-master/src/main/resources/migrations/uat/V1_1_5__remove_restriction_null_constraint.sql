alter table medical_coverage_item alter column restriction_type_id drop not null;
alter table rule_configuration alter column restriction_type_id drop not null;