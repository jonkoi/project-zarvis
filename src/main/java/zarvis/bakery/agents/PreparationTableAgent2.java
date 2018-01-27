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

public class PreparationTableAgent2 extends Agent {
	private Bakery bakery;
	private boolean isAvailable = true;
	private AID sender;
	private List<Product> productList;
	private WakerBehaviour prepFunction;
	
	public PreparationTableAgent2(Bakery bakery) {
		this.bakery = bakery;
		productList = bakery.getProducts();
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "PrepTableAgent", this.bakery.getGuid());
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new AnswerAvailability());
		pal.addSubBehaviour(new ReceiveProduct());
//		pal.addSubBehaviour(new CleanNewDay());
		addBehaviour(pal);
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		private MessageTemplate avaiTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(CustomMessage.INQUIRE_AVAILABILITY),
				MessageTemplate.MatchConversationId("table-availability"));
		
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
				MessageTemplate.MatchConversationId("prep-product"));
		public void action() {
			ACLMessage productMsg = myAgent.receive(productTemplate);
			if (productMsg!=null && isAvailable) {
				String productString = productMsg.getContent();
				sender = productMsg.getSender();
//				System.out.println("[PREP TAB] Product received: " + productString);
//				System.out.println("[PREP TAB] Sender is: " + sender.getLocalName());
				isAvailable = false;
				
				ACLMessage productReply = productMsg.createReply();
				productReply.setPerformative(ACLMessage.CONFIRM);
				myAgent.send(productReply);
				
				long startCalculate = System.currentTimeMillis();
				long waitTime = calculateTime(productString) - (System.currentTimeMillis() - startCalculate);
				
				prepFunction = new Function(myAgent, 45*Util.MILLIS_PER_MIN, productString);
//				kneadFunction = new Function(myAgent, waitTime, productString);
				
				myAgent.addBehaviour(prepFunction);
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
//			System.out.println("[PREP TAB] Finish product: " + productString);
			isAvailable = true;
			Util.sendMessage(myAgent,
					sender,
					CustomMessage.FINISH_PRODUCT,
					myAgent.getLocalName()+","+productString,
					"prep-product-finish");
			prepFunction = null;
			myAgent.removeBehaviour(this);
		}
	}
	
	private long calculateTime(String productString) {
		return 45*Util.MILLIS_PER_MIN;
	}

}
