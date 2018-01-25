package zarvis.bakery.behaviors.PreparationTableManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class SendProductsToPreparationTableBehavior extends CyclicBehaviour{
	
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(SendProductsToPreparationTableBehavior.class);
	private Bakery bakery;
	private int step = 0;
	private String product = "";
	private String availablePreparationTableAgent = "";
	private DFAgentDescription[] preparationTables;
	private int counter = 0;
	
	
	public SendProductsToPreparationTableBehavior(Bakery bakery) {
		this.bakery = bakery;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void action() {
		switch (step) {
		case 0:
			DFAgentDescription[] df = Util.searchInYellowPage(myAgent, "KneedingMachineAgent", bakery.getGuid());
			ACLMessage mesg = new ACLMessage(ACLMessage.REQUEST);
			mesg.setPerformative(ACLMessage.REQUEST);
			mesg.addReceiver(df[0].getName());
			mesg.setConversationId("next-product-request-preparationTableManager");
			mesg.setContent(" ");
			myAgent.send(mesg);
			step = 1;
			break;
		case 1:
			ACLMessage response = myAgent.receive();
			if(response != null){
				if (response.getPerformative() == CustomMessage.RESPONSE && response.getConversationId().equals("next-product-request-preparationTableManager")){

					logger.info("next product-preparationTableManager" + response.getContent());
					product = response.getContent();
					step = 2;
				}
				if (response.getPerformative() == ACLMessage.REFUSE && response.getConversationId().equals("next-product-request-preparationTableManager")) {
					//logger.info(response.getContent());
					step = 0;
				}
			}
			else
				block();
			break;
		case 2:
			preparationTables = Util.searchInYellowPage(myAgent, "PreparationTableAgent", bakery.getGuid());
			if (preparationTables.length != 0){
				for (DFAgentDescription ovenMachine : preparationTables) {
					Util.sendMessage(myAgent, ovenMachine.getName(), CustomMessage.REQUEST_STATUS, "","preparationTable-availability");
				}
				step = 3;
			}
			break;
		case 3:
			ACLMessage message = myAgent.receive();

			if (message != null) {
				if (message.getPerformative() == CustomMessage.RESPONSE_STATUS && message.getConversationId().equals("preparationTable-availability")) {
					if (message.getContent().equals("Available")) {
						availablePreparationTableAgent = message.getSender().getLocalName();
						step = 4;
						counter = 0;
					}
				}
				if (message.getPerformative() == ACLMessage.REFUSE && message.getConversationId().equals("preparationTable-status")) {
					logger.info("request refused [preparationTable-status]");
					counter ++;
					if (counter == preparationTables.length){
						step = 0;
					}
				}
			} 
			else block();
			break;
		case 4:
			if (!product.isEmpty() && !availablePreparationTableAgent.isEmpty()){
				Util.sendMessage(myAgent,new AID(availablePreparationTableAgent, AID.ISLOCALNAME), ACLMessage.INFORM, product,"preparationTable-product");
				step = 5;
			}
			break;
		case 5:
			ACLMessage preparationTableConfirmation = myAgent.receive();

			if (preparationTableConfirmation != null) {
				if (preparationTableConfirmation.getPerformative() == ACLMessage.CONFIRM && preparationTableConfirmation.getConversationId().equals("oven-product")) {
					logger.info(preparationTableConfirmation.getContent());
					step = 0;
					System.out.println("END");
				}
			} 
			else block();
			break;
			
		}
		
	}

}
