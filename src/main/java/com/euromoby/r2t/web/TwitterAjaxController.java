package com.euromoby.r2t.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.euromoby.r2t.core.twitter.TwitterManager;
import com.euromoby.r2t.core.twitter.model.TwitterRssFeed;

@Controller
public class TwitterAjaxController {

	@Autowired
	private TwitterManager twitterManager;

	@RequestMapping(value = "/twitter/feeds", method = RequestMethod.GET)
	public @ResponseBody
	List<TwitterRssFeed> feeds(ModelMap model) {
		return new ArrayList<TwitterRssFeed>();
	}

}
