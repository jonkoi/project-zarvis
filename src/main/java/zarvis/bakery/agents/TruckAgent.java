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
import zarvis.bakery.models.Location;
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
	private Location bakeryLoc;
	public TruckAgent(Bakery bakery) {
		this.bakery = bakery;
		neig = Util.InitializeGraph();
	}
	
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "TruckAgent", this.bakery.getGuid());
//		System.out.println("TRUCK!!!");
		bakeryGuid = bakery.getGuid();
		bakeryLoc = bakery.getLocation();
		
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
					
					long waitTime = calculateWaitTime(truckMsg.getContent());
					
					myAgent.addBehaviour(new Deliver(myAgent, waitTime, truckMsg.getContent()));
					myAgent.addBehaviour(new Return(myAgent, waitTime));
					
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
		
//		long waitTime = 0;
//		System.out.println(content + "!!!!!!!!!!!!!!!!!!!!!!!!");
//		String customer = content.split(",")[1];
//		Location targetLoc = new Location();
//		Location currentLoc = bakeryLoc;
//		for (Map.Entry<Node, List<Node>> n : neig.GetMap().entrySet()) {
//			if (n.getKey().getCompany().equals(customer)) {
//				targetLoc = n.getKey().getLocation();
//				
//				while (currentLoc.equals(targetLoc) == false) {
//					double  minDist = 9999.00;
//					Location minLoc = currentLoc;
//					
//					for (Node n1 : n.getValue()) {
//						double dist = Math.pow(n1.getLocation().getX() - targetLoc.getX(),2) + Math.pow(n1.getLocation().getY() - targetLoc.getY(),2);
//						if (dist < minDist) {
//							minDist = dist;
//							minLoc = n1.getLocation();
//						}
//					}
//					
//					double travelCost = Math.sqrt(Math.pow(currentLoc.getX() - minLoc.getX(),2) + Math.pow(currentLoc.getY() - minLoc.getY(),2));
//					waitTime += (long) travelCost;
//					currentLoc = minLoc;
//				}
//				break;
//			}
//		}
		return waitTime;
	}
}