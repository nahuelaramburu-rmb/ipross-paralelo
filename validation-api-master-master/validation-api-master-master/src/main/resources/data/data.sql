INSERT INTO insurance_plan_type(id, name)
VALUES (1, 'Normal'),
       (2, 'Especial')
ON CONFLICT DO NOTHING;

INSERT INTO studies (id, name)
VALUES (1, 'Sin Estudios'),
       (2, 'Primario Comenzado'),
       (3, 'Primario Completado'),
       (4, 'Secundario Comenzado'),
       (5, 'Secundario Completado'),
       (6, 'Terciario Comenzado'),
       (7, 'Terciario Completado'),
       (8, 'Universidad Comenzada'),
       (9, 'Universidad Completada')
ON CONFLICT DO NOTHING;

INSERT INTO certificate_type (id, name)
VALUES (1, 'Certificado de Estudios'),
       (2, 'Certificado de Nacido Vivo'),
       (3, 'Comprobante de Reintegro')
ON CONFLICT DO NOTHING;

INSERT INTO status_scope (id, name)
VALUES (1, 'GENERAL'),
       (2, 'VALIDACION'),
       (3, 'BENEFICIARIO'),
       (4, 'LIQUIDACION'),
       (5, 'CAJA'),
       (6, 'LOTE'),
       (7, 'TRAMITE'),
       (8, 'RECETA'),
       (9, 'HOMOCLAVE')
ON CONFLICT DO NOTHING;

INSERT INTO status (id, name, status_scope_id)
VALUES (1, 'HABILITADO', 1),
       (2, 'DESHABILITADO', 1),
       (3, 'PENDIENTE DE APROBACION', 2),
       (4, 'APROBADO', 2),
       (5, 'PARCIALMENTE APROBADO', 2),
       (6, 'RECHAZADO', 2),
       (7, 'LIQUIDACION ABIERTA', 4),
       (8, 'CON COBERTURA', 3),
       (9, 'SIN COBERTURA', 3),
       (10, 'CANCELADO', 2),
       (11, 'LIQUIDACION CERRADA', 4),
       (12, 'AUTORIZADO', 2),
       (13, 'RENDIDO', 5),
       (14, 'NO RENDIDO', 5),
       (15, 'PENDIENTE', 6),
       (16, 'ACTIVO', 6),
       (17, 'CANCELADO', 6),
       (18, 'VENCIDO', 6),
       (19, 'EN REVISION', 7),
       (20, 'APROBADO', 7),
       (21, 'RECHAZADO', 7),
       (22, 'VENCIDO', 7),
       (23, 'PENDIENTE', 8),
       (24, 'APROBADA', 8),
       (25, 'RECHAZADA', 8),
       (26, 'CANCELADA', 8),
       (27, 'UTILIZADA', 8),
       (28, 'VENCIDA', 8),
       (29, 'CONSUMIDA', 9),
       (30, 'VENCIDA', 9),
       (31, 'ANULADA', 9),
       (32, 'ACTIVA', 9)
ON CONFLICT DO NOTHING;

INSERT INTO occupation (id, name)
VALUES (1, 'Operador Industrial'),
       (2, 'Recepcionista'),
       (3, 'Operador Industrial'),
       (4, 'Otro')
ON CONFLICT DO NOTHING;

INSERT INTO authorization_type(id, name)
VALUES (1, 'CODIGO WEB (TOKEN)'),
       (2, 'NUMERO DE DOCUMENTO'),
       (3, 'CODIGO QR'),
       (4, 'HOMOCLAVE'),
       (5, 'NUMERO DE AFILIADO')
ON CONFLICT DO NOTHING;

INSERT INTO authorization_condition (id, name)
VALUES (1, 'TASA DE USO EXCEDIDA'),
       (2, 'BENEFICIARIO EN TRANSITO'),
       (3, 'LIMITE MAXIMO EXCEDIDO'),
       (4, 'LIMITE MONETARIO EXCEDIDO'),
       (5, 'EXCESO EN CONVENIO')
ON CONFLICT DO NOTHING;

INSERT INTO charge_type(id, name)
VALUES (1, 'Monto Fijo'),
       (2, 'Porcentaje')
ON CONFLICT DO NOTHING;

INSERT INTO restriction_type(id, name)
VALUES (1, 'Auditoria'),
       (2, 'Rechazo')
ON CONFLICT DO NOTHING;

INSERT INTO payment_method (id, name)
VALUES (1, 'Recibo de Sueldo'),
       (2, 'Voluntario')
ON CONFLICT DO NOTHING;

INSERT INTO tenant_properties (id, preauthorization_max_days, prescription_service, prescription_expiration_period,
                               holder_beneficiary_min_age,
                               beneficiary_min_account_age,
                               mappings,
                               tenant_id)
VALUES (1, 30, 'defaultPrescriptionServiceImpl', 'MONTHLY', 18, 16, '{}', null)
ON CONFLICT DO NOTHING;