package com.euromoby.r2t.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelpController {
	
	@Autowired
	private Session session;
	
    @RequestMapping("/help")
    public String welcome(ModelMap model) {
    	model.put("session", session);
    	model.put("pageTitle", "How to share RSS on Twitter");
    	model.put("page", "help");    	
        return "help";
    }	
    
}
