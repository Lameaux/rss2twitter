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
public class ProfileController {

	private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
	
	@Autowired
	private TwitterManager twitterManager;

	@Autowired
	private TwitterProvider twitterProvider;
	
	@Autowired
	private Session session;

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(ModelMap model) {
		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}
		TwitterAccount twitterAccount = twitterManager.getAccountByScreenName(session.getScreenName());
		if (twitterAccount == null) {
			log.debug("account {} not found", session.getScreenName());
			return "redirect:/error";
		}
		model.put("twitter", twitterAccount);
    	model.put("session", session);
		return "profile";
	}

	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String error(ModelMap model) {
    	model.put("session", session);
		return "error";
	}	

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(ModelMap model) {
		session.setScreenName(null);
		return "redirect:/";
	}	
	
	@RequestMapping(value = "/twitter/connect", method = RequestMethod.GET)
	public String connectTwitterAccount(ModelMap model) {
		try {
			return "redirect:" + twitterProvider.getAuthorizationUrl();
		} catch (TwitterException e) {
			log.error("Error getting auth url", e);
			return "redirect:/error";
		}
	}

	@RequestMapping(value = "/twitter/oauth", method = RequestMethod.GET)
	public String oAuthTwitterAccount(ModelMap model, @RequestParam("oauth_token") String oAuthToken, @RequestParam("oauth_verifier") String oAuthVerifier) {
		try {
			AccessToken accessToken = twitterProvider.getAccessToken(oAuthToken, oAuthVerifier);
			TwitterAccount twitterAccount = twitterManager.saveAccessToken(accessToken);
			session.setScreenName(twitterAccount.getScreenName());
			return "redirect:/profile";
		} catch (TwitterException e) {
			log.debug("auth failed", e);
			return "redirect:/error";
		}
	}

}
