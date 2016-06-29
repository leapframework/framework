
SELECT (112&16=16) AS f FROM test; -- bug: error parsing semc in select body

select * from t where id in('','12345'); -- bug : empty '' literal_chars

select (:price * num) from owners; -- bug: error parsing semc in select body

-- error parsing row_number() over(partition by r.c1) 
select row_number() over (partition by r.c1) rn from ( 			    
	select * from t
	union  				
	select * from t
) r;

-- error parsing CASE(manager_id AS VARCHAR2(2000))
SELECT CAST(manager_id AS VARCHAR2(2000)), department_name, SUM(salary) dept_total
     FROM employees e, departments d
     WHERE e.department_id = d.department_id
GROUP BY department_name;

-- error parsing join .. on t0.c1 = t1.c1 -> and t0.c2 = 'a' <- ..
select * from t0 
join t1 on t0.c1 = t1.c1 and t0.c2 = 'a'
join (select * from t2 union select * from t3) tt on t0.c1 = tt.c1;

-- error parsing join .. and -> exists ( .. ) <-
select * from Person p join p1 on p.id = p1.name and exists (select id from Person);

SELECT 
  link.*,
  res.resourceName,
  `user`.`NAME` sharerName 
FROM
  pan_share_link link 
  JOIN pan_share_to t 
    ON link.id = t.link_id 
    AND principal_pk = '7b3677b3-59f8-4b2b-a9f9-832f2d13bafa'
  LEFT JOIN 
    (SELECT 
      dir_id id,
      dir_name resourceName,
      IS_DELETED 
    FROM
      pan_dir 
    UNION
    SELECT 
      file_id id,
      file_name resourceName,
      IS_DELETED 
    FROM
      pan_file) res
    ON res.id = link.`resource_pk` 
    AND res.`is_deleted` = '0' 
  JOIN uam_user `user`
    ON `user`.`USER_ID` = link.`share_by`
    
-- order by convert(name using gb2312)
select * from t order by convert(name using gb2312) asc;

SELECT u.*,
			o.`NAME` orgName
		FROM
			uam_user u
		JOIN pan_user_group q ON u.`USER_ID` = q.`user_id`
		LEFT JOIN uam_organization o ON u.ORG_ID = o.ORG_ID
		WHERE u.`IS_DELETED` = 0 
		AND q.`quota_id`=:groupId
		AND u.name like :name
		order by convert(u.name using gb2312) asc;
		
-- case when expr	
select case when 1 then 1 else 2 end
from 
(
	select 1 from dual
	union
	select 1 from dual
) t1 join t2;		

-- case when expr
SELECT * FROM (
	SELECT T.*,
       CASE WHEN :isAdmin THEN 511 WHEN ss.perm IS NULL THEN :defPerm ELSE ss.perm END AS perm,
       CASE WHEN :isAdmin THEN 1 WHEN sss.hasUpload IS NULL THEN :defRestore ELSE sss.hasUpload END AS hasUpload FROM 
   (SELECT d.dir_id id,
	  d.dir_name NAME,
	  d.full_path path,
	  d.`version`,
	  d.`type`,
	  0 size,
	  d.`principal_type` principalType,
	  d.`principal_id` principalId,
	  d.`creator_id` createdId,
	  d.`created_at` createdAt,
	  d.`updated_at` updatedAt,
	  d.`is_deleted` isDeleted,
	  'D' cate,
	  '' ext,
	  d.scope_id AS scopeId,
	  pd.scope_id AS pscopeId
     FROM pan_dir_trash d,pan_dir pd
   WHERE d.principal_id= :teamPrincipalId
     AND d.parent_id = pd.dir_id	
	UNION
      SELECT f.file_id id,
             f.`file_name` NAME,
             f.full_path path,
             f.`version`,
             f.`type`,
             f.`file_size` size,
             f.`principal_type` principalType,
             f.`principal_id` principalId,
	     f.`creator_id` createdId,
	     f.`created_at` createdAt,
	     f.`updated_at` updatedAt,
	     f.`is_deleted` isDeleted,
	     'F' cate,
	     f.file_ext ext,
	     f.`scope_id` AS scopeId,
	     pd.scope_id AS pscopeId
	FROM pan_file_trash f,pan_dir pd
      WHERE f.principal_id= :teamPrincipalId
	AND f.dir_id = pd.dir_id) T 
  LEFT JOIN 
	(SELECT scope_id,perm,MAX(hasUpload) AS hasUpload FROM (SELECT a.scope_id, r.perm,CASE WHEN p.code = 'UPLOAD' THEN 1 ELSE 0 END AS hasUpload
	FROM pan_auth a,pan_role r,pan_role_perm rp,pan_perm p
	WHERE a.`role_id`=r.`id`
	 AND r.id = rp.role_id
	 AND rp.permission_id = p.id
	 AND principal_id IN :principalId
	 AND resource_type='T') s GROUP BY scope_id,perm) ss
        ON T.scopeId = ss.scope_id
   LEFT JOIN 
	(SELECT scope_id,perm,MAX(hasUpload) AS hasUpload FROM (SELECT a.scope_id, r.perm,CASE WHEN p.code = 'UPLOAD' THEN 1 ELSE 0 END AS hasUpload
	FROM pan_auth a,pan_role r,pan_role_perm rp,pan_perm p
	WHERE a.`role_id`=r.`id`
	 AND r.id = rp.role_id
	 AND rp.permission_id = p.id
	 AND principal_id IN :principalId
	 AND resource_type='T') s GROUP BY scope_id,perm) sss
        ON T.pscopeId = sss.scope_id
) recycle;

--nested case when
SELECT 
 f.file_name fileName, o.name orgName, u.`NAME` reviewerName, f.dir_id, f.principal_type,
 CASE
   WHEN d.dir_name IS NULL 
   THEN 
   CASE
     WHEN f.principal_type = 'U' 
     THEN '个人文档' 
     WHEN f.principal_type = 'T' 
     THEN '团队文档' 
     WHEN f.principal_type = 'D' 
     THEN '部门文档' 
     WHEN f.principal_type = 'P' 
     THEN '公共文档' 
     ELSE '其它文档'
   END 
   ELSE d.dir_name END AS dirName, r.* 
FROM
 pan_file f 
 LEFT JOIN (SELECT * FROM pan_file_review WHERE is_delete != 1 )r 
   ON r.file_id = f.file_id 
 LEFT JOIN uam_user u 
   ON r.reviewer = u.USER_ID 
 LEFT JOIN uam_organization o 
   ON o.org_id = u.org_id 
 LEFT JOIN PAN_DIR d 
   ON d.dir_id = f.dir_id 
WHERE f.file_id IN :fileIds
ORDER BY fileName, review_at ASC;

SELECT 
 f.filename,
 CASE
   WHEN f.dir_name IS not NULL 
   THEN 
   CASE
     WHEN f.principal_type = 'U' 
     THEN '个人文档' 
     WHEN f.principal_type = 'T' 
     THEN '团队文档' 
     WHEN f.principal_type = 'D' 
     THEN '部门文档' 
     WHEN f.principal_type = 'P' 
     THEN '公共文档' 
     ELSE '其它文档'
   END 
   ELSE f.dir_name END AS dirName
FROM
 pan_file f;

select * from t where (true) order by c;

-- complex update
UPDATE pan_used_quota
JOIN (
	SELECT
		sum(size) AS fused,
		DISK_QUOTA AS total
	FROM
		(
			SELECT
				sum(file_size) AS size
			FROM
				pan_file
			WHERE
				principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
			UNION
				SELECT
					sum(data_size) AS size
				FROM
					pan_file
				LEFT JOIN pan_version ON data_id = file_id
				WHERE
					principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
		) f
	LEFT JOIN uam_user ON user_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
) t
SET used = fused,
 `usage` = round(
	fused / (total * 1024 * 1024) * 100,
	2
)
WHERE
	quota_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
AND principal_type = 'D';UPDATE pan_used_quota
JOIN (
	SELECT
		sum(size) AS fused,
		DISK_QUOTA AS total
	FROM
		(
			SELECT
				sum(file_size) AS size
			FROM
				pan_file
			WHERE
				principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
			UNION
				SELECT
					sum(data_size) AS size
				FROM
					pan_file
				LEFT JOIN pan_version ON data_id = file_id
				WHERE
					principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
		) f
	LEFT JOIN uam_user ON user_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
) t
SET used = fused,
 `usage` = round(
	fused / (total * 1024 * 1024) * 100,
	2
)
WHERE
	quota_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
AND principal_type = 'D';

-- complex update
UPDATE pan_used_quota
JOIN (
	SELECT
		sum(size) AS fused,
		DISK_QUOTA AS total
	FROM
		(
			SELECT
				sum(file_size) AS size
			FROM
				pan_file
			WHERE
				principal_id = $principalId$
			UNION
				SELECT
					sum(data_size) AS size
				FROM
					pan_file
				LEFT JOIN pan_version ON data_id = file_id
				WHERE
					principal_id = #principalId#
		) f
	LEFT JOIN uam_user ON user_id = ?
) t
SET used = fused,
 `usage` = round(
	fused / (total * 1024 * 1024) * 100,
	2
)
WHERE
	quota_id = ?
AND principal_type = 'D';UPDATE pan_used_quota
JOIN (
	SELECT
		sum(size) AS fused,
		DISK_QUOTA AS total
	FROM
		(
			SELECT
				sum(file_size) AS size
			FROM
				pan_file
			WHERE
				principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
			UNION
				SELECT
					sum(data_size) AS size
				FROM
					pan_file
				LEFT JOIN pan_version ON data_id = file_id
				WHERE
					principal_id = 'Uaad14163-13af-413f-a51c-31cc83527449'
		) f
	LEFT JOIN uam_user ON user_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
) t
SET used = fused,
 `usage` = round(
	fused / (total * 1024 * 1024) * 100,
	2
)
WHERE
	quota_id = '01182800-585b-49e6-8e74-f5fdcc1b30c4'
AND principal_type = 'D';