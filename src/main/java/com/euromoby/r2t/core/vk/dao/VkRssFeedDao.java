package com.euromoby.r2t.core.vk.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.r2t.core.vk.model.VkRssFeed;

@Component
public class VkRssFeedDao {

	private DataSource dataSource;

	private static final VkRssFeedRowMapper ROW_MAPPER = new VkRssFeedRowMapper();

	@Autowired
	public VkRssFeedDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public VkRssFeed findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from vk_rss_feed where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<VkRssFeed> findAll() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from vk_rss_feed order by id", ROW_MAPPER);
	}	
	
	public List<VkRssFeed> findAllByUserId(String userId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from vk_rss_feed where user_id = ? order by id", ROW_MAPPER, userId);
	}

	public List<VkRssFeed> findAllByUserIdAndUrl(String userId, String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from vk_rss_feed where user_id = ? and url = ? order by id", ROW_MAPPER, userId, url);
	}	

	public VkRssFeed findByUserIdAndId(String userId, Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from vk_rss_feed where user_id = ? and id = ?", ROW_MAPPER, userId, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}	
	
	public void save(VkRssFeed vkRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into vk_rss_feed(user_id, wall_owner_id, url, frequency, updated) values (?,?,?,?,?)", 
				vkRssFeed.getUserId(), vkRssFeed.getWallOwnerId(), 
				vkRssFeed.getUrl(), vkRssFeed.getFrequency(),
				vkRssFeed.getUpdated());
		vkRssFeed.setId(jdbcTemplate.queryForObject("select LAST_INSERT_ID()", Integer.class));		
	}

	public void update(VkRssFeed vkRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update vk_rss_feed set user_id = ?, wall_owner_id = ?, url=?, frequency=?, status=?, error_text=?, updated=? where id = ?", 
				vkRssFeed.getUserId(), vkRssFeed.getWallOwnerId(),
				vkRssFeed.getUrl(), vkRssFeed.getFrequency(), vkRssFeed.getStatus(), vkRssFeed.getErrorText(), vkRssFeed.getUpdated(), vkRssFeed.getId());
	}

	public void delete(VkRssFeed vkRssFeed) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("delete from vk_rss_feed where id = ?", vkRssFeed.getId());
	}

	static class VkRssFeedRowMapper implements RowMapper<VkRssFeed> {
		@Override
		public VkRssFeed mapRow(ResultSet rs, int rowNum) throws SQLException {
			VkRssFeed vkRssFeed = new VkRssFeed();
			vkRssFeed.setId(rs.getInt("id"));
			vkRssFeed.setUserId(rs.getString("user_id"));
			vkRssFeed.setWallOwnerId(rs.getString("wall_owner_id"));
			vkRssFeed.setUrl(rs.getString("url"));
			vkRssFeed.setFrequency(rs.getInt("frequency"));
			vkRssFeed.setStatus(rs.getInt("status"));
			vkRssFeed.setErrorText(rs.getString("error_text"));
			vkRssFeed.setUpdated(rs.getLong("updated"));
			return vkRssFeed;
		}
	}
}
