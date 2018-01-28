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
import zarvis.bakery.models.Product;
import zarvis.bakery.utils.Util;
import zarvis.bakery.messages.CustomMessage;

public class KneedingMachineAgent2 extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	private boolean isAvailable = true;
	private AID sender;
	private List<Product> productList;
	private WakerBehaviour kneadFunction;
	
	public KneedingMachineAgent2(Bakery bakery) {
		this.bakery = bakery;
		productList = bakery.getProducts();
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineAgent", this.bakery.getGuid());
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new AnswerAvailability());
		pal.addSubBehaviour(new ReceiveProduct());
//		pal.addSubBehaviour(new CleanNewDay());
		addBehaviour(pal);
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		
		private MessageTemplate avaiTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(CustomMessage.INQUIRE_AVAILABILITY),
				MessageTemplate.MatchConversationId("machine-availability"));
		
		@Override
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
				MessageTemplate.MatchConversationId("kneading-product"));

		@Override
		public void action() {
			ACLMessage productMsg = myAgent.receive(productTemplate);
			if (productMsg!=null && isAvailable) {
				String productString = productMsg.getContent();
				sender = productMsg.getSender();
//				System.out.println("Product received: " + productString);
				isAvailable = false;
				
				ACLMessage productReply = productMsg.createReply();
				productReply.setPerformative(ACLMessage.CONFIRM);
				myAgent.send(productReply);
				
				long startCalculate = System.currentTimeMillis();
				long waitTime = calculateTime(productString) - (System.currentTimeMillis() - startCalculate);
				
//				kneadFunction = new Function(myAgent, 15*Util.MILLIS_PER_MIN, productString);
				kneadFunction = new Function(myAgent, waitTime, productString);
				myAgent.addBehaviour(kneadFunction);
				
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
			isAvailable = true;
			Util.sendMessage(myAgent,
					sender,
					CustomMessage.FINISH_PRODUCT,
					myAgent.getLocalName()+","+productString,
					"kneading-product-finish");
			kneadFunction = null;
			myAgent.removeBehaviour(this);
		}
	}
	
	private long calculateTime(String productString) {
		int productIdx = Integer.parseInt(productString);
		String productName = Util.PRODUCTNAMES.get(productIdx);
		
		long waitTime = 0;
		
		for (Product p: productList) {
			if (p.getGuid().equals(productName)) {
				waitTime = (p.getItem_prep_time() + p.getResting_time())*Util.MILLIS_PER_MIN;
			}
		}
		
		return waitTime;
		
	}
	
	private class CleanNewDay extends CyclicBehaviour {
		
		private MessageTemplate newDayTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("new-day-to-machine"),
				MessageTemplate.MatchPerformative(CustomMessage.NEW_DAY));
		
		@Override
		public void action() {
			ACLMessage newDayMsg = myAgent.receive(newDayTemplate);
			if (newDayMsg!=null) {
				System.out.println("Machine clean");
				EraseOrder();
			} else {
				block();
			}
		}
			
	}
		
	private void EraseOrder() {
		isAvailable = true;
		if (kneadFunction!=null) {
			this.removeBehaviour(kneadFunction);
		}
	}
}
