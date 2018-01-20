package zarvis.bakery.behaviors.truckAgent;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.utils.Util;

public class TruckAgentProcessBehaviour extends CyclicBehaviour{

	private static final long serialVersionUID = 1L;
	private ArrayList<String> listProducts = new ArrayList<String>();
	private Logger logger = LoggerFactory.getLogger(TruckAgentProcessBehaviour.class);
	

	@Override
	public void action() {
		ACLMessage message = myAgent.receive();
		if (message == null){
			block();
		}
		else{
			if (message.getPerformative() == ACLMessage.INFORM 
					&& message.getConversationId().equals("packaging-product")) {
				logger.info("product received from packaging Agent" + message.getContent());
				listProducts.add(message.getContent());
				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,"product accecpted by Truck Agent","packaging-product");
			}
		}
	}

}