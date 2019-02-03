package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		@Autowired
		private RedisTemplate redisTemplate;
		
		@Autowired
		private TbSeckillGoodsMapper seckillGoodsMapper;
		
		@Autowired
		private IdWorker idWorker;

		@Override
		public void submitOrder(Long seckillId, String userId) {
			//1、从缓存中提取数据
			TbSeckillGoods seckillGoods =  (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);//商品ID
			
			//如果等于空的情况
			if(seckillGoods == null) {
				
				throw new RuntimeException("商品不存在!");
			}
			
			//如果数量小于0
			if(seckillGoods.getStockCount() <= 0 ) {
				throw new RuntimeException("商品被抢光!");
			}
			
			//2、存储减少
			
			seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
			//再把新的存库存取缓存
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			
			//如果被抢购完
			if(seckillGoods.getStockCount() == 0) {
				//同步到数据库
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//清楚缓存
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
				System.out.println("存入数据库（redis）");
			}
			
			
			//3、保存order订单
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(idWorker.nextId());//订单号
			seckillOrder.setCreateTime(new Date());//创建时间
			seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
			seckillOrder.setSeckillId(seckillId);
			seckillOrder.setSellerId(seckillGoods.getSellerId());//商家ID
			seckillOrder.setUserId(userId);//设置用户ID
			seckillOrder.setStatus("0");//状态
			
			//存入缓存
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
			System.out.println("从缓存读取数据");
			
		}

		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			// TODO Auto-generated method stub
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		}

		@Override
		public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
			//1、根据用户id查询
			TbSeckillOrder seckillOrder =(TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			
			if(seckillOrder == null){
				throw new RuntimeException("订单不存在!");
				
			}
			//如果传递单号不一致
			if(seckillOrder.getId().longValue() != orderId.longValue()) {
				
				throw new RuntimeException("订单不相符!");
			}
			
			//2、封装数据
			seckillOrder.setCreateTime(new Date());//下单时间
			seckillOrder.setTransactionId(transactionId);//流水号
			seckillOrder.setStatus("1");//状态
			
			//3、存入数据库
			seckillOrderMapper.insert(seckillOrder);//保存到数据库
			
			//4、删除缓存
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			
		}

		@Override
		public void deleteOrderFromRedis(String userId, Long orderId) {
			// TODO Auto-generated method stub
			//1、查询用户id
			TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
			if(seckillOrder != null) {
				//2、删除缓存中的订单
				redisTemplate.boundHashOps("seckillOrder").delete(userId);
				//3、回退库存
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				if(seckillGoods != null) {
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);	
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);//存入缓存
				}else {
					seckillGoods = new TbSeckillGoods();
					seckillGoods.setId(seckillOrder.getSeckillId());
					seckillGoods.setStockCount(1);//数量
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
				}
				System.out.println("订单取消："+orderId);
			}
		}
	
}
