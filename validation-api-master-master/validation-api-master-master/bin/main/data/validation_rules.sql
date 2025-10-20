insert into rule (id, name, rule_type, description, dto_class_name)
values (1, 'maxAmountOfValidationItems', 'AUTHORIZATION', 'Cantidad maxima de practicas que pueden ser incluidas simultaneamente en una misma validacion', 'com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountOnlyDTO'),
       (2, 'beneficiaryUniqueMedicalPracticeWithSamePractitionerInAPeriod', 'AUTHORIZATION_ITEM','Transaccion unica Aprobada que puede realizar un Beneficiario con un mismo Prestador dentro de un periodo determinado de dias', 'com.capacidad.validationapi.module.ruleprocessor.dto.DayOnlyDTO'),
       (3, 'secureAuthorizationBeneficiaryAgeInARegion', 'AUTHORIZATION', 'Control de QR y Token obligatorio segun edad y region (Region vacia aplica el control sobre todas las localidades)', 'com.capacidad.validationapi.module.ruleprocessor.dto.MaxAmountRegionAndNomenclatorSetDTO'),
       (4, 'timedNomenclators', 'AUTHORIZATION_ITEM', 'Control de horario para practicas medicas', 'com.capacidad.validationapi.module.ruleprocessor.dto.TimedNomenclatorSetDTO')
on conflict do nothing;

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