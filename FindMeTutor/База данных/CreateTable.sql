CREATE SEQUENCE IF NOT EXISTS account_idaccount_seq
    INCREMENT 1
    START 6
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY account.idaccount;

CREATE SEQUENCE IF NOT EXISTS request_idrequest_seq
    INCREMENT 1
    START 6
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY request.idrequest;

CREATE TABLE account (
  idAccount BIGINT  NOT NULL  default nextval('account_idaccount_seq'::regclass),
  email VARCHAR(255)   NOT NULL ,
  password_a VARCHAR(255)   NOT NULL ,
  role VARCHAR(20)   NOT NULL   ,
PRIMARY KEY(idAccount));

CREATE TABLE search_criteria (
  account_idAccount BIGINT   NOT NULL ,
  age INTEGER    ,
  gender VARCHAR(20)    ,
  work_experience INTEGER  DEFAULT 0 NOT NULL ,
  city VARCHAR(45)    ,
  disciplines VARCHAR[]      ,
PRIMARY KEY(account_idAccount),
  FOREIGN KEY(account_idAccount)
    REFERENCES account(idAccount));

CREATE INDEX IFK_Rel_06 ON search_criteria (account_idAccount);

CREATE TABLE tutor (
  account_idAccount BIGINT  NOT NULL ,
  surname VARCHAR(45)   NOT NULL ,
  name VARCHAR(45)   NOT NULL ,
  date_of_birth DATE   NOT NULL ,
  gender VARCHAR(20)   NOT NULL ,
  city VARCHAR(45)   NOT NULL ,
  work_experience INTEGER  DEFAULT 0  ,
  disciplines VARCHAR[]    ,
  education VARCHAR(255) ,
  dop_info TEXT      ,
PRIMARY KEY(account_idAccount),
  FOREIGN KEY(account_idAccount)
    REFERENCES account(idAccount));

CREATE INDEX IFK_Rel_01 ON tutor (account_idAccount);

CREATE TABLE account_now (
  id_user VARCHAR(45)   NOT NULL ,
  account_idAccount BIGINT      ,
PRIMARY KEY(id_user),
  FOREIGN KEY(account_idAccount)
    REFERENCES account(idAccount));

CREATE INDEX IFK_Rel_07 ON account_now (account_idAccount);

CREATE TABLE client (
  account_idAccount BIGINT   NOT NULL ,
  surname VARCHAR(45)   NOT NULL ,
  name VARCHAR(45)   NOT NULL ,
  state VARCHAR(45)      ,
PRIMARY KEY(account_idAccount),
  FOREIGN KEY(account_idAccount)
    REFERENCES account(idAccount));

CREATE INDEX IFK_Rel_02 ON client (account_idAccount);

CREATE TABLE request (
  idRequest BIGINT  NOT NULL  default nextval('request_idrequest_seq'::regclass),
  tutor_account_idAccount BIGINT   NOT NULL ,
  client_account_idAccount BIGINT   NOT NULL ,
  date_request DATE   NOT NULL ,
  dop_info TEXT   NOT NULL ,
  request_status VARCHAR(45)   NOT NULL ,
  date_lesson TIMESTAMP[]   NOT NULL   ,
PRIMARY KEY(idRequest, tutor_account_idAccount, client_account_idAccount),
  FOREIGN KEY(tutor_account_idAccount)
    REFERENCES tutor(account_idAccount),
  FOREIGN KEY(client_account_idAccount)
    REFERENCES client(account_idAccount));

CREATE INDEX IFK_Rel_04 ON request (tutor_account_idAccount);
CREATE INDEX IFK_Rel_05 ON request (client_account_idAccount);
