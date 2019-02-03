package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.util.Hash;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

	@RequestMapping("/name")
	public Map showName() {
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();//得到当前登录名
		
		Map map = new HashMap<>();
		map.put("loginName", name);
		return map;
	}
}
