package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map search(Map searchMap) {
		Map map=new HashMap();		
		
//		1、关键字搜索空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格
		
//		1、查询列表
		map.putAll(searchList(searchMap));
		
//		2、分组查询
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
//      3.查询品牌和规格列表
		String category= (String) searchMap.get("category");
		if(!category.equals("")){						
			map.putAll(searchBrandAndSpecList(category));
		}else{
			if(categoryList.size()>0){			
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}	
		}

		return map;
		
	}
	
//	查询全部高亮部分
	private Map searchList(Map searchMap) {
		Map map=new HashMap();
//		高亮初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
		highlightOptions.setSimplePostfix("</em>");//设置高亮后缀
		query.setHighlightOptions(highlightOptions );//设置两个高亮选项
		
//		1.1、关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//		1.2、分类查询
		if(!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new  SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
//		1.3、品牌过滤
		if(!"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new  SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null){			
			Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key :specMap.keySet()){
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);					
			}		
			
		}
		
//		1.6、价格筛选
		if(!"".equals(searchMap.get("price"))) {
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")) {//如果最低价格不等于0
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery );
			}
			System.out.println("price执行了。。。。。。");
			if(!price[1].equals("*")) {//如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery );
			}
		}
		
//		1.7价格升降排序
		String sortValue = (String) searchMap.get("sort");//ASC DESC
		String sortField = (String) searchMap.get("sortField");//排序字段
//		如果是升序排序
		if(sortValue!=null && !sortValue.equals("")) {
			if(sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
//		降序排序
			if(sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
//		分页
		Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
		if(pageNo==null) {
			pageNo=1;//默认赋值为1
		}
		
		Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
		if(pageSize==null) {
			pageSize=20;//默认20
		}
		
		query.setOffset((pageNo-1)*pageSize);//从第几条开始查询
		query.setRows(pageSize);
		
		
		//*****************获取结果集*************
//		高亮对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
//		高亮入口集合
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
		for (HighlightEntry<TbItem> h : highlighted) {//获取高亮入口
			TbItem item = h.getEntity();//获取原实体类
//			判断是否有数据
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0) {
//				设置高亮结果
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
	/*	map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
*/		
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数

		return map;
	}
	
	
	/**
	 * 查询结果集
	 */
	
	private List<String> searchCategoryList(Map searchMap){
		List<String> list = new ArrayList<>();
		
		Query query = new SimpleQuery("*:*");
		
//		关键字查询
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
//		查询分组列
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//条件域
		query.setGroupOptions(groupOptions);
		
//		得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
//		获取分组结果集
		GroupResult<TbItem> result = page.getGroupResult("item_category");
//		获取入口分页
		Page<GroupEntry<TbItem>> groupEntries = result.getGroupEntries();
//		获取分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		
		for (GroupEntry<TbItem> group : content) {
			list.add(group.getGroupValue());//获取分组结果集
		}
//		返回一个集合
		return list;
		
	}
	
	/**
	 * 查询品牌和规格列表
	 */
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map=new HashMap();
		//1.根据商品分类名称得到模板ID		
		Long templateId= (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId!=null){
			//2.根据模板ID获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);	
			//System.out.println("品牌列表条数："+brandList.size());
			
			//3.根据模板ID获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);		
			//System.out.println("规格列表条数："+specList.size());
		}	
		
		return map;
	}

	/**
	 * 导入列表数据方法
	 */
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);	
		solrTemplate.commit();

	}
	/**
	 * 删除方法
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}

	

}
