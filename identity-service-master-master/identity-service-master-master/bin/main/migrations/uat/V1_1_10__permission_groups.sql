create table application_context_permission_group (application_context_id int8 not null, application_permission_group_id int8 not null);
create sequence application_permission_group_seq increment by 50;
create table application_permission_group (id int8 not null, created_at timestamp, deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, name varchar(255) not null, primary key (id));
create sequence application_permission_suggestion_seq increment by 50;
create table application_permission_suggestion (id int8 not null, created_at timestamp, deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, description varchar(255), name varchar(255) not null, application_role_id int8 not null, primary key (id));
create table application_permission_suggestion_group (application_permission_suggestion_id int8 not null, application_permission_group_id int8 not null);
create table application_role_permission_group (application_role_id int8 not null, application_permission_group_id int8 not null);
create table permission_group_resource_operations (permission_group_id int8 not null, resource_operations varchar(255), resource varchar(255) not null, primary key (permission_group_id, resource));
alter table if exists application_context_permission_group drop constraint if exists UKd77ouiaylfot9uqyijdr9uech;

alter table if exists application_context_permission_group add constraint UKd77ouiaylfot9uqyijdr9uech unique (application_context_id, application_permission_group_id);
alter table if exists application_permission_group drop constraint if exists UK_6kusbnjokxvj7m8q0y7om4lxr;

alter table if exists application_permission_group add constraint UK_6kusbnjokxvj7m8q0y7om4lxr unique (name);
alter table if exists application_permission_suggestion drop constraint if exists UK_mgapg4qdd03yygsy7wwf8b6vb;

alter table if exists application_permission_suggestion add constraint UK_mgapg4qdd03yygsy7wwf8b6vb unique (name);
alter table if exists application_permission_suggestion_group drop constraint if exists UK8niolixnmvfc80i6svc6feb69;

alter table if exists application_permission_suggestion_group add constraint UK8niolixnmvfc80i6svc6feb69 unique (application_permission_suggestion_id, application_permission_group_id);
alter table if exists application_role_permission_group drop constraint if exists UKrcru17bxvohs6843lw4ygrh45;

alter table if exists application_role_permission_group add constraint UKrcru17bxvohs6843lw4ygrh45 unique (application_role_id, application_permission_group_id);
alter table if exists application_context_permission_group add constraint FKf8og727w9d87us9lrtboieba8 foreign key (application_permission_group_id) references application_permission_group;
alter table if exists application_context_permission_group add constraint FKt237006qgtnwdm8clp7b9wee4 foreign key (application_context_id) references application_user_context;
alter table if exists application_permission_suggestion add constraint FKla7u783b8sak6dyt971s9pvya foreign key (application_role_id) references application_role;
alter table if exists application_permission_suggestion_group add constraint FKgd9bem522bnwkfbkil9jnbv2i foreign key (application_permission_group_id) references application_permission_group;
alter table if exists application_permission_suggestion_group add constraint FKofnlfa761oi4vxyku65usukev foreign key (application_permission_suggestion_id) references application_permission_suggestion;
alter table if exists application_role_permission_group add constraint FK7ybvl2ag4f9xp4y7gjj3brjwn foreign key (application_permission_group_id) references application_permission_group;
alter table if exists application_role_permission_group add constraint FKefhurfr97fqqs979m2x3gatm4 foreign key (application_role_id) references application_role;
alter table if exists permission_group_resource_operations add constraint FKdk5rlosk9krae4m3mx1e2973y foreign key (permission_group_id) references application_permission_group;

alter table if exists application_user_context add column application_permission_suggestion_id int8;
alter table if exists application_user_context add constraint FK1d9mymwsl92wofhnm07np0htw foreign key (application_permission_suggestion_id) references application_permission_suggestion;

alter table if exists application_user_context add column permission_strategy varchar(255);
update application_user_context set permission_strategy = 'DEFAULT_ROLE';
alter table if exists application_user_context alter column permission_strategy set not null;

INSERT INTO application_permission_group (id, name)
VALUES --beneficiaries
       (1, 'Alta, Baja, Modificacion, Consulta de Afiliados'),
       (2, 'Consulta de Afiliados'),
       --practitioners
       (3, 'Alta, Baja, Modificacion, Consulta de Prestadores'),
       (4, 'Consulta de Prestadores'),
       --medical_authorizations
       (5, 'Modificacion, Consulta de Validaciones'),
       (6, 'Alta, Modificacion, Consulta de Validaciones'),
       (7, 'Consulta de Validaciones'),
       --medical_coverages
       (8, 'Alta, Baja, Modificacion, Consulta de Coberturas'),
       (9, 'Consulta de Coberturas'),
       --contracts,
       (10, 'Alta, Baja, Modificacion, Consulta de Convenios'),
       (11, 'Consulta de Convenios'),
       --organizations,
       (12, 'Alta, Baja, Modificacion, Consulta de Asociaciones'),
       (13, 'Consulta de Asociaciones'),
       --medical_centers
       (14, 'Alta, Baja, Modificacion, Consulta de Centros Medicos'),
       (15, 'Consulta de Centros Medicos'),
       --settlements,
       (16, 'Alta, Baja, Modificacion, Consulta de Liquidaciones'),
       (17, 'Alta, Modificacion, Consulta de Liquidaciones'),
       (18, 'Modificacion y Consulta de Liquidaciones'),
       (19, 'Consulta de Liquidaciones'),
       --users
       (20, 'Alta, Baja, Modificacion, Consulta de Usuarios'),
       (21, 'Alta, Baja, Consulta de Usuarios'),
       --regions
       (22, 'Alta, Baja, Modificacion, Consulta de Regiones'),
       (23, 'Consulta de Regiones'),
       --nomenclators
       (24, 'Alta, Baja, Modificacion, Consulta de Nomenclador'),
       (25, 'Consulta de Nomenclador'),
       --categories
       (26, 'Alta, Baja, Modificacion, Consulta de Categorias'),
       (27, 'Consulta de Categorias'),
       --companies
       (28, 'Alta, Baja, Modificacion, Consulta de Empresas/Organismos'),
       (29, 'Consulta de Empresas/Organismos'),
       --insurance_plans
       (30, 'Alta, Baja, Modificacion, Consulta de Planes'),
       (31, 'Consulta de Planes'),
       --reports
       (32, 'Consulta de Tablero'),
       --rules
       (33, 'Alta, Baja, Modificacion, Consulta de Reglas'),
       (34, 'Consulta de Reglas'),
       --files
       (35, 'Alta, Baja, Modificacion, Consulta de Archivos'),
       (36, 'Consulta de Archivos'),
       --audit_trays
       (37, 'Alta, Baja, Modificacion, Consulta de Bandejas'),
       (38, 'Consulta de Bandejas'),
       --audit_tray
       (39, 'Conexion a Bandejas'),
       --practitioner_budgets
       (40, 'Modificacion y Consulta de Consumo de Coseguros Voluntarios del Prestador'),
       (41, 'Consulta de Consumo de Coseguros Voluntarios del Prestador'),
       --batches
       (42, 'Alta, Baja, Modificacion, Consulta de Modulos'),
       (43, 'Consulta de Modulos'),
       --beneficiary_budgets
       (44, 'Alta, Baja, Modificacion, Consulta de Consumo de Coseguros R.S del Afiliado'),
       (45, 'Consulta de Consumo de Coseguros R.S del Afiliado'),
       --notifications
       (46, 'Modificacion y Consulta de Notificaciones'),
       --devices
       (47, 'Alta y Modificacion de Dispositivo en uso (apps)'),
       --procedures
       (48, 'Alta, Modificacion y Consulta de Tramites'),
       (49, 'Alta y Consulta de Tramites'),
       (50, 'Consulta de Tramites'),
       --properties
       (51, 'Modificacion y Consulta de Propiedades de Financiador'),
       --pre_authorizations
       (52, 'Alta, Modificacion y Consulta de Preautorizaciones/Homoclaves'),
       (53, 'Consulta de Preautorizaciones/Homoclaves'),
       --prescriptions
       (54, 'Modificacion, Consulta de Recetas'),
       (55, 'Alta, Modificacion, Consulta de Recetas'),
       (56, 'Consulta de Recetas'),
       --medicines
       (57, 'Consulta de Medicamentos'),
       --medical_authorization_items
       (58, 'Consulta de Items de Validacion'),
       --audit_history
       (59, 'Modificacion y Consulta de Validacion Auditoria (Objetos del Auditor'),
       --prescription_restrictions
       (60, 'Alta, Baja, Modificacion, Consulta de Restricciones de Receta'),
       (61, 'Consulta de Restricciones de Receta'),
       --topics
       (62, 'Alta, Baja, Modificacion, Consulta de Topicos'),
       --expiration
       (63, 'Alta, Baja, Modificacion, Consulta de Vencimientos Afiliado'),
       (64, 'Consulta de Vencimientos Afiliado'),
       --calendar
       (65, 'Alta, Baja, Modificacion, Consulta de Calendario'),
       --trade_unions
       (66, 'Alta, Baja, Modificacion, Consulta de Gremios y Coberturas'),
       (67, 'Consulta de Grupos de Permisos'),
       --CUSTOM PERMS
       (68, 'Registro de Usuarios Afiliado'),
       (69, 'Modificacion de Afiliados'),
       (70, 'Consulta de Tablero Recetas'),
       (71, 'Notificaciones Push'),
       (72, 'Importaciones')
       ON CONFLICT DO NOTHING;

-- resource_operations_key = resource_id
INSERT INTO permission_group_resource_operations(permission_group_id,resource, resource_operations)
VALUES (1, 'beneficiaries', 'all'),
       (2, 'beneficiaries', 'read'),
       (3, 'practitioners', 'all'),
       (4, 'practitioners', 'read'),
       (5, 'medical_authorizations','read,update'),
       (6, 'medical_authorizations','read,update,create'),
       (7, 'medical_authorizations','read'),
       (8, 'medical_coverages', 'all'),
       (9, 'medical_coverages', 'read'),
       (10, 'contracts', 'all'),
       (11, 'contracts', 'read'),
       (12, 'organizations', 'all'),
       (13, 'organizations', 'read'),
       (14, 'medical_centers', 'all'),
       (15, 'medical_centers', 'read'),
       (16, 'settlements', 'all'),
       (17, 'settlements', 'read,create,update'),
       (18, 'settlements', 'read,update'),
       (19, 'settlements', 'read'),
       (20, 'users', 'all'),
       (21, 'users', 'read,create,delete'),
       (22, 'regions', 'all'),
       (23, 'regions', 'read'),
       (24, 'nomenclators', 'all'),
       (25, 'nomenclators', 'read'),
       (26, 'categories', 'all'),
       (27, 'categories', 'read'),
       (28, 'companies', 'all'),
       (29, 'companies', 'read'),
       (30, 'insurance_plans', 'all'),
       (31, 'insurance_plans', 'read'),
       (32, 'reports', 'read'),
       (33, 'rules', 'all'),
       (34, 'rules', 'read'),
       (35, 'files', 'all'),
       (36, 'files', 'read'),
       (37, 'audit_trays', 'all'),
       (38, 'audit_trays', 'read'),
       (39, 'audit_tray', 'read'),
       (40, 'practitioner_budgets', 'read,update'),
       (41, 'practitioner_budgets', 'read'),
       (42, 'batches', 'all'),
       (43, 'batches', 'read'),
       (44, 'beneficiary_budgets', 'all'),
       (45, 'beneficiary_budgets', 'read'),
       (46, 'notifications', 'read,update'),
       (47, 'devices', 'create,update'),
       (48, 'procedures', 'read,create,update'),
       (49, 'procedures', 'read,create'),
       (50, 'procedures', 'read'),
       (51, 'properties', 'read,update'),
       (52, 'pre_authorizations', 'read,create,update'),
       (53, 'pre_authorizations', 'read'),
       (54, 'prescriptions', 'read,update'),
       (55, 'prescriptions', 'read,create,update'),
       (56, 'prescriptions', 'read'),
       (57, 'medicines', 'read'),
       (58, 'medical_authorization_items', 'read'),
       (59, 'audit_history', 'read,update'),
       (60, 'prescription_restrictions', 'all'),
       (61, 'prescription_restrictions', 'read'),
       (62, 'topics', 'all'),
       (63, 'expiration', 'all'),
       (64, 'expiration', 'read'),
       (65, 'calendar', 'all'),
       (66, 'trade_unions', 'all'),
       (67, 'permission_groups', 'read'),
       (68, 'beneficiary_users', 'create,delete,update'),
       (69, 'beneficiaries', 'update'),
       (70, 'report_prescriptions', 'read'),
       (71, 'notifications_push', 'create'),
       (72, 'imports', 'create')
ON CONFLICT DO NOTHING;

-- BENEFICIARY = 1
-- PRACTITIONER = 2
-- MEDICAL CENTER = 3
-- FUNDER = 4
-- ADMIN = 5
-- ORGANIZATION = 6
-- AUDITOR  = 8
INSERT INTO application_role_permission_group(application_role_id, application_permission_group_id)
VALUES --beneficiaries
       (5, 1),
       (4, 1),
       (1, 2),
       (2, 2),
       (3, 2),
       (6, 2),
       (8, 2),
       --practitioners
       (5, 3),
       (4, 3),
       (2, 4),
       (3, 4),
       (7, 4),
       (6, 4),
       (8, 4),
       --medical_authorizations
       (5, 5),
       (4, 5),
       (1, 7),
       (2, 6),
       (3, 6),
       (6, 7),
       (8, 5),
       --medical_coverages
       (5, 8),
       (4, 8),
       (6, 9),
       (8, 9),
       --contracts,
       (5, 10),
       (4, 10),
       (2, 11),
       (3, 11),
       (6, 11),
       (8, 11),
       --organizations,
       (5, 12),
       (4, 12),
       (6, 13),
       --medical_centers
       (5, 14),
       (4, 14),
       (6, 15),
       --settlements
       (5, 16),
       (4, 17),
       (2, 18),
       (3, 18),
       (6, 18),
       --users,
       (5, 20),
       (4, 20),
       (3, 21),
       (6, 21),
       --regions
       (5, 22),
       (4, 22),
       (8, 23),
       (6, 23),
       --nomenclators
       (5, 24),
       (4, 24),
       (2, 25),
       (3, 25),
       (6, 25),
       (8, 25),
       --categories
       (5, 26),
       (4, 26),
       --companies
       (5, 28),
       (4, 28),
       --insurance_plans
       (5, 30),
       (4, 30),
       (6, 31),
       (8, 31),
       --reports
       (5, 32),
       (4, 32),
       (1, 32),
       (2, 32),
       (3, 32),
       (6, 32),
        --rules
       (5, 33),
       (4, 33),
       --files
       (5, 35),
       (4, 35),
       (1, 36),
       (2, 35),
       (3, 35),
       (6, 36),
       (8, 36),
       --audit_trays
       (5, 37),
       (4, 37),
       --audit_tray
       (8, 38),
       (8, 39),
       --practitioner_budgets
       (3, 40),
       (2, 41),
       --batches
       (5, 42),
       (4, 42),
       (8, 42),
       (1, 43),
       --beneficiary_budgets
       (5, 44),
       (4, 44),
       (1, 45),
       (8, 45),
       --notifications
       (5, 46),
       (4, 46),
       (1, 46),
       (2, 46),
       (3, 46),
       (6, 46),
       (8, 46),
       --devices
       (1, 47),
       (2, 47),
       (3, 47),
       --procedures
       (5, 48),
       (4, 48),
       (1, 49),
       (8, 50),
       --properties
       (5, 51),
       (4, 51),
       --pre_authorizations
       (5, 52),
       (4, 52),
       (1, 53),
       (2, 53),
       (3, 53),
       --prescriptions
       (5, 54),
       (4, 54),
       (1, 56),
       (2, 55),
       (3, 55),
       (8, 56),
       --medicines
       (5, 57),
       (4, 57),
       (2, 57),
       (3, 57),
       (8, 57),
       --medical_authorization_items
       (5, 58),
       (4, 58),
       (2, 58),
       (3, 58),
       (6, 58),
       (8, 58),
       --audit_history
       (8, 59),
       --prescription_restrictions
       (5, 60),
       (4, 60),
       (8, 61),
       --topics
       (5, 62),
       --expiration
       (5, 63),
       (4, 63),
       (8, 64),
       --calendar
       (5, 65),
       (4, 65),
       --calendar
       (5, 66),
       (4, 66),
       --permission_groups
       (5, 67),
       (4, 67),
       --imports
       (5, 72),
       --notifications_push
       (5, 71)
ON CONFLICT DO NOTHING;

INSERT INTO application_permission_suggestion (id, name, application_role_id, description)
VALUES (1, 'Directivos', 4, 'Gestion de Afiliados, Planes y Convenios. ' ||
                            'Visualizacion de Modulos, Validaciones, Liquidaciones, Tramites, Recetas, Prestadores, Entidades, Tableros y Configuraciones ' ||
                            '(Planes, Nomenclador, Convenios y Auditoria)'),
       (2, 'Delegados y Sub Delegados', 4, 'Gestion de Afiliados y Homoclaves. ' ||
                                           'Visualizacion de Modulos, Validaciones, Tramites, Recetas, Prestadores y Configuraciones (Planes, Nomenclador, Convenios)'),
       (3, 'CAT', 4, 'Consulta, Usuarios y Edicion de Afiliados. Gestion de Homoclaves. ' ||
                  'Visualizacion de Modulos, Validaciones, Tramites, Recetas, Prestadores y Configuraciones (Planes, Nomenclador, Convenios)'),
       (4, 'Administrativo Delegacion', 4, 'Consulta, Usuarios y Edicion de Afiliados. Gestion de Homoclaves. Resolucion de Tramites ' ||
                         'Visualizacion de Modulos, Validaciones, Tramites, Recetas, Prestadores y Configuraciones (Planes, Nomenclador, Convenios)'),
       (5, 'Auditor Facturacion', 8, 'Visualizacion de Modulos, Validaciones, Liquidaciones, Recetas, Afiliados, Prestadores y Configuraciones (Planes, Nomenclador, Convenios)'),
       (6, 'Auditor Farmacia', 8, 'Visualizacion de Recetas, Afiliados, Prestadores, Tablero de Recetas'),
       (7, 'Prensa', 4, 'Visualizacion de Tableros. Notificaciones Push')
ON CONFLICT DO NOTHING;

INSERT INTO application_permission_suggestion_group (application_permission_suggestion_id, application_permission_group_id)
VALUES --Directivos
       (1, 1),
       (1, 8),
       (1, 30),
       (1, 10),
       (1, 43),
       (1, 7),
       (1, 19),
       (1, 50),
       (1, 56),
       (1, 58),
       (1, 4),
       (1, 15),
       (1, 13),
       (1, 29),
       (1, 32),
       (1, 25),
       (1, 38),
       (1, 68),
       (1, 21),
       (1, 46),
       --Delegados y Sub Delegados
       (2, 1),
       (2, 9),
       (2, 52),
       (2, 43),
       (2, 7),
       (2, 50),
       (2, 56),
       (2, 4),
       (2, 31),
       (2, 25),
       (2, 11),
       (2, 68),
       (2, 21),
       (2, 46),
       --CAT
       (3, 2),
       (3, 9),
       (3, 68),
       (3, 21),
       (3, 69),
       (3, 52),
       (3, 43),
       (3, 7),
       (3, 50),
       (3, 56),
       (3, 4),
       (3, 31),
       (3, 25),
       (3, 11),
       (3, 46),
       --Administrativo Delegacion
       (4, 2),
       (4, 9),
       (4, 68),
       (4, 21),
       (4, 69),
       (4, 52),
       (4, 43),
       (4, 7),
       (4, 48),
       (4, 56),
       (4, 4),
       (4, 31),
       (4, 25),
       (4, 11),
       (4, 46),
       --Facturacion
       (5, 2),
       (5, 9),
       (5, 43),
       (5, 7),
       (5, 18),
       (5, 56),
       (5, 4),
       (5, 31),
       (5, 25),
       (5, 11),
       (5, 58),
       --Auditor Farmacia
       (6, 2),
       (6, 4),
       (6, 70),
       (6, 56),
       --Prensa
       (7, 32),
       (7, 71),
       (7, 62),
       (7, 46)
ON CONFLICT DO NOTHING;