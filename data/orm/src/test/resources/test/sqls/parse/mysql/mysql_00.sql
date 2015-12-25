-- copy from druid test resources, see https://github.com/alibaba/druid
SELECT      distinct a.id "id",    a.col "col",     a.position "position",     a.panel_id "panelId"   
FROM     view_position_info a 
LEFT JOIN view_portal b ON a.panel_id = b.panel_id      
LEFT JOIN view_portal_panel c 
ON a.panel_id = c.panel_id   
WHERE     b.user_id = ? and     ((b.is_grid='y' and c.param_name='is_hidden' and c.param_value='false') or      b.is_grid  != 'y')   
ORDER BY    a.col ASC, a.position ASC;

   SELECT     id, item_type 'itemType', item_id 'itemId', item_name 'itemName', app_id 'appId'
   , sequence, refresh_intv "refreshIntv"   
   FROM     view_portal_info   
   WHERE 1=1          
   AND item_type = ?                   
   AND app_id = ?                         
   ORDER BY sequence desc         
   LIMIT ?, ?;
   
UPDATE AVATAR_CASE SET GMT_MODIFIED=sysdate,STATUS = ?,SYS_DECISION = ?,MANUAL_DECISION = ?,OPERATOR=? , MEMO = ? where id in ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? );

SELECT 5 & ~1;

SELECT 4 >> 2;

SELECT 'Monty!' REGEXP '.*';

   SELECT    id "id",    username "username",    password "password",    name "name",    staff_num "staffNumber",    
   wangwang "wangwang",    email "email",    mobile "mobile",    
   is_deleted "isDeleted",    is_admin "isAdmin",    gmt_create "gmtCreate"   
   FROM    sys_user               
   LIMIT    ?, ?;
   
SELECT COUNT(*) FROM (SELECT COUNT(*) FROM m_web_uri_m5 
WHERE monitor_item_id = ? AND app_num = ? AND inst_num = ? AND service_tag = ? AND monitor_item_id = ? 
AND collect_date >= ? AND collect_date < ? 
AND (1=0 OR c_URI in (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83)) 
GROUP BY c_URI) A    ;

   select s.*,d.*   
   from server s   
   left join device_base d on   s.device_base_id= d.id   
   left join logic_site l on l.id = d.logic_site_id   
   where s.gmt_modify > ? and l.site_name = "开发测试"  ;
   
   SELECT /*+ use_hash(a b e) index(e)*/Count(*)  		    
FROM q_matchrelation a,q_offerdetail b,q_keyword e , Q_ADCREATIVE f 		  	
WHERE a.offerid=b.offerid  			    
	AND a.keywordid = e.keywordid 			    
	AND a.offerid=f.offerid  			    
	AND a.issuspect='1' 			     		 		    			     		
ORDER BY e.word_alias,a.createtime DESC ;

insert into accontexchangeinfo a 			 
	(a.id,a.sourceid,a.batchno,a.applicant,a.out_uid,a.in_uid,a.moneytype,a.amount,a.remark,a.gmt_create,a.gmt_modified) 			
	values 			
(
	SEQ_ACCONTEXCHANGEINFO.nextval,?,?,?,?,?,?,?,?,SYSDATE,SYSDATE);
	
	
SELECT a.examine_id examineId,a.examine_name examineName,a.level_limit levelLimit 				
FROM c_examinepaper a,
	(SELECT examine_id FROM c_examinepaper WHERE level_limit=? AND status=1
	MINUS
	SELECT DISTINCT examine_id FROM course) b             
WHERE a.examine_id = b.examine_id 		;

