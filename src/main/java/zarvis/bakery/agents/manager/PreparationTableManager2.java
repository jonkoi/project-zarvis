package zarvis.bakery.agents.manager;

import java.util.Arrays;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import zarvis.bakery.behaviors.kneedingmachinemanager.SendProductsToKneedingMachineBehavior;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class PreparationTableManager2 extends Agent{
	private Bakery bakery;
	private boolean isAvailable = true;
	private boolean isOrderReady = false;
	private DFAgentDescription[] prepTables;
	private boolean[] isTableAvailable;
	
	private boolean hasOrder;
	private boolean isRemainEmpty;
	private String currentOrderGuid;
	private int[] currentOrderOrigin = new int[Util.PRODUCTNAMES.size()];
	private int[] currentOrderRemains = new int[Util.PRODUCTNAMES.size()];
	private int[] currentOrderExisting = new int[Util.PRODUCTNAMES.size()];
	private String currentOrderString;
	
	private boolean talkWithOven;
	
	public PreparationTableManager2(Bakery bakery) {
		this.bakery = bakery;
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "PrepTableManager", "preptablemanager-" + bakery.getGuid());
		prepTables = Util.searchInYellowPage(this, "PrepTableAgent", bakery.getGuid());
		isTableAvailable = new boolean[prepTables.length];
		Arrays.fill(isTableAvailable, true);
		this.talkWithOven = false;
		this.hasOrder = false;
		this.isRemainEmpty = false;
		Arrays.fill(currentOrderRemains, 0);
		
		ParallelBehaviour pal = new ParallelBehaviour();
		
		pal.addSubBehaviour(new AnswerAvailability());
		pal.addSubBehaviour(new ReceiveOrder());
		pal.addSubBehaviour(new WorkDistribution());
		pal.addSubBehaviour(new TalkWithOven());
		pal.addSubBehaviour(new FinishListener());
		
		addBehaviour(pal);
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		private MessageTemplate avaiTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.INQUIRE_AVAILABILITY),
				MessageTemplate.MatchConversationId("prep-availability"));
		
		public void action() {
			ACLMessage avaiMsg = myAgent.receive(avaiTemplate);
			if (avaiMsg!=null) {
				ACLMessage avaiReply = avaiMsg.createReply();
				avaiReply.setContent(isAvailable ? "A" : "U");
				avaiReply.setPerformative(CustomMessage.RESPOND_AVAILABILITY);
				myAgent.send(avaiReply);
			} else {
				block();
			}
		}
	}
	
	private class ReceiveOrder extends CyclicBehaviour{
		private MessageTemplate orderTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.INFORM_ORDER),
				MessageTemplate.MatchConversationId("prep-order"));
		public void action() {
			ACLMessage orderMsg = myAgent.receive(orderTemplate);
			if (orderMsg!=null && isAvailable) {
				String orderString = orderMsg.getContent();
				currentOrderString = orderString;
				
				System.out.println("[PREP] Order received: " + orderString);
				ACLMessage orderReply = orderMsg.createReply();
				orderReply.setPerformative(ACLMessage.CONFIRM);
				isAvailable = false;
				myAgent.send(orderReply);
				
				InitOrder(orderString);
			} else if (orderMsg!=null && isAvailable == false) {
				ACLMessage orderReply = orderMsg.createReply();
				orderReply.setPerformative(ACLMessage.REFUSE);
				myAgent.send(orderReply);
			} else {
				block(15*Util.MILLIS_PER_MIN);
			}
		}
	}
	
	private class WorkDistribution extends CyclicBehaviour{
		
		private MessageTemplate avaiTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("table-availability"),
				MessageTemplate.MatchPerformative(CustomMessage.RESPOND_AVAILABILITY));
		private MessageTemplate productConfirmTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("prep-product"),
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
		
		private int step = 0;
		private int consideringTable = 0;
		private int consideringProduct = 0;
		
		public void action() {
			switch(step) {
			case 0:
				if (hasOrder && isRemainEmpty == false) {
					for (int i = 0; i < isTableAvailable.length; i++ ) {
						if (isTableAvailable[i] == true) {
//							System.out.println("[PREP] Ask Table");
							Util.sendMessage(myAgent, prepTables[i].getName(), CustomMessage.INQUIRE_AVAILABILITY, "", "table-availability");
							consideringTable = i;
							step = 1;
							break;
						}
					}
				}
				
				block(15*Util.MILLIS_PER_MIN);
				break;
			case 1:
				ACLMessage avaiReply = myAgent.receive(avaiTemplate);
				if (avaiReply!=null && avaiReply.getContent().equals("U")) {
					//Not really free
					isTableAvailable[consideringTable] = false;
					step = 0;
				}
				else if (avaiReply!=null && avaiReply.getContent().equals("A")) {
//					System.out.println("[PREP] Receive Table available");
					isRemainEmpty = true;
					for (int i = 0; i < currentOrderRemains.length; i++) {
						if (currentOrderRemains[i]> 0) {
							consideringProduct = i;
							isRemainEmpty = false;
							Util.sendMessage(myAgent, prepTables[consideringTable].getName(), CustomMessage.INFORM_PRODUCT, Integer.toString(i), "prep-product");
							step = 2;
							break;
						}
					}
					
					if (isRemainEmpty) {
						step = 0;
					}
				} else {
					block();
				}
				
				break;
				
			case 2:
				ACLMessage productReply = myAgent.receive(productConfirmTemplate);
				
				if (productReply!=null && productReply.getPerformative()==ACLMessage.CONFIRM) {
//					System.out.println("[PREP] Get Table confirm");
					currentOrderRemains[consideringProduct]--;
					isTableAvailable[consideringTable] = false;
					step = 0;
				} else if (productReply!=null && productReply.getPerformative()==ACLMessage.REFUSE) {
					step = 0;
				} else {
					block();
				}
				break;
			}
		}
	}
	
	private class FinishListener extends CyclicBehaviour{
		private MessageTemplate finishProductTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("prep-product-finish"),
				MessageTemplate.MatchPerformative(CustomMessage.FINISH_PRODUCT));
		
		public void action() {
			ACLMessage productFinishMsg = myAgent.receive(finishProductTemplate);
			
			if (productFinishMsg!=null) {
				System.out.println("[PREP] " + productFinishMsg.getContent());
				String[] content = productFinishMsg.getContent().split(",");
				int productIdx = Integer.parseInt(content[1]);
				currentOrderExisting[productIdx]++;
				
				for (int i = 0; i < isTableAvailable.length; i++) {
					if (prepTables[i].getName().getLocalName().equals(content[0])) {
						isTableAvailable[i] = true;
					}
				}
				
				if(Arrays.equals(currentOrderExisting, currentOrderOrigin)) {
					talkWithOven = true;
//					isAvailable = true;
//					Util.sendMessage(myAgent, bakery.getAid(), CustomMessage.FINISH_ORDER, currentOrderGuid, "FINISH");
				}	
			} else {
				block();
			}
		}
		
	}
	
	private class TalkWithOven extends CyclicBehaviour{
		private int step2 = 0;
		private int stuckCase1 = 0;
		private MessageTemplate avaiTemplate2 = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.RESPOND_AVAILABILITY),
				MessageTemplate.MatchConversationId("oven-availability"));
		private MessageTemplate prepConfirmTemplate = MessageTemplate.MatchConversationId("oven-order");
		
		public void action() {
			switch(step2) {
			case 0:
				if (talkWithOven) {
					stuckCase1 = 0;
					Util.sendMessage(myAgent,
							new AID("ovenManager-"+bakery.getAid().getLocalName(), AID.ISLOCALNAME),
							CustomMessage.INQUIRE_AVAILABILITY,
							"",
							"oven-availability");
					step2=1;
				} else {
					block(15*Util.MILLIS_PER_MIN);
				}
				break;
			case 1:
				ACLMessage avaiReply2 = myAgent.receive(avaiTemplate2);
				if (avaiReply2!=null) {
					if (avaiReply2.getContent().equals("A")) {
//						System.out.println("CASE 1: ");
						Util.sendMessage(myAgent,
								new AID("ovenManager-"+bakery.getAid().getLocalName(), AID.ISLOCALNAME),
								CustomMessage.INFORM_ORDER,
								currentOrderString,
								"oven-order");
						step2 = 2;
					}
				} else {
					stuckCase1++;
					if (stuckCase1 > 2) {
						step2 = 0;
					}
					block(15*Util.MILLIS_PER_MIN);
				}
				break;
			case 2:
				ACLMessage orderReply = myAgent.receive(prepConfirmTemplate);
				if (orderReply!=null && orderReply.getPerformative()==ACLMessage.CONFIRM) {
					System.out.println("[PREP]: OVEN confirmed!");
					isAvailable = true;
					currentOrderString = "";
					talkWithOven = false;
					step2 = 0;
				} else if (orderReply!=null && orderReply.getPerformative()==ACLMessage.REFUSE) {
					step2 = 0;
				} else {
					block();
				}
			}
		}
	}
	
	private void InitOrder(String orderString) {
		String[] content = orderString.split(",");
		hasOrder = true;
		isRemainEmpty = false;
		currentOrderGuid = content[0];
		String[] currentOrderinString = content[1].split("\\.");
		for (int i = 0; i < currentOrderinString.length; i++) {
			currentOrderOrigin[i] = Integer.parseInt(currentOrderinString[i]);
		}
		Arrays.fill(currentOrderExisting, 0);
		currentOrderRemains = currentOrderOrigin.clone();
	}
}
