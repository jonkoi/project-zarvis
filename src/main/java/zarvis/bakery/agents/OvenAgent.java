package zarvis.bakery.agents;


import jade.core.Agent;
import zarvis.bakery.behaviors.OvenAgent.BakingProcessBehaviour;
import zarvis.bakery.behaviors.OvenAgent.StatusOfOvenAgentBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class OvenAgent extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	public OvenAgent(Bakery bakery) {
		this.bakery = bakery;
	}
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "OvenAgent", bakery.getGuid());
		addBehaviour(new StatusOfOvenAgentBehaviour());
		addBehaviour(new BakingProcessBehaviour());
	}

}
