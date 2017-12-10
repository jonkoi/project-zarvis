package zarvis.bakery.models.junitTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Location;
import zarvis.bakery.models.Oven;
import zarvis.bakery.models.Product;

public class BakeryJunitTest {

	@Test
	public void getNameTest() {
		Bakery bakery = new Bakery();
		bakery.setName("bakery01");
		assertEquals("bakery01", bakery.getName());
	}
	
	@Test
	public void getGuidTest(){
		Bakery bakery = new Bakery();
		bakery.setGuid("bakeryId001");
		assertEquals("bakeryId001", bakery.getGuid());
	}
	
	@Test
	public void getLocationTest(){
		Location location = new Location();
		location.setX(3);
		location.setY(4);
		Bakery bakery = new Bakery();
		bakery.setLocation(location);
		
		assertEquals(location, bakery.getLocation());
		
	}
	
	@Test
	public void getOvensTest(){
		ArrayList<Oven> ovens = new ArrayList<Oven>();
		Oven oven1 = new Oven();
		oven1.setGuid("oven1");
		oven1.setCooling_rate(12);
		oven1.setHeating_rate(13);
		ovens.add(oven1);
		Bakery bakery = new Bakery();
		bakery.setOvens(ovens);
		
		assertEquals(ovens,bakery.getOvens());
	}
	
	
	

}
