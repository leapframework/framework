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
AND principal_type = 'D';