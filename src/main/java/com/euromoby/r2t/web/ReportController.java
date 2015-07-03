package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.euromoby.r2t.core.twitter.TwitterManager;

@Controller
public class ReportController {

	@Autowired
	private TwitterManager twitterManager;

	@Autowired
	private Session session;

	@RequestMapping("/report")
	public String report(ModelMap model) {

		if (session.isNotAuthenticated()) {
			return "redirect:/";
		}

		return "report";
	}

}
