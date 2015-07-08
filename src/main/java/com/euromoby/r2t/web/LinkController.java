package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;
import com.euromoby.r2t.web.exception.ResourceNotFoundException;

@Controller
public class LinkController {

	private static final String URL_ID_BASE = "100000";	
	
	@Autowired
	private TwitterManager twitterManager;
	
    @RequestMapping("/{id:[0-9]+[a-z0-9]+}")
    public String link(@PathVariable("id") String id, ModelMap model) {
    	int statusId = Integer.parseInt(id, Character.MAX_RADIX) - Integer.parseInt(URL_ID_BASE, Character.MAX_RADIX);   
    	TwitterStatusLog twitterStatusLog = twitterManager.getStatusLogById(statusId);
    	if (twitterStatusLog == null) {
    		throw new ResourceNotFoundException();
    	}
        return "redirect:" + twitterStatusLog.getUrl();
    }	
    
}
