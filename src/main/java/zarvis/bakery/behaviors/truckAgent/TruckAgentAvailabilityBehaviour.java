package zarvis.bakery.behaviors.truckAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Status;

public class TruckAgentAvailabilityBehaviour extends CyclicBehaviour{
	
	private static final long serialVersionUID = 1L;
	private Status status = new Status(true);
	
	
	@Override
	public void action() {
		ACLMessage message = myAgent.receive();

		if (message != null) {

			if (message.getPerformative() == CustomMessage.REQUEST_STATUS
					&& message.getConversationId().equals("truckAgent-availability")) {
				ACLMessage reply = message.createReply();
				reply.setPerformative(CustomMessage.RESPONSE_STATUS);
				reply.setContent(status.getStatus() ? "Available" : "Unavailable");
				reply.setConversationId("truckAgent-availability");
				myAgent.send(reply);

			}

			if (message.getPerformative() == CustomMessage.CHANGE_STATUS) {
				status.setStatus(!status.getStatus());
			}

		} else {
			block();
		}
	}

}