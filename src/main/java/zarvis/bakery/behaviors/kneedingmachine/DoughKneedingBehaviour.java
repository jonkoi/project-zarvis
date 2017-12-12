package zarvis.bakery.behaviors.kneedingmachine;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Status;
import zarvis.bakery.utils.Util;

public class DoughKneedingBehaviour extends CyclicBehaviour {

	private Logger logger = LoggerFactory.getLogger(DoughKneedingBehaviour.class);


	@Override
	public void action() {

		ACLMessage message = myAgent.receive();

		if (message != null) {

			if (message.getPerformative() == ACLMessage.INFORM
					&& message.getConversationId().equals("kneeding-product")) {

				logger.info("product received for dough preparation " + message.getContent());
				Util.sendReply(myAgent,message,ACLMessage.CONFIRM,
						"product accecpted by kneeding machine",
						"kneeding-product");
			}

		} else {
			block();
		}

	}

}