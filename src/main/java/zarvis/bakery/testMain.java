package zarvis.bakery;
import java.util.ArrayList;
import java.util.List;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import zarvis.bakery.agents.BakeryAgent;
import zarvis.bakery.agents.CoolingAgent;
import zarvis.bakery.agents.CustomerAgent;
import zarvis.bakery.agents.KneedingMachineAgent;
import zarvis.bakery.agents.OvenAgent;
import zarvis.bakery.agents.PreparationTableAgent;
import zarvis.bakery.agents.manager.OvenManager;
import zarvis.bakery.agents.manager.PreparationTableManager;
import zarvis.bakery.agents.manager.CoolingManager;
import zarvis.bakery.agents.manager.KneedingMachineManager;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.DoughPrepTable;
import zarvis.bakery.models.KneedingMachine;
import zarvis.bakery.models.Oven;
import zarvis.bakery.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class testMain {
	public static void main(String[] args) {
		
		try {
			//Delay for 5 seconds to wait for all to initialize
			
			Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
			
			List<CustomerAgent> customerAgentsList = new ArrayList<>();
			Runtime runtime = Runtime.instance();
			runtime.setCloseVM(true);

			Properties properties = new ExtendedProperties();
			properties.setProperty(Profile.GUI, "true");

			ProfileImpl profileImpl = new ProfileImpl(properties);

			AgentContainer mainContainer = runtime.createMainContainer(profileImpl);
			mainContainer.acceptNewAgent("resp", new RespAgent()).start();
			mainContainer.acceptNewAgent("init", new InitAgent()).start();;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
