create table prescription_restriction
(
    id                   bigint                     not null
        constraint prescription_restriction_pkey
            primary key,
    client_id            varchar(255),
    created_at           timestamp,
    created_by           varchar(255),
    deleted              boolean default false      not null,
    deletion_token       uuid    default uuid_nil() not null,
    modified_at          timestamp,
    modified_by          varchar(255),
    tenant_id            uuid                       not null,
    medical_specialty_id bigint                     not null
        constraint fksl1cmwjmojeh0kqehv94f1i5u
            references medical_specialty
);

create table prescription_restriction_audit_log
(
    id                   int8 not null,
    rev                  int4 not null,
    revtype              int2,
    client_id            varchar(255),
    created_at           timestamp,
    created_by           varchar(255),
    modified_at          timestamp,
    modified_by          varchar(255),
    medical_specialty_id int8,
    primary key (id, rev)
);

create sequence prescription_restriction_seq start 1 increment 1;

alter table if exists prescription_restriction_audit_log
    add constraint FKfh25auh1h6modw1n7bvkldgty foreign key (rev) references revinfo