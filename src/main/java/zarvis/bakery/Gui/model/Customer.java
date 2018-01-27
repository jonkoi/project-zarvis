package zarvis.bakery.Gui.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import zarvis.bakery.models.Location;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;

public class Customer {
	private final StringProperty guid;
	private final StringProperty name;
	private final StringProperty type;
	private final ObjectProperty<Location> location;
	private final IntegerProperty totalType1;
	private final IntegerProperty totalType2;
	private final IntegerProperty totalType3;
	private ArrayList<Order> orders = new ArrayList<Order>();
	
	public Customer(){
		this(null,null);
	}
	
	public Customer(String guid, String name) {
		this.guid = new SimpleStringProperty(guid);
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty("");
		this.location = new SimpleObjectProperty<Location>();
		this.location.set(new Location());
		this.totalType1 = new SimpleIntegerProperty(1);
		this.totalType2 = new SimpleIntegerProperty(1);
		this.totalType3 = new SimpleIntegerProperty(1);
	}

	public int getTotalType1() {
		return totalType1.get();
	}

	public void setTotalType1(int totalType1) {
		this.totalType1.set(totalType1);
	}
	
	 public IntegerProperty totalType1Property(){
		 return this.totalType1;
	 }
	
	public int getTotalType2() {
			return totalType2.get();
	}

	public void setTotalType2(int totalType2) {
			this.totalType2.set(totalType2);
	}
		
	public IntegerProperty totalType2Property(){
			 return this.totalType2;
	}
	public int getTotalType3() {
		return totalType3.get();
	}

	public void setTotalType3(int totalType3) {
		this.totalType3.set(totalType3);
	}
	
	public IntegerProperty totalType3Property(){
		 return this.totalType3;
	}

	public String getGuid() {
		return guid.get();
	}

	public void setGuid(String guid) {
		this.guid.set(guid);
	}
	
	public StringProperty guidProperty(){
		return this.guid;
	}
	
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	public StringProperty nameProperty(){
		return this.name;
	}
	
	public String getType() {
		return type.get();
	}

	public void setType(String type) {
		this.type.set(type);
	}

	public Location getLocation() {
		return location.get();
	}

	public void setLocation(Location location) {
		this.location.set(location);
	}

	public List<Order> getOrders() {
		for (Order order : Util.getWrapper().getOrders()) {
			if (order.getCustomer_id().equals(guid.get())) {
				this.orders.add(order);
			}
		}
		return orders;
	}
	
	public void setOrders(List<Order> orders){
		this.orders = (ArrayList<Order>) orders;
	}

}
