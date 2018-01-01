package zarvis.bakery.agents;

import jade.core.Agent;
import zarvis.bakery.behaviors.PreparationTable.ProcessPreparationTableBehavior;
import zarvis.bakery.behaviors.PreparationTable.StatusOfPreparationTableAgentBehavior;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class PreparationTableAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	private Bakery bakery;
	public PreparationTableAgent(Bakery bakery) {
		this.bakery= bakery;
	}

	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "PreparationTableAgent", bakery.getGuid());
		addBehaviour(new StatusOfPreparationTableAgentBehavior());
		addBehaviour(new ProcessPreparationTableBehavior());
	}

}
