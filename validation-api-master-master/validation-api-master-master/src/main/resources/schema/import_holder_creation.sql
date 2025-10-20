create procedure holder_creation(clientid character varying, createdat timestamp without time zone,
                                 createdby character varying, modifiedat timestamp without time zone,
                                 modifiedby character varying, tenantid uuid, apartmentvar character varying,
                                 districtvar character varying, streetvar character varying, streetnumber integer,
                                 cityid bigint, phonenumber bigint, phonetype character varying, birthdate date,
                                 emailvar character varying, gendervar character varying, idnumber bigint,
                                 idtypeid bigint, lastname character varying, maritalstatusid bigint,
                                 namevar character varying, workidnumber bigint, beneficiarycode character varying,
                                 resourceid uuid, relationshiptypeid bigint, statusid bigint,
                                 insuranceplans character varying[], holderrelationshiptypeid bigint,
                                 beneficiarycategoryid bigint, companyid bigint,
                                 familyid uuid, paymentmethodid bigint)
    language plpgsql
as
$$
declare
    new_address_id     bigint;
    new_phone_id       bigint;
    new_beneficiary_id bigint;
    tempVar            varchar;
begin
    insert into address (id, client_id, created_at, created_by, deleted, deletion_token, modified_at, modified_by,
                         tenant_id, apartment, district, street, street_number, city_id)
    select nextval('address_seq'),
           clientId,
           createdAt,
           createdBy,
           false,
           uuid_nil(),
           null,
           null,
           tenantId,
           apartmentVar,
           districtVar,
           streetVar,
           streetNumber,
           cityId
    where not exists
        (select 1 from beneficiary where id_number = idNumber and id_type_id = idTypeId and tenant_id = tenantid)
    returning id into new_address_id;
    if new_address_id is not null then
        if phoneNumber is not null and phoneType is not null then
            insert into phone (id, client_id, created_at, created_by, deleted, deletion_token, modified_at, modified_by,
                               tenant_id,
                               area_code, phone_number, phone_type)
            select nextval('phone_seq'),
                   clientId,
                   createdAt,
                   createdBy,
                   false,
                   uuid_nil(),
                   null,
                   null,
                   tenantId,
                   null,
                   phoneNumber,
                   phoneType
            where not exists
                (select 1
                 from beneficiary
                 where id_number = idNumber
                   and id_type_id = idTypeId
                   and tenant_id = tenantid)
            returning id into new_phone_id;
        end if;
        /*Beneficiary does not exists by idNumber and idType*/
        insert into beneficiary (id, client_id, created_at, created_by, deleted, deletion_token, modified_at,
                                 modified_by,
                                 tenant_id,
                                 address_id, birth_date, email, gender, id_number, id_type_id, last_name,
                                 marital_status_id,
                                 name,
                                 occupation_id, phone_id, studies_id, work_id_number, active_batch,
                                 beneficiary_category_id,
                                 beneficiary_code,
                                 company_id, family_id, payment_method_id, related_beneficiary_id, relationship_type_id,
                                 resource_id,
                                 status_id, status_update_description)
        values (nextval('beneficiary_seq'), clientId, createdAt, createdBy, false, uuid_nil(), null, null, tenantId,
                new_address_id, birthDate, emailVar, genderVar, idNumber, idTypeId, lastName, maritalStatusId, nameVar,
                null, new_phone_id, null, workIdNumber, false, beneficiaryCategoryId, beneficiaryCode, companyId,
                familyId, paymentMethodId,
                null, relationshipTypeId,
                resourceId, statusId, null)
            /*At this point, expected conflict is beneficiaryCode or workIdNumber*/
        on conflict do nothing
        returning id into new_beneficiary_id;
        if new_beneficiary_id is null then
            begin
                update beneficiary
                set beneficiary_code        = beneficiaryCode,
                    id_number               = idnumber,
                    id_type_id              = idtypeid,
                    last_name               = lastname,
                    name                    = namevar,
                    work_id_number          = workidnumber,
                    beneficiary_category_id = beneficiaryCategoryId,
                    company_id              = companyId,
                    payment_method_id       = paymentMethodId,
                    relationship_type_id    = relationshipTypeId,
                    related_beneficiary_id  = null,
                    family_id               = (select case
                                                          when relationship_type_id = holderRelationshipTypeId
                                                              then family_id
                                                          else uuid_generate_v4() end
                                               from beneficiary
                                               where beneficiary_code like beneficiarycode),
                    modified_at             = modifiedAt,
                    modified_by             = modifiedBy,
                    status_id               = statusid
                where beneficiary_code like beneficiarycode
                  and tenant_id = tenantid
                returning id into new_beneficiary_id;
                delete from address where id = new_address_id;
                delete from phone where id = new_phone_id;
            exception
                when unique_violation then null;
            end;
        end if;
    else
        /*Beneficiary exists by idNumber and idType*/
        begin
            update beneficiary
            set beneficiary_code        = beneficiaryCode,
                work_id_number          = workidnumber,
                last_name               = lastname,
                name                    = namevar,
                beneficiary_category_id = beneficiaryCategoryId,
                company_id              = companyId,
                payment_method_id       = paymentMethodId,
                relationship_type_id    = relationshipTypeId,
                related_beneficiary_id  = null,
                family_id               = (select case
                                                      when relationship_type_id = holderRelationshipTypeId
                                                          then family_id
                                                      else uuid_generate_v4() end
                                           from beneficiary
                                           where id_number = idNumber
                                             and id_type_id = idTypeId),
                modified_at             = modifiedAt,
                modified_by             = modifiedBy,
                status_id               = statusid
            where id_number = idNumber
              and id_type_id = idTypeId
              and tenant_id = tenantid
            returning id into new_beneficiary_id;
        exception
            when unique_violation then null;
        end;
    end if;
    if new_beneficiary_id is not null then
        update address
        set city_id       = cityId,
            street        = streetVar,
            street_number = streetNumber,
            district      = districtVar,
            apartment     = apartmentVar
        where id = (select address_id from beneficiary where id = new_beneficiary_id);
        foreach tempVar in array insurancePlans
            loop
                insert into beneficiary_insurance_plan (id, beneficiary_id, insurance_plan_id, client_id, created_at,
                                                        created_by, deleted, deletion_token, modified_at, modified_by,
                                                        tenant_id, expiration_date, priority)
                values (nextval('beneficiary_insurance_plan_seq'), new_beneficiary_id,
                        (tempVar::json ->> 'insurancePlanId')::bigint,
                        clientId, createdAt,
                        createdBy, false, uuid_nil(), null, null, tenantId, (tempVar::json ->> 'expirationDate')::date,
                        (tempVar::json ->> 'priority')::int)
                on conflict do nothing;
            end loop;
    end if;
end;
$$;