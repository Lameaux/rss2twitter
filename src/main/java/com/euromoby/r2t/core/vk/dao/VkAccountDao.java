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

import com.euromoby.r2t.core.vk.model.VkAccount;

@Component
public class VkAccountDao {

	private DataSource dataSource;

	private static final VkAccountRowMapper ROW_MAPPER = new VkAccountRowMapper();

	@Autowired
	public VkAccountDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<VkAccount> findAll() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query("select * from vk_account", ROW_MAPPER);
	}	
	
	public VkAccount findByUserId(String userId) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from vk_account where user_id = ?", ROW_MAPPER, userId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void save(VkAccount vkAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into vk_account(user_id, access_token) values (?,?)", vkAccount.getUserId(),
				vkAccount.getAccessToken());
	}

	public void update(VkAccount vkAccount) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update vk_account set access_token = ? where user_id = ?", vkAccount.getAccessToken(),
				vkAccount.getUserId());
	}

	static class VkAccountRowMapper implements RowMapper<VkAccount> {
		@Override
		public VkAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
			VkAccount vkAccount = new VkAccount();
			vkAccount.setUserId(rs.getString("user_id"));
			vkAccount.setAccessToken(rs.getString("access_token"));
			return vkAccount;
		}
	}
}
