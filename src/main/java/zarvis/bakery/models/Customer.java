package zarvis.bakery.models;

import java.util.ArrayList;
import java.util.List;

import zarvis.bakery.utils.Util;

public class Customer {
	private String guid;
	private String name;
	private String type;
	private Location location;
	private int total_type1;
	private int total_type2;
	private int total_type3;
	private List<Order> orders = new ArrayList<Order>();

	public int getTotal_type1() {
		return total_type1;
	}

	public void setTotal_type1(int total_type1) {
		this.total_type1 = total_type1;
	}

	public int getTotal_type2() {
		return total_type2;
	}

	public void setTotal_type2(int total_type2) {
		this.total_type2 = total_type2;
	}

	public int getTotal_type3() {
		return total_type3;
	}

	public void setTotal_type3(int total_type3) {
		this.total_type3 = total_type3;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Order> getOrders() {
		for (Order order : Util.getWrapper().getOrders()) {
			if (order.getCustomer_id().equals(name)) {
				this.orders.add(order);
			}
		}
		return orders;
	}

	@Override
	public String toString() {
		return "Customer [guid=" + guid + ", name=" + name + ", type=" + type + ", location=" + location
				+ ", total_type1=" + total_type1 + ", total_type2=" + total_type2 + ", total_type3=" + total_type3
				+ "]";
	}

}
