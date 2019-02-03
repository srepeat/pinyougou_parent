package com.pinyougou.manager.controller;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@Autowired 
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDeleteDestination;
	
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//删除
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			//删除索引库
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				 
				@Override
				public Message createMessage(Session session) throws JMSException {
					// TODO Auto-generated method stub
					return session.createObjectMessage(ids);
				}
			});
			//删除商品详情
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					// TODO Auto-generated method stub
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
//	@Reference(timeout=1000)
//	private ItemSearchService itemSearchService;
	
//	JMS模板
	@Autowired
	private JmsTemplate jmsTemplate;
//	配置文件中的Bean和Destination配置的id对应，最好注入进来，点对点方式
	@Autowired 
	private Destination queueSolrDestination;//导入索引库
//	配置文件中的Bean和Destination配置的id对应，最好注入进来，订阅主题方式
	@Autowired
	private Destination topicPageDestination;//发布订阅方式
	
	
	@RequestMapping("/updateStauts")
	public Result updateStauts(Long[] ids,String status) {
		try {
			goodsService.updateStuts(ids, status);
			//按照SPU ID查询 SKU列表(状态为1)		
			if(status.equals("1")){//审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);							
			//调用搜索接口实现数据批量导入
				final String jsonString = JSON.toJSONString(itemList);//转换为json传输
				
				jmsTemplate.send(queueSolrDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						
						return session.createTextMessage(jsonString);
					}
				});			
				
			//静态页生成
			for(final Long goodsId:ids){
//				itemPageService.genItemHtml(goodsId);
				jmsTemplate.send(topicPageDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						// TODO Auto-generated method stub
						return session.createTextMessage(goodsId+"");
					}
				});
				
				
			}				

		}
			return new Result(true, "修改状态成功");
		}catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "修改状态失败");
		}
		
	}
	
//	@Reference(timeout=40000)
//	private ItemPageService itemPageService;
	/**
	 * 生成测试静态页面
	 * @param goodsId
	 */
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		
//		itemPageService.genItemHtml(goodsId);
		
	}
	
}
