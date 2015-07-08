package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {
	
	@Autowired
	private Session session;
	
    @RequestMapping("/")
    public String welcome(ModelMap model) {
    	
		if (!session.isNotAuthenticated()) {
			return "redirect:/profile";
		}    	
    	
    	model.put("session", session);
    	model.put("pageTitle", "RSS to Twitter");
    	model.put("page", "welcome");
        return "welcome";
    }	
    
}
