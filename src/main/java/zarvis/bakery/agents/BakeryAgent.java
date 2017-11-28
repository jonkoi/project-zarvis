package zarvis.bakery.agents;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import zarvis.bakery.behaviors.bakery.ProcessOrderBehaviour;
import zarvis.bakery.models.Bakery;

public class BakeryAgent extends Agent {
	private Bakery bakery;
	
	public BakeryAgent(Bakery bakery){
		this.bakery= bakery;
	}

	@Override
	protected void setup() {
		System.out.println("....... Bakery "+this.getAID().getName());

		// Create agent description and set AID 
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(getAID());

		// Create service description and set type and bakery name
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("bakery");
		serviceDescription.setName(bakery.getName());

		// add the service description to this agent
		agentDescription.addServices(serviceDescription);

		// Now add this agent description to yellow pages, so that other agents can identify this agent
		try {
			DFService.register(this, agentDescription);
			System.out.println("Bakery agent is added to yellow pages");
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new ProcessOrderBehaviour(bakery));
	}
	protected void takeDown() {}
}

