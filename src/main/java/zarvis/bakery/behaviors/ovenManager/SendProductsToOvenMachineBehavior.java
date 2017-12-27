package zarvis.bakery.behaviors.ovenManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;
import jade.lang.acl.ACLMessage;

public class SendProductsToOvenMachineBehavior extends CyclicBehaviour {
	
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(SendProductsToOvenMachineBehavior.class);
	private Bakery bakery;
	private int step = 0;
	private String product = "";
	private String availableOvenMachine = "";
	private DFAgentDescription[] ovenMachines;
	private int counter = 0;
	
	public SendProductsToOvenMachineBehavior(Bakery bakery) {
		this.bakery = bakery;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void action() {
		switch (step) {
		case 0:
			Util.sendMessage(myAgent,
					Util.searchInYellowPage(myAgent, "KneedingMachineManager", "kneedingmachinemanager-" + bakery.getGuid())[0].getName(),
					ACLMessage.REQUEST, "", "next-product-request-ovenManager");
			step = 1;
			break;
		case 1:
			ACLMessage response = myAgent.receive();
			if(response != null){
				if (response.getPerformative() == CustomMessage.RESPONSE && response.getConversationId().equals("next-product-request-ovenManager")){
					logger.info("next product-ovenManager" + response.getContent());
					product = response.getContent();
					step = 2;
				}
				if (response.getPerformative() == ACLMessage.REFUSE && response.getConversationId().equals("next-product-request-ovenManager")) {
					//logger.info(response.getContent());
					step = 0;
				}
			}
			else
				block();
			break;
		case 2:
			ovenMachines = Util.searchInYellowPage(myAgent, "OvenAgent", bakery.getGuid());
			if (ovenMachines.length != 0){
				for (DFAgentDescription ovenMachine : ovenMachines) {
					Util.sendMessage(myAgent, ovenMachine.getName(), CustomMessage.REQUEST_STATUS, "","ovenMachine-availability");
				}
				step = 3;
			}
			break;
		case 3:
			ACLMessage message = myAgent.receive();

			if (message != null) {
				if (message.getPerformative() == CustomMessage.RESPONSE_STATUS && message.getConversationId().equals("ovenMachine-status")) {
					if (message.getContent().equals("Available")) {
						availableOvenMachine = message.getSender().getName();
						step = 4;
						counter = 0;
					}
				}
				if (message.getPerformative() == ACLMessage.REFUSE && message.getConversationId().equals("ovenMachine-status")) {
					logger.info("request refused [ovenMachine-status]");
					counter ++;
					if (counter == ovenMachines.length){
						step = 0;
					}
				}
			} 
			else block();
			break;
		case 4:
			if (!product.isEmpty() && !availableOvenMachine.isEmpty()){
				Util.sendMessage(myAgent,new AID(availableOvenMachine), ACLMessage.INFORM, product,"oven-product");
				step = 5;
			}
			break;
		case 5:
			ACLMessage ovenConfirmation = myAgent.receive();

			if (ovenConfirmation != null) {
				if (ovenConfirmation.getPerformative() == ACLMessage.CONFIRM && ovenConfirmation.getConversationId().equals("oven-product")) {
					logger.info(ovenConfirmation.getContent());
				}
			} 
			else block();
			break;
			
		}
		
	}

}
