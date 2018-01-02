package zarvis.bakery.agents;

import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import zarvis.bakery.behaviors.customer.RequestPerformerBehavior;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;

public class CustomerAgent extends TimeAgent {
	private static final long serialVersionUID = 1L;
	private boolean finish = false;
	//private Logger logger = LoggerFactory.getLogger(CustomerAgent.class);
	private Customer customer;
	private List<Order> orders;
	private HashMap<String, Integer> orderAggregation = new HashMap<String, Integer>();
	private TreeMap<String, Integer> sortedOrderAggregation = new TreeMap<String, Integer>();
	private DFAgentDescription[] bakeries;
	//Book keeping
	private TreeMap<String, Integer> inWaitOrderAggregation = new TreeMap<String,Integer>();
	private TreeMap<String, Integer> inProcessOrderAggregation = new TreeMap<String,Integer>();
	private TreeMap<String, Integer> finishedOrderAggregation = new TreeMap<String, Integer>();

	public CustomerAgent(Customer customer, long globalStartTime) {
		super(globalStartTime);
		this.customer = customer;
		this.orders = Util.getWrapper().getOrderByIdCustomer(customer.getGuid());
		for (Order order : orders) {
			if (order.getOrder_date().getDay() == 1)
				this.orderAggregation.put(order.getGuid(), order.getOrder_date().getHour());
			else {
				int time = order.getOrder_date().getHour() + (order.getOrder_date().getDay() - 1) * 24;
				this.orderAggregation.put(order.getGuid(), time);
			}
		}
		this.sortedOrderAggregation = Util.sortMapByValue(orderAggregation);
	}

	@Override
	protected void setup() {
		
		Util.registerInYellowPage(this, "Customer", customer.getGuid());
		//TreeMap<String, Integer> aggregatedOrders = Util.sortMapByValue(orderAggregation);
		
		// Koi: My take on the FSMBehavior
		// FSM building
		FSMBehaviour fb = new FSMBehaviour();
		fb.registerFirstState(new WaitSetup(), "WaitSetup-state");
		fb.registerState(new GetBakeries(), "GetBakeries-state");
		fb.registerState(new CheckNextOrders(), "CheckNextOrders-state");
		fb.registerState(new CheckTime(this, millisLeft), "CheckTime-state");
		fb.registerState(new PlaceOrder(), "PlaceOrder-state");
		
		//Transitions
		fb.registerDefaultTransition("WaitSetup-state", "GetBakeries-state");
		fb.registerTransition("GetBakeries-state", "NoBakeries-state", 0); // No bakeries found
		fb.registerTransition("GetBakeries-state", "CheckNextOrders-state", 1);
		fb.registerTransition("CheckNextOrders-state", "P0", 0);
		fb.registerTransition("CheckNextOrders-state", "P1", 1);
		fb.registerTransition("CheckNextOrders-state", "CheckTime-state", 2);
		fb.registerTransition("CheckTime-state", "CheckTime-state", 0);
		fb.registerTransition("CheckTime-state", "PlaceOrder-state", 1);
		fb.registerDefaultTransition("PlaceOrder-state", "CheckNextOrders-state");
		
		addBehaviour(fb);
//		addBehaviour(new RequestPerformerBehavior(customer, Util.sortMapByValue(orderAggregation)));
		finish = true;
	}

	protected void takeDown() {
		Util.deregisterInYellowPage(this);
		finish = true;
		System.out.println("Agent " + getAID().getName() + " terminating.");
	}
	
	public boolean isFinished(){
		return finish;
	}
	
	// Behaviours: To be modularized
	// Get the bakery list | 0: No bakeries found, 1: Bakeries, move to check next order
	private class GetBakeries extends OneShotBehaviour{
		private int exitValue = 0;
		
		

		@Override
		public void action() {
			for (Map.Entry<String, Integer> entry : sortedOrderAggregation.entrySet()) {
				System.out.println(entry.getKey() + " " + entry.getValue());
			}
			System.out.println("GS");
			bakeries = Util.searchInYellowPage(myAgent, "BakeryService", null);
			if (bakeries.length > 0) {
				exitValue = 1;
			}
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	// Get the next order(s) to be auctioned/ordered | 0: No more orders, 1: Error, previous order has not been processed, 2: Next order(s) had
	private class CheckNextOrders extends OneShotBehaviour {
		private int exitValue = 0;

		@Override
		public void action() {
			
			exitValue = 0;
			
			if (sortedOrderAggregation.isEmpty()) {
				exitValue = 0;
				return;
			}
			if (inWaitOrderAggregation.isEmpty() == false) {
				exitValue = 1;
				return;
			}
			System.out.println("CS");
			Map.Entry<String,Integer> entry = sortedOrderAggregation.entrySet().iterator().next();
			String key;
			int value = entry.getValue();
			do {
				key = entry.getKey();
				inWaitOrderAggregation.put(key, value);
				sortedOrderAggregation.remove(key);
				entry = sortedOrderAggregation.entrySet().iterator().next();
			} while (value == entry.getValue());
			exitValue = 2;
			
			UpdateTime();
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	private class CheckTime extends WakerBehaviour {
		private int exitValue = 0;
		
		public CheckTime(Agent a, long timeout) {
			super(a, timeout);
		}
		public void onWake() {
			UpdateTime();
			exitValue = 0;
			System.out.println("Hours: " + totalHoursElapsed);
			System.out.println("Next Order: "+ inWaitOrderAggregation.values().toArray()[0]);
			if (inWaitOrderAggregation.entrySet().iterator().next().getValue() <= totalHoursElapsed) {
				exitValue = 1;
			}
		}
		public int onEnd() {
			System.out.println(exitValue);
			reset(millisLeft);
			return exitValue;
		}
	}
	
	private class PlaceOrder extends OneShotBehaviour{
		
		public void action() {
			// pass the string to the bakeryAgent wait for the confirmation.
			//ToDo handle
			System.out.println("placing customer order---------\n");
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.setContent("Hello");
			cfp.setConversationId("order_proposal");
			cfp.addReceiver(new AID("bakery-001",AID.ISLOCALNAME));
			myAgent.send(cfp);
			System.out.println("SENT!");
			
			inWaitOrderAggregation.clear();
			
			
//			cfp.setReplyWith("cfp"+System.currentTimeMillis());
//			send(cfp);
//			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("order_proposal-reply"),
//					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
//			addBehaviour(new AcknowledgeOrder());
			
		}
	}
	
	private void UpdateTime() {
		long operatingDuration = System.currentTimeMillis() - globalStartTime;
		totalHoursElapsed = (long) Math.floorDiv(operatingDuration , MILLIS_PER_HOUR);
		daysElapsed = (long) Math.floorDiv(operatingDuration , MILLIS_PER_DAY);
		hoursElapsed = (long) Math.floorDiv(operatingDuration - daysElapsed * MILLIS_PER_DAY, MILLIS_PER_HOUR);
		millisLeft = MILLIS_PER_HOUR - (operatingDuration - totalHoursElapsed * MILLIS_PER_HOUR);
		System.out.println(millisLeft);
	}
}
