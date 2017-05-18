INSERT INTO domain (name, url, create_time, update_time, version)
SELECT "IDP", "http://localhost:4033", NOW(), NOW(), 1 FROM DUAL
WHERE NOT EXISTS (
	SELECT * FROM domain WHERE name = 'IDP'
)
LIMIT 1;