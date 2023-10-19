-- PUBLIC.BLOCKLIST definition

-- Drop table

-- DROP TABLE BLOCKLIST;

CREATE TABLE BLOCKLIST (
	OWNER BIGINT NOT NULL,
	PHONE CHARACTER VARYING(100) NOT NULL,

	CONSTRAINT BLOCKLIST_PK PRIMARY KEY (OWNER,PHONE)
);

-- PUBLIC.EXCLUDES definition

-- Drop table

-- DROP TABLE EXCLUDES;

CREATE TABLE EXCLUDES (
	OWNER BIGINT NOT NULL,
	PHONE CHARACTER VARYING(100) NOT NULL,

	CONSTRAINT EXCLUDES_PK PRIMARY KEY (OWNER,PHONE)
);

-- PUBLIC.SPAMREPORTS definition

-- Drop table

-- DROP TABLE SPAMREPORTS;

CREATE TABLE SPAMREPORTS (
	PHONE VARCHAR(100) NOT NULL,
	VOTES INTEGER DEFAULT 1 NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	DATEADDED BIGINT DEFAULT 0 NOT NULL,

	CONSTRAINT SPAMREPORTS_PK PRIMARY KEY (PHONE)
);

CREATE TABLE OLDREPORTS (
	PHONE VARCHAR(100) NOT NULL,
	VOTES INTEGER DEFAULT 1 NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	DATEADDED BIGINT DEFAULT 0 NOT NULL,

	CONSTRAINT OLDREPORTS_PK PRIMARY KEY (PHONE)
);

-- PUBLIC.WHITELIST definition

-- Drop table

-- DROP TABLE WHITELIST;

CREATE TABLE WHITELIST (
	PHONE CHARACTER VARYING(100) NOT NULL,

	CONSTRAINT WHITELIST_PK PRIMARY KEY (PHONE)
);

-- PUBLIC.USERS definition

-- Drop table

-- DROP TABLE USERS;

CREATE TABLE USERS (
	ID BIGINT NOT NULL AUTO_INCREMENT,

	LOGIN CHARACTER VARYING(255) NOT NULL,
	PWHASH BINARY VARYING(128) NOT NULL,
	
	CLIENTNAME CHARACTER VARYING(64) NOT NULL,
	EXTID CHARACTER VARYING(255),

	DISPLAYNAME CHARACTER VARYING(255) NOT NULL,
	EMAIL CHARACTER VARYING(255),
	LASTACCESS BIGINT DEFAULT 0 NOT NULL,
	REGISTERED BIGINT DEFAULT 0 NOT NULL,
	MIN_VOTES INTEGER NOT NULL,
	MAX_LENGTH INTEGER NOT NULL,
	USERAGENT CHARACTER VARYING(512) DEFAULT NULL,

	NOTIFIED BOOLEAN DEFAULT 0 NOT NULL,
	WILDCARDS BOOLEAN DEFAULT 1 NOT NULL,

	CONSTRAINT USERS_PK PRIMARY KEY (ID)
);

CREATE INDEX USERS_CLIENTNAME_IDX ON USERS (CLIENTNAME,EXTID);
CREATE UNIQUE INDEX USERS_UN_INDEX_4 ON USERS (LOGIN);

-- PUBLIC.CALLREPORT definition

-- Drop table

-- DROP TABLE CALLREPORT;

CREATE TABLE CALLREPORT (
	USERID BIGINT NOT NULL,
	LASTID CHARACTER VARYING(64) NOT NULL,
	"TIMESTAMP" CHARACTER VARYING(64) NOT NULL,
	LASTACCESS BIGINT NOT NULL,

	CONSTRAINT CALLREPORT_PK PRIMARY KEY (USERID)
);

-- PUBLIC.CALLERS definition

-- Drop table

-- DROP TABLE CALLERS;

CREATE TABLE CALLERS (
	USERID BIGINT NOT NULL,
	PHONE CHARACTER VARYING(100) NOT NULL,
	CALLS INTEGER DEFAULT 0 NOT NULL,
	LASTUPDATE BIGINT NOT NULL,

	CONSTRAINT CALLERS_PK PRIMARY KEY (USERID,PHONE)
);

-- PUBLIC.RATINGS definition

-- Drop table

-- DROP TABLE RATINGS;

CREATE TABLE RATINGS (
	PHONE CHARACTER VARYING(100) NOT NULL,
	RATING CHARACTER VARYING(15) NOT NULL,
	COUNT INTEGER DEFAULT 0 NOT NULL,
	LASTUPDATE BIGINT DEFAULT 0 NOT NULL,
	BACKUP INTEGER DEFAULT 0 NOT NULL,
	
	CONSTRAINT RATINGS_PK PRIMARY KEY (PHONE,RATING)
);


-- PUBLIC.RATINGHISTORY definition

-- Drop table

-- DROP TABLE RATINGHISTORY;

CREATE TABLE RATINGHISTORY (
	REV INTEGER NOT NULL,
	PHONE CHARACTER VARYING(100) NOT NULL,
	RATING CHARACTER VARYING(15) NOT NULL,
	COUNT INTEGER DEFAULT 1 NOT NULL,

	CONSTRAINT RATINGHISTORY_PK PRIMARY KEY (REV,PHONE,RATING)
);

CREATE TABLE META_UPDATE (
	PHONE CHARACTER VARYING(100) NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	
	CONSTRAINT META_UPDATE_PK PRIMARY KEY (PHONE)
);

CREATE TABLE COMMENTS (
	ID VARCHAR(64) NOT NULL,

	PHONE CHARACTER VARYING(100) NOT NULL,
	RATING CHARACTER VARYING(15) NOT NULL,
	COMMENT CHARACTER VARYING(8192) NULL,
	SERVICE CHARACTER VARYING(30) NULL,
	CREATED BIGINT NOT NULL,
	UP INTEGER DEFAULT 0 NOT NULL,
	DOWN INTEGER DEFAULT 0 NOT NULL,

	CONSTRAINT COMMENTS_PK PRIMARY KEY (ID)
);
CREATE INDEX COMMENTS_PHONE_IDX ON COMMENTS (PHONE,CREATED);

-- PUBLIC.SEARCHES definition

-- Drop table

-- DROP TABLE SEARCHES;

CREATE TABLE SEARCHES (
	PHONE CHARACTER VARYING(100) NOT NULL,
	COUNT INTEGER DEFAULT 1 NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	BACKUP INTEGER DEFAULT 0 NOT NULL,

	CONSTRAINT SEARCHES_PK PRIMARY KEY (PHONE)
);

-- PUBLIC.SEARCHCLUSTER definition

-- Drop table

-- DROP TABLE SEARCHCLUSTER;

CREATE TABLE SEARCHCLUSTER (
	ID INTEGER NOT NULL AUTO_INCREMENT,
	CREATED BIGINT NOT NULL,

	CONSTRAINT SEARCHCLUSTER_PK PRIMARY KEY (ID)
);

-- PUBLIC.SEARCHHISTORY definition

-- Drop table

-- DROP TABLE SEARCHHISTORY;

CREATE TABLE SEARCHHISTORY (
	CLUSTER INTEGER NOT NULL,
	PHONE CHARACTER VARYING(100) NOT NULL,
	COUNT INTEGER NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	
	CONSTRAINT SEARCHHISTORY_PK PRIMARY KEY (CLUSTER,PHONE)
);

-- PUBLIC.SUMMARY_REQUEST definition

-- Drop table

-- DROP TABLE SUMMARY_REQUEST;

CREATE TABLE SUMMARY_REQUEST (
	PHONE CHARACTER VARYING(100) NOT NULL,
	PRIORITY INTEGER NOT NULL AUTO_INCREMENT,
	CONSTRAINT SUMMARY_REQUEST_PK PRIMARY KEY (PHONE)
);
CREATE INDEX SUMMARY_REQUEST_PRIORITY_IDX ON SUMMARY_REQUEST (PRIORITY);

-- PUBLIC.SUMMARY definition

-- Drop table

-- DROP TABLE SUMMARY;

CREATE TABLE SUMMARY (
	PHONE CHARACTER VARYING(100) NOT NULL,
	COMMENT CHARACTER VARYING(4096) NOT NULL,
	CREATED BIGINT NOT NULL,
	CONSTRAINT SUMMARY_PK PRIMARY KEY (PHONE)
);

-- PUBLIC.PREFIX definition

-- Drop table

-- DROP TABLE PREFIX;

CREATE TABLE PREFIX (
	PHONE CHARACTER VARYING(100) NOT NULL,
	PREFIX CHARACTER VARYING(100),
	CONSTRAINT PREFIX_PK PRIMARY KEY (PHONE)
);
CREATE INDEX PREFIX_PREFIX_IDX ON PREFIX (PREFIX);

