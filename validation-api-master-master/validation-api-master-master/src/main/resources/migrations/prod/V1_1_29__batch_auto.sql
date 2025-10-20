alter table medical_authorization drop column special_authorization;
alter table if exists rule_configuration add column apply_to_batch boolean DEFAULT false not null