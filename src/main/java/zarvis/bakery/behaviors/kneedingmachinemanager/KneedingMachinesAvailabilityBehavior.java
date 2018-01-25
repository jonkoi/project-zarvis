package zarvis.bakery.behaviors.kneedingmachinemanager;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class KneedingMachinesAvailabilityBehavior extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(KneedingMachinesAvailabilityBehavior.class);

	private List<String> availableKneedingMachines = new ArrayList<>();

	private int step = 0;

	@SuppressWarnings("unused")
	private MessageTemplate mt;

	@SuppressWarnings("unused")
	private static final int blockingTime = 3000;
	private Bakery bakery;

	public KneedingMachinesAvailabilityBehavior(Bakery bakery) {
		this.bakery = bakery;
	}

	@Override
	public void action() {
		switch (step) {
		case 0:
			if (availableKneedingMachines.size() < 1) {
				if (requestKneedingMachineAvailability()){
					step = 1;	
				}
			}
			break;
		case 1:
			ACLMessage statusmessage = myAgent.receive();
			if (statusmessage != null) {
				if (statusmessage.getPerformative() == CustomMessage.RESPONSE_STATUS
						&& statusmessage.getConversationId().equals("kneeding-machine-status")) {
					if (statusmessage.getContent().equals("Available")) {
						availableKneedingMachines.add(statusmessage.getSender().getName());
						step = 2;
					}
				}

				if (statusmessage.getPerformative() == ACLMessage.REQUEST
						&& statusmessage.getConversationId().equals("next-available-kneeding-machine")) {
					Util.sendReply(myAgent,statusmessage,
							ACLMessage.REFUSE,"",
							"next-available-kneeding-machine");
					logger.info("here");

				}

			} else {
				block();
			}
			break;
		case 2:
			ACLMessage nextKneedingMachineRequest = myAgent.receive();
			if (nextKneedingMachineRequest != null) {
				if (nextKneedingMachineRequest.getPerformative() == ACLMessage.REQUEST
						&& nextKneedingMachineRequest.getConversationId().equals("next-available-kneeding-machine")) {

					Util.sendReply(myAgent,nextKneedingMachineRequest,
							CustomMessage.RESPONSE,availableKneedingMachines.get(0),
							"next-available-kneeding-machine");
					logger.info("response sent back");
					availableKneedingMachines.remove(0);

					step = 0;
				}

			} else {
				block();
			}
			break;
		}
	}

	public boolean requestKneedingMachineAvailability() {

		DFAgentDescription[] kneedingMachines = Util.searchInYellowPage(myAgent, "KneedingMachineAgent", bakery.getGuid());

		if (kneedingMachines.length == 0)
			return false;

		for (DFAgentDescription kneedingMachine : kneedingMachines) {
			Util.sendMessage(myAgent, kneedingMachine.getName(), CustomMessage.REQUEST_STATUS, "",
					"kneeding-machine-availability");
		}
		return true;
	}

}