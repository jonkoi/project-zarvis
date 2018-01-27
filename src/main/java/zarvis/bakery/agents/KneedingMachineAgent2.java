package zarvis.bakery.agents;

import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
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
				
//				myAgent.addBehaviour(new Function(myAgent, waitTime, productString));
				myAgent.addBehaviour(new Function(myAgent, 15*Util.MILLIS_PER_MIN, productString));
				
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
}
