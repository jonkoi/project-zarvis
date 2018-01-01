package zarvis.bakery.behaviors.PreparationTable;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Status;

public class StatusOfPreparationTableAgentBehavior extends CyclicBehaviour{
	
	private static final long serialVersionUID = 1L;
	private Status status = new Status(true);	
	@Override
	public void action() {
		ACLMessage message = myAgent.receive();

		if (message != null) {
			if (message.getPerformative() == CustomMessage.REQUEST_STATUS && message.getConversationId().equals("preparationTable-availability")) {
				ACLMessage reply = message.createReply();
				reply.setPerformative(CustomMessage.RESPONSE_STATUS);
				reply.setContent(status.getStatus() ? "Available" : "Unavailable");
				reply.setConversationId("preparationTable-status");
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
