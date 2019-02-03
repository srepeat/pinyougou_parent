package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	private TbItemMapper  itemMapper;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1、根据SKU查询商品明细SKU对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		//预防空指针异常
		if(item == null) {
			throw new RuntimeException("商品不存在");
		}
		
		//判断商品是否审核通过
		if(!item.getStatus().equals("1")) {
			
			throw new RuntimeException("商品不合法");
		}
		
		//2、根据SKU查询商家ID
		String sellerId = item.getSellerId();
		
		//3、根据商家ID在购物车列表查询购物车对象
		Cart cart = searchCartBySellerId(cartList, sellerId);
		//4、如果购物车不存在该购物车
		if(cart==null) {
			//4.1、如果没有就创建一个新的购物车对象
			cart = new Cart();
			//封装item属性到购物车列表
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			List<TbOrderItem> orderItemList = new ArrayList<>();
			//明细列表对象
			TbOrderItem orderItem = createOrderItem(item, num);
			//添加到明细列表
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList );
			
			//4.2、将新的购物车对象添加到这个购物车列表
			cartList.add(cart);
		}else {
			//5、如果购物车中存在这个该商家的购物列表
			//判断该购车的明细列表是否存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			if(orderItem==null) {
				//5.1、如果不存在，就创建一个新的明细购物对象，添加到这个购物车列表中
				orderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(orderItem);
			}else {
				//5.2、如果存在，就将添加的购物对象的数量以及更新金额
				orderItem.setNum(orderItem.getNum()+num);
				//金额  数量和单价进行计算
				orderItem.setTotalFee( new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()) );
				//如果数量小于等于0，移除
				if(orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				
				//如果移购物车为空，也就明细数量为0时，把购物列表移除
				if(cart.getOrderItemList().size()==0) {
					cartList.remove(cart);
				}
			}
		}
		
		return cartList;
	}
	
	/**
	 * 根据商家ID查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for(Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
			
		}
		return null;
		
	}
	
	/**
	 * 根据商品查询明细ID
	 * @param orderItemList 
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long  itemId) {
		for(TbOrderItem orderItem : orderItemList ) {
			if(orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
		
	}
	
	/**
	 * 创建明细对象
	 * @param item
	 * @param num
	 * @return
	 */

	private TbOrderItem createOrderItem(TbItem item , Integer num) {
		//数量小于等于0时，报数量异常
		if(num<=0) {
			throw new RuntimeException("数量非法");
		}
		
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee( new BigDecimal(item.getPrice().doubleValue()*num) );
		
		return orderItem;
	}

	
	@Autowired
	private RedisTemplate  redisTemplate;
	
	//取
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("向redis获取购物车数据....."+username);
		List<Cart>  cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		//等于空就创建一个新的集合
		if(cartList==null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	//存
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis存入购物车数据....."+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
	}
	/**
	 * 合并购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		
		//遍历一个购物车
		for(Cart cart :cartList2 ) {
			for(TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList1 =addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		
		return cartList1;
	}
}
