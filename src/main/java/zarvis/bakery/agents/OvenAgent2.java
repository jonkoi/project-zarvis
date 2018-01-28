package zarvis.bakery.agents;

import java.util.Arrays;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Oven;
import zarvis.bakery.models.Product;
import zarvis.bakery.utils.Util;
import zarvis.bakery.messages.CustomMessage;

public class OvenAgent2 extends Agent {
	private Bakery bakery;
	private boolean isAvailable = true;
	private AID sender;
	private List<Product> productList;
	private WakerBehaviour ovenFunction;
	
	private int heating_rate;
	private int cooling_rate;
	private int currentTemp = 0;
	
	public OvenAgent2(Bakery bakery) {
		this.bakery = bakery;
		productList = bakery.getProducts();
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "OvenAgent", this.bakery.getGuid());
		for (Oven o : bakery.getOvens()) {
			if (this.getAID().getLocalName().equals(o.getGuid()+ "-" + bakery.getGuid())) {
				System.out.println(this.getAID().getLocalName() + o.getHeating_rate());
				heating_rate = o.getHeating_rate();
				cooling_rate = o.getCooling_rate();
				break;
			}
		}
		
		if (heating_rate > 0 && cooling_rate > 0) {
			ParallelBehaviour pal = new ParallelBehaviour();
			pal.addSubBehaviour(new AnswerAvailability());
			pal.addSubBehaviour(new ReceiveProduct());
//			pal.addSubBehaviour(new CleanNewDay());
			addBehaviour(pal);
		} else {
			
			//Kaputt oven
			isAvailable = false;
			addBehaviour(new AnswerAvailability());
		}
		
		
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		private MessageTemplate avaiTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(CustomMessage.INQUIRE_AVAILABILITY),
				MessageTemplate.MatchConversationId("single-oven-availability"));
		
		public void action() {
			ACLMessage avaiMsg = myAgent.receive(avaiTemplate);
			if (avaiMsg!=null) {
//				System.out.println("Received avai");
				ACLMessage avaiReply = avaiMsg.createReply();
				avaiReply.setContent(isAvailable ? "A" : "U");
				avaiReply.setPerformative(CustomMessage.RESPOND_AVAILABILITY);
				myAgent.send(avaiReply);
			} else {
				block();
			}
		}
	}
	
	private class ReceiveProduct extends CyclicBehaviour{
		
		private MessageTemplate productTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(CustomMessage.INFORM_PRODUCT),
				MessageTemplate.MatchConversationId("oven-product"));
		public void action() {
			ACLMessage productMsg = myAgent.receive(productTemplate);
			if (productMsg!=null && isAvailable) {
				String message = productMsg.getContent();
				String[] content = productMsg.getContent().split(",");
				String productString = content[0];
//				String bread_per_oven = content[1];
				sender = productMsg.getSender();
//				System.out.println("[OVEN TAB] Product received: " + productString);
//				System.out.println("[OVEN TAB] Sender is: " + sender.getLocalName());
				isAvailable = false;
				
				ACLMessage productReply = productMsg.createReply();
				productReply.setPerformative(ACLMessage.CONFIRM);
				myAgent.send(productReply);
				
				long startCalculate = System.currentTimeMillis();
				long waitTime = calculateTime(productString) - (System.currentTimeMillis() - startCalculate);
				
//				ovenFunction = new Function(myAgent, 15*Util.MILLIS_PER_MIN, productString);
				ovenFunction = new Function(myAgent, waitTime, message);
				
				myAgent.addBehaviour(ovenFunction);
			} else if (productMsg!=null && isAvailable == false) {
				ACLMessage productReply = productMsg.createReply();
				productReply.setPerformative(ACLMessage.REFUSE);
				myAgent.send(productReply);
			} else {
				block();
			}
		}
	}
	
	private class Function extends WakerBehaviour{
		private String productString;

		public Function(Agent a, long timeout, String productString) {
			super(a, timeout);
			this.productString = productString;
		}
		
		public void onWake() {
//			System.out.println("[OVEN TAB] Finish product: " + productString);
			isAvailable = true;
			Util.sendMessage(myAgent,
					sender,
					CustomMessage.FINISH_PRODUCT,
					myAgent.getLocalName()+","+productString,
					"oven-product-finish");
			ovenFunction = null;
			myAgent.removeBehaviour(this);
		}
	}
	
	private long calculateTime(String productString) {
		int productIdx = Integer.parseInt(productString);
		String productName = Util.PRODUCTNAMES.get(productIdx);
		
		long waitTime = 0;
		
		for (Product p: productList) {
			if (p.getGuid().equals(productName)) {
				int baking_temp = p.getBaking_temp();
				int temp_diff = baking_temp - currentTemp;
				if (temp_diff > 0) {
					waitTime += (int)(temp_diff/heating_rate)*Util.MILLIS_PER_MIN;
				} else {
					waitTime += (int)(temp_diff/cooling_rate)*Util.MILLIS_PER_MIN;
				}
				waitTime += (p.getBaking_time() + (baking_temp - p.getBoxing_temp())/p.getCooling_rate()) * Util.MILLIS_PER_MIN;
			}
		}
		
		return waitTime;
	}

}
