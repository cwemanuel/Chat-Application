DROP DATABASE IF EXISTS carlProject;

CREATE DATABASE carlProject;

USE carlProject;

DROP TABLE IF EXISTS accounts;


CREATE TABLE accounts (
	id INT NOT NULL AUTO_INCREMENT,
	username VARCHAR(50) NOT NULL,
	password VARCHAR(50) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO accounts (username, password)
VALUES ('root', 'password');
