package zarvis.bakery.agents;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import zarvis.bakery.behaviors.kneedingmachine.CurrentStatusBehaviour;
import zarvis.bakery.behaviors.kneedingmachine.DoughKneedingBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;


public class KneedingMachineAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	private Bakery bakery;

	public KneedingMachineAgent(Bakery bakery) {
		this.bakery = bakery;
	}

	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineAgent", this.bakery.getGuid());
		
		addBehaviour(new CurrentStatusBehaviour());
		addBehaviour(new DoughKneedingBehaviour());
	}
	
}


