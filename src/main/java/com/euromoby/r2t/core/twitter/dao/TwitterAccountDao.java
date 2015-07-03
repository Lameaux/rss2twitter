package com.euromoby.r2t.core.twitter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.twitter.model.TwitterAccount;

@Component
public class TwitterAccountDao {

	private DataSource dataSource;

	private static final TwitterAccountRowMapper ROW_MAPPER = new TwitterAccountRowMapper();

	@Autowired
	public TwitterAccountDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public TwitterAccount findByScreenName(String screenName) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_account where screen_name = ?", ROW_MAPPER, screenName);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void save(TwitterAccount twitterAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into twitter_account(screen_name, access_token, access_token_secret) values (?,?,?)", twitterAccount.getScreenName(),
				twitterAccount.getAccessToken(), twitterAccount.getAccessTokenSecret());
	}

	public void update(TwitterAccount twitterAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update twitter_account set access_token = ?, access_token_secret = ? where screen_name = ?", twitterAccount.getAccessToken(),
				twitterAccount.getAccessTokenSecret(), twitterAccount.getScreenName());
	}

	static class TwitterAccountRowMapper implements RowMapper<TwitterAccount> {
		@Override
		public TwitterAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterAccount twitterAccount = new TwitterAccount();
			twitterAccount.setScreenName(rs.getString("screen_name"));
			twitterAccount.setAccessToken(rs.getString("access_token"));
			twitterAccount.setAccessTokenSecret(rs.getString("access_token_secret"));
			return twitterAccount;
		}
	}
}
