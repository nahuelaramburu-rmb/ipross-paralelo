alter table if exists rule_configuration drop constraint if exists UK3cs8ysp60gqlvuu1oy25yarba;
alter table if exists rule_configuration drop constraint if exists uk9lpa5hk5oap02uhlv2gox5x1v;
alter table if exists rule_configuration add constraint UK3cs8ysp60gqlvuu1oy25yarba unique (contract_id, rule_id, deleted, deletion_token, tenant_id);