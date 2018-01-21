package zarvis.bakery.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import zarvis.bakery.agents.TimeAgent.WaitSetup;
import zarvis.bakery.behaviors.bakery.ProcessOrderBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Product;
import zarvis.bakery.utils.ContentExtractor;
import zarvis.bakery.utils.Util;
import jade.proto.ContractNetResponder;

public class BakeryAgent extends TimeAgent {

	private Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
	private Bakery bakery;
	private List<Product> products;
	
	private MessageTemplate orderTemplate;
	
	//Process
	private AID sender;
	private ACLMessage processingMsg;
	private int sucess;
	private int failure;
	private boolean managingProduction;
	private boolean[] availableKnead;
	
	//Test
	private transient List<ContentExtractor> ordersList = new ArrayList<>();
	private transient Map<ContentExtractor, Integer> todaysOrderMap = new HashMap<>();
	private transient List<ContentExtractor> todaysOrder = new ArrayList<>();
	private int[] todayGoals = new int[Util.PRODUCTNAMES.size()];
	private int[] currentMadeAmounts = new int[Util.PRODUCTNAMES.size()];
	private int[] currentOrderAmounts = new int[Util.PRODUCTNAMES.size()];
	private ContentExtractor currentCE;
	Map<String,Integer> currentOrderProducts = new HashMap<>();
 
	public BakeryAgent(Bakery bakery, long globalStartTime) {
		super(globalStartTime);
		this.bakery = bakery;
		this.products = bakery.getProducts();
		this.sucess = 0;
		this.failure = 0;
		this.managingProduction = false;
//		this.availableKnead = new boolean[bakery.getKneading_machines().size()];
		this.availableKnead = new boolean[1];
		Arrays.fill(todayGoals, 0);
		Arrays.fill(currentMadeAmounts,0);
		Arrays.fill(currentOrderAmounts,0);
		Arrays.fill(availableKnead, true);
		if (this.products.size() > 0) {
			Collections.sort(this.products, new Comparator<Product>() {
				public int compare(final Product object1, final Product object2) {
					return object1.getGuid().compareTo(object2.getGuid());
				}
			});
		}
		
		for (Product p: this.products) {
			System.out.println(p.getGuid());
		}
	}

	@Override
	protected void setup() {

		Util.registerInYellowPage(this, "BakeryService", bakery.getGuid());
		
//		orderTemplate = MessageTemplate.and(
//		  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
//		  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		
//		FSMBehaviour fb = new FSMBehaviour();
//		fb.registerFirstState(new WaitSetup(), "WaitSetup-state");
//		fb.registerState(new ReceiveOrder(), "ReceiveOrder-state");
////		fb.registerState(new WaitAccept(), "WaitAccept-state");
//		
//		
//		fb.registerDefaultTransition("WaitSetup-state", "ReceiveOrder-state");
//		fb.registerDefaultTransition("WaitSetup-state", "ReceiveOrder-state");
		
//		SequentialBehaviour seq = new SequentialBehaviour();
		ParallelBehaviour pal = new ParallelBehaviour();
		
		pal.addSubBehaviour(new ReceiveOrder());
		pal.addSubBehaviour(new CheckTime());
		pal.addSubBehaviour(new ManageProduction());
		
		//ContractNetSequence
//		fb.registerTransition("ReceiveOrder-state", "ReceiveOrder-state", 0);
//		fb.registerTransition("ReceiveOrder-state", "WaitAccept-state", 1);
//		fb.registerTransition("WaitAccept-state", "WaitAccept-state", 0);
//		fb.registerTransition("WaitAccept-state", "ReceiveOrder-state", 1);
		
		addBehaviour(pal);
//		addBehaviour(new ProcessOrderBehaviour(bakery));
	}

	protected void takeDown() {
	}
	
	private class ReceiveOrder extends CyclicBehaviour{
		public void action(){
			String orders;
			String price;
			ContentExtractor currentOrder;
			
			ACLMessage data = myAgent.receive();
			if( data != null){
				UpdateTime();
    			if(data.getPerformative() == ACLMessage.CFP){
    				ACLMessage msg = data;
	    			orders = msg.getContent();
	    			System.out.println(orders);
	    			
	    			String[] splitOrders = orders.split(";");
	    			
	    			for (String o: splitOrders) {
	    				currentOrder = new ContentExtractor(o);
	    				if(checkOrders(currentOrder)){
							price = getPrice(currentOrder);
							System.out.print("price: ");
							System.out.println(price);
							Util.sendReply(myAgent, msg, ACLMessage.PROPOSE, price);						
						}
						else{
							Util.sendReply(myAgent, msg, ACLMessage.REJECT_PROPOSAL, orders);
						}		
	    			}
    			}
    			if(data.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
    				orders = data.getContent();
    				String[] splitOrders = orders.split(";");
    				for (String o: splitOrders) {
    					ContentExtractor extractor = new ContentExtractor(o);
        				ordersList.add(extractor);
        				ordersList.sort(Comparator.comparing(ContentExtractor::getDeliveryTime));
        				System.out.println(ordersList.size());
        				if(extractor.getDeliveryDay()==daysElapsed){
        					addBehaviour(new UpdateOrder(extractor));
        				}
        				Util.sendReply(myAgent, data, ACLMessage.CONFIRM, extractor.getDeliveryDateString());
    				}
  
//    				Orders order = jsonData.getOrder(extractor.getOrderGuid());
//    				order.setBakery(bakery);
//    				
//    				liteUtil.addOrder(order);
    			}
    			if (data.getPerformative() == ACLMessage.AGREE) {
    				// An order have been done
    				
    			}
    		} else {
    			UpdateTime();
    			block();
    		}
		}
    	
		public Map<String, String> extractOrder(String orders){
			String [] orderArray;
			Map<String,String> ordersMap = new HashMap<>();
			orders = orders.substring(1, orders.length()-1);
			orderArray = orders.split(",");
			for(String order : orderArray)
			{
			    String[] entry = order.split("="); 
			    ordersMap.put(entry[0].trim(), entry[1].trim());
			}
			return ordersMap;
			
		}
		
		public boolean checkOrders(ContentExtractor orderExtractor) {
			//Many criteria, return true for now
			//Invalid delivery date
			return true;
		}
		
		public String getPrice(ContentExtractor orderExtractor){
			
			double price = 0;
			for(Map.Entry<String, Integer> item: orderExtractor.getProducts().entrySet()){
				for(Product p: products) {
					if(p.getGuid().equals(item.getKey())) {
						price += item.getValue()*p.getSales_price();
					}
				}
			}
			price = Math.round(price * 100.0) / 100.0;
			return String.valueOf(price);
		}
	}
	
	private class CheckTime extends CyclicBehaviour {
		@Override
		public void action() {
			UpdateTime();
			String log = getAID().getLocalName() + " - " + "Day: " + daysElapsed + " " + "Hours: " + totalHoursElapsed;
			System.out.println(log);
			if (totalHoursElapsed%24 == 0) {
				addBehaviour(new setTodayOrder());
			} 
			
			// Add todaysOrder
			
			block(millisLeft);
		}
	}
	
	public class setTodayOrder extends OneShotBehaviour{

		@Override
		public void action() {
			UpdateTime();
			todaysOrder.clear();
			Arrays.fill(currentMadeAmounts, 0);
			Arrays.fill(todayGoals, 0);
			for (int i = 0; i < ordersList.size(); i++) {
				ContentExtractor ce = ordersList.get(i);
				if (ce.getDeliveryDay() == daysElapsed) {
					todaysOrder.add(ce);
					Map<String,Integer> productAmounts = ce.getProducts();
					for (int j = 0; j < productAmounts.size(); j ++) {
						todayGoals[j] += productAmounts.get(Util.PRODUCTNAMES.get(j));
					}
				}
			}
			
			
		}
		
	}
	
	public class UpdateOrder extends OneShotBehaviour {
		private ContentExtractor ce;
		public UpdateOrder(ContentExtractor ce) {
			this.ce = ce;
		}

		@Override
		public void action() {
			UpdateTime();
			todaysOrder.add(ce);
			Map<String,Integer> productAmounts = ce.getProducts();
			for (int j = 0; j < productAmounts.size(); j ++) {
				todayGoals[j] += productAmounts.get(Util.PRODUCTNAMES.get(j));
			}
			todaysOrder.sort(Comparator.comparing(ContentExtractor::getDeliveryTime));
		}
		
	}
	
	public class ManageProduction extends CyclicBehaviour{
		
		MessageTemplate confirmMt;
		
		public ManageProduction() {
			confirmMt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
					MessageTemplate.MatchConversationId("kneeding-product"));
		}

		@Override
		public void action() {
			if (todaysOrder.size() > 0 || currentCE != null) {
				if (currentCE == null) {
					
					
					currentCE = todaysOrder.get(0);
					todaysOrder.remove(0);
					
					currentOrderProducts = currentCE.getProducts();
					int i = 0;
					for (Map.Entry<String, Integer> entry : currentOrderProducts.entrySet()) {
						currentOrderAmounts[i] = entry.getValue();
						i++;
					}
				} 
				
				else if (currentCE != null) {
					int idx = 9999;
					for (int i = 0; i < currentOrderAmounts.length; i++) {
						if (currentOrderAmounts[i] > 0) {
							idx = i;
							break;
						}
					}
					
					
					if (idx == 9999) {
						currentCE = null;
					} else {
						for (int i = 0; i < availableKnead.length; i++) {
							if (availableKnead[i] == true) {
//								System.out.println("CHECK!" + i);
								if (currentOrderAmounts[idx] == 0) break;
								ACLMessage toKnead = new ACLMessage(ACLMessage.INFORM);
								toKnead.addReceiver(
										new AID(bakery.getKneading_machines().get(i).getGuid(),AID.ISLOCALNAME));
								toKnead.setConversationId("kneeding-product");
								toKnead.setContent(Integer.toString(idx));
								myAgent.send(toKnead);
								
								//Quite a nested hell
								while(true)
								{
									ACLMessage msg = myAgent.receive(confirmMt);
									if (msg!=null) {
//										System.out.println("CHECK!");
										availableKnead[i] = false;
										currentOrderAmounts[idx]--;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
//	ACLMessage toBakery = myAgent.receive(mt);
//	if (toBakery != null) {
//		if (toBakery.getConversationId() == "free-knead") {
//			AID senderAID = toBakery.getSender();
//			String guid = senderAID.getLocalName();
//			for (int i = 0; i < availableKnead.length; i++) {
//				if (bakery.getKneading_machines().get(i).getGuid() == guid) {
//					availableKnead[i] = true;
//				}
//			}
//		} 
//	}
}
