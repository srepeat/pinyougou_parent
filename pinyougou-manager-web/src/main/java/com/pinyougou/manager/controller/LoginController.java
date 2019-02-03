package com.pinyougou.manager.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

//	显示用户登录名称
	@RequestMapping("/name")
	public Map name() {
		String name = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
		Map map = new HashMap<>();
		map.put("loginName", name);
		return map;
	}
}
