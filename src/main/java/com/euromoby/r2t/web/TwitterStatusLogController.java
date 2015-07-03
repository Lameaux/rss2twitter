package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.euromoby.r2t.core.twitter.TwitterManager;

@Controller
public class TwitterStatusLogController {

	@Autowired
	private TwitterManager twitterManager;

	@Autowired
	private Session session;

	@RequestMapping("/log")
	public String statusLog(ModelMap model) {

		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}

		return "status_log";
	}

}
