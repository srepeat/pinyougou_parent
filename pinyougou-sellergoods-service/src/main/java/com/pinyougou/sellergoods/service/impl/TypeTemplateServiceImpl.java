package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);	
//		存入缓存
		saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		
		
		@Autowired 
		private RedisTemplate redisTemplate;
		
		private void saveToRedis() {
			
			List<TbTypeTemplate> typeTemplateList = findAll();
			
			for (TbTypeTemplate typeTemplate  : typeTemplateList) {
//				存储品牌列表
				List<Map> brandList = com.alibaba.fastjson.JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
				redisTemplate.boundHashOps("brandList").put(typeTemplate .getId(),brandList );
//				存储规格列表
//				根据模板id查询
				List<Map> specList = findSpecList(typeTemplate.getId());
				redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
			}
			System.out.println("缓存品牌列表");
			
		}
		

		/**
		 * 商品规格
		 */
		@Override
		public List<Map> findSpecList(Long id) {
			// TODO Auto-generated method stub
			
			TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
			
			List<Map> list = com.alibaba.fastjson.JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
//			遍历取参、查询规格选项列表
			
			for (Map map : list) {
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				com.pinyougou.pojo.TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
//				转换类型
				criteria.andSpecIdEqualTo(new Long((Integer)map.get("id")));
				List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example );
//				存入map集合
				map.put("options", options);
			}
			
			return list;
		}
	
}
