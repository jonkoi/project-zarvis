package zarvis.bakery.agents.manager;

import jade.core.Agent;
import zarvis.bakery.behaviors.bakingManager.SendProductsToOvenMachineBehavior;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class BakingManager extends Agent {

	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	
	public BakingManager(Bakery bakery) {
		this.bakery = bakery;
	}
	
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "BakingManager", "bakingManager-"+bakery.getGuid());
		addBehaviour(new SendProductsToOvenMachineBehavior(bakery));
	}

}
