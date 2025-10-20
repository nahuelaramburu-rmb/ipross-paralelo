update medical_authorization set company_id = null where payment_method_id = 2;
update medical_authorization m set company_id = (select distinct(b.company_id) from budget_item bi inner join budget b on bi.budget_id = b.id
                                                 where bi.medical_authorization_id = m.id and b.dtype = 'BeneficiaryBudget')
where m.payment_method_id = 1 and m.company_id is null;
update scheduler_job_info set cron_expression = '0 0/30 * 1/1 * ? *' where id = 10;