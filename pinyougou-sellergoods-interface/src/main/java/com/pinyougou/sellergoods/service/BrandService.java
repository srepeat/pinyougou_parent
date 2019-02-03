package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌管理
 * @author 鲜磊
 *
 */
public interface BrandService {

	
//	查询所有品牌
	public List<TbBrand> findAll();
	
	
	/**
	 * 分页接口
	 * @param pageNum 记录总页数
	 * @param pageSize 当前记录数
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 增加信息
	 * @param brand
	 */
	public void add(TbBrand brand);
	
	/**
	 * id查询信息
	 * @return
	 */
	public TbBrand findOne(long id);
	
	/**
	 * 修改信息
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	
	/**
	 * 存多个ids
	 * @param ids
	 */
	public void delete(long [] ids);
	
	
	/**
	 * 分页接口
	 * @param pageNum 记录总页数
	 * @param pageSize 当前记录数
	 * @return
	 */
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	/**
	 * 查询品牌全部信息
	 * @return
	 */
	public List<Map> selectOptionList();
	
	
}
