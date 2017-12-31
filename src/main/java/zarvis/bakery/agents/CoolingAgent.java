package zarvis.bakery.agents;

import jade.core.Agent;
import zarvis.bakery.behaviors.CoolingAgent.CoolingProcessBehaviour;
import zarvis.bakery.behaviors.CoolingAgent.StatusOfCoolingAgentBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class CoolingAgent extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	public CoolingAgent(Bakery bakery) {
		this.bakery = bakery;
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "CoolingAgent", bakery.getGuid());
		addBehaviour(new StatusOfCoolingAgentBehaviour());
		addBehaviour(new CoolingProcessBehaviour());
	}
}
