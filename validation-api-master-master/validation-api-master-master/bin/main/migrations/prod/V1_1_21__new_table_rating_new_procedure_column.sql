alter table if exists medical_authorization add column rating_id int8;
alter table if exists practitioner add column rating_id int8;
create table rating (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), tenant_id uuid not null, average numeric(19, 2) not null, charges numeric(19, 2) not null, duration numeric(19, 2) not null, quality numeric(19, 2) not null, quantity int4 not null, wait_time numeric(19, 2) not null, primary key (id));
create sequence rating_seq start 1 increment 1;
alter table if exists medical_authorization add constraint FKs5pjf5k20uidcvmkcrbv11v0i foreign key (rating_id) references rating;
alter table if exists practitioner add constraint FKt41soevg4555uid2i6jk7e8di foreign key (rating_id) references rating;
alter table if exists procedure add column medical_authorization_id int8;
alter table if exists procedure add constraint FKhwda6wo5cavxqmai3oklktlg3 foreign key (medical_authorization_id) references medical_authorization;