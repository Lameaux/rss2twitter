package com.euromoby.r2t.core.twitter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.twitter.model.TwitterFriend;

@Component
public class TwitterFriendDao {

	private DataSource dataSource;

	private static final TwitterFriendRowMapper ROW_MAPPER = new TwitterFriendRowMapper();

	@Autowired
	public TwitterFriendDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public TwitterFriend find(String screenName, String friendScreenName) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_friend where screen_name = ? and friend_screen_name = ?", ROW_MAPPER, screenName, friendScreenName);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void save(TwitterFriend twitterFriend) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into twitter_friend(screen_name, friend_screen_name) values (?,?)", twitterFriend.getScreenName(),
				twitterFriend.getFriendScreenName());
	}

	static class TwitterFriendRowMapper implements RowMapper<TwitterFriend> {
		@Override
		public TwitterFriend mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterFriend twitterFriend = new TwitterFriend();
			twitterFriend.setScreenName(rs.getString("screen_name"));
			twitterFriend.setFriendScreenName(rs.getString("friend_screen_name"));
			return twitterFriend;
		}
	}
}
