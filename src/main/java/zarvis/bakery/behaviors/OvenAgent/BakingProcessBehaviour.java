package zarvis.bakery.behaviors.OvenAgent;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.utils.Util;

public class BakingProcessBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(BakingProcessBehaviour.class);
	private ArrayList<String> listProducts = new ArrayList<String>();

	@Override
	public void action() {
		
		ACLMessage message = myAgent.receive();
		if (message != null) {
			if (message.getPerformative() == ACLMessage.INFORM && message.getConversationId().equals("oven-product")) {
				logger.info("product received from preparation table " + message.getContent());
				listProducts.add(message.getContent());
				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,"product accecpted by oven machine","oven-product");
			}
			else 
				if(message.getPerformative() == ACLMessage.REQUEST 
				&& message.getConversationId().equals("next-product-request-coolingManager")){
					if(listProducts.size() == 0){
						Util.sendReply(myAgent, message, ACLMessage.REFUSE, "No products available for oven machine",
								"next-product-request-coolingManager");
					}
					else{
						Util.sendReply(myAgent, message, CustomMessage.RESPONSE,
								listProducts.get(0), "next-product-request-collingManager");
						listProducts.remove(0);
					}
				}
		}
		else 
			block();

	}
}
