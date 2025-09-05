alter table medical_authorization add column payment_method_id int8;
update medical_authorization ma set payment_method_id = (select payment_method_id from beneficiary where id = ma.beneficiary_id);
alter table medical_authorization alter column payment_method_id set not null;
alter table medical_authorization add constraint FK1sulwutshus6d2027ogb8c7bx foreign key (payment_method_id) references payment_method;