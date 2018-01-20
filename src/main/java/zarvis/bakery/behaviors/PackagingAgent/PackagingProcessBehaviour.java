package zarvis.bakery.behaviors.PackagingAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;

public class PackagingProcessBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(PackagingProcessBehaviour.class);
	private Bakery bakery;
	private int step = 0;
	private String product = "";
	private String productPackaging;
	
	public PackagingProcessBehaviour(Bakery bakery) {
		this.bakery = bakery;
	}
	
	@Override
	public void action() {
		switch (step) {
		case 0:
			DFAgentDescription[] df = Util.searchInYellowPage(myAgent, "CoolingAgent", bakery.getGuid());
			ACLMessage mesg = new ACLMessage(ACLMessage.REQUEST);
			mesg.setPerformative(ACLMessage.REQUEST);
			mesg.addReceiver(df[0].getName());
			mesg.setConversationId("next-product-request-packagingAgent");
			mesg.setContent(" ");
			myAgent.send(mesg);
			step = 1;
			break;
		case 1:
			ACLMessage response = myAgent.receive();
			if(response == null) 
				block();
			else{
				if (response.getPerformative() == CustomMessage.RESPONSE && response.getConversationId().equals("next-product-request-packagingAgent")){
					logger.info("next product packagingAgent " + response.getContent());
					product = response.getContent();
					productPackaging = this.doPackaging(product);
					step = 2;
				}
				else{
					if (response.getPerformative() == ACLMessage.REFUSE && response.getConversationId().equals("next-product-request-packagingAgent")){
						logger.info(response.getContent());
						step = 0;
					}
				}
			}
			break;
		case 2:
			DFAgentDescription[] dfTruckAgent = Util.searchInYellowPage(myAgent, "TruckAgent", bakery.getGuid());
			if(dfTruckAgent != null){
				for(DFAgentDescription TruckAgent: dfTruckAgent){
					ACLMessage msgRequest = new ACLMessage(CustomMessage.REQUEST_STATUS);
					msgRequest.setPerformative(CustomMessage.REQUEST_STATUS);
					msgRequest.addReceiver(TruckAgent.getName());
					msgRequest.setConversationId("truckAgent-availability");
					msgRequest.setContent(" ");
					myAgent.send(msgRequest);
				}
			step = 3;
			}
			break;
		case 3:
			ACLMessage message = myAgent.receive();
			if(message == null)
				block();
			else{
				if (message.getPerformative() == CustomMessage.RESPONSE_STATUS && message.getConversationId().equals("truckAgent-availability")) {
					if (message.getContent().equals("Available")) {
						ACLMessage msgRequest = new ACLMessage(CustomMessage.REQUEST_STATUS);
						msgRequest.setPerformative(CustomMessage.REQUEST_STATUS);
						msgRequest.addReceiver(message.getSender());
						msgRequest.setConversationId("packaging-product");
						msgRequest.setContent(productPackaging);
						myAgent.send(msgRequest);
						step = 4;
					}
				}

			}
			break;
		case 4:
			ACLMessage confirmation = myAgent.receive();
			if (confirmation != null) {
				if (confirmation.getPerformative() == ACLMessage.CONFIRM && confirmation.getConversationId().equals("packaging-product")) {
					logger.info(confirmation.getContent());
					step = 0;
				}
			}
			else block();
			break;
		}
	}
	private String doPackaging(String product){
		String productPackaging = " ";
		String[] str = product.split(" ");
		BakeryJsonWrapper wrapper = Util.getWrapper();
		Order order = wrapper.getOrderById(str[0]);
		productPackaging = product +" "+"["+order.getDelivery_date().getDay()+"-"+order.getDelivery_date().getHour()+"]";
		return productPackaging;
	}
}