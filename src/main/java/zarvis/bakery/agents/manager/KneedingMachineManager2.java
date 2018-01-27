package zarvis.bakery.agents.manager;
import java.util.Arrays;

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

public class KneedingMachineManager2 extends Agent {
	private Bakery bakery;
	private boolean isAvailable = true;
	private boolean isOrderReady = false;
	private DFAgentDescription[] kneadingMachines;
	private boolean[] isMachineAvailable;
	
	private boolean hasOrder;
	private String currentOrderGuid;
	private int[] currentOrderOrigin = new int[Util.PRODUCTNAMES.size()];
	private int[] currentOrderRemains = new int[Util.PRODUCTNAMES.size()];
	private int[] currentOrderExisting = new int[Util.PRODUCTNAMES.size()];
	private boolean isRemainEmpty;

	public KneedingMachineManager2(Bakery bakery) {
		this.bakery = bakery;
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineManager", "kneedingmachinemanager-" + bakery.getGuid());
		kneadingMachines = Util.searchInYellowPage(this, "KneedingMachineAgent", bakery.getGuid());
		isMachineAvailable = new boolean[kneadingMachines.length];
		Arrays.fill(isMachineAvailable, true);
		this.hasOrder = false;
		this.isRemainEmpty = false;
		Arrays.fill(currentOrderRemains, 0);
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new AnswerAvailability());
		pal.addSubBehaviour(new ReceiveOrder());
		pal.addSubBehaviour(new WorkDistribution());
		pal.addSubBehaviour(new FinishListener());
		addBehaviour(pal);
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		private MessageTemplate avaiTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.INQUIRE_AVAILABILITY),
				MessageTemplate.MatchConversationId("kneading-availability"));
		public void action() {
			ACLMessage avaiMsg = myAgent.receive(avaiTemplate);
			if (avaiMsg!=null) {
//				System.out.println("Received");
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
				MessageTemplate.MatchConversationId("kneading-order"));

		@Override
		public void action() {
			ACLMessage orderMsg = myAgent.receive(orderTemplate);
			if (orderMsg!=null && isAvailable) {
				String orderString = orderMsg.getContent();
				System.out.println("Order received: " + orderString);
				isAvailable = false;
				
				ACLMessage orderReply = orderMsg.createReply();
				orderReply.setPerformative(ACLMessage.CONFIRM);
//				orderReply.addReceiver(r);
				myAgent.send(orderReply);
				
				InitOrder(orderString);
				
//				myAgent.addBehaviour(new DummyWait(myAgent, 120*Util.MILLIS_PER_MIN));
			} else if (orderMsg!=null && isAvailable == false) {
				ACLMessage orderReply = orderMsg.createReply();
				orderReply.setPerformative(ACLMessage.REFUSE);
				myAgent.send(orderReply);
			} else {
				block();
			}
		}
	}
	
	private class WorkDistribution extends CyclicBehaviour{
		private MessageTemplate avaiTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("machine-availability"),
				MessageTemplate.MatchPerformative(CustomMessage.RESPOND_AVAILABILITY));
		private MessageTemplate productConfirmTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("kneading-product"),
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
		private int consideringMachine = 0;
		private int consideringProduct = 0;
		private int step = 0;
		@Override
		public void action() {
			switch(step) {
			case 0:
				if (hasOrder && isRemainEmpty == false) {
					for (int i = 0; i < isMachineAvailable.length; i++ ) {
//						System.out.println("Knead CASE 0: out");
						if (isMachineAvailable[i] == true) {
//							System.out.println("Knead CASE 0: in");
							Util.sendMessage(myAgent, kneadingMachines[i].getName(), CustomMessage.INQUIRE_AVAILABILITY, "", "machine-availability");
							consideringMachine = i;
							step = 1;
							break;
						}
					}
				} 
				block(15*Util.MILLIS_PER_MIN);
				break;
			case 1:
				//Check if really available
				ACLMessage avaiReply = myAgent.receive(avaiTemplate);
				if (avaiReply!=null && avaiReply.getContent().equals("U")) {
					//Not really free
					isMachineAvailable[consideringMachine] = false;
					step = 0;
				} 
				
				else if (avaiReply!=null && avaiReply.getContent().equals("A")) {
					//Actually free, sending product
					isRemainEmpty = true;
					for (int i = 0; i < currentOrderRemains.length; i++) {
						if (currentOrderRemains[i]> 0) {
							consideringProduct = i;
							isRemainEmpty = false;
//							System.out.println("Knead CASE 1: in");
							Util.sendMessage(myAgent, kneadingMachines[consideringMachine].getName(), CustomMessage.INFORM_PRODUCT, Integer.toString(i), "kneading-product");
							step = 2;
							break;
						}
					}
					
					if (isRemainEmpty) {
//						System.out.println("No product left!");
						step = 0;
					}
				}
				break;
				
			case 2:
				ACLMessage productReply = myAgent.receive(productConfirmTemplate);
				if (productReply!=null && productReply.getPerformative()==ACLMessage.CONFIRM) {
//					System.out.println("Knead CASE 2: in");
					currentOrderRemains[consideringProduct]--;
					isMachineAvailable[consideringMachine] = false;
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
		
		private MessageTemplate finishProductTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("kneading-product-finish"),
				MessageTemplate.MatchPerformative(CustomMessage.FINISH_PRODUCT));

		@Override
		public void action() {
			ACLMessage productFinishMsg = myAgent.receive(finishProductTemplate);
			if (productFinishMsg!=null) {
				System.out.println("Received product done: " + productFinishMsg.getContent());
				String[] content = productFinishMsg.getContent().split(",");
				int productIdx = Integer.parseInt(content[1]);
				currentOrderExisting[productIdx]++;
				//Revert to true for available
				for (int i = 0; i < isMachineAvailable.length; i++) {
					if (kneadingMachines[i].getName().getLocalName().equals(content[0])) {
						isMachineAvailable[i] = true;
					}
				}
				
				if(Arrays.equals(currentOrderExisting, currentOrderOrigin)) {
					//Send finish back to bakery or Preptable
					//Might need confirmation feature
					isAvailable = true;
//					System.out.println(bakery.getAid().getLocalName());
					Util.sendMessage(myAgent, bakery.getAid(), CustomMessage.FINISH_ORDER, currentOrderGuid, "FINISH");
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
