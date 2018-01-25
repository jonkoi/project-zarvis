package zarvis.bakery.behaviors.kneedingmachine;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Product;
import zarvis.bakery.utils.Util;

//import zarvis.bakery.behaviors.kneedingmachine.PerformKneading;

public class DoughKneedingBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(DoughKneedingBehaviour.class);
	private ArrayList<Integer> listProducts = new ArrayList<Integer>();
	private Bakery bakery;
	
	public DoughKneedingBehaviour(Bakery bakery) {
		this.bakery = bakery;
	}

	@Override
	public void action() {
		ACLMessage message = myAgent.receive();
		if (message != null) {
			if (message.getPerformative() == ACLMessage.INFORM && message.getConversationId().equals("kneeding-product")) {
				logger.info("product received for dough preparation " + message.getContent());
				System.out.println("product received for dough preparation " + message.getContent());
//				Util.sendMessage(myAgent, myAgent.getAID(), CustomMessage.CHANGE_STATUS, "", "knead-change-status");
				int product = Integer.parseInt(message.getContent());
				listProducts.add(product);
				System.out.println(listProducts.size());
//				Product p = bakery.getProduct(Util.PRODUCTNAMES.get(product));
//				long dpt = p.getDough_prep_time();
//				long rt = p.getResting_time();
//				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,"product accecpted by kneeding machine","kneeding-product");
//				System.out.println("Shit");
//				myAgent.addBehaviour(new PerformKneading(myAgent, (rt+dpt)*Util.MILLIS_PER_MIN, product));
			}
//			else {
//				if(message.getPerformative() == ACLMessage.REQUEST 
//				&& message.getConversationId().equals("next-product-request-preparationTableManager")){
//					if(listProducts.size() == 0){
//						Util.sendReply(myAgent, message, ACLMessage.REFUSE, "No products available for kneeding machine",
//								"next-product-request-preparationTableManager");
//					}
//					else{
//						Util.sendReply(myAgent, message, CustomMessage.RESPONSE,
//								Integer.toString(listProducts.get(0)), "next-product-request-preparationTableManager");
//						listProducts.remove(0);
//					}
//				}
//			}
		}
		else {
			block();
		}
	}
	
	private class PerformKneading extends WakerBehaviour {
		
		private int product;
		private long timeout2;

		public PerformKneading(Agent a, long timeout, int product) {
			super(a, timeout);
			this.product = product;
			this.timeout2 = timeout;
		}
		
		public void onWake() {
			listProducts.add(product);
//			System.out.println("Product list: " + listProducts.size());
			System.out.println("Product end: " + listProducts.get(listProducts.size()-1));
			Util.sendMessage(myAgent, myAgent.getAID(), CustomMessage.CHANGE_STATUS, "", "knead-change-status");
		}
	}
		
		

}