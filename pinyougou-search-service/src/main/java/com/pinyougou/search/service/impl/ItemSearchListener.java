package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemSearchListener implements MessageListener {

	/**
	 * 消息消费者，使用同一个节点队列，远程调用这个服务，先存到activeMQ中，但未被消费，search等同于本地服务，未被远程调用
	 * 但当本地服务一旦开启，自动寻找对应的生产消费者消息
	 * @param message
	 */
	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage=(TextMessage)message;
			String text = textMessage.getText();//json字符串
			System.out.println("监听到消息:"+text);
			
			List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
			itemSearchService.importList(itemList);
			System.out.println("导入到solr索引库");
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
