package com.euromoby.r2t.web;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.web.exception.BadRequestException;
import com.euromoby.r2t.web.exception.ResourceNotFoundException;
import com.euromoby.r2t.web.model.RssFeed;

@Controller
public class AjaxController {

	@Autowired
	private TwitterManager twitterManager;

	@Autowired
	private Session session;	

	@RequestMapping(value = "/feeds", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody
	List<TwitterRssFeed> feeds(ModelMap model) {
		if (session.isNotAuthenticated()) {
			throw new BadRequestException();
		}		
		return twitterManager.findRssFeedsByScreenName(session.getScreenName());
	}	
	
	@RequestMapping(value = "/feeds/new", method = RequestMethod.POST)
	public @ResponseBody
	String newFeed(@Valid @ModelAttribute("rss_feed") RssFeed rssFeed, BindingResult result, ModelMap model) {
		
		if (session.isNotAuthenticated() || result.hasErrors()) {
			throw new BadRequestException();
		}		
		
		// ignore duplicates		
		List<TwitterRssFeed> existingFeeds = twitterManager.findRssFeedsByScreenNameAndUrl(session.getScreenName(), rssFeed.getUrl());
		if (existingFeeds.isEmpty()) {
			TwitterRssFeed twitterRssFeed = new TwitterRssFeed();
			twitterRssFeed.setScreenName(session.getScreenName());
			twitterRssFeed.setUrl(rssFeed.getUrl());
			twitterRssFeed.setFrequency(rssFeed.getFrequency());
			twitterRssFeed.setUpdated(System.currentTimeMillis());
			twitterManager.saveRssFeed(twitterRssFeed);
		}
		return rssFeed.getUrl();
	}

	@RequestMapping(value = "/feeds/{id}/delete", method = RequestMethod.POST)	
	public @ResponseBody
	String deleteFeed(ModelMap model, @PathVariable("id") Integer id) {
		
		if (session.isNotAuthenticated()) {
			throw new BadRequestException();
		}		
		TwitterRssFeed twitterRssFeed = twitterManager.findRssFeedsByScreenNameAndId(session.getScreenName(), id);
		if (twitterRssFeed == null) {
			throw new ResourceNotFoundException();
		}
		twitterManager.deleteRssFeed(twitterRssFeed);
		return id.toString();
	}	
	
}
