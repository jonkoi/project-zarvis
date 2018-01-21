package zarvis.bakery.agents;

import zarvis.bakery.models.Bakery;

public class BakeryManagerAgent extends TimeAgent {
	
	private Bakery bakery;

	public BakeryManagerAgent(Bakery bakery, long globalStartTime) {
		super(globalStartTime);
		this.bakery = bakery;
	}

}
