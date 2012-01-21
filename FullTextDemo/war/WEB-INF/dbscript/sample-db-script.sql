CREATE SCHEMA search_values DEFAULT CHARACTER SET utf8 ;

USE search_values;


CREATE TABLE articles  (
  entity_key varchar(250),
  title text,
  body text,
  PRIMARY KEY RESULTS_PK (entity_key),
  FULLTEXT (title,body)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


