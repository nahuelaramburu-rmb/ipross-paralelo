alter table if exists medical_practice add column if not exists tenant_id uuid;
update medical_practice set tenant_id = '812a47c9-748f-450f-bff7-934bebbb0b5e' where tenant_id is null;
alter table medical_practice alter column tenant_id set not null;

alter table if exists medical_practice drop constraint if exists UK7r9tbixstkfepyl3s2wi46oi8;
alter table if exists medical_practice add constraint UK7r9tbixstkfepyl3s2wi46oi8 unique (name, deleted, deletion_token, tenant_id);

DO
$do$
declare
    elem bigint;
begin
    for elem in
        select id from medical_practice where id not in
        (select medical_practice_id from medical_specialties_practices)
    loop
    insert into medical_specialties_practices (medical_practice_id, medical_specialty_id)
    values (elem, 63);
end loop;
end
$do$;