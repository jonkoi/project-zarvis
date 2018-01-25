package zarvis.bakery.behaviors.kneedingmachine;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zarvis.bakery.behaviors.bakery.ProcessOrderBehaviour;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Status;

public class CurrentStatusBehaviour extends CyclicBehaviour {

	
	private static final long serialVersionUID = 1L;
	private Status status = new Status(true);
	private Logger logger = LoggerFactory.getLogger(CurrentStatusBehaviour.class);
	
	private MessageTemplate fromKneadManagerTemplate = MessageTemplate.MatchConversationId("kneeding-machine-availability");
	private MessageTemplate changeStatusTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(CustomMessage.CHANGE_STATUS),
			MessageTemplate.MatchConversationId("knead-change-status"));


	@Override
	public void action() {

		ACLMessage message = myAgent.receive(fromKneadManagerTemplate);

		if (message != null) {
			if (message.getPerformative() == CustomMessage.REQUEST_STATUS) {
				System.out.println("We are in");
				ACLMessage reply = message.createReply();
				reply.setPerformative(CustomMessage.RESPONSE_STATUS);
				reply.setContent(status.getStatus() ? "Available" : "Unavailable");
				reply.setConversationId("kneeding-machine-status");
				myAgent.send(reply);

			}
		ACLMessage message1 = myAgent.receive(changeStatusTemplate);
//		System.out.println("State changed!");
		if (message1 != null) {
			System.out.println("State changed!");
			status.setStatus(!status.getStatus());
		}

		} else {
			block();
		}

	}

}