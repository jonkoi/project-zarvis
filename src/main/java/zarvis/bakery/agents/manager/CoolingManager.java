package zarvis.bakery.agents.manager;

import jade.core.Agent;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class CoolingManager extends Agent {
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	
	public CoolingManager(Bakery bakery) {
		this.bakery = bakery;
	}
	
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "CoolingManager", "coolingManager-"+bakery.getGuid());
		//addBehaviour(new SendProductsToOvenMachineBehavior(bakery));
	}

}