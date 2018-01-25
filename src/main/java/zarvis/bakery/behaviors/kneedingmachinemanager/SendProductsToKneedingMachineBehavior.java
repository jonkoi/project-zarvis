package zarvis.bakery.behaviors.kneedingmachinemanager;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

import java.util.HashSet;

public class SendProductsToKneedingMachineBehavior extends CyclicBehaviour {
	private Logger logger = LoggerFactory.getLogger(SendProductsToKneedingMachineBehavior.class);

	private HashSet<String> availableKneedingMachines = new HashSet<String>();

	private int step = 0;

	private MessageTemplate fromBakeryTemplate;
	private MessageTemplate toMachineTemplate;

	private static final int blockingTime = 3000;

	private String product = "";
	private String availableKneedingMachine = "";
	private DFAgentDescription[] kneedingMachines;
	private int refuseCounter = 0;

	private Bakery bakery;

	public SendProductsToKneedingMachineBehavior(Bakery bakery){
		this.bakery = bakery;
		this.fromBakeryTemplate =
				MessageTemplate.MatchConversationId("next-product-request");
		this.toMachineTemplate =
				MessageTemplate.MatchConversationId("kneeding-machine-status");
	}

	@Override
	public void action() {

		switch (step) {

		case 0:

			Util.sendMessage(myAgent,Util.searchInYellowPage(myAgent,"BakeryService",bakery.getGuid())[0].getName(),
					ACLMessage.REQUEST,"","next-product-request");
			System.out.println("START");
			step = 1;

			break;
		case 1:

			ACLMessage productResponse = myAgent.receive(fromBakeryTemplate);
			if (productResponse != null) {
				
				if (productResponse.getPerformative() == CustomMessage.RESPONSE) {
//					System.out.println("next product " + productResponse.getContent());
					product = productResponse.getContent();
					step = 2;
				}
				if (productResponse.getPerformative() == ACLMessage.REFUSE) {
					//logger.info(productResponse.getContent());

					step = 0;
				}
			} else {
				block();
			}


			break;

		case 2:
			kneedingMachines = Util.searchInYellowPage(myAgent, "KneedingMachineAgent", bakery.getGuid());

			if (kneedingMachines.length != 0){
				for (DFAgentDescription kneedingMachine : kneedingMachines) {
					Util.sendMessage(myAgent, kneedingMachine.getName(), CustomMessage.REQUEST_STATUS, "",
							"kneeding-machine-availability");
				}
				step = 3;
			}
			break;

		case 3:

			ACLMessage message = myAgent.receive(toMachineTemplate);
			if (message != null) {
				if (message.getPerformative() == CustomMessage.RESPONSE_STATUS
						&& message.getConversationId().equals("kneeding-machine-status")) {
					if (message.getContent().equals("Available")) {
						availableKneedingMachine = message.getSender().getLocalName();
						step = 4;
						refuseCounter = 0;
					} else {
						block();
					}
				}

				if (message.getPerformative() == ACLMessage.REFUSE &&
						message.getConversationId().equals("kneeding-machine-status")) {
					logger.info("request refused");
					refuseCounter ++;
					if (refuseCounter == kneedingMachines.length){
						step = 0;
					}
				}
			} else {
				block();
			}

			break;

		case 4:
			
			if (!product.isEmpty() && !availableKneedingMachine.isEmpty()){
//				System.out.println("Step4");
				Util.sendMessage(myAgent,new AID(availableKneedingMachine, AID.ISLOCALNAME), ACLMessage.INFORM, product,
						"kneeding-product");
				step = 5;
			}
			break;

		case 5:
			ACLMessage kneedingConfirmation = myAgent.receive();

			if (kneedingConfirmation != null) {
				if (kneedingConfirmation.getPerformative() == ACLMessage.CONFIRM
						&& kneedingConfirmation.getConversationId().equals("kneeding-product")) {
					logger.info(kneedingConfirmation.getContent());
					System.out.println("END!");
					step = 0;
				}
			} else {
				block();
			}
			break;
		}

	}

}