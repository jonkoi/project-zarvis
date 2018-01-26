package zarvis.bakery.agents.manager;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.behaviors.kneedingmachinemanager.SendProductsToKneedingMachineBehavior;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class KneedingMachineManager2 extends Agent {
	private Bakery bakery;
	private boolean isAvailable = true;
	private boolean isOrderReady = false;

	public KneedingMachineManager2(Bakery bakery) {
		this.bakery = bakery;
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineManager", "kneedingmachinemanager-" + bakery.getGuid());
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new AnswerAvailability());
		pal.addSubBehaviour(new ReceiveOrder());
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
				myAgent.send(orderReply);
				
				myAgent.addBehaviour(new DummyWait(myAgent, 120*Util.MILLIS_PER_MIN));
			} else if (orderMsg!=null && isAvailable == false) {
				ACLMessage orderReply = orderMsg.createReply();
				orderReply.setPerformative(ACLMessage.REFUSE);
				myAgent.send(orderReply);
			} else {
				block();
			}
		}
	}
	
	private class DummyWait extends WakerBehaviour{

		public DummyWait(Agent a, long timeout) {
			super(a, timeout);
		}
		
		public void onWake() {
			System.out.println(bakery.getAid().getLocalName());
			isAvailable = true;
			Util.sendMessage(myAgent,
					bakery.getAid(),
					CustomMessage.FINISH_ORDER,
					"",
					"FINISH");
			
		}
		
	}
}
