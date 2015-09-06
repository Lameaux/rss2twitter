package com.euromoby.r2t.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;

@Controller
public class WelcomeController {
	
	@Autowired
	private Session session;
	
	@Autowired
	private TwitterManager twitterManager;
	
    @RequestMapping("/")
    public String welcome(ModelMap model) {
    	
		if (!session.isNotAuthenticated()) {
			return "redirect:/profile";
		}    	
    	
		List<TwitterStatusLog> statusLogs = twitterManager.findLastStatusLogs(20);
		List<TwitterStatusLog> lastTweets = new ArrayList<TwitterStatusLog>();
		Map<Integer, String> urlMap = new HashMap<Integer, String>();
		for (TwitterStatusLog statusLog : statusLogs) {
			if (statusLog.getStatus() == TwitterStatusLog.STATUS_OK) {
				lastTweets.add(statusLog);
				urlMap.put(statusLog.getId(), twitterManager.generateShortLink(statusLog.getId()));
			}
			if (lastTweets.size() == 10) {
				break;
			}
		}
		
    	model.put("session", session);
    	model.put("lastTweets", lastTweets);
    	model.put("urlMap", urlMap);
		model.put("escape", new EscapeTool());
    	model.put("pageTitle", "Share RSS on Twitter");
    	model.put("page", "welcome");
        return "welcome";
    }	
 
	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String error(ModelMap model) {
    	model.put("session", session);
    	model.put("pageTitle", "Error");
    	model.put("page", "error");    	    	
		return "error";
	}    

    @RequestMapping("/help")
    public String help(ModelMap model) {
    	model.put("session", session);
    	model.put("pageTitle", "How to share RSS on Twitter");
    	model.put("page", "help");    	
        return "help";
    }	
	
}
