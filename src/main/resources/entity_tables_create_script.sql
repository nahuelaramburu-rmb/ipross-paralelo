-- ==========================
-- SEQUENCES
-- ==========================
CREATE SEQUENCE application_permission_group_seq START 1;
CREATE SEQUENCE application_profile_seq START 1;
CREATE SEQUENCE application_resource_seq START 1;
CREATE SEQUENCE application_role_seq START 1;
CREATE SEQUENCE application_state_seq START 1;
CREATE SEQUENCE application_tenant_seq START 1;
CREATE SEQUENCE application_user_seq START 1;
CREATE SEQUENCE application_permission_suggestion_seq START 1;
CREATE SEQUENCE application_user_context_seq START 1;
CREATE SEQUENCE application_scope_role_seq START 1;
CREATE SEQUENCE application_login_seq START 1;

-- ==========================
-- TABLES BASE
-- ==========================
CREATE TABLE application_permission_group (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_permission_group_seq'),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE application_profile (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_profile_seq'),
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    id_number BIGINT NOT NULL,
    id_type VARCHAR(255) NOT NULL
);

CREATE TABLE application_resource (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_resource_seq'),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE application_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_role_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    access_level INT NOT NULL,
    resource_id_required BOOLEAN NOT NULL,
    reusable_resource_id BOOLEAN NOT NULL
);

CREATE TABLE application_state (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_state_seq'),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE application_tenant (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_tenant_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    tenant_id UUID NOT NULL UNIQUE
);

-- ==========================
-- TABLES CON DEPENDENCIAS
-- ==========================
CREATE TABLE application_user (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_user_seq'),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    resource_id UUID NOT NULL,
    sub UUID NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL,
    challenge_type VARCHAR(255),
    user_group VARCHAR(255) NOT NULL,
    profile_id BIGINT NOT NULL,
    state_id BIGINT NOT NULL,
    verification_otp INT,
    restore_otp INT,
    CONSTRAINT fk_user_profile FOREIGN KEY(profile_id) REFERENCES application_profile(id),
    CONSTRAINT fk_user_state FOREIGN KEY(state_id) REFERENCES application_state(id)
);

CREATE TABLE application_permission_suggestion (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_permission_suggestion_seq'),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    application_role_id BIGINT NOT NULL,
    CONSTRAINT fk_permission_suggestion_role FOREIGN KEY(application_role_id)
        REFERENCES application_role(id)
);

CREATE TABLE application_user_context (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_user_context_seq'),
    application_user_id BIGINT NOT NULL,
    application_tenant_id BIGINT NOT NULL,
    application_role_id BIGINT NOT NULL,
    application_permission_suggestion_id BIGINT,
    permission_strategy VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_context_user FOREIGN KEY(application_user_id) REFERENCES application_user(id),
    CONSTRAINT fk_user_context_tenant FOREIGN KEY(application_tenant_id) REFERENCES application_tenant(id),
    CONSTRAINT fk_user_context_role FOREIGN KEY(application_role_id) REFERENCES application_role(id),
    CONSTRAINT fk_user_context_permission_suggestion FOREIGN KEY(application_permission_suggestion_id) REFERENCES application_permission_suggestion(id),
    CONSTRAINT uq_user_context UNIQUE(application_user_id, application_tenant_id, application_role_id)
);

CREATE TABLE application_scope_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_scope_role_seq'),
    resource_id BIGINT NOT NULL,
    tenant_id BIGINT,
    operations TEXT[] NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_scope_role_resource FOREIGN KEY(resource_id) REFERENCES application_resource(id),
    CONSTRAINT fk_scope_role_tenant FOREIGN KEY(tenant_id) REFERENCES application_tenant(id),
    CONSTRAINT fk_scope_role_role FOREIGN KEY(role_id) REFERENCES application_role(id)
);

CREATE TABLE application_login (
    id BIGINT PRIMARY KEY DEFAULT nextval('application_login_seq'),
    principal VARCHAR(255) NOT NULL,
    principal_class VARCHAR(255) NOT NULL,
    tenant_context VARCHAR(255),
    login_event VARCHAR(255) NOT NULL,
    agent VARCHAR(255),
    ip_address VARCHAR(50)
);

-- ==========================
-- MANY-TO-MANY RELATIONSHIP TABLES
-- ==========================
CREATE TABLE application_role_permission_group (
    application_role_id BIGINT NOT NULL,
    application_permission_group_id BIGINT NOT NULL,
    CONSTRAINT fk_role_permission_role FOREIGN KEY(application_role_id) REFERENCES application_role(id),
    CONSTRAINT fk_role_permission_group FOREIGN KEY(application_permission_group_id) REFERENCES application_permission_group(id),
    CONSTRAINT uq_role_permission UNIQUE(application_role_id, application_permission_group_id)
);

CREATE TABLE application_permission_suggestion_group (
    application_permission_suggestion_id BIGINT NOT NULL,
    application_permission_group_id BIGINT NOT NULL,
    CONSTRAINT fk_permission_suggestion_group_permission FOREIGN KEY(application_permission_suggestion_id) REFERENCES application_permission_suggestion(id),
    CONSTRAINT fk_permission_suggestion_group_group FOREIGN KEY(application_permission_group_id) REFERENCES application_permission_group(id),
    CONSTRAINT uq_permission_suggestion_group UNIQUE(application_permission_suggestion_id, application_permission_group_id)
);

CREATE TABLE application_context_permission_group (
    application_context_id BIGINT NOT NULL,
    application_permission_group_id BIGINT NOT NULL,
    CONSTRAINT fk_context_permission_context FOREIGN KEY(application_context_id) REFERENCES application_user_context(id),
    CONSTRAINT fk_context_permission_group FOREIGN KEY(application_permission_group_id) REFERENCES application_permission_group(id),
    CONSTRAINT uq_context_permission UNIQUE(application_context_id, application_permission_group_id)
);
