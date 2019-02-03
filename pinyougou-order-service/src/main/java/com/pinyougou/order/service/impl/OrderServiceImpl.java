package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mysql.fabric.xmlrpc.base.Data;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	
	@Autowired
	private RedisTemplate  redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		
		//1、从redis中获取数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		
		//订单集合
		List<String> orderIdList = new ArrayList<>();
		double total_money=0.00;
		
		//2、遍历购物车明细列表
		for(Cart cart : cartList) {
			TbOrder tborder = new TbOrder();//创建订单对象
			long orderId = idWorker.nextId();//生成ID
			tborder.setOrderId(orderId);//订单ID
			tborder.setUserId(order.getUserId());//用户名
			tborder.setPaymentType(order.getPaymentType());//支付类型
			tborder.setStatus("1");//状态：未支付
			tborder.setCreateTime(new Date());//创建订单时间
			tborder.setUpdateTime(new Date());//更新订单时间
			tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
			tborder.setReceiverMobile(order.getReceiverMobile());//手机号
			tborder.setReceiver(order.getReceiver());//收货人
			tborder.setSourceType(order.getSourceType());//订单来源
			tborder.setSellerId(cart.getSellerId());//商家ID	

			double money = 0;
			
			//循环购物每条明细
			for(TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);//订单ID
				orderItem.setSellerId(cart.getSellerId());//商家id
				money+= orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			tborder.setPayment(new BigDecimal(money));
			orderMapper.insert(tborder);
			
			orderIdList.add(orderId+"");//转换为分
			total_money+=money;//总金额
		}
		
		//保存支付日志
		if("1".equals(order.getPaymentType())) {
			//创建日志对象
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId()+"");//订单号
			payLog.setCreateTime(new Date());//创建时间
			//订单列表,逗号分隔，使用replace替换
			String ids = orderIdList.toString().replace("[", "").replace("]", "");
			payLog.setOrderList(ids);
			//支付类型
			payLog.setPayType("1");
			//支付金额
			payLog.setTotalFee((long)total_money*100 );
			//支付状态
			payLog.setTradeState("0");
			//用户ID
			payLog.setUserId(order.getUserId());
			//插入日志记录表中
			payLogMapper.insert(payLog);
			//存入缓存
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
		}
		
		
		//3、删除redis中的购物车数据
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {
			// TODO Auto-generated method stub
			return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		}

		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			// TODO Auto-generated method stub
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			payLog.setPayTime(new Date());//支付时间
			payLog.setTradeState("1");//已支付
			payLog.setTransactionId(transaction_id);//支付流水号
			//更改
			payLogMapper.updateByPrimaryKey(payLog);
			
			//修改订单状态
			String orderList = payLog.getOrderList();
			String[] ids = orderList.split(",");
			
			for(String orderId: ids) {
				TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
				if(order!=null) {
					order.setStatus("2");//已支付
					orderMapper.updateByPrimaryKey(order);
				}
			}
			
			//清楚缓存
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		}
	
}
