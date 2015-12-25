SELECT * FROM
(SELECT EXTRACT(YEAR FROM order_date) year, order_mode, order_total FROM orders)
PIVOT
(SUM(order_total) FOR order_mode IN ('direct' AS Store, 'online' AS Internet));

SELECT * FROM pivot_table
  UNPIVOT (yearly_total FOR order_mode IN (store AS 'direct',
           internet AS 'online'))
  ORDER BY year, order_mode;
  
SELECT * FROM pivot_table
  UNPIVOT INCLUDE NULLS 
    (yearly_total FOR order_mode IN (store AS 'direct', internet AS 'online'))
  ORDER BY year, order_mode;
  
SELECT last_name, job_id, departments.department_id, department_name
   FROM employees, departments
   WHERE employees.department_id = departments.department_id
   ORDER BY last_name, job_id;
   
SELECT last_name, job_id, departments.department_id, department_name
   FROM employees, departments
   WHERE employees.department_id = departments.department_id
   AND job_id = 'SA_MAN'
   ORDER BY last_name;
   
SELECT last_name, employee_id, hire_date
  FROM employees
  WHERE EXTRACT(YEAR FROM TO_DATE(hire_date, 'DD-MON-RR')) > 2007
  ORDER BY hire_date;
  
SELECT EXTRACT(TIMEZONE_REGION FROM TIMESTAMP '1999-01-01 10:00:00 -08:00')
  FROM DUAL;
  
SELECT TIMESTAMP '2009-10-29 01:30:00' AT TIME ZONE 'US/Pacific'
  FROM DUAL;
  
SELECT *
  FROM my_table
  WHERE datecol = DATE '2002-10-03';
  
SELECT *
  FROM my_table
  WHERE TRUNC(SYSDATE) = DATE '2002-10-03';
  
SELECT 25, +6.34, 0.5, 25e-03, -1, 25f, +6.34F, 0.5d, -1D
FROM DUAL;

SELECT 3 FROM DUAL
   INTERSECT
SELECT 3f FROM DUAL;

SELECT location_id, department_name "Department", 
   TO_CHAR(NULL) "Warehouse"  FROM departments
   UNION
   SELECT location_id, TO_CHAR(NULL) "Department", warehouse_name 
   FROM warehouses;
   
SELECT location_id  FROM locations 
UNION ALL 
SELECT location_id  FROM departments
ORDER BY location_id;

SELECT product_id FROM inventories
MINUS
SELECT product_id FROM order_items
ORDER BY product_id;

SELECT employee_id, manager_id 
   FROM employees
   WHERE employees.manager_id(+) = employees.employee_id;
   
SELECT * FROM employees 
   WHERE department_id NOT IN 
   (SELECT department_id FROM departments 
       WHERE location_id = 1700)
   ORDER BY last_name;
   
SELECT t1.department_id, t2.* FROM hr_info t1, TABLE(t1.people) t2
   WHERE t2.department_id = t1.department_id;
   
SELECT t1.department_id, t2.* 
   FROM hr_info t1, TABLE(CAST(MULTISET(
      SELECT t3.last_name, t3.department_id, t3.salary 
         FROM people t3
      WHERE t3.department_id = t1.department_id)
      AS people_tab_typ)) t2;
-- copy from druid test resources, see https://github.com/alibaba/druid      
SELECT LPAD(' ',2*(LEVEL-1)) || last_name org_chart, 
        employee_id, manager_id, job_id
    FROM employees
    START WITH job_id = 'AD_VP' 
    CONNECT BY PRIOR employee_id = manager_id; 
    
SELECT LPAD(' ',2*(LEVEL-1)) || last_name org_chart, 
        employee_id, manager_id, job_id
    FROM employees
    WHERE job_id != 'FI_MGR'
    START WITH job_id = 'AD_VP' 
    CONNECT BY PRIOR employee_id = manager_id; 
    
SELECT last_name, department_name 
   FROM employees@remote, departments
   WHERE employees.department_id = departments.department_id; 
   
SELECT e1.last_name FROM employees e1
   WHERE f(
   CURSOR(SELECT e2.hire_date FROM employees e2
   WHERE e1.employee_id = e2.manager_id),
   e1.hire_date) = 1
   ORDER BY last_name;
   
select b.* 		from 		 ( 		    select * from (select row_.*, rownum rownum_ from (    	 		    	  select * 		                    from ( 		                    		select '1' is_draft,a.IS_DISPLAY,a.score,a.type,a.repository_type,a.GMT_MODIFIED,ID 		                            from product_draft a 		                            where 1=1 									 										and 											a.COMPANY_ID = ? 										 									 									 									 										 									 									 										 									 									 										 									 									 									     									 									 										 													 									 										and 																	 																	a.REPOSITORY_TYPE in ( 																	 											 												? 														, 												? 														 																	 																	) 																	 										 									 									 										and 											upper(a.SUBJECT) like 											upper(?) 																 									 									 										 									 									 										 									 									 										 									 									 										and 														 														a.OWNER_MEMBER_ID in ( 														 											 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														 														 														) 														 										 									 									 										and 														 														a.STATUS in ( 														 											 												? 														, 												? 														, 												? 														, 												? 														, 												? 														, 												? 														 														 														) 														 										 									 									 										 									 									 										 									 		                          union all 		                          select '0' is_draft,a.IS_DISPLAY,a.score,a.type,a.repository_type,a.GMT_MODIFIED,ID 		                            from product a 		                            where a.draft_status = 'no_status' 									 											and 												a.COMPANY_ID = ? 											 									 									 									 										 									 									 										 									 									 										 									 									 									     									 									 										 													 									 										and 											 											a.REPOSITORY_TYPE in ( 											 											 												? 											, 												? 											 											 											) 											 										 									 									 										and 											upper(a.SUBJECT) like 											upper(?) 										 									 									 										 									 									 										 									 									 										 									 									 										and 											a.OWNER_MEMBER_ID in ( 											 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															 											) 										 									 									 										and 															 															a.STATUS in ( 														 											 												? 															, 												? 															, 												? 															, 												? 															, 												? 															, 												? 															 															 															) 														 										 										 									 									 										 									 									 										 									) 				                    									 										order by 										 										 											type asc 										 										 										 										 											, 										 									 									 									 										 											"GMT_MODIFIED" 											desc 										, 											"ID" 											desc 										 									 								 								 		    )row_ where rownum <= ? ) where rownum_ >= ?    	) a, 		   (select '1' as is_draft, id, m.ACTION_TRACE,m.TYPE,m.IMAGE_REPOSITORY_IDS,m.SUBJECT,m.MEMBER_ID,m.MEMBER_SEQ, m.REPOSITORY_TYPE,m.STATUS,m.GROUP_ID,  							m.GROUP_ID2,m.GROUP_ID3,m.GMT_MODIFIED,m.GMT_CREATE,m.COMPANY_ID, m.OWNER_MEMBER_ID,m.OWNER_MEMBER_SEQ,m.IND_BY_ALL,m.IND_BY_GROUP, 							m.IND_BY_GROUP2,m.IND_BY_GROUP3,m.HAVE_IMAGE,m.IMAGE_COUNT,m.IS_DISPLAY,m.RED_MODEL,m.KEYWORDS,m.EXPORT_TYPE,                             m.IMAGE_VERSION, m.SCORE from product_draft m 		   union all 		   select '0' as is_draft, id, NULL AS ACTION_TRACE,n.TYPE,n.IMAGE_REPOSITORY_IDS,n.SUBJECT,n.MEMBER_ID,n.MEMBER_SEQ,n.REPOSITORY_TYPE, 					n.STATUS,n.GROUP_ID,n.GROUP_ID2,n.GROUP_ID3,n.GMT_MODIFIED,n.GMT_CREATE,n.COMPANY_ID,n.OWNER_MEMBER_ID,n.OWNER_MEMBER_SEQ, 					n.IND_BY_ALL,n.IND_BY_GROUP,n.IND_BY_GROUP2,n.IND_BY_GROUP3,n.HAVE_IMAGE,n.IMAGE_COUNT,n.IS_DISPLAY,n.RED_MODEL,n.KEYWORDS,                              n.EXPORT_TYPE, n.IMAGE_VERSION, n.SCORE from product n) b 		where a.is_draft=b.is_draft 		and a.id=b.id 		 			 				order by 				 				 					b.type asc 				 				 				 				 					, 				 			 			 			 				 					b."GMT_MODIFIED" 					desc 				, 					b."ID" 					desc;

UPDATE company_approved SET gmt_modified = NULL WHERE ID = ?; 		

