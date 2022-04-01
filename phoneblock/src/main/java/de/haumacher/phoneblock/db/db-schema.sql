-- PUBLIC.CONTACTS definition

-- Drop table

-- DROP TABLE CONTACTS;

CREATE TABLE BLOCKLIST (
	PHONE VARCHAR(100) NOT NULL,
	OWNER BIGINT NOT NULL,
	HASH BIGINT NOT NULL,
	CONSTRAINT BLOCKLIST_PK PRIMARY KEY (PHONE,OWNER)
);

-- PUBLIC.SPAMREPORTS definition

-- Drop table

-- DROP TABLE SPAMREPORTS;

CREATE TABLE SPAMREPORTS (
	PHONE VARCHAR(100) NOT NULL,
	VOTES INTEGER DEFAULT 1 NOT NULL,
	LASTUPDATE BIGINT NOT NULL,
	CONSTRAINT SPAMREPORTS_PK PRIMARY KEY (PHONE)
);

-- PUBLIC.USERS definition

-- Drop table

-- DROP TABLE USERS;

CREATE TABLE USERS (
	EMAIL VARCHAR(255) NOT NULL,
	ID BIGINT NOT NULL AUTO_INCREMENT,
	PWHASH VARBINARY(128) NOT NULL,
	CONSTRAINT USERS_PK PRIMARY KEY (ID)
);
CREATE UNIQUE INDEX USERS_UN_INDEX_4 ON USERS (EMAIL);
