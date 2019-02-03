package com.pinyougou.sellergoods.service.impl;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper  goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	
	@Override
	public void add(Goods goods) {
		
		goods.getGoods().setAuditStatus("0");//状态：未审核
		goodsMapper.insert(goods.getGoods());//插入商品基本信息
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的ID给商品扩展表
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展表数据
		
//		保存
		saveItemList(goods);
	}
	
	
	private void setItemValus(TbItem item , Goods goods) {
		
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期 
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());		
		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());		
		//图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage ( (String)imageList.get(0).get("url"));
		}		
		
	}

	private void saveItemList(Goods goods) {
//		是否启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
		for(TbItem item : goods.getItemList()) {
//			标题
			String title = goods.getGoods().getGoodsName();
			Map<String, Object> specMap = JSON.parseObject(item.getSpec());
			for(String key:specMap.keySet()) {
				title = " "+specMap.get(key);
			}
			item.setTitle(title);		
			
			setItemValus(item, goods);
			itemMapper.insert(item);
		  }		
		}else {
			
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品名称
			item.setStatus("1");//状态
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setIsDefault("1");//默认状态
			item.setNum(9999);//库存
			item.setSpec("{}");
			setItemValus(item, goods);
			
			itemMapper.insert(item);
			
			
		}
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
//		更新
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria createCriteria = example.createCriteria();
		createCriteria.andGoodsIdEqualTo(goods.getGoods().getId());
		//		删除SKU表
		itemMapper.selectByExample(example);
		
//		增加
		saveItemList(goods);
		
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
//		创建goods对象
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria createCriteria = example.createCriteria();
		createCriteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example );
		goods.setItemList(itemList);
		
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
//			逻辑删除并非真正意义上的删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
//		非物理删除
		criteria.andIsDeleteIsNull();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		/**
		 * 状态
		 */
		@Override
		public void updateStuts(Long[] ids, String status) {
			for (Long id : ids) {
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				goods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(goods);
			}
			
		}

		@Override
		public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
			
			TbItemExample example=new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo(status);//状态
			criteria.andGoodsIdIn( Arrays.asList(goodsIds));//指定条件：SPUID集合
			
			return itemMapper.selectByExample(example);
		}
	
}
