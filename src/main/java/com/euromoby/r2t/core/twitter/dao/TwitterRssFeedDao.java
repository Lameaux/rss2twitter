package com.euromoby.r2t.core.twitter.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;

@Component
public class TwitterRssFeedDao {

	private DataSource dataSource;

	private static final TwitterRssFeedRowMapper ROW_MAPPER = new TwitterRssFeedRowMapper();

	@Autowired
	public TwitterRssFeedDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public TwitterRssFeed findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_rss_feed where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<TwitterRssFeed> findAll() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_rss_feed order by id", ROW_MAPPER);
	}	
	
	public List<TwitterRssFeed> findAllByScreenName(String screenName) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_rss_feed where screen_name = ? order by id", ROW_MAPPER, screenName);
	}

	public List<TwitterRssFeed> findAllByScreenNameAndUrl(String screenName, String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from twitter_rss_feed where screen_name = ? and url = ? order by id", ROW_MAPPER, screenName, url);
	}	

	public TwitterRssFeed findByScreenNameAndId(String screenName, Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from twitter_rss_feed where screen_name = ? and id = ?", ROW_MAPPER, screenName, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}	
	
	public void save(TwitterRssFeed twitterRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into twitter_rss_feed(screen_name, url, frequency, updated) values (?,?,?,?)", twitterRssFeed.getScreenName(), 
				twitterRssFeed.getUrl(), twitterRssFeed.getFrequency(),
				new Timestamp(twitterRssFeed.getUpdated()));
	}

	public void update(TwitterRssFeed twitterRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update twitter_rss_feed set screen_name = ?, url=?, frequency=?, updated=? where id = ?", twitterRssFeed.getScreenName(),
				twitterRssFeed.getUrl(), twitterRssFeed.getFrequency(), new Timestamp(twitterRssFeed.getUpdated()), twitterRssFeed.getId());
	}

	public void delete(TwitterRssFeed twitterRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("delete from twitter_rss_feed where id = ?", twitterRssFeed.getId());
	}

	static class TwitterRssFeedRowMapper implements RowMapper<TwitterRssFeed> {
		@Override
		public TwitterRssFeed mapRow(ResultSet rs, int rowNum) throws SQLException {
			TwitterRssFeed twitterRssFeed = new TwitterRssFeed();
			twitterRssFeed.setId(rs.getInt("id"));
			twitterRssFeed.setScreenName(rs.getString("screen_name"));
			twitterRssFeed.setUrl(rs.getString("url"));
			twitterRssFeed.setFrequency(rs.getInt("frequency"));
			twitterRssFeed.setUpdated(rs.getTimestamp("updated").getTime());
			return twitterRssFeed;
		}
	}
}
