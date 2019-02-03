package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

//	注入dao
//	本地调用
	@Autowired
	private TbBrandMapper brandmapper;
	
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return brandmapper.selectByExample(null);
	}

//	分页
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//分页
		
		Page<TbBrand> page = (Page<TbBrand>) brandmapper.selectByExample(null);
		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		
		brandmapper.insert(brand);
	}

	@Override
	public TbBrand findOne(long id) {
		// TODO Auto-generated method stub
		return brandmapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand brand) {
		// TODO Auto-generated method stub
		brandmapper.updateByPrimaryKey(brand);
	}

	@Override
	public void delete(long[] ids) {
		// TODO Auto-generated method stub
		for(long id : ids) {
			brandmapper.deleteByPrimaryKey(id);
		}
		
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
//		分页插件
		PageHelper.startPage(pageNum, pageSize);
		
		TbBrandExample example = new TbBrandExample();
		
		Criteria create = example.createCriteria();
//		进行模糊查询
	if(brand != null) {
		if(brand.getName() !=null && brand.getName().length()>0) {
			create.andFirstCharLike("%"+brand.getName()+"%");
		}
		
		if(brand.getFirstChar() !=null && brand.getFirstChar().length()>0) {
			create.andFirstCharLike("%"+brand.getFirstChar()+"%");
		}
	}	
		
			Page<TbBrand> page = (Page<TbBrand>) brandmapper.selectByExample(example );
		
//		返回查询结果集
			return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		// TODO Auto-generated method stub
		return brandmapper.selectOptionList();
	}

}
