-- copy from druid test resources, see https://github.com/alibaba/druid
SELECT                
ID as id,                
to_char(GMT_MODIFIED,'yyyymmdd hh24:mi:ss') as gmtModified,                               
COL5 as col5            
FROM ABC_ABC            
WHERE IS_DELETED='n'                  
AND end_time is null  
AND deadline_time < sysdate  
AND approval_time is null 
AND             id > :1        
AND col2 in    
(:2,:3,:4,:5,:6,:7,:8,:9,:10,:11,:12,:13,:14,:15,:16,:17,:18,:19,:20,:21,:22,:23,:24,:25,:26,:27,:28,:29,:30,:31,:32,:33)      
AND rownum<:34  
ORDER BY id   ;

INSERT INTO AC_DE_F
  (ID,
   GMT_MODIFIED,
   MODIFIER,
   GMT_CREATE,
   CREATOR,
   DOMAIN,
   EMPLOYEE_NUMBER,
   COMPANY_NAME,
   HOMEPAGE_URL,
   BACKGROUND,
   CATEGORY_ID,
   REMARK,
   MEMBER_ID,
   SALES_OWNER_ID,
   ACCOUNT_ID,
   IS_DELETED,
   CUST_GRADE)
  SELECT ID,
         GMT_MODIFIED,
         MODIFIER,
         GMT_CREATE,
         CREATOR,
         DOMAIN,
         EMPLOYEE_NUMBER,
         COMPANY_NAME,
         HOMEPAGE_URL,
         BACKGROUND,
         CATEGORY_ID,
         REMARK,
         MEMBER_ID,
         SALES_OWNER_ID,
         ACCOUNT_ID,
         IS_DELETED,
         CUST_GRADE
    FROM APOLLO_ZEUS.GLOBAL_ACCOUNT
   WHERE ACCOUNT_ID = :B2
     AND DOMAIN = :B1
  UNION ALL
  SELECT ID,
         GMT_MODIFIED,
         MODIFIER,
         GMT_CREATE,
         CREATOR,
         DOMAIN,
         EMPLOYEE_NUMBER,
         COMPANY_NAME,
         HOMEPAGE_URL,
         BACKGROUND,
         CATEGORY_ID,
         REMARK,
         MEMBER_ID,
         SALES_OWNER_ID,
         ACCOUNT_ID,
         IS_DELETED,
         CUST_GRADE
    FROM APOLLO_NIR.GLOBAL_ACCOUNT
   WHERE ACCOUNT_ID = :B2
     AND DOMAIN = :B1;
     
SELECT employee_id FROM (SELECT employee_id+1 AS employee_id FROM employees)
   FOR UPDATE;
   
SELECT employee_id FROM (SELECT employee_id+1 AS employee_id FROM employees)
   FOR UPDATE;

SELECT * 
   FROM employees 
   WHERE department_id = 30
   ORDER BY last_name;
   
SELECT a.department_id "Department",
   a.num_emp/b.total_count "%_Employees",
   a.sal_sum/b.total_sal "%_Salary"
FROM
(SELECT department_id, COUNT(*) num_emp, SUM(salary) sal_sum
   FROM employees
   GROUP BY department_id) a,
(SELECT COUNT(*) total_count, SUM(salary) total_sal
   FROM employees) b
ORDER BY a.department_id;

SELECT * FROM sales PARTITION (sales_q2_2000) s
   WHERE s.amount_sold > 1500
   ORDER BY cust_id, time_id, channel_id;
   
SELECT COUNT(*) * 10 FROM orders SAMPLE (10);

SELECT COUNT(*) * 10 FROM orders SAMPLE(10) SEED (1);

SELECT salary FROM employees
   AS OF TIMESTAMP (SYSTIMESTAMP - INTERVAL '1' MINUTE)
   WHERE last_name = 'Chung';
   
SELECT salary FROM employees
  VERSIONS BETWEEN TIMESTAMP
    SYSTIMESTAMP - INTERVAL '10' MINUTE AND
    SYSTIMESTAMP - INTERVAL '1' MINUTE
  WHERE last_name = 'Chung';
  
UPDATE employees SET salary =      
   (SELECT salary FROM employees
   AS OF TIMESTAMP (SYSTIMESTAMP - INTERVAL '2' MINUTE)
   WHERE last_name = 'Chung')
   WHERE last_name = 'Chung';
   
SELECT department_id, MIN(salary), MAX (salary)
     FROM employees
     GROUP BY department_id
   ORDER BY department_id;
   
SELECT DECODE(GROUPING(department_name), 1, 'All Departments',
      department_name) AS department_name,
   DECODE(GROUPING(job_id), 1, 'All Jobs', job_id) AS job_id,
   COUNT(*) "Total Empl", AVG(salary) * 12 "Average Sal"
   FROM employees e, departments d
   WHERE d.department_id = e.department_id
   GROUP BY CUBE (department_name, job_id)
   ORDER BY department_name, job_id;
   
SELECT channel_desc, calendar_month_desc, co.country_id,
      TO_CHAR(sum(amount_sold) , '9,999,999,999') SALES$
   FROM xxxx, bbb, times, ccc, ddd co
   WHERE xxxx.time_id=times.time_id 
      AND xxxx.cust_id=bbb.cust_id 
      AND xxxx.channel_id= ccc.channel_id 
      AND bbb.country_id = co.country_id
      AND ccc.channel_desc IN ('Direct Sales', 'Internet') 
      AND times.calendar_month_desc IN ('2000-09', '2000-10')
      AND co.country_iso_code IN ('UK', 'US')
  GROUP BY GROUPING SETS( 
      (channel_desc, calendar_month_desc, co.country_id), 
      (channel_desc, co.country_id), 
      (calendar_month_desc, co.country_id) );
      
SELECT *    					
FROM (SELECT A.*, rownum cnt 						  
		FROM (SELECT  						                  
				A.CUSTID,A.PAYCOUNT, A.MEMBERID,A.ACCREDITSTATUS,C.CUSTNAME, C.CONTNAME, C.CONTPHONECITYCODE,  						                  
				C.CONTPHONE, C.CONTMOBPHONE, C.CONTFAXCITYCODE, C.CONTFAX, C.CONTEMAIL,  						                  
				C.CUSTAREA AS AREANAME, C.CUSTADDR, C.ORGINDUSTRY AS ORGINDUSTRYNAME,  						                  
				E.DESCRIPTION AS CUSTSTATNAME, F.DESCRIPTION AS ATYPENAME,  						                  
				null AS FNAME, G.DESCRIPTION AS B2BVALIDFLAGNAME, A.VALIDFLAG  						          
				FROM Q_CUSTOMERS A, Q_CUSTINFO C,  						                  
					(SELECT * FROM F_A_DE WHERE DOMAINCD = 'CUSTOMERS_CUSTSTAT') E,  						                  
					(SELECT * FROM F_A_DE WHERE DOMAINCD = 'CUSTOMERS_ATYPE') F,  						                  
					(SELECT * FROM F_A_DE WHERE DOMAINCD = 'CUSTOMERS_B2BVALIDFLAG') G  						          
				WHERE  						                  
					A.CUSTID = C.CUSTID  						                  
					AND A.CUSTSTAT = E.CODE( + )                                                        						                  
					AND A.ATYPE = F.CODE( + )   						                  
					AND A.b2bValidFlag = G.CODE( + )  		 		 		    		    		       
					AND A.MEMBERID = ? 		                		 		 		 		 		 		 		 		 		 		 		 		 		 		 		 		 				
				ORDER BY A.CUSTID DESC) A      		
WHERE ROWNUM < ?)      	WHERE CNT > ?   ;

SELECT ID,CLASSTYPE,KEY,VALUE 
FROM            
	(SELECT A.*,ROWNUM CNT FROM(SELECT ID,CLASSTYPE,KEY,VALUE           
		FROM F_XXX          
		WHERE CLASSTYPE = ?          
		ORDER BY ID DESC) A                    
WHERE ROWNUM < ?          
)          
WHERE CNT > ?     ;

select custid,memberid,
	to_char(createtime,'yyyy-mm-dd hh24:mi:ss') createtime ,
	planid,auditstate,to_char(modifytime,'yyyy-mm-dd hh24:mi:ss') modifytime,createid ,
	to_char(custappdate,'yyyy-mm-dd hh24:mi:ss') custappdate,executeState,gmtcreate,custname,planbatchid 		
from( 		
	select custid,memberid,createtime,planid,auditstate,createid ,custappdate,executeState,custname,gmtcreate ,cnt,planbatchid ,modifytime 		
	from( 		
		select p.custid,p.memberid,p.createtime,p.planid,p.auditstate,p.createid ,p.custappdate,p.modifytime,p.gmtcreate,p.executeState,
			c.custname,row_number() over(order by p.memberid asc ,p.createtime DESC ) cnt,p.planbatchid 
		from x_plp_f_u_c_k p ,x_xcde c 
		where p.custid=c.custid  		
			AND p.applytype = 0   		 			  				 				
			AND p.createtime BETWEEN to_date(?,'yyyy-mm-dd') 
			AND to_date(?,'yyyy-mm-dd hh24:mi:ss') 				  				 				 			    
			AND 				 (p.auditstate = ? and p.executestate = 1) 			     			     			     			 			
			AND auditstate != 3	 		     		    
			AND 		    	p.memberid in (?) 		     		 		
		)t 
	WHERE t.cnt < ? 		)k  
WHERE k.cnt > ? 		
ORDER BY memberid ASC, createtime DESC 	;

SELECT /*+ use_hash(a b e) index(e)*/Count(*)  		    
FROM bbbb a,aaaa b,q_keyword e , CCCC f 		  	
WHERE a.offerid=b.offerid  			    
	AND a.keywordid = e.keywordid 			    
	AND a.offerid=f.offerid  			    
	AND a.issuspect='1' 			     		 		    			     		
ORDER BY e.word_alias,a.createtime DESC 	;

select * 
from ( 
	select rownum rnm, z.* 
	from (
		select name as value,value as key 
		from a_b_c 
		where is_deleted='n' and type='fuse' order by ordering
		) z 
where rownum < ? ) where rnm >= ?;

 SELECT A_B_C.*, EE_2.NAME AS TRUNK_NAME     		  
 FROM AA_2,A_B_C, EE_2      		  
 WHERE AA_2.is_deleted='n' and      		  	
 	AA_2.TRUNK_ID=EE_2.ID 
 	AND      		  	AA_2.ID=A_B_C.CLOT_ID 
 	AND     		  	A_B_C.GMT_MODIFIED >=?     		  
ORDER BY EE_2.NAME,AA_2.id  NULLS LAST   ;

select * from ( 
	select rownum rnm, z.* 
	from (
		select distinct(tt.id) use_less_id, tt.* 
		from (
			select t.*, rct.parent_id, RCT.NAME as trunk_name  
			from x_1_2 t, k_2_3 rct 
			where t.is_deleted ='n' and t.trunk_id = rct.id
		) tt 
		CONNECT BY prior parent_id = trunk_id 
		start with trunk_name = 'rc.mf.Caesar' 
		order by load_order nulls last
) z where rownum < ? ) where rnm >= ?;

select * 
from 
	( select rownum rnm, z.* 
		from (
			select * 
			FROM (
				SELECT DISTINCT * 
				FROM (
					SELECT lower(a.role_name) || '#' || a.permission_id as key, 'y' as value 
					FROM f_p_23 A 
					WHERE a.is_deleted = 'n' 
				UNION ALL 
				SELECT lower(b.role_name) || '#' || a.permission_id, 'y' 
				FROM f_p_23 A, app_role_base_role B 
				WHERE a.is_deleted = 'n' AND b.is_deleted = 'n' AND a.role_name = b.base_role_name
				)
			) c 
			ORDER BY c.key) z 
	where rownum < ? 
	) 
where rnm >= ?;

select nodeId,nodeName,taskflowName,taskflowVersion 
from ( 			
	select /*+ use_hash(n f) ordered */ n.ID nodeId,n.name nodeName,f.name taskflowName,f.version taskflowVersion 
	from "AB" n, "BC_3" f 			
	where 			n.owner = ? 			and n.STATUS in 			(   				? 			) 			and n.GMT_MODIFIED < ? 			and n.IS_AUTO = 'Y' 			and n.task_flow_id = f.id 			order by f.priority desc nulls last, f.gmt_create asc 		) where rownum < ?;
	

select INDUSTRY_ID, 			   sale_type, 			   service_type, 			   RISK_TYPE, 		       
	to_char(gmt_create, 'yyyy-mm-dd hh24:mi:ss') gmt_create 		
from (
	select /*+ use_hash(a b c) ordered */ 					 b.service_type, 					 b.RISK_TYPE, 					 
		b.sale_type, 			         b.INDUSTRY_ID, 			         b.GMT_CREATE, 			         
		row_number() over(partition by b.service_type,b.RISK_TYPE,b.sale_type,b.INDUSTRY_ID order by b.GMT_CREATE) xh 		       
	from "AAAAA" a, "F" b, "BBBBB" c 			   
	where 			   		a.id = b.task_flow_id 			   		
		AND a.id = c.task_flow_id 					AND c.status = 'waiting' 					and c.IS_AUTO = 'N' 					
		AND a.status = 'running' 					AND 					      c.name in (?) 
	) 		
where xh=1;


select * from (
	select row_.*, rownum rownum_ 
	from (    	 		
		select * 
			from 		
			(
				select results.*,row_number() over ( partition by results.object_id order by results.gmt_modified desc) rn 
				from 		    
				( 			    
					( 			    
						select 				              sus.ID                ID,          sus.GMT_CREATE        GMT_CREATE,          
							sus.GMT_MODIFIED      GMT_MODIFIED,          sus.COMPANY_ID        COMPANY_ID,          
							sus.OBJECT_ID         OBJECT_ID,          sus.OBJECT_TYPE       OBJECT_TYPE,          
							sus.CONFIRM_TYPE      CONFIRM_TYPE,          sus.OPERATOR          OPERATOR,          
							sus.FILTER_TYPE       FILTER_TYPE,          sus.MEMBER_ID         MEMBER_ID,          
							sus.MEMBER_FUC_Q        MEMBER_FUC_Q,          sus.RISK_TYPE         RISK_TYPE     , 'Y' IS_DRAFT 				
						from 		            f_U_c_ sus , a_b_c_draft p 				         	     	 	    ,
							member m     	     	
						where 1=1 		 		 		and 	        p.company_id = m.company_id      	 		
							and 			m.login_id=? 		 		 		 		      			         		 			
							and p.sale_type in( 		        ? 			) 		      			        
							and p.id=sus.object_id 				
					)  				
					union  				
					( 			    
						select 				              sus.ID                ID,          sus.GMT_CREATE        GMT_CREATE,          
							sus.GMT_MODIFIED      GMT_MODIFIED,          sus.COMPANY_ID        COMPANY_ID,          
							sus.OBJECT_ID         OBJECT_ID,          sus.OBJECT_TYPE       OBJECT_TYPE,          
							sus.CONFIRM_TYPE      CONFIRM_TYPE,          sus.OPERATOR          OPERATOR,          
							sus.FILTER_TYPE       FILTER_TYPE,          sus.MEMBER_ID         MEMBER_ID,          
							sus.MEMBER_FUC_Q        MEMBER_FUC_Q,          sus.RISK_TYPE         RISK_TYPE     , 'N' IS_DRAFT  				
						from f_U_c_ sus , a_b_c p 				         	     	 	    ,member m     	     	
						where 1=1 		 		 		
							and 	        p.company_id = m.company_id      	 		
							and 			m.login_id=? 	      			         		 			
							and p.sale_type in( 		        ? 			) 		      			        
							and p.id=sus.object_id 				
					) 		    
					) results		 		
				) 		where rn = 1 order by gmt_modified desc 		 		    
			)row_ where rownum <= ? 
	) 
where rownum_ >= ? ;   	

select * 
from ( 
	select rownum rnm, z.* 
	from (
		select name key,value   
		from ccccc t 
		where t.type='VODKA._CTPCLEAN_NIRVANA_URL' and is_deleted='n' order by id
		) z 
	where rownum < ? ) where rnm >= ?;
	
select  bk.attr_id as attrId,                   group_concat(distinct(bk.value_id)) as attrValueStr,                   
	group_concat(distinct(pt.name)) as themeNameStr           
from                a_c_ pt 
	left join cde_f bk on pt.spu_id=bk.spu_id            
where                pt.status='action'                and pt.category_id=? 		 		 		           
group by bk.attr_id;              ;

select count(*) 
from CREDIT_CMT_APPLY ,A_B_C 			 			 		 		
where 1=2 		
OR 			 				 					operator =  ?  			   
 and 					CREDIT_CMT_APPLY.GMT_CREATE >=  ?  			    
 and 					CREDIT_CMT_APPLY.GMT_CREATE <=  ?  			     			 			 		    
 and 		    	    CREDIT_CMT_APPLY.ID = A_B_C.cmt_apply_id 			    
 and  					Type = 'denounce' 			    
 and  					(SHEET_STATUS = 'assigned_unresolved') 		;
 
 WITH
  org_chart (eid, emp_last, mgr_id, reportLevel, salary, job_id) AS
  (
    SELECT employee_id, last_name, manager_id, 0 reportLevel, salary, job_id
    FROM employees
    WHERE manager_id is null
  UNION ALL
    SELECT e.employee_id, e.last_name, e.manager_id,
           r.reportLevel+1 reportLevel, e.salary, e.job_id
    FROM org_chart r, employees e
    WHERE r.eid = e.manager_id
  )
  SEARCH DEPTH FIRST BY emp_last SET order1
SELECT lpad(' ',2*reportLevel)||emp_last emp_name, eid, mgr_id, salary, job_id
FROM org_chart
ORDER BY order1;	

WITH
  dup_hiredate (eid, emp_last, mgr_id, reportLevel, hire_date, job_id) AS
  (
    SELECT employee_id, last_name, manager_id, 0 reportLevel, hire_date, job_id
    FROM employees
    WHERE manager_id is null
  UNION ALL
    SELECT e.employee_id, e.last_name, e.manager_id,
           r.reportLevel+1 reportLevel, e.hire_date, e.job_id
    FROM dup_hiredate r, employees e
    WHERE r.eid = e.manager_id
  )
  SEARCH DEPTH FIRST BY hire_date SET order1
  CYCLE hire_date SET is_cycle TO 'Y' DEFAULT 'N'
SELECT lpad(' ',2*reportLevel)||emp_last emp_name, eid, mgr_id,
       hire_date, job_id, is_cycle
FROM dup_hiredate
ORDER BY order1;

