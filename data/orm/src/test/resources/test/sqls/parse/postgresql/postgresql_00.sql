-- copy from druid test resources, see https://github.com/alibaba/druid

/*!cobar: select,4,ireport.dm_mdm_mem_prod_effect_sdt0.admin_member_seq=201152175*/ 
               select product_id, 
                      sum_prod_show_num, 
                      sum_prod_click_num, 
                      sum_prod_fb_num, 
                      total_cnt 
                 from (select product_id, 
                              sum_prod_show_num, 
                              sum_prod_click_num, 
                              sum_prod_fb_num, 
                              count(*) over(order by a,ba,c) as total_cnt 
                         from (select product_id, 
                                      sum_prod_show_num, 
                                      sum_prod_click_num, 
                                      sum_prod_fb_num 
                                 from ireport.dm_mdm_mem_prod_effect_sdt0 
                                where stat_date = '2012-02-19' 
                                  and admin_member_seq = ?) b 
                        Order by sum_prod_show_num desc, product_id desc) a limit ? offset (? -1) * ? ;
                        
WITH RECURSIVE employee_recursive(distance, employee_name, manager_name) AS (
    SELECT 1, employee_name, manager_name
    FROM employee
    WHERE manager_name = 'Mary'
  UNION ALL
    SELECT er.distance + 1, e.employee_name, e.manager_name
    FROM employee_recursive er, employee e
    WHERE er.employee_name = e.manager_name
  )
SELECT distance, employee_name FROM employee_recursive;