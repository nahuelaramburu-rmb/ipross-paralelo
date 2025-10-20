alter table if exists settlement_item add column refundable boolean;
update settlement_item set refundable = false;
alter table settlement_item alter column refundable set not null;