package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}
			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}
			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}
			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}
			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}
	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		
		@Autowired
		private RedisTemplate redisTemplate;
		
		@Override
		public List<TbSeckillGoods> findList() {
			//1、放入缓存
			List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();//values表示一个集合
			
			//判断缓存是否存在数据以及看数据的长度是否小于0
			if(seckillGoodsList == null || seckillGoodsList.size() == 0) {
				//2、读取数据库
				TbSeckillGoodsExample example=new TbSeckillGoodsExample();
				Criteria criteria = example.createCriteria();
				criteria.andStatusEqualTo("1");//审核通过
				criteria.andStockCountGreaterThan(0);//剩余库存大于0
				criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
				/**
				 * 异常代码截至时间未查询出来
				 */
				criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
				System.out.println(criteria.andEndTimeGreaterThan(new Date()));
				//System.out.println(seckillGoodsMapper.selectByExample(example));
				seckillGoodsList = seckillGoodsMapper.selectByExample(example);
				//遍历商品存入缓存
				for (TbSeckillGoods seckillGoods : seckillGoodsList) {
					redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
				}
				System.out.println("从数据库拿取数据");
				
			}else {
					System.out.println("从缓存读取数据");
				}
			
			return seckillGoodsList;
		
		}

		@Override
		public TbSeckillGoods findOneFromRedis(Long id) {
			// TODO Auto-generated method stub
			return (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(id);
		}
}
