package zarvis.bakery.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TimeAgent extends Agent {
//	Time parameter
	protected long globalStartTime;
	protected final long MILLIS_PER_HOUR = 500;
	protected final long MILLIS_PER_DAY = 24*MILLIS_PER_HOUR;
	
	protected long millisLeft;
	protected long totalHoursElapsed;
	protected long daysElapsed;
	protected long hoursElapsed;
	
	public TimeAgent(long globalStartTime) {
		this.globalStartTime = globalStartTime;
	}
	
	protected void registerService(ServiceDescription sd) {
		// Register to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	//Delay so that every agent is initialized
		class WaitSetup extends Behaviour {
			private boolean started = false;
			
			@Override
			public void action() {
				if (System.currentTimeMillis() >= globalStartTime) {
					started = true;
				} else {
					block(globalStartTime - System.currentTimeMillis());
				}
			}

			@Override
			public boolean done() {
				return started;
			}
		}

}
