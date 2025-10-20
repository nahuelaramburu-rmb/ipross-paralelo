delete
from status
where id = 15;
delete
from status
where id = 18;
update status
set name = 'APROBADO'
where name = 'LOTE APROBADO';
update status
set name = 'CANCELADO'
where name = 'LOTE RECHAZADO';
update certificate_type
set name = 'Certificado de Nacido Vivo'
where id = 2;