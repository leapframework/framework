-- copy from druid test resources, see https://github.com/alibaba/druid
INSERT INTO m_browser_common_base_aggr
            (gmt_create,
             gmt_modify,
             collect_date_str,
             page_id,
             geo_type,
             geo_value,
             count_load_time,
             min_load_time,
             max_load_time,
             avg_load_time)
SELECT Now()
       gmt_create,
       Now()
       gmt_modify,
       "2012-04-08"       collect_date_str,
       pageId                                                          page_id,
       "all_country"                                                              AS
       geo_type,
       '(ALLCOUNTRIES)'                                                AS
       geo_value,
       Count(IF(loadTime IS NULL
                 OR loadTime < 0
                                OR loadTime >= 30000, NULL, loadTime))
       count_load_time,
       Min(IF(loadTime IS NULL
               OR loadTime < 0
                              OR loadTime >= 30000, NULL, loadTime))
       min_load_time,
       Max(IF(loadTime IS NULL
               OR loadTime < 0
                              OR loadTime >= 30000, NULL, loadTime))
       max_load_time,
       Sum(IF(loadTime IS NULL
               OR loadTime < 0
                              OR loadTime >= 30000, NULL, loadTime)) /
       Count(IF(loadTime IS NULL
                 OR loadTime < 0
                                OR loadTime >= 30000, NULL, loadTime))
       avg_load_time
FROM   m_browser_common
WHERE  collect_date >= '2012-04-08'
       AND collect_date < '2012-04-09'
       AND pageId IS NOT NULL
       AND country_code IS NOT NULL
AND pageId= 'SOURCING_HOME'
GROUP  BY pageId;


select count(*) 		
from (
	(select a.activity_offer_id adResourceId, 			       
		a.title adResourceName, 			       
		a.b2boffer_url adResourceURL, 			       
		a.custid, 			       
		b.memberid, 			       
		c.position_name positionName, 			       
		c.charge_setting feeRegulation, 			       
		a.priority_weight priorityWeight, 			       
		a.audit_state adResourceState, 			       
		0 adResourceType, 			       
		a.gmt_create, 			       
		a.refuse_remark 			
	from q_activity_offer_enroll a,q_custinfo b,q_activity_position c 			
	where a.custid = b.custid 			      
	and a.activity_position_id = c.activity_position_id 			      
	and c.activity_id = ?) 			
	union all 			
	(select a.reg_position_id adResourceId, 			       
		b.custname adResourceName, 			       
		null adResourceURL, 			       
		a.custid, 			       
		b.memberid,  			       
		c.position_name positionName, 			       
		c.charge_setting feeRegulation, 			       
		null priorityWeight, 			       
		a.audit_state adResourceState, 			       
		1 adResourceType, 			       
		a.gmt_create, 			       a.refuse_remark 			
	from q_activity_custom_enroll a,q_custinfo b,q_activity_position c 			
	where a.custid = b.custid 			      
		and a.activity_position_id = c.activity_position_id 			      
		and c.activity_id = ?)) a ;
		
SELECT a.id AS "appId", a.name AS "appName", b.number AS "instanceNumber", b.hostname AS "hostname", c.name AS "monitorItemName" , d.id AS "alarmRuleStatusId", d.name AS "alarmRuleName", d.current_status AS "alarmRuleStatus", d.last_change_time AS "lastChangeTimeOfAlarmRule" FROM mi_alarm_rule_status d LEFT JOIN monitor_item_status c ON d.mi_status_id = c.id LEFT JOIN instance b ON b.number = c.inst_num AND b.app_num = c.app_num AND b.hostname = c.hostname LEFT JOIN app a ON a.number = b.app_num WHERE 1 = 0 OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? 
OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? OR a.id = ? ORDER BY lastChangeTimeOfAlarmRule DESC LIMIT ?, ?;
