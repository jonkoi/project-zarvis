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
import zarvis.bakery.agents.CustomerAgent;
import zarvis.bakery.agents.KneedingMachineAgent;
import zarvis.bakery.agents.manager.BakingManager;
import zarvis.bakery.agents.manager.KneedingMachineManager;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.BakeryJsonWrapper;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.KneedingMachine;
import zarvis.bakery.models.Oven;
import zarvis.bakery.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainContainer {
	public static void main(String[] args) {
		try {
			Logger logger = LoggerFactory.getLogger(BakeryAgent.class);

			List<CustomerAgent> customerAgentsList = new ArrayList<>();
			Runtime runtime = Runtime.instance();
			runtime.setCloseVM(true);

			Properties properties = new ExtendedProperties();
			properties.setProperty(Profile.GUI, "true");

			ProfileImpl profileImpl = new ProfileImpl(properties);

			AgentContainer mainContainer = runtime.createMainContainer(profileImpl);

			BakeryJsonWrapper wrapper = Util.getWrapper();

			// create multiple bakery agents
			for (Bakery bakery : wrapper.getBakeries()) {
				mainContainer.acceptNewAgent(bakery.getGuid(), new BakeryAgent(bakery)).start();
				for (KneedingMachine kneedingMachine : bakery.getKneading_machines()) {
					mainContainer.acceptNewAgent(kneedingMachine.getGuid() + "-" + bakery.getGuid(),
							new KneedingMachineAgent(bakery)).start();
				}
				mainContainer.acceptNewAgent("kneeding_machine_manager-" + bakery.getGuid(),new KneedingMachineManager(bakery)).start();
				
				for (Oven ovenMachine : bakery.getOvens()) {
					mainContainer.acceptNewAgent(ovenMachine.getGuid() + "-" + bakery.getGuid(),
							new KneedingMachineAgent(bakery)).start();
				}
				mainContainer.acceptNewAgent("bakingManager-" + bakery.getGuid(),new BakingManager(bakery)).start();

				break;
			}

			// create multiple customer agents
			for (Customer customer : wrapper.getCustomers().subList(0, 2)) {
				CustomerAgent agent =  new CustomerAgent(customer);
				customerAgentsList.add(agent);
				mainContainer.acceptNewAgent(customer.getGuid(), agent).start();
			}
			
			
			while (true) {
				Thread.sleep(30000);
				boolean finished = true;
				for (CustomerAgent customerAgent : customerAgentsList) {
					if (!customerAgent.isFinished()) {
						finished = false;
					}
				}
				if (finished) {
					logger.info("All customers are done, exit the platform...");
					mainContainer.kill();
					break;
				}
			}
			
			
			//
			// mainContainer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}