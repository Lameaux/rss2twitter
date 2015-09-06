package com.euromoby.r2t.core.vk;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.euromoby.r2t.core.vk.dao.VkAccountDao;
import com.euromoby.r2t.core.vk.dao.VkRssFeedDao;
import com.euromoby.r2t.core.vk.model.VkAccount;
import com.euromoby.r2t.core.vk.model.VkRssFeed;

@Component
public class VkManager {
	
	@Autowired
	private VkAccountDao vkAccountDao;
	@Autowired
	private VkRssFeedDao vkRssFeedDao;
	
	@Transactional(readOnly = true)
	public VkAccount getAccountByUserId(String userId) {
		return vkAccountDao.findByUserId(userId);
	}	

	@Transactional(readOnly = true)
	public List<VkRssFeed> findRssFeedsByUserId(String userId) {
		return vkRssFeedDao.findAllByUserId(userId);
	}	

	@Transactional(readOnly = true)
	public List<VkRssFeed> findRssFeedsByUserIdAndUrl(String userId, String url) {
		return vkRssFeedDao.findAllByUserIdAndUrl(userId, url);
	}	

	@Transactional(readOnly = true)
	public VkRssFeed findRssFeedsByUserIdAndId(String userId, Integer id) {
		return vkRssFeedDao.findByUserIdAndId(userId, id);
	}	
	
	@Transactional
	public VkAccount saveOrUpdateVkAccount(VkAccount vkAccount) {
		VkAccount account = vkAccountDao.findByUserId(vkAccount.getUserId());
		if (account == null) {
			vkAccountDao.save(vkAccount);
		} else {
			vkAccountDao.update(vkAccount);
		}
		return account;
	}
	
	@Transactional
	public void saveRssFeed(VkRssFeed vkRssFeed) {
		vkRssFeedDao.save(vkRssFeed);
	}

	@Transactional
	public void updateRssFeed(VkRssFeed vkRssFeed) {
		vkRssFeedDao.update(vkRssFeed);
	}

	@Transactional
	public void deleteRssFeed(VkRssFeed vkRssFeed) {
		vkRssFeedDao.delete(vkRssFeed);
	}	
	
}
