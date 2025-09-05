alter table if exists medical_authorization add column company_id int8;
alter table if exists medical_authorization add constraint FKm5uhw0ks77ly6dg9paf33cjf8 foreign key (company_id) references company;
alter table if exists medical_authorization_item add column contract_item int8;
alter table if exists medical_authorization_item add constraint fk3x3dpe5ljxj6xdiqdv7y1go1j foreign key (contract_item) references contract_item;