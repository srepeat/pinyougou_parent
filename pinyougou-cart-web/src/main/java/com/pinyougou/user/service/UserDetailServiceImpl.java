package com.pinyougou.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("经过了认证类："+username);
		
		List<GrantedAuthority> authorities = new ArrayList<>();
		//用户角色
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		//返回用户名和这个角色信息
		return new User(username, "", authorities);
	}

}
