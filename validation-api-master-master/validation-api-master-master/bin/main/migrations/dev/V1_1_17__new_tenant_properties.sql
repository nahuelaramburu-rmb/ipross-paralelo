alter table if exists tenant_properties add column beneficiary_min_account_age int4;
alter table if exists tenant_properties add column holder_beneficiary_min_age int4;
update tenant_properties set beneficiary_min_account_age = 16;
update tenant_properties set holder_beneficiary_min_age = 18;
alter table tenant_properties alter column beneficiary_min_account_age set not null;
alter table tenant_properties alter column holder_beneficiary_min_age set not null;