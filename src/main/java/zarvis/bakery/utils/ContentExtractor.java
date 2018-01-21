package zarvis.bakery.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zarvis.bakery.utils.Util;

public class ContentExtractor {
	private Map<String,Integer> productlist = new HashMap<>();
	
	private int deliveryHour;
	private int deliveryDay;
	private int deliveryTime;
	private int priority;
	
	private String [] amounts;
	private String [] content;
	private String [] split;
	
	private String guid;
	private String customer;
	private String productString;
	private String deliveryDateString;
	private String orginalMessage;
	
	public ContentExtractor(String message) {
		orginalMessage = message;
		content = message.split(",");
		guid = content[0];
		customer = content[1];
		setDeliveryDate(content[3]);
		deliveryDateString = content[3];
		setProducts(content[4]);
		productString = content[4];
	}
	
	private void setDeliveryDate(String date){
		split = date.split("\\.");
		deliveryDay =Integer.parseInt(split[0]);
		deliveryHour = Integer.parseInt(split[1]);
		deliveryTime = 24*deliveryDay+deliveryHour;
	}
	
	private void setProducts(String a) {
		amounts = a.split("\\.");
		for (int i = 0; i < amounts.length; i++) {
			productlist.put(Util.PRODUCTNAMES.get(i), Integer.parseInt(amounts[i]));
		}
	}
	
	public Map<String,Integer> getProducts(){
		return productlist;
	}
	
	public String getCustomer(){
		return customer;
	}
	
	public int getPriority(){
		return priority;
	}
	
	public int getDeliveryHour(){
		return deliveryHour;
	}
	
	public int getDeliveryDay(){
		return deliveryDay;
	}
	
	public String getProductString(){
		return productString;
	}
	
	public int getDeliveryTime(){
		return deliveryTime;
	}
	
	public String getDeliveryDateString(){
		return deliveryDateString;
	}
	
	public String getOriginalMessage(){
		return orginalMessage;
	}
}
