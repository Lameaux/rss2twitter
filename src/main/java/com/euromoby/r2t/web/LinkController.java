package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterStatusLog;
import com.euromoby.r2t.web.exception.ResourceNotFoundException;

@Controller
public class LinkController {

	@Autowired
	private TwitterManager twitterManager;
	
    @RequestMapping("/{id:0[a-z0-9]+}")
    public ModelAndView link(@PathVariable("id") String id) {
    	
    	TwitterStatusLog twitterStatusLog = null;
    	
    	try {
    		int statusId = Integer.parseInt(id.substring(1), Character.MAX_RADIX);   
    		twitterStatusLog = twitterManager.getStatusLogById(statusId);
    	} catch (Exception e) {
    		// ignore
    	}
    	
    	if (twitterStatusLog == null) {
    		throw new ResourceNotFoundException();
    	}
    	
    	RedirectView rv = new RedirectView(twitterStatusLog.getUrl());
		rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		rv.setUrl(twitterStatusLog.getUrl());
		ModelAndView mv = new ModelAndView(rv);
		return mv;    	
    }	
    
}
