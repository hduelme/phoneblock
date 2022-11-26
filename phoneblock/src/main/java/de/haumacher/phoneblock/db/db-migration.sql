# Introducing OAuth

DROP INDEX USERS_UN_INDEX_4;

ALTER TABLE USERS ADD LOGIN VARCHAR_IGNORECASE(255) NULL;
UPDATE USERS SET LOGIN = EMAIL WHERE LOGIN IS NULL;
ALTER TABLE USERS ALTER COLUMN LOGIN SET NOT NULL;

ALTER TABLE USERS ADD CLIENTNAME VARCHAR_IGNORECASE(64) NULL;
UPDATE USERS SET CLIENTNAME = 'email' WHERE CLIENTNAME IS NULL;
ALTER TABLE USERS ALTER COLUMN CLIENTNAME SET NOT NULL;

ALTER TABLE USERS ADD EXTID VARCHAR_IGNORECASE(64) NULL;

ALTER TABLE USERS ADD DISPLAYNAME VARCHAR_IGNORECASE(255) NULL;
UPDATE USERS SET DISPLAYNAME = EMAIL WHERE DISPLAYNAME IS NULL;
ALTER TABLE USERS ALTER COLUMN DISPLAYNAME SET NOT NULL;

ALTER TABLE USERS ALTER COLUMN EMAIL SET NULL;

CREATE UNIQUE INDEX USERS_CLIENTNAME_IDX ON USERS (CLIENTNAME,EXTID);
CREATE UNIQUE INDEX USERS_UN_INDEX_4 ON USERS (LOGIN);



