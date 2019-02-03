package com.pinyougou.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

public class UserDetailsServiceImpl implements UserDetailsService {

//		引入service接口
	private SellerService sellerService;
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		创建角色列表
		List<GrantedAuthority> grantedAuths = new ArrayList<>();
		// TODO Auto-generated method stub
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
//		判断用户名和密码 username=id
		TbSeller seller = sellerService.findOne(username);
			if(seller != null) {
//				判断状态审核通过
				if(seller.getStatus().equals("1")) {
					return new User(username, seller.getPassword(), grantedAuths );
				}else {
					return null;
				}
			}else {
				return null;
			}
	}

}
