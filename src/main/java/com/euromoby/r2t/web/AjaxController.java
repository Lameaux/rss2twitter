package com.euromoby.r2t.web;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;
import com.euromoby.r2t.web.exception.BadRequestException;
import com.euromoby.r2t.web.model.RssUrl;

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
	String newFeed(@Valid @ModelAttribute("rss_url") RssUrl rssUrl, BindingResult result, ModelMap model) {
		
		if (session.isNotAuthenticated()) {
			throw new BadRequestException();
		}		

		// ignore duplicates		
		List<TwitterRssFeed> existingFeeds = twitterManager.findRssFeedsByScreenNameAndUrl(session.getScreenName(), rssUrl.getUrl());
		if (existingFeeds.isEmpty()) {
			TwitterRssFeed twitterRssFeed = new TwitterRssFeed();
			twitterRssFeed.setScreenName(session.getScreenName());
			twitterRssFeed.setUrl(rssUrl.getUrl());
			twitterRssFeed.setUpdated(new Date());
			twitterManager.saveRssFeed(twitterRssFeed);
		}
		
		return rssUrl.getUrl();
	}

}
