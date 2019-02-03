package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference
	private CartService cartService;
	
	@Autowired
	private  HttpServletRequest request;
	
	@Autowired
	private  HttpServletResponse response;

	/**
	 * 查询购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//获取登录名称
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录得用户为:"+username);
		
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		//判断是否为空
		if(cartListString==null || cartListString.equals("")) {
			cartListString="[]";
		}
		//
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		//判断用户是否登录
		if(username.equals("anonymousUser")) {
			
			return cartList_cookie;
		}else {
			//redis取数据
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			//如果本地存在购物车
			if(cartList_cookie.size()>0) {
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//清楚本地购物车
				util.CookieUtil.deleteCookie(request, response, "cartList");
				//再次存购物车到redis
				cartService.saveCartListToRedis(username, cartList);
			}
			return cartList_redis;
		}
	}
	
	/**
	 * 添加商品到购物车
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	//@CrossOrigin(origins="http://localhost:9105")//跨域请求注解，cookie默认缺省为true
	public Result addGoodsToCartList(Long itemId,Integer num) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录得用户为:"+username);
		
		
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//跨域解决(此方法不针对cookie)
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		
		//1、获取购物车列表
		try {
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if(username.equals("anonymousUser")) {
				String cookieValue = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cookieValue,3600*24 ,"UTF-8");
				System.out.println("向cookie存入数据");
			}else {
				cartService.saveCartListToRedis(username, cartList);
				System.out.println("向redis存入数据");
			}
			
			return new Result(true, "添加购物车列表成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "添加购物车列表失败");
		}
		
	}
}
