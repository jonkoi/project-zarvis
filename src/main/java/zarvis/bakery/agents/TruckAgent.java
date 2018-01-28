package zarvis.bakery.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import zarvis.bakery.utils.NeiGraph;
import zarvis.bakery.utils.Util;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Node;

public class TruckAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	private String bakeryGuid;
	private boolean isAvailable = true;
	private NeiGraph neig;
	public TruckAgent(Bakery bakery) {
		this.bakery = bakery;
		neig = Util.InitializeGraph();
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "TruckAgent", this.bakery.getGuid());
//		System.out.println("TRUCK!!!");
		bakeryGuid = bakery.getGuid();
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new ReceiveRequest());
//		pal.addSubBehaviour(new ReceiveProduct());
//		pal.addSubBehaviour(new CleanNewDay());
		addBehaviour(pal);
	}
	
	private class ReceiveRequest extends CyclicBehaviour{
		private MessageTemplate truckTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(CustomMessage.REQUEST_DELIVERY),
				MessageTemplate.MatchConversationId("delivery-request"));
		public void action() {
			ACLMessage truckMsg= myAgent.receive(truckTemplate);
			if (truckMsg != null) {
				if (isAvailable) {
					
					System.out.println(bakery.getGuid() + " [TRUCK] accept request");
					ACLMessage truckReply = truckMsg.createReply();
					truckReply.setPerformative(ACLMessage.CONFIRM);
					myAgent.send(truckReply);
					
					isAvailable = false;
					
					long waitTime = calculateWaitTime(truckReply.getContent());
					
					myAgent.addBehaviour(new Deliver(myAgent, waitTime, truckMsg.getContent()));
					myAgent.addBehaviour(new Return(myAgent, waitTime*2));
					
				} else {
					ACLMessage truckReply = truckMsg.createReply();
					truckReply.setPerformative(ACLMessage.REFUSE);
					myAgent.send(truckReply);
				}
			}
		}
	}
	
	private class Deliver extends WakerBehaviour {
		
		private String customer;
		private String msg;

		public Deliver(Agent a, long timeout, String msg) {
			super(a, timeout);
//			System.out.println(msg);
			this.customer = msg.split(",")[1];
			this.msg = msg;
		}
		
		public void onWake() {
//			System.out.println(msg);
			Util.sendMessage(myAgent, new AID(customer, AID.ISLOCALNAME), CustomMessage.FINISH_ORDER, msg, "to-customer-finish-order");
			myAgent.removeBehaviour(this);
		}
		
	}
	
	private class Return extends WakerBehaviour {

		public Return(Agent a, long timeout) {
			super(a, timeout);
			// TODO Auto-generated constructor stub
		}
		
		public void onWake() {
			Util.sendMessage(myAgent, new AID(bakery.getGuid(), AID.ISLOCALNAME), CustomMessage.HAS_RETURNED, myAgent.getLocalName(), "truck-return");
			isAvailable = true;
			myAgent.removeBehaviour(this);
		}
	}
	
	private long calculateWaitTime(String content) {
		long waitTime = 5 * Util.MILLIS_PER_MIN;
//		Node currentNode = new Node();
//		for (Map.Entry<Node, List<String>> e : neig.GetMap().entrySet()) {
//			if(e.getKey().getCompany().equals(bakeryGuid)) {
//				currentNode = e.getKey();
//			}
//		}
		return waitTime;
	}
}