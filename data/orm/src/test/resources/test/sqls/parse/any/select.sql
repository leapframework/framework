select 1 from dual;

select User.* from User User;

SELECT ot.typeGroup FROM "Order" o 
JOIN (
	SELECT  DISTINCT(oi.orderId),t.typeGroup FROM orderItem oi 
	  INNER JOIN product     p ON oi.prdtId = p.Id 
	  INNER JOIN productType t ON p.typeId  = t.Id 
	WHERE (t.typeGroup = '3')
) ot ON o.Id = ot.orderId;

select pt.typeGroup from Product p join (select id,typeGroup from ProductType) pt on p.typeId = pt.id;

select SimpleEntity1.attr1_1 from ( 
	select SimpleEntity1.attr1 attr1_1 from (
	   select SimpleEntity1.attr1,SimpleEntity1.attr2 attr2 from SimpleEntity1
	) SimpleEntity1
) SimpleEntity1;

select * from t where exists (select 1 from dual) and name = ?;

select * from t where id in (select 1 from dual) and name = ?;

 SELECT a.examine_id examineId,a.examine_name examineName,a.level_limit levelLimit 				
FROM c_examinepaper a,
	(SELECT examine_id FROM c_examinepaper WHERE level_limit=? AND status=1
	MINUS
	SELECT DISTINCT examine_id FROM course) b             
WHERE a.examine_id = b.examine_id;
