alter table if exists rule add column max_amount_required boolean;
update rule set max_amount_required = true where id in (1,2,4);
update rule set max_amount_required = false,
                description = 'Solo una practica (Aprobada y/o Pendiente de Aprobacion) por transaccion, entre un mismo Beneficiario y Prestador, dentro de un periodo determinado de dias',
                name = 'beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod',
                rule_type = 'AUTHORIZATION_ITEM'
where id = 3;
alter table rule alter column max_amount_required set not null;
alter table rule_configuration alter column max_amount drop not null;
update rule_configuration set max_amount = null where id = 3;
INSERT INTO rule (id, name, rule_type, days_required, max_amount_required, description)
values  (5, 'itemMaxQuantityValidation', 'AUTHORIZATION_ITEM', false, false,
         'Control de cantidades por practica segun configuracion de nomenclador');
alter table if exists nomenclator_config add column max_in_transaction integer;
update nomenclator_config set max_in_transaction = 1;
alter table nomenclator_config alter column max_in_transaction set not null;
alter table prescription alter column dtype set not null;