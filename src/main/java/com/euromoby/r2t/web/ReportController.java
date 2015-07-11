package com.euromoby.r2t.web;

import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.utils.DateUtils;

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
		model.put("actions", twitterManager.findLastOkStatusLogsByScreenName(session.getScreenName(), 50));
		model.put("escape", new EscapeTool());
		model.put("date", new DateUtils());
    	model.put("session", session);
    	model.put("pageTitle", "Report - Last tweets");
    	model.put("page", "report");     	
		return "report";
	}

}
