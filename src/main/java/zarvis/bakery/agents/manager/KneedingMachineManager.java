package zarvis.bakery.agents.manager;
import jade.core.Agent;
import zarvis.bakery.agents.TimeAgent;
import zarvis.bakery.behaviors.kneedingmachinemanager.SendProductsToKneedingMachineBehavior;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.utils.Util;

public class KneedingMachineManager extends Agent {
	private static final long serialVersionUID = 1L;
	//private Logger logger = LoggerFactory.getLogger(KneedingMachineManager.class);
	private Bakery bakery;

	public KneedingMachineManager(Bakery bakery) {
		this.bakery = bakery;
	}

	@Override
	protected void setup() {
		Util.registerInYellowPage(this, "KneedingMachineManager", "kneedingmachinemanager-" + bakery.getGuid());
//		addBehaviour(new ManageProductsBehavior());
//		Util.waitForSometime(2000);
//		addBehaviour(new KneedingMachinesAvailabilityBehavior(bakery));
		addBehaviour(new SendProductsToKneedingMachineBehavior(bakery));

	}
}
