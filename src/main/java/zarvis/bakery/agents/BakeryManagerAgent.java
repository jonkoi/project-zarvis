package zarvis.bakery.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class BakeryManagerAgent extends TimeAgent {
	
	private Bakery bakery;
	private MessageTemplate mt;

	public BakeryManagerAgent(Bakery bakery, long globalStartTime) {
		super(globalStartTime);
		this.bakery = bakery;
		mt = MessageTemplate.and(
		  		MessageTemplate.MatchConversationId("Inform-order"),
		  		MessageTemplate.MatchPerformative(ACLMessage.INFORM) );
	}
	
	protected void setup() {
		Util.registerInYellowPage(this, "BakeryManagerService", bakery.getGuid()+"-manager");
		
		ParallelBehaviour pal = new ParallelBehaviour();
		
		pal.addSubBehaviour(new UpdateListen());
		pal.addSubBehaviour(new ManageProduction());
		
		addBehaviour(pal);
	}
	
	private class UpdateListen extends CyclicBehaviour {

		@Override
		public void action() {
			
			
		}
	}
	
	private class ManageProduction extends CyclicBehaviour {

		@Override
		public void action() {
			
			
		}
	}

}
