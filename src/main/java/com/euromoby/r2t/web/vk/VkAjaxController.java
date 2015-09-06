package com.euromoby.r2t.web.vk;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.core.vk.VkManager;
import com.euromoby.r2t.core.vk.VkProvider;
import com.euromoby.r2t.core.vk.model.VkAccount;
import com.euromoby.r2t.core.vk.model.VkRssFeed;
import com.euromoby.r2t.core.vk.model.json.VkGroup;
import com.euromoby.r2t.core.vk.model.json.VkGroupResponse;
import com.euromoby.r2t.web.Session;
import com.euromoby.r2t.web.exception.BadRequestException;
import com.euromoby.r2t.web.exception.ResourceNotFoundException;
import com.euromoby.r2t.web.model.VkRssFeedDto;

@Controller
public class VkAjaxController {

	private static final Logger log = LoggerFactory.getLogger(VkAjaxController.class);	
	
	@Autowired
	private VkManager vkManager;

	@Autowired
	private VkProvider vkProvider;	
	
	@Autowired
	private Session session;

	@RequestMapping(value = "/vk/feeds", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	List<VkRssFeed> feeds(ModelMap model) {
		if (session.isVkNotAuthenticated()) {
			throw new BadRequestException();
		}
		return vkManager.findRssFeedsByUserId(session.getVkUserId());
	}

	@RequestMapping(value = "/vk/groups", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	List<VkGroup> groups(ModelMap model) {
		if (session.isVkNotAuthenticated()) {
			throw new BadRequestException();
		}
		
		VkAccount vkAccount = vkManager.getAccountByUserId(session.getVkUserId());
		if (vkAccount == null) {
			throw new BadRequestException();	
		}
		try {
			VkGroupResponse response = vkProvider.getAdminGroups(vkAccount);
			return Arrays.asList(response.getResponse().getItems());
		} catch (Exception e) {
			log.error("Error getting groups", e);
			throw new BadRequestException();
		}
	}	
	
	@RequestMapping(value = "/vk/feeds/new", method = RequestMethod.POST)
	public @ResponseBody
	Integer newFeed(@Valid @ModelAttribute("rss_feed") VkRssFeedDto rssFeed, BindingResult result, ModelMap model) {
		if (session.isVkNotAuthenticated() || result.hasErrors()) {
			throw new BadRequestException();
		}
		// ignore duplicates
		List<VkRssFeed> existingFeeds = vkManager.findRssFeedsByUserIdAndUrl(session.getVkUserId(), rssFeed.getUrl());
		if (!existingFeeds.isEmpty()) {
			return 0;
		}
		VkRssFeed vkRssFeed = new VkRssFeed();
		vkRssFeed.setUserId(session.getVkUserId());
		vkRssFeed.setWallOwnerId(rssFeed.getWall());
		vkRssFeed.setUrl(rssFeed.getUrl());
		vkRssFeed.setFrequency(rssFeed.getFrequency());
		vkRssFeed.setUpdated(0);
		vkManager.saveRssFeed(vkRssFeed);
		return vkRssFeed.getId();
	}

	@RequestMapping(value = "/vk/feeds/{id}/delete", method = RequestMethod.POST)
	public @ResponseBody
	Integer deleteFeed(ModelMap model, @PathVariable("id") Integer id) {
		if (session.isVkNotAuthenticated()) {
			throw new BadRequestException();
		}
		VkRssFeed vkRssFeed = vkManager.findRssFeedsByUserIdAndId(session.getVkUserId(), id);
		if (vkRssFeed == null) {
			throw new ResourceNotFoundException();
		}
		vkManager.deleteRssFeed(vkRssFeed);
		return id;
	}

	@RequestMapping(value = "/vk/feeds/{id}/edit", method = RequestMethod.POST)
	public @ResponseBody
	Integer editFeed(@Valid @ModelAttribute("rss_feed") VkRssFeedDto rssFeed, BindingResult result, @PathVariable("id") Integer id, ModelMap model) {
		if (session.isVkNotAuthenticated() || result.hasErrors()) {
			throw new BadRequestException();
		}
		VkRssFeed vkRssFeed = vkManager.findRssFeedsByUserIdAndId(session.getVkUserId(), id);
		if (vkRssFeed == null) {
			throw new ResourceNotFoundException();
		}		
		if (!vkRssFeed.getUrl().equals(rssFeed.getUrl())) {
			vkRssFeed.setUpdated(0);
			vkRssFeed.setStatus(TwitterRssFeed.STATUS_NEW);
			vkRssFeed.setErrorText(null);
		}
		vkRssFeed.setUrl(rssFeed.getUrl());
		vkRssFeed.setWallOwnerId(rssFeed.getWall());
		vkRssFeed.setFrequency(rssFeed.getFrequency());
		vkManager.updateRssFeed(vkRssFeed);
		return vkRssFeed.getStatus();
	}	
	
}
