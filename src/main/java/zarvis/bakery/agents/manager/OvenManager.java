package zarvis.bakery.agents.manager;

import jade.core.Agent;
import zarvis.bakery.behaviors.ovenManager.SendProductsToOvenMachineBehavior;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class OvenManager extends Agent {

	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	
	public OvenManager(Bakery bakery) {
		this.bakery = bakery;
	}
	
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "OvenManager", "ovenManager-"+bakery.getGuid());
		addBehaviour(new SendProductsToOvenMachineBehavior(bakery));
	}

}
