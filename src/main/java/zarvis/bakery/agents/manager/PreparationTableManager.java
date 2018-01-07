package zarvis.bakery.agents.manager;

import jade.core.Agent;
import zarvis.bakery.behaviors.PreparationTableManager.SendProductsToPreparationTableBehavior;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class PreparationTableManager extends Agent {
	
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	
	public PreparationTableManager(Bakery bakery) {
		this.bakery= bakery;
	}

	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "preparationTableManager", "preparationTableManager-"+bakery.getGuid());
		addBehaviour(new SendProductsToPreparationTableBehavior(bakery));
		super.setup();
	}

}
