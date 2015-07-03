package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.euromoby.r2t.core.twitter.TwitterManager;

@Controller
public class TwitterRssFeedController {

	@Autowired
	private TwitterManager twitterManager;
    
    @RequestMapping("/twitter/feeds")
    public String manageGroups(ModelMap model) {
    	return "twitter_groups";
    }    
    
}
