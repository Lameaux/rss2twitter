package com.euromoby.r2t.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.TwitterProvider;
import com.euromoby.r2t.core.twitter.model.TwitterAccount;

@Controller
public class TwitterAccountController {

	private static final Logger log = LoggerFactory.getLogger(TwitterAccountController.class);
	
	@Autowired
	private TwitterManager twitterManager;

	@Autowired
	private TwitterProvider twitterProvider;
	
	@Autowired
	private Session session;

	@RequestMapping(value = "/twitter/profile", method = RequestMethod.GET)
	public String twitterProfile(ModelMap model) {
		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}
		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(session.getScreenName());
		if (twitterAccount == null) {
			log.debug("account {} not found", session.getScreenName());
			return "redirect:/twitter/error";
		}
		model.put("twitter", twitterAccount);
		return "twitter_profile";
	}

	@RequestMapping(value = "twitter/error", method = RequestMethod.GET)
	public String twitterError(ModelMap model) {
		return "twitter_error";
	}	
	
	@RequestMapping(value = "/twitter/connect", method = RequestMethod.GET)
	public String connectTwitterAccount(ModelMap model) {
		try {
			return "redirect:" + twitterProvider.getAuthorizationUrl();
		} catch (TwitterException e) {
			log.error("Error getting auth url", e);
			return "redirect:/twitter/error";
		}
	}

	@RequestMapping(value = "/twitter/oauth", method = RequestMethod.GET)
	public String oAuthTwitterAccount(ModelMap model, @RequestParam("oauth_token") String oAuthToken, @RequestParam("oauth_verifier") String oAuthVerifier) {
		try {
			AccessToken accessToken = twitterProvider.getAccessToken(oAuthToken, oAuthVerifier);
			TwitterAccount twitterAccount = twitterManager.saveAccessToken(accessToken);
			session.setScreenName(twitterAccount.getScreenName());
			return "redirect:/twitter/profile";
		} catch (TwitterException e) {
			log.debug("auth failed", e);
			return "redirect:/twitter/error";
		}
	}

}
