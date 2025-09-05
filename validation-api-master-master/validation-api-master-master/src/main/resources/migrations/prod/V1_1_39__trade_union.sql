create table trade_union (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), tenant_id uuid not null, name varchar(255) not null, resource_id uuid not null, address_id int8 not null, primary key (id));
create table trade_union_coverage_item (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), tenant_id uuid not null, percentage numeric(19, 2) not null, period varchar(255) not null, quantity int4 not null, nomenclator_id int8 not null, trade_union_id int8 not null, primary key (id));
alter table if exists trade_union drop constraint if exists UKrj0d2igxx660d30slkdtih40d;
alter table if exists trade_union add constraint UKrj0d2igxx660d30slkdtih40d unique (name, deleted, deletion_token, tenant_id);
alter table if exists trade_union drop constraint if exists UKgrj7bwg69f2cm0t7y7crfhtou;
alter table if exists trade_union add constraint UKgrj7bwg69f2cm0t7y7crfhtou unique (resource_id, deleted, deletion_token, tenant_id);
create sequence trade_union_coverage_item_seq start 1 increment 1;
create sequence trade_union_seq start 1 increment 1;
alter table if exists trade_union add constraint FK3ecg381stwfoh3n6rytk18wak foreign key (address_id) references address;
alter table if exists trade_union_coverage_item add constraint FK5loyv2nwpkr1ljwv8boerh1c4 foreign key (nomenclator_id) references nomenclator;
alter table if exists trade_union_coverage_item add constraint FKr7v3vd4sp3sycfu0022git2wl foreign key (trade_union_id) references trade_union;
create table beneficiary_trade_union (beneficiary_id int8 not null, trade_union_id int8 not null, primary key (beneficiary_id, trade_union_id));
alter table if exists beneficiary_trade_union add constraint FK9lnve7h4ehvxlegm7unnpj5wm foreign key (trade_union_id) references trade_union;
alter table if exists beneficiary_trade_union add constraint FK9nxef4p6e04umiy42kitih6e2 foreign key (beneficiary_id) references beneficiary;
create table trade_union_coverage_item_audit_log (id int8 not null, rev int4 not null, revtype int2, client_id varchar(255), created_at timestamp, created_by varchar(255), modified_at timestamp, modified_by varchar(255), percentage numeric(19, 2), percentage_mod boolean, period varchar(255), period_mod boolean, quantity int4, quantity_mod boolean, primary key (id, rev));
alter table if exists trade_union_coverage_item_audit_log add constraint FKl96sjhblr4gemxbdyfvac516h foreign key (rev) references revinfo;

alter table if exists trade_union add column auto_settlement boolean DEFAULT false not null;
alter table if exists trade_union add column day_of_settlement integer DEFAULT 31 not null;
alter table if exists trade_union add column includes_family_group boolean DEFAULT false not null;
create table trade_union_audit_log (id int8 not null, rev int4 not null, revtype int2, client_id varchar(255), created_at timestamp, created_by varchar(255), modified_at timestamp, modified_by varchar(255), auto_settlement boolean, auto_settlement_mod boolean, day_of_settlement int4, day_of_settlement_mod boolean, includes_family_group boolean, includes_family_group_mod boolean, name varchar(255), name_mod boolean, primary key (id, rev));
alter table if exists trade_union_audit_log add constraint FKmj1xy53ejgvpwcf6mb8bgy4m6 foreign key (rev) references revinfo;

alter table if exists trade_union_coverage_item drop constraint if exists UKelmneeds79b0w2nxpm3i0kd76;
alter table if exists trade_union_coverage_item add constraint UKelmneeds79b0w2nxpm3i0kd76 unique (nomenclator_id, trade_union_id, deleted, deletion_token, tenant_id);