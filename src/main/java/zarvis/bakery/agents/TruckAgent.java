package zarvis.bakery.agents;

import jade.core.Agent;
import zarvis.bakery.behaviors.truckAgent.TruckAgentAvailabilityBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class TruckAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	public TruckAgent(Bakery bakery) {
		this.bakery = bakery;
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "TruckAgent", this.bakery.getGuid());
		addBehaviour(new TruckAgentAvailabilityBehaviour());
	}
}