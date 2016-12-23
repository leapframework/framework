select pt.typeGroup from Product p join (select id,typeGroup from ProductType) pt on p.typeId = pt.id;
select pt.type_group from product_ p join (select id_,type_group from product_type) pt on p.type_id = pt.id_;


select f.* from File f where f.deleted = 1 and f.principalId = :principalId and 
(f.directoryId is null or (select dir.deleted from Directory dir where dir.id = f.directoryId) == 0);
select f.* from file_ f where f.deleted_ = 1 and f.principal_id = :principalId and 
(f.dir_id is null or (select dir.deleted_ from pan_dir dir where dir.dir_id = f.dir_id) == 0);

select case when (t.start_time > :now) then 1 when (t.end_time < :now) then 2 when (t.remain_num = 0) then 3 when (t.num <= ifnull(t2.num,0)) then 4 when (ifnull(t.winning_rate,100) < rand() * 100) then 5  else 0 end as validateCode from t_red_package t left join t_red_package_user t2 on t.id = t2.red_package_id and t2.user_id = :userId where t.id = :redPackageId and t.status = 1;
select case when (t.start_time > :now) then 1 when (t.end_time < :now) then 2 when (t.remain_num = 0) then 3 when (t.num <= ifnull(t2.num,0)) then 4 when (ifnull(t.winning_rate,100) < rand() * 100) then 5  else 0 end as validateCode from t_red_package t left join t_red_package_user t2 on t.id = t2.red_package_id and t2.user_id = :userId where t.id = :redPackageId and t.status = 1;