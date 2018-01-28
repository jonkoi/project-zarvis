package zarvis.bakery.agents;

import java.util.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import zarvis.bakery.messages.CustomMessage;
import zarvis.bakery.models.Customer;
import zarvis.bakery.models.Order;
import zarvis.bakery.utils.Util;

public class CustomerAgent extends TimeAgent {
	private static final long serialVersionUID = 1L;
	private boolean finish = false;
	private Customer customer;
	private List<Order> orders;
	private HashMap<String, Integer> orderAggregation = new HashMap<String, Integer>();
	private TreeMap<String, Integer> sortedOrderAggregation = new TreeMap<String, Integer>();
	private DFAgentDescription[] bakeries;
	private TreeMap<String, Integer> inWaitOrderAggregation = new TreeMap<String,Integer>();
	@SuppressWarnings("unused")
	private List<String>inProcessOrderAggregation = new ArrayList<>();
	private String preemptFailOrder;
	@SuppressWarnings("unused")
	private TreeMap<String, Boolean> finishedOrderAggregation = new TreeMap<String, Boolean>();
	
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
		Util.registerInYellowPage(this, "Customer", customer.getGuid());
		
		
		
		// Koi: My take on the FSMBehavior
		// FSM building
		ParallelBehaviour pal = new ParallelBehaviour();
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
		fb.registerTransition("CheckNextOrders-state", "CheckTime-state", 0);
		fb.registerTransition("CheckNextOrders-state", "P1", 1);
		fb.registerTransition("CheckNextOrders-state", "CheckTime-state", 2);
		fb.registerTransition("CheckTime-state", "CheckTime-state", 0);
		fb.registerTransition("CheckTime-state", "PlaceOrder-state", 1);
		fb.registerDefaultTransition("PlaceOrder-state", "CheckNextOrders-state");
		
		pal.addSubBehaviour(fb);
		pal.addSubBehaviour(new OrderFinishListener());
		
		addBehaviour(pal);
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
		private static final long serialVersionUID = 1L;
		private int exitValue = 0;

		@Override
		public void action() {
//			for (Map.Entry<String, Integer> entry : sortedOrderAggregation.entrySet()) {
//				System.out.println(entry.getKey() + " " + entry.getValue());
//			}

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
		private static final long serialVersionUID = 1L;
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
			Map.Entry<String,Integer> entry = sortedOrderAggregation.entrySet().iterator().next();
			String key;
			int value = entry.getValue();
			
			String msg = "";
			
			key = entry.getKey();
			inWaitOrderAggregation.put(key, value);
			
			msg = Util.buildOrderMessage(key, orders, getAID().getLocalName());
			sortedOrderAggregation.remove(key);			
			
			//Below are multi order in one message
//			do {
//				key = entry.getKey();
//				inWaitOrderAggregation.put(key, value);
//				
//				msg += Util.buildOrderMessage(key, orders, getAID().getLocalName());
//				
//				sortedOrderAggregation.remove(key);
//				if (sortedOrderAggregation.size() == 0) {
//					break;
//				}
//				entry = sortedOrderAggregation.entrySet().iterator().next();
//			} while (value == entry.getValue());
			
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
		private static final long serialVersionUID = 1L;
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
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		private int replies = 0;
		private AID cheapestBakery;
		private double bestPrice;
		private static final int SEND_ORDER = 0;
		private static final int RECEIVE_PROPOSAL = 1;
		private static final int ACCEPT_LOWEST = 2;
		private static final int RECEIVE_ACKNOWLEDGEMENT = 3;
		private static final int DONE = 4;
		private static final int FAIL_PREEMP = 5;
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
					bestPrice = 0;
					cheapestBakery = null;
					replies = 0;
					preemptFailOrder = "";
					break;
					
				case PlaceOrder.RECEIVE_PROPOSAL:
					ACLMessage reply = myAgent.receive(mt);
					
					if(reply == null){
						block();		
					}
					else if (reply.getPerformative() == ACLMessage.PROPOSE) {
//						System.out.println("Message returned");
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
						if (cheapestBakery!=null) {
							giveOrderState = PlaceOrder.ACCEPT_LOWEST;
							for (Map.Entry<String, Integer> o : inWaitOrderAggregation.entrySet()) {
								inProcessOrderAggregation.add(o.getKey());
							}
							inWaitOrderAggregation.clear();
						} else {
							//Order fails preemptively
							giveOrderState = PlaceOrder.FAIL_PREEMP;
							for (Map.Entry<String, Integer> o : inWaitOrderAggregation.entrySet()) {
								preemptFailOrder += inProcessOrderAggregation.add(o.getKey());
							}
							inWaitOrderAggregation.clear();
						}
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
				case PlaceOrder.FAIL_PREEMP:
					for (Order o: orders) {
						if (o.getGuid().equals(preemptFailOrder)) {
							finishedOrderAggregation.put(preemptFailOrder, false);
						}
					}
					giveOrderState = PlaceOrder.DONE;
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
			//Push order into wait
			return 0;
		}
	}
	
	private class OrderFinishListener extends CyclicBehaviour {
		
		private MessageTemplate orderFinishTemplate =
				MessageTemplate.and(MessageTemplate.MatchPerformative(CustomMessage.FINISH_ORDER),
						MessageTemplate.MatchConversationId("to-customer-finish-order"));
		
		@Override
		public void action() {
			ACLMessage orderFinishMsg = myAgent.receive(orderFinishTemplate);
			if (orderFinishMsg != null) {
				System.out.print(orderFinishMsg.getContent() + "!!!!!!!!!!!!!!!!!!!!!!");
				String[] content = orderFinishMsg.getContent().split(",");
				String orderBack = content[0];
				
				for (Order o: orders) {
					if (o.getGuid().equals(orderBack)) {
						long evaluatedTime = (o.getDelivery_date().getDay()-1)*24 + o.getDelivery_date().getHour() - totalHoursElapsed;
						if (evaluatedTime < 0) {
							System.out.println("ORDER BACK!: " + orderFinishMsg.getContent() + " false");
							finishedOrderAggregation.put(orderBack, false);
						} else {
							System.out.println("ORDER BACK!: " + orderFinishMsg.getContent() + " true");
							finishedOrderAggregation.put(orderBack, true);
						}
					}
				}
				
			} else {
				block();
			}
		}
		
	}
	
}
