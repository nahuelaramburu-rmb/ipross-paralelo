delete from rule_configuration;
alter table if exists rule drop column days_required;
alter table if exists rule drop column max_amount_required;
alter table if exists rule add column dto_class_name varchar(255);
alter table if exists rule_configuration drop column days;
alter table if exists rule_configuration drop column max_amount;
alter table if exists rule_configuration add column data jsonb;
alter table if exists rule_configuration add column contract_id int8;
alter table if exists rule_configuration add constraint FK9taf74gmrt4ycicfw8vclexa4 foreign key (contract_id) references contract;
create table rule_property_metadata (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), data_identifier varchar(255) not null, data_key varchar(255) not null, data_key_wrapper varchar(255), data_type varchar(255) not null, description varchar(255) not null, label_key varchar(255), rule_property_type varchar(255) not null, required boolean default false not null, rule_id int8 not null, primary key (id));
alter table if exists rule_property_metadata add constraint FKe38gix5brwswmvu13q4m7r125 foreign key (rule_id) references rule;
delete from rule where id = 5;
delete from rule where id = 4;
delete from rule where id = 2;
update rule set id = 2 where id = 3;
update rule set dto_class_name = 'com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountOnlyDTO' where id = 1;
update rule set description = 'Transaccion unica Aprobada que puede realizar un Beneficiario con un mismo Prestador dentro de un periodo determinado de dias', dto_class_name = 'com.capacidad.validationapi.module.ruleprocessor.dto.DayOnlyDTO' where id = 2;
insert into rule (id, name, rule_type, dto_class_name, description) values
(3, 'secureAuthorizationBeneficiaryAgeInARegion', 'AUTHORIZATION', 'com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountRegionAndNomenclatorSetDTO', 'Control de QR y Token obligatorio segun edad y region (Region vacia aplica el control sobre todas las localidades)');
insert into rule (id, name, rule_type, description, dto_class_name) values
(4, 'timedNomenclators', 'AUTHORIZATION_ITEM', 'Control de horario para practicas medicas', 'com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorSetDTO');
insert into rule_property_metadata (id, data_identifier, data_key, data_type, description, label_key, rule_property_type, data_key_wrapper, required, rule_id)
values  (1, 'maxAmount', 'maxAmount', 'integer', 'Cantidad Maxima', null, 'RAW_VALUE', null, true, 1),
        (2, 'days', 'days', 'integer', 'Dias', null, 'RAW_VALUE', null, true, 2),
        (3, 'region', 'id', 'object', 'Region a controlar', 'name', 'REGION', null, false, 3),
        (4, 'nomenclators', 'id', 'array', 'Nomencladores a excluir', 'name', 'NOMENCLATOR', null, false, 3),
        (5, 'maxAmount', 'maxAmount', 'integer', 'Edad limite', null, 'RAW_VALUE', null, true, 3),
        (6, 'timedNomenclators', 'id', 'object', 'Nomenclador', 'name', 'NOMENCLATOR', 'nomenclator', true, 4),
        (7, 'timedNomenclators', 'timeFrom', 'time', 'Hora Desde', null, 'TIME', null, true, 4),
        (8, 'timedNomenclators', 'timeTo', 'time', 'Hora Hasta', null, 'TIME', null, true, 4),
        (9, 'maxUnsecured', 'maxUnsecured', 'integer', 'Cuota mensual regular (DNI, Credencial) permitida por centro', null, 'RAW_VALUE', null, true, 3)
on conflict do nothing;
alter table if exists failure add column extra jsonb;
alter table if exists rule_configuration drop constraint if exists UK3cs8ysp60gqlvuu1oy25yarba;
alter table if exists rule_configuration drop constraint if exists uk9lpa5hk5oap02uhlv2gox5x1v;
alter table if exists rule_configuration add constraint UK3cs8ysp60gqlvuu1oy25yarba unique (contract_id, rule_id, deleted, deletion_token, tenant_id);