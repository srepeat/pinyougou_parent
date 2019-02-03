package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取用户名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//到缓存中查询日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(username);
		if(payLog != null) {
			System.out.println(weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+""));
			return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
		}else {
			
			return new HashMap<>();
		}
	}
	
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		
		int x = 0;
		while(true) {
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			//出错
			if(map==null) {
				result = new Result(false, "支付出现异常");
				break;
			}
			
			//判断支付状态
			if(map.get("trade_state").equals("SUCCESS")){
				result = new Result(true, "支付成功");
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				
				break;
			}
			//休眠三秒再循环
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//设置五分钟为超时时间  3000*1分钟  每次发送20次请求 
			x++;
			if(x>=100) {
				result = new Result(false, "二维码超时");
				break;
			}
		}
		
		return result;
	}

}
