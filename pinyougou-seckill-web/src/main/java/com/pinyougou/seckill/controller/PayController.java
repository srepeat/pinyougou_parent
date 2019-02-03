package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取用户名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//到缓存中查询日志
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
		if(seckillOrder != null) {
			long fen = (long)(seckillOrder.getMoney().doubleValue()*100);
			return weixinPayService.createNative(seckillOrder.getId()+"",+fen +"");
		}else {
			
			return new HashMap<>();
		}
	}
	
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		//获取用户名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
				seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no) , map.get("transaction_id"));
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
				Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
				if( !"SUCCESS".equals(payResult.get("result_code")) ){//如果返回结果是正常关闭
					if("ORDERPAID".equals(payResult.get("err_code"))){
						result=new Result(true, "支付成功");	
						seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
					}					
				}		
				
				//调用删除缓存方法
				if(result.isSuccess() == false) {
					seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
				}
				break;
			}
		}
		
		return result;
	}

}
