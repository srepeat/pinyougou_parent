package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;
@Service
public class ItemPageServiceImpl implements ItemPageService {

	/**
	 * 注入free mark属性
	 * 文件路径
	 * 数据模型访问层
	 */
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		try {

			Configuration configuration = freeMarkerConfig.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");
			
			//商品模型
			Map dataModel = new HashMap<>();
			//查询全部商品主表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			
			//加载扩展表属性
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			
			//读取商品分类 1级分类
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
//			System.out.println("12333333");
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			
			//读取SKU扩展数据
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);//指定查询id
			criteria.andStatusEqualTo("1");//状态审核通过的为：1
			example.setOrderByClause("is_default desc");//查询结果排序
			List<TbItem> itemList = itemMapper.selectByExample(example );
			dataModel.put("itemList", itemList);//存入数据模型
			
			
			Writer out = new FileWriter(pagedir+goodsId+".html");
			template.process(dataModel, out);
			
			//关流
			out.close();
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

//	删除页面方法
	public boolean deleteItemHtml(Long[] goodsIds) {
		try {
			for(Long goodsId:goodsIds){
				new File(pagedir+goodsId+".html").delete();		
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}

}
