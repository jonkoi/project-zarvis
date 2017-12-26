package zarvis.bakery.agents;


import jade.core.Agent;
import zarvis.bakery.behaviors.OvenAgent.StatusOfOvenAgentBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Oven;
import zarvis.bakery.utils.Util;

public class OvenAgent extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	public OvenAgent(Oven oven, Bakery bakery) {
		this.bakery = bakery;
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "OvenAgent", bakery.getGuid());
		addBehaviour(new StatusOfOvenAgentBehaviour());
	}

}
