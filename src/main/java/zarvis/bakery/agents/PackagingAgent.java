package zarvis.bakery.agents;

import jade.core.Agent;
import zarvis.bakery.behaviors.PackagingAgent.PackagingProcessBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class PackagingAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	
	public PackagingAgent (Bakery bakery){
		this.bakery = bakery;
	}
	
	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "PackagingAgent", this.bakery.getGuid());
		addBehaviour(new PackagingProcessBehaviour(bakery));
	}
}
