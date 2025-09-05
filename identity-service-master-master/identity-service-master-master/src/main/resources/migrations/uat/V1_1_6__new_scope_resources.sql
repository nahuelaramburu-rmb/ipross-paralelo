INSERT INTO application_resource (id, name) VALUES
(37, 'practitioner'),
(38, 'beneficiary'),
(39, 'medical_authorization_items'),
(40, 'audit_history');

update application_scope_role
set operations = 'read,create,update'
where id = 161;

update application_scope_role
set operations = 'read,create,update,delete'
where id = 190;

update application_role set access_level = 1 where id in (8,7,6,3,2,1);
update application_role set access_level = 9 where id = 4;
update application_role set access_level = 10 where id = 5;

insert into application_scope_role (id, role_id, resource_id, operations)
values (144, 2, 5, 'read'),
       (175, 3, 5, 'read'),
       (202, 6, 7, 'all'),
       (203, 6, 10, 'read'),
       (204, 6, 6, 'read'),
       (205, 6, 12, 'read'),
       (206, 6, 37, 'create,read,delete'),
       (207, 6, 11, 'read'),
       (30, 5, 39, 'read'),
       (80, 4, 39, 'read'),
       (145, 2, 39, 'read'),
       (176, 3, 39, 'read'),
       (208, 6, 39, 'read'),
       (279, 8, 39, 'read'),
       (280, 8, 40, 'read,update');

update oauth_client_details set scope = 'token:beneficiary,create:users,create:beneficiary' where client_id = '9qsnKK70wB';