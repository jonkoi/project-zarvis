package zarvis.bakery.agents;

import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.CyclicBehaviour;
import zarvis.bakery.behaviors.customer.RequestPerformerBehavior;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;
import jade.proto.ContractNetInitiator;

public class CustomerAgent extends TimeAgent {
	private static final long serialVersionUID = 1L;
	private boolean finish = false;
	//private Logger logger = LoggerFactory.getLogger(CustomerAgent.class);
	private Customer customer;
	private List<Order> orders;
//	private List<Bakeries> bakeries;
	private HashMap<String, Integer> orderAggregation = new HashMap<String, Integer>();
	private TreeMap<String, Integer> sortedOrderAggregation = new TreeMap<String, Integer>();
	private DFAgentDescription[] bakeries;
	//Book keeping
	private TreeMap<String, Integer> inWaitOrderAggregation = new TreeMap<String,Integer>();
	private TreeMap<String, Integer> inProcessOrderAggregation = new TreeMap<String,Integer>();
	private TreeMap<String, Integer> finishedOrderAggregation = new TreeMap<String, Integer>();
	
	private ACLMessage orderMsg;

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
//		System.out.println("My local-name is "+getAID().getLocalName());
		Util.registerInYellowPage(this, "Customer", customer.getGuid());
		//TreeMap<String, Integer> aggregatedOrders = Util.sortMapByValue(orderAggregation);
		
		
		
		// Koi: My take on the FSMBehavior
		// FSM building
		FSMBehaviour fb = new FSMBehaviour();
		fb.registerFirstState(new WaitSetup(), "WaitSetup-state");
		fb.registerState(new GetBakeries(), "GetBakeries-state");
		fb.registerState(new CheckNextOrders(), "CheckNextOrders-state");
		fb.registerState(new CheckTime(this, millisLeft), "CheckTime-state");
		fb.registerState(new PlaceOrder(this, orderMsg), "PlaceOrder-state");
		
		fb.registerState(new DummyReceive(),"dum");
		
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
//			System.out.println("GS");
			bakeries = Util.searchInYellowPage(myAgent, "BakeryService", null);
			if (bakeries.length > 0) {
				exitValue = 1;
			}
			
			orderMsg = new ACLMessage(ACLMessage.CFP);
			for (int i = 0; i < bakeries.length; ++i) {
//				System.out.println("Hello, hello" + bakeries[i].getName());
				orderMsg.addReceiver(new AID(bakeries[i].getName().getLocalName(), AID.ISLOCALNAME));
	  		}
			orderMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			
			
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	// Get the next order(s) to be auctioned/ordered | 0: No more orders, 1: Error, previous order has not been processed, 2: Next order(s) had
	private class CheckNextOrders extends OneShotBehaviour {
		private int exitValue = 0;
		
		public void onStart() {
			System.out.println("Hello");
		}

		@Override
		public void action() {
			System.out.println("SIZE: " + sortedOrderAggregation.size());
			exitValue = 0;
			
			if (sortedOrderAggregation.isEmpty()) {
				System.out.println("Shit");
				exitValue = 0;
				return;
			}
			if (inWaitOrderAggregation.isEmpty() == false) {
				
				exitValue = 1;
				return;
			}
//			System.out.println("CS");
			Map.Entry<String,Integer> entry = sortedOrderAggregation.entrySet().iterator().next();
			String key;
			int value = entry.getValue();
			
			String msg = "";
			do {
				key = entry.getKey();
				inWaitOrderAggregation.put(key, value);
				
				msg += Util.buildOrderMessage(key, orders, getAID().getLocalName());
				
				sortedOrderAggregation.remove(key);
				entry = sortedOrderAggregation.entrySet().iterator().next();
			} while (value == entry.getValue());
			
			orderMsg.setContent(msg.substring(0, msg.length() - 1));
			
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
//			System.out.println(exitValue);
			reset(millisLeft);
			return exitValue;
		}
	}
	
	private class PlaceOrder extends ContractNetInitiator{
		
		private ACLMessage msg;
		
		public PlaceOrder(Agent a, ACLMessage cfp) {
			super(null, null);
			msg = cfp;
		}
		public void onStart() {
			System.out.println("in placeorder");
			inWaitOrderAggregation.clear();
		}
		@Override
		protected Vector prepareCfps(ACLMessage cfp) {
			cfp = new ACLMessage(ACLMessage.CFP);
//			cfp = (ACLMessage) orderMsg.clone();
			for (int i = 0; i < bakeries.length; ++i) {
				cfp.addReceiver(new AID(bakeries[i].getName().getLocalName(), AID.ISLOCALNAME));
	  		}
			cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
			cfp.setContent(orderMsg.getContent());
			Vector v = new Vector();
			v.add(cfp); 
			return v;
		}
//		protected void handlePropose(ACLMessage propose, Vector v) {
//			System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
//			ACLMessage reply = propose.createReply();
//			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//			v.add(reply);
//		}
//		@Override
//		public int onEnd() {
////			reset(orderMsg);
//			System.out.println("END!");
//			return 0;
//		}
	}
	

	
	private class DummyReceive extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt =
			  		MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(msg.getContent());
			}
			
		}
	}
	
	private class DummyPlaceOrder extends OneShotBehaviour {

		public void action() {
			System.out.println("Sending");
			inWaitOrderAggregation.clear();
			send(orderMsg);
			
		}
		
	}
	
	
}
