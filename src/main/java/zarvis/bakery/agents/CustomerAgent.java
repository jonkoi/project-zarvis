package zarvis.bakery.agents;

import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.Behaviour;
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

	//Test
	private AID[] bakeriesAID;
	private String orderMsgString;
	
	
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
		fb.registerState(new PlaceOrder(), "PlaceOrder-state");
		fb.registerState(new WaitProposal(), "WaitProposal-state");
//		fb.registerState(new AcceptProposal(), "WaitProposal-state");
		
		//Transitions
		fb.registerDefaultTransition("WaitSetup-state", "GetBakeries-state");
		fb.registerTransition("GetBakeries-state", "NoBakeries-state", 0); // No bakeries found
		fb.registerTransition("GetBakeries-state", "CheckNextOrders-state", 1);
		fb.registerTransition("CheckNextOrders-state", "CheckTime-state", 0);
		fb.registerTransition("CheckNextOrders-state", "P1", 1);
		fb.registerTransition("CheckNextOrders-state", "CheckTime-state", 2);
		fb.registerTransition("CheckTime-state", "CheckTime-state", 0);
		fb.registerTransition("CheckTime-state", "PlaceOrder-state", 1);
		fb.registerDefaultTransition("PlaceOrder-state", "CheckNextOrders-state");
		

		//ContractNet Sequence
//		fb.registerDefaultTransition("PlaceOrder-state", "WaitProposal-state");
//		fb.registerTransition("WaitProposal-state", "WaitProposal-state", 0);
//		fb.registerTransition("WaitProposal-state", "CheckNextOrders-state", 1);
		
		
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
				System.out.println(bakeries.length);
				exitValue = 1;
			}
			
			orderMsg = new ACLMessage(ACLMessage.CFP);
			for (int i = 0; i < bakeries.length; ++i) {
				orderMsg.addReceiver(new AID(bakeries[i].getName().getLocalName(), AID.ISLOCALNAME));
	  		}
			
			orderMsg.setConversationId("order");			
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
				if (sortedOrderAggregation.size() == 0) {
					break;
				}
				entry = sortedOrderAggregation.entrySet().iterator().next();
			} while (value == entry.getValue());
			
			orderMsgString = msg.substring(0, msg.length() - 1);
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
			
			if (inWaitOrderAggregation.size() > 0) {
				String log = getAID().getLocalName() + " - " + "Day: " + daysElapsed + " "
						+ "Hours: " + totalHoursElapsed + " " + "Next Order: "
						+ inWaitOrderAggregation.values().toArray()[0];
				System.out.println(log);
				if (inWaitOrderAggregation.entrySet().iterator().next().getValue() <= totalHoursElapsed) {
					exitValue = 1;
				}
			} else {
				String log = getAID().getLocalName() + " - " + "Day: " + daysElapsed + " "
						+ "Hours: " + totalHoursElapsed;
				System.out.println(log);
			}
		}
		public int onEnd() {
			reset(millisLeft);
			return exitValue;
		}
	}
	
	private class PlaceOrder extends Behaviour{
		private MessageTemplate mt;
		private int replies = 0;
		private AID cheapestBakery;
		private double bestPrice;
		private static final int SEND_ORDER = 0;
		private static final int RECEIVE_PROPOSAL = 1;
		private static final int ACCEPT_LOWEST = 2;
		private static final int RECEIVE_ACKNOWLEDGEMENT = 3;
		private static final int DONE = 4;
		private int giveOrderState = PlaceOrder.SEND_ORDER;
		
		public void action(){
			bakeriesAID = new AID[bakeries.length];
			for (int i = 0; i < bakeries.length; ++i) {
				bakeriesAID[i] = bakeries[i].getName();
			}
			switch(giveOrderState){
				case PlaceOrder.SEND_ORDER:
					Util.sendMessage(myAgent, bakeriesAID, ACLMessage.CFP, orderMsgString, "Order");
					mt = MessageTemplate.MatchConversationId("Order");
					giveOrderState = PlaceOrder.RECEIVE_PROPOSAL;
					inWaitOrderAggregation.clear();
					break;
					
				case PlaceOrder.RECEIVE_PROPOSAL:
					ACLMessage reply = myAgent.receive(mt);
					
					if(reply == null){
						block();		
					}
					else if (reply.getPerformative() == ACLMessage.PROPOSE) {
						
						double price = Double.parseDouble(reply.getContent());
						if(cheapestBakery == null || price < bestPrice){
							cheapestBakery = reply.getSender();
							bestPrice = price;
						}
						replies++;
					}
					
					else if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
						
						replies++;
					}
				
					if (replies >= bakeriesAID.length){
						giveOrderState = PlaceOrder.ACCEPT_LOWEST;
					}
					break;
					
				case PlaceOrder.ACCEPT_LOWEST:
					Util.sendMessage(myAgent, cheapestBakery, ACLMessage.ACCEPT_PROPOSAL, orderMsgString, "Accepting");
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Accepting"),
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
					giveOrderState = PlaceOrder.RECEIVE_ACKNOWLEDGEMENT;
					break;
					
				case PlaceOrder.RECEIVE_ACKNOWLEDGEMENT:
					reply = myAgent.receive(mt);
					
					if(reply != null){
						giveOrderState = PlaceOrder.DONE;
					}
					else {
						block();
					}
					break;
					
				default:
					System.out.println("Error in give order sequence...");
			}
		}
		
		public boolean done(){
			return (giveOrderState == PlaceOrder.DONE);
		}
		
		public int onEnd() {
			giveOrderState = PlaceOrder.SEND_ORDER;
			return 0;
		}
	}
	
	private class PlaceOrder_old2 extends OneShotBehaviour{
		@Override
		public void action() {
			send(orderMsg);
			inWaitOrderAggregation.clear();
		}
	}
	private class WaitProposal extends OneShotBehaviour{
		private int exitValue = 0;
		public void action() {
			System.out.println("GOT PROPOSAL 1!");
			MessageTemplate template = 
					MessageTemplate.MatchConversationId("proposal");
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				System.out.println("GOT PROPOSAL!");
				System.out.println(msg.getContent());
				ACLMessage acceptMsg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				acceptMsg.addReceiver(new AID(bakeries[0].getName().getLocalName(), AID.ISLOCALNAME));
				acceptMsg.setConversationId("accept-proposal");
				myAgent.send(acceptMsg);
				exitValue = 1;
			} else {
				block();
			}
		}
		public int onEnd() {
			return exitValue;
		}
	}
	
}
