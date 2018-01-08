package zarvis.bakery.behaviors.CoolingAgent;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.utils.Util;

public class CoolingProcessBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(CoolingProcessBehaviour.class);
	private ArrayList<String> listProducts = new ArrayList<String>();

	@Override
	public void action() {
		
		ACLMessage message = myAgent.receive();
		if (message != null) {
			if (message.getPerformative() == ACLMessage.INFORM && message.getConversationId().equals("cooling-product")) {
				logger.info("product received for oven machine " + message.getContent());
				listProducts.add(message.getContent());
				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,"product accecpted by clooling machine","cooling-product");
			}
			else 
				if(message.getPerformative() == ACLMessage.REQUEST 
				&& message.getConversationId().equals("next-product-request-packagingAgent")){
					if(listProducts.size() == 0){
						Util.sendReply(myAgent, message, ACLMessage.REFUSE, "No products available for cooling machine",
								"next-product-request-packagingAgent");
					}
					else{
						Util.sendReply(myAgent, message, CustomMessage.RESPONSE,
								listProducts.get(0), "next-product-request-packagingAgent");
						listProducts.remove(0);
					}
				}
		}
		else 
			block();

	}
}

