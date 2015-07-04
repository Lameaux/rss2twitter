CREATE DATABASE rss2twitter CHARACTER SET utf8 COLLATE utf8_general_ci;

-- DROP TABLE IF EXISTS twitter_account;
-- DROP TABLE IF EXISTS twitter_rss_feed;
-- DROP TABLE IF EXISTS twitter_status_log;

CREATE TABLE IF NOT EXISTS twitter_account (
	screen_name VARCHAR(20) NOT NULL PRIMARY KEY,
	access_token VARCHAR(255),
	access_token_secret VARCHAR(255)	
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS twitter_rss_feed (
	id INT auto_increment PRIMARY KEY, 
	screen_name VARCHAR(20),
	url VARCHAR(255),
	frequency INT,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP	
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS twitter_status_log (
	id INT auto_increment PRIMARY KEY,
	screen_name VARCHAR(20),
	url VARCHAR(255),	
	message VARCHAR(140),
	status INT DEFAULT 0,
	error_text TEXT,
	updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
CREATE INDEX twitter_status_log_url ON twitter_status_log(screen_name, url);


