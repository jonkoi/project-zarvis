package zarvis.bakery.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import zarvis.bakery.utils.Util;

public class TimeAgent extends Agent {
//	Time parameter
	protected long globalStartTime;
	protected final long MILLIS_PER_MIN = Util.MILLIS_PER_MIN;
	protected final long MILLIS_PER_HOUR = 60*MILLIS_PER_MIN;
	protected final long MILLIS_PER_DAY = 24*MILLIS_PER_HOUR;
	
	protected long millisLeft;
	protected long totalHoursElapsed;
	protected long daysElapsed;
	protected long hoursElapsed;
	
	public TimeAgent(long globalStartTime) {
		this.globalStartTime = globalStartTime;
	}
	
	protected class WaitSetup extends Behaviour {
		
		private boolean started = false;
		
		@Override
		public void action() {
//			System.out.println("WS");
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
	
	
	
	protected void UpdateTime() {
		long operatingDuration = System.currentTimeMillis() - globalStartTime;
		totalHoursElapsed = (long) Math.floorDiv(operatingDuration , MILLIS_PER_HOUR);
		daysElapsed = (long) Math.floorDiv(operatingDuration , MILLIS_PER_DAY) + 1;
		hoursElapsed = (long) Math.floorDiv(operatingDuration - daysElapsed * MILLIS_PER_DAY, MILLIS_PER_HOUR);
		millisLeft = MILLIS_PER_HOUR - (operatingDuration - totalHoursElapsed * MILLIS_PER_HOUR);
//		System.out.println(millisLeft);
	}

}
