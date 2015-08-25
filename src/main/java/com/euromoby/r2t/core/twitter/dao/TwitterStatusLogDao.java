package com.euromoby.r2t.core.twitter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;


@Component
public class TwitterStatusLogDao {

	private DataSource dataSource;

	private static final TwitterStatusLogRowMapper ROW_MAPPER = new TwitterStatusLogRowMapper();

	@Autowired
	public TwitterStatusLogDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public TwitterStatusLog findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_status_log where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}	
	
	public List<TwitterStatusLog> findAllByScreenName(String screenName) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_status_log where screen_name = ? order by id desc", ROW_MAPPER, screenName);
	}

	public List<TwitterStatusLog> findLastOkByScreenName(String screenName, int limit) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_status_log where screen_name = ? and status = ? order by id desc limit ?", ROW_MAPPER, screenName, TwitterStatusLog.STATUS_OK, limit);
	}	

	public List<TwitterStatusLog> findLast(int limit) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_status_log order by id desc limit ?", ROW_MAPPER, limit);
	}	
	
	public TwitterStatusLog findOkByScreenNameAndUrl(String screenName, String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_status_log where screen_name = ? and status = ? and url = ? limit 1", ROW_MAPPER, screenName, TwitterStatusLog.STATUS_OK, url);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} 
	}

	public TwitterStatusLog findErrorByScreenNameAndUrl(String screenName, String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_status_log where screen_name = ? and status = ? and url = ? limit 1", ROW_MAPPER, screenName, TwitterStatusLog.STATUS_ERROR, url);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} 
	}	
	
	
	public void save(TwitterStatusLog twitterStatusLog) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into twitter_status_log(screen_name, url, message, status, updated) values (?,?,?,?,?)", 
				twitterStatusLog.getScreenName(), twitterStatusLog.getUrl(), twitterStatusLog.getMessage(), twitterStatusLog.getStatus(), twitterStatusLog.getUpdated());
		twitterStatusLog.setId(jdbcTemplate.queryForObject("select LAST_INSERT_ID()", Integer.class));		
	}

	public void update(TwitterStatusLog twitterStatusLog) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update twitter_status_log set screen_name=?, url=?, message=?, status=?, error_text=?, updated=? where id = ?", 
				twitterStatusLog.getScreenName(), twitterStatusLog.getUrl(), twitterStatusLog.getMessage(), twitterStatusLog.getStatus(), twitterStatusLog.getErrorText(), twitterStatusLog.getUpdated(), twitterStatusLog.getId());
	}	
	
	static class TwitterStatusLogRowMapper implements RowMapper<TwitterStatusLog> {
		@Override
		public TwitterStatusLog mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterStatusLog twitterStatusLog = new TwitterStatusLog();
			twitterStatusLog.setId(rs.getInt("id"));
			twitterStatusLog.setScreenName(rs.getString("screen_name"));
			twitterStatusLog.setUrl(rs.getString("url"));			
			twitterStatusLog.setMessage(rs.getString("message"));
			twitterStatusLog.setStatus(rs.getInt("status"));
			twitterStatusLog.setErrorText(rs.getString("error_text"));
			twitterStatusLog.setUpdated(rs.getLong("updated"));			
			return twitterStatusLog;
		}
	}
}
