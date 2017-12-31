package zarvis.bakery.behaviors.CoolingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class SendProductsToCoolingMachineBehaviour extends CyclicBehaviour {
	
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(SendProductsToCoolingMachineBehaviour.class);
	private Bakery bakery;
	private int step = 0;
	private String product = "";
	private String availableCoolingMachine = "";
	private DFAgentDescription[] coolingMachines;
	private int counter = 0;
	
	public SendProductsToCoolingMachineBehaviour(Bakery bakery) {
		this.bakery = bakery;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void action() {
		switch (step) {
		case 0:
			Util.sendMessage(myAgent,
					Util.searchInYellowPage(myAgent, "OvenAgent", bakery.getGuid())[0].getName(),
					ACLMessage.REQUEST, "", "next-product-request-coolingManager");
			step = 1;
			break;
		case 1:
			ACLMessage response = myAgent.receive();
			if(response != null){
				if (response.getPerformative() == CustomMessage.RESPONSE && response.getConversationId().equals("next-product-request-coolingManager")){
					logger.info("next product-coolingManager" + response.getContent());
					product = response.getContent();
					step = 2;
				}
				if (response.getPerformative() == ACLMessage.REFUSE && response.getConversationId().equals("next-product-request-coolingManager")) {
					logger.info(response.getContent());
					step = 0;
				}
			}
			else
				block();
			break;
		case 2:
			coolingMachines = Util.searchInYellowPage(myAgent, "CoolingAgent", bakery.getGuid());
			if (coolingMachines.length != 0){
				for (DFAgentDescription coolingMachine : coolingMachines) {
					Util.sendMessage(myAgent, coolingMachine.getName(), CustomMessage.REQUEST_STATUS, "","coolingMachine-availability");
				}
				step = 3;
			}
			break;
		case 3:
			ACLMessage message = myAgent.receive();

			if (message != null) {
				if (message.getPerformative() == CustomMessage.RESPONSE_STATUS && message.getConversationId().equals("coolingMachine-status")) {
					if (message.getContent().equals("Available")) {
						availableCoolingMachine = message.getSender().getName();
						step = 4;
						counter = 0;
					}
				}
				if (message.getPerformative() == ACLMessage.REFUSE && message.getConversationId().equals("coolingMachine-status")) {
					logger.info("request refused [coolingMachine-status]");
					counter ++;
					if (counter == coolingMachines.length){
						step = 0;
					}
				}
			} 
			else block();
			break;
		case 4:
			if (!product.isEmpty() && !availableCoolingMachine.isEmpty()){
				Util.sendMessage(myAgent,new AID(availableCoolingMachine), ACLMessage.INFORM, product,"cooling-product");
				step = 5;
			}
			break;
		case 5:
			ACLMessage coolingConfirmation = myAgent.receive();

			if (coolingConfirmation != null) {
				if (coolingConfirmation.getPerformative() == ACLMessage.CONFIRM && coolingConfirmation.getConversationId().equals("cooling-product")) {
					logger.info(coolingConfirmation.getContent());
				}
			} 
			else block();
			break;
			
		}
		
	}

}
