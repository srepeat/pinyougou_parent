package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class PageDeleteListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;
	/**
	 * 同步删除SKU表中的数据
	 * 使用对象监听器转换Item这个中的ID
	 * 
	 */
	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long[]  goodsIds = (Long[]) objectMessage.getObject();
			System.out.println("内容为:"+goodsIds);
			boolean b = itemPageService.deleteItemHtml(goodsIds);
			System.out.println("删除成功:"+b);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
