package com.euromoby.r2t.web.twitter;

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
import com.euromoby.r2t.web.Session;
import com.euromoby.r2t.web.exception.BadRequestException;
import com.euromoby.r2t.web.exception.ResourceNotFoundException;
import com.euromoby.r2t.web.model.TwitterRssFeedDto;

@Controller
public class TwitterAjaxController {

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
	Integer newFeed(@Valid @ModelAttribute("rss_feed") TwitterRssFeed rssFeed, BindingResult result, ModelMap model) {
		if (session.isNotAuthenticated() || result.hasErrors()) {
			throw new BadRequestException();
		}
		// ignore duplicates
		List<TwitterRssFeed> existingFeeds = twitterManager.findRssFeedsByScreenNameAndUrl(session.getScreenName(), rssFeed.getUrl());
		if (!existingFeeds.isEmpty()) {
			return 0;
		}
		TwitterRssFeed twitterRssFeed = new TwitterRssFeed();
		twitterRssFeed.setScreenName(session.getScreenName());
		twitterRssFeed.setUrl(rssFeed.getUrl());
		twitterRssFeed.setFrequency(rssFeed.getFrequency());
		twitterRssFeed.setUpdated(0);
		twitterManager.saveRssFeed(twitterRssFeed);
		return twitterRssFeed.getId();
	}

	@RequestMapping(value = "/feeds/{id}/delete", method = RequestMethod.POST)
	public @ResponseBody
	Integer deleteFeed(ModelMap model, @PathVariable("id") Integer id) {
		if (session.isNotAuthenticated()) {
			throw new BadRequestException();
		}
		TwitterRssFeed twitterRssFeed = twitterManager.findRssFeedsByScreenNameAndId(session.getScreenName(), id);
		if (twitterRssFeed == null) {
			throw new ResourceNotFoundException();
		}
		twitterManager.deleteRssFeed(twitterRssFeed);
		return id;
	}

	@RequestMapping(value = "/feeds/{id}/edit", method = RequestMethod.POST)
	public @ResponseBody
	Integer editFeed(@Valid @ModelAttribute("rss_feed") TwitterRssFeed rssFeed, BindingResult result, @PathVariable("id") Integer id, ModelMap model) {
		if (session.isNotAuthenticated() || result.hasErrors()) {
			throw new BadRequestException();
		}
		TwitterRssFeed twitterRssFeed = twitterManager.findRssFeedsByScreenNameAndId(session.getScreenName(), id);
		if (twitterRssFeed == null) {
			throw new ResourceNotFoundException();
		}		
		if (!twitterRssFeed.getUrl().equals(rssFeed.getUrl())) {
			twitterRssFeed.setUpdated(0);
			twitterRssFeed.setStatus(TwitterRssFeed.STATUS_NEW);
			twitterRssFeed.setErrorText(null);
		}
		twitterRssFeed.setUrl(rssFeed.getUrl());
		twitterRssFeed.setFrequency(rssFeed.getFrequency());
		twitterManager.updateRssFeed(twitterRssFeed);
		return twitterRssFeed.getStatus();
	}	
	
}
