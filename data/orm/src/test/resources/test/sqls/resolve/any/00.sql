select pt.typeGroup from Product p join (select id,typeGroup from ProductType) pt on p.typeId = pt.id;
select pt.type_group from product_ p join (select id_,type_group from product_type) pt on p.type_id = pt.id_;


select f.* from File f where f.deleted = 1 and f.principalId = :principalId and 
(f.directoryId is null or (select dir.deleted from Directory dir where dir.id = f.directoryId) == 0);
select f.* from file_ f where f.deleted_ = 1 and f.principal_id = :principalId and 
(f.dir_id is null or (select dir.deleted_ from pan_dir dir where dir.dir_id = f.dir_id) == 0);