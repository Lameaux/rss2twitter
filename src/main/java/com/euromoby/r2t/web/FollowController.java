package com.euromoby.r2t.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import twitter4j.User;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;

@Controller
public class FollowController {

	private static final Logger log = LoggerFactory.getLogger(FollowController.class);

	@Autowired
	private TwitterManager twitterManager;
	@Autowired
	private TwitterProvider twitterProvider;

	@Autowired
	private Session session;

	@RequestMapping("/followers")
	public String followers(ModelMap model) {
		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}

		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(session.getScreenName());
		if (twitterAccount == null) {
			log.debug("account {} not found", session.getScreenName());
			return "redirect:/error";
		}
		try {
			List<User> friends = twitterProvider.getFollowers(twitterAccount);
			model.put("accounts", friends);
		} catch (Exception e) {
			log.error("Unable to get followers", e);
			return "redirect:/error";
		}

		model.put("session", session);
		model.put("pageTitle", "Followers");
		model.put("page", "followers");
		return "follow";
	}

	@RequestMapping("/friends")
	public String friends(ModelMap model) {
		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}

		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(session.getScreenName());
		if (twitterAccount == null) {
			log.debug("account {} not found", session.getScreenName());
			return "redirect:/error";
		}
		try {
			List<User> accounts = twitterProvider.getFriends(twitterAccount);
			model.put("accounts", accounts);
		} catch (Exception e) {
			log.error("Unable to get followers", e);
			return "redirect:/error";
		}

		model.put("session", session);
		model.put("pageTitle", "Friends");
		model.put("page", "friends");
		return "follow";
	}	

	@RequestMapping("/suggestions/{slug}")
	public String suggestions(ModelMap model, @PathVariable(value="slug") String slug) {
		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}

		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(session.getScreenName());
		if (twitterAccount == null) {
			log.debug("account {} not found", session.getScreenName());
			return "redirect:/error";
		}
		try {
			List<User> accounts = twitterProvider.getSuggestions(twitterAccount, slug);
			model.put("accounts", accounts);
		} catch (Exception e) {
			log.error("Unable to get followers", e);
			return "redirect:/error";
		}

		model.put("session", session);
		model.put("pageTitle", "Suggestions");
		model.put("page", "suggestions");
		return "follow";
	}	
	
}
