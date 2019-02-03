package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * cron 表达式
	 * 全部为*号代表都显示通配(顺序为秒、分、小时、天数、月份、星期)
	 */
	@Scheduled(cron="0/10 * * * * ?")
	public void refreshSeckillGoods() {
		
		System.out.println("执行了任务调度"+new Date());
		
		//查询缓存中的数据
		List goodIdKey = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		System.out.println(goodIdKey);
		//1、读取数据库
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过
		criteria.andStockCountGreaterThan(0);//剩余库存大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
		criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
		
		if(goodIdKey.size()>0) {
			//排除已有商品
			criteria.andIdNotIn(goodIdKey);
		}
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		//遍历商品存入缓存
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("将增量商品+"+seckillGoods.getId()+"装入缓存");
		}
		System.out.println(".....end.....");
	}
	
	
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods(){
		System.out.println("执行了清楚任务调度"+new Date());
		//去缓存数据
		List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
		//遍历
		for(TbSeckillGoods seckill : seckillGoods) {
			//将时间转换为long类型，秒杀时间小于当前时间表示过期
			if(seckill.getEndTime().getTime() < new Date().getTime()) {
				//更新数据库
				seckillGoodsMapper.updateByPrimaryKey(seckill);
				//清除缓存
				redisTemplate.delete("移除秒杀商品："+seckill.getId());//商品ID
			}
			System.out.println("移除秒杀商品任务结束");	
		}
	}

}
