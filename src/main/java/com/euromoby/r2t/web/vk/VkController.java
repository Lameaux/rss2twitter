package com.euromoby.r2t.web.vk;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.euromoby.r2t.core.Config;
import com.euromoby.r2t.core.utils.StringUtils;
import com.euromoby.r2t.core.vk.VkManager;
import com.euromoby.r2t.core.vk.VkProvider;
import com.euromoby.r2t.core.vk.model.VkAccount;
import com.euromoby.r2t.web.Session;

@Controller
public class VkController {

	@Autowired
	private Session session;

	@Autowired
	private Config config;

	@Autowired
	private VkManager vkManager;
	
	@Autowired
	private VkProvider vkProvider;

	private static final Logger log = LoggerFactory.getLogger(VkController.class);

	@RequestMapping(value = "/vk/connect", method = RequestMethod.GET)
	public String connectVkAccount(ModelMap model) {
		try {
			String permissions = "offline,groups,wall";
			return "redirect:" + vkProvider.getAuthorizationUrl(permissions);
		} catch (Exception e) {
			log.error("Error getting auth url", e);
			return "redirect:/error";
		}
	}

	@RequestMapping(value = "/vk", method = RequestMethod.GET)
	public String authVk(ModelMap model) {
		model.put("session", session);
		model.put("pageTitle", "VK Auth");
		model.put("page", "vk");
		return "vk";
	}

	@RequestMapping(value = "/vk/oauth", method = RequestMethod.GET)
	public String oAuthVkAccount(ModelMap model, @RequestParam(value = "access_token", required = false) String accessToken,
			@RequestParam(value = "expires_in", required = false) String expiresIn, @RequestParam(value = "user_id", required = false) String userId) {

		if (StringUtils.nullOrEmpty(accessToken) || StringUtils.nullOrEmpty(userId)) {
			return "redirect:/error";
		}

		VkAccount vkAccount = new VkAccount();
		vkAccount.setUserId(userId);
		vkAccount.setAccessToken(accessToken);
		vkManager.saveOrUpdateVkAccount(vkAccount);
		session.setVkUserId(userId);
		return "redirect:/vk/profile";
	}

	@RequestMapping(value = "/vk/profile", method = RequestMethod.GET)
	public String profile(ModelMap model) {
		if (session.isVkNotAuthenticated()) {
			return "redirect:/";
		}
		VkAccount vkAccount = vkManager.getAccountByUserId(session.getVkUserId());
		if (vkAccount == null) {
			log.debug("account {} not found", session.getVkUserId());
			return "redirect:/error";
		}

		model.put("vk", vkAccount);

    	model.put("session", session);
    	model.put("pageTitle", "VK " + session.getVkUserId());
    	model.put("page", "vk_profile");    	
		return "vk_profile";
	}	

	@RequestMapping(value = "/vk/group/{id}/post/{message}", method = RequestMethod.GET)
	public String group(ModelMap model, @PathVariable("id") String id, @PathVariable("message") String message) {
		if (session.isVkNotAuthenticated()) {
			return "redirect:/";
		}
		VkAccount vkAccount = vkManager.getAccountByUserId(session.getVkUserId());
		if (vkAccount == null) {
			log.debug("account {} not found", session.getVkUserId());
			return "redirect:/error";
		}
		
		try {
			String response = vkProvider.postToWall(vkAccount, id, message);
			model.put("response", response);
		} catch (IOException e) {
			log.debug("Error getting groups", e);
			return "redirect:/error";			
		}
		model.put("vk", vkAccount);

    	model.put("session", session);
    	model.put("pageTitle", "VK " + session.getVkUserId());
    	model.put("page", "vk_group");    	
		return "vk_group";
	}	
	
	
}
