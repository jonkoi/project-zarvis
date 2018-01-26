package zarvis.bakery.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;
import zarvis.bakery.messages.CustomMessage;

public class KneedingMachineAgent2 extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	private boolean isAvailable = true;
	
	public KneedingMachineAgent2(Bakery bakery) {
		this.bakery = bakery;
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineAgent", this.bakery.getGuid());
		
		ParallelBehaviour pal = new ParallelBehaviour();
		pal.addSubBehaviour(new AnswerAvailability());
		addBehaviour(pal);
	}
	
	private class AnswerAvailability extends CyclicBehaviour{
		
		private MessageTemplate avaiTemplate;
		
		@Override
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
	
	private class ReceiveProduct extends CyclicBehaviour{
		
		private MessageTemplate productTemplate;

		@Override
		public void action() {
			ACLMessage productMsg = myAgent.receive(productTemplate);
			if (productMsg!=null && isAvailable) {
				
			} else if (orderMsg!=null && isAvailable == false) {
				
			} else {
				block();
			}
		}
		
	}
}
