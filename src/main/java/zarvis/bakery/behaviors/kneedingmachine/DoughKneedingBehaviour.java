package zarvis.bakery.behaviors.kneedingmachine;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.utils.Util;

public class DoughKneedingBehaviour extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(DoughKneedingBehaviour.class);
	private ArrayList<String> listProducts = new ArrayList<String>();


	@Override
	public void action() {
		ACLMessage message = myAgent.receive();
		if (message != null) {
			if (message.getPerformative() == ACLMessage.INFORM && message.getConversationId().equals("kneeding-product")) {
				logger.info("product received for dough preparation " + message.getContent());
				listProducts.add(message.getContent());
				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,"product accecpted by kneeding machine","kneeding-product");
			}
			else {
				if(message.getPerformative() == ACLMessage.REQUEST 
				&& message.getConversationId().equals("next-product-request-preparationTableManager")){
					
					if(listProducts.size() == 0){
						Util.sendReply(myAgent, message, ACLMessage.REFUSE, "No products available for kneeding machine",
								"next-product-request-preparationTableManager");
					}
					else{
						Util.sendReply(myAgent, message, CustomMessage.RESPONSE,
								listProducts.get(0), "next-product-request-preparationTableManager");
						listProducts.remove(0);
					}
				}
				
			}
		}
		else 
			block();

	}

}