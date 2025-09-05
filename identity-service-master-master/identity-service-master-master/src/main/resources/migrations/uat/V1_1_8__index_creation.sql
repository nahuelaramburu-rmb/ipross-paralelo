create index concurrently if not exists application_user_username_idx on application_user (username);
create index concurrently if not exists application_user_email_idx on application_user (email);
create index concurrently if not exists application_user_user_email_idx on application_user (username, email);

create index concurrently if not exists application_scope_role_resource_tenant_role_fk_idx on application_scope_role (resource_id, tenant_id, role_id);
create index concurrently if not exists application_scope_role_resource_null_tenant_role_fk_idx on application_scope_role (resource_id, role_id) where tenant_id is null;

create index concurrently if not exists application_login_failure_principal_idx on application_login (principal) where login_event = 'FAILURE';
create index concurrently if not exists application_login_failure_ip_idx on application_login (ip_address) where login_event = 'FAILURE';
create index concurrently if not exists application_login_failure_principal_ip_idx on application_login (principal, ip_address) where login_event = 'FAILURE';

create index concurrently if not exists oauth_client_details_client_id_idx on oauth_client_details (client_id);
create index concurrently if not exists oauth_refresh_token_token_id_idx on oauth_refresh_token (token_id);