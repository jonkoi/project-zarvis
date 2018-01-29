package zarvis.bakery.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import zarvis.bakery.utils.ValueComparatorAscending;
import zarvis.bakery.models.*;

public class Util {
	public static final List<String> PRODUCTNAMES = Arrays.asList("Bagel", "Baguette", "Berliner", "Bread", "Brezel", "Bun", "Ciabatta",
			"Cookie", "Croissant", "Donut", "Muffin","Multigrain Bread");
	public static final long MILLIS_PER_MIN = 2;

	public static BakeryJsonWrapper getWrapper() {
		final String FILENAME = "src/main/config/random-scenario.json";
		//final String FILENAME = "/home/aniruddha/Downloads/WS2017/MultiAgent/project-zarvis/src/main/config/random-scenario.json";
		//final String FILENAME = "/home/yassine/WS17_yboukn2s/project-zarvis/src/main/config/random-scenario.json";
		BakeryJsonWrapper jsonwrapper = null;
		BufferedReader reader = null;
		try {
			// read json file and convert them to objects
			reader = new BufferedReader(new FileReader(FILENAME));
			jsonwrapper = new Gson().fromJson(reader, BakeryJsonWrapper.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				if(reader != null)
					reader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}		
			
		}
		return jsonwrapper;
	}

	public static DFAgentDescription[] searchInYellowPage(Agent agent, String type, String name) {
		DFAgentDescription agentDescription = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();

		serviceDescription.setType(type);
		if (name != null) {
			serviceDescription.setName(name);
		}
		agentDescription.addServices(serviceDescription);

		try {
			DFAgentDescription[] searchResult = DFService.search(agent, agentDescription);
			return (searchResult.length != 0) ? searchResult : null;
		} catch (FIPAException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static boolean registerInYellowPage(Agent agent, String type, String name) {
		// Create agent description and set AID
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(agent.getAID());

		// Create service description and set type and bakery name
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(type);
		serviceDescription.setName(name);

		// add the service description to this agent
		agentDescription.addServices(serviceDescription);
		try {
			DFService.register(agent, agentDescription);
			return true;
		} catch (FIPAException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void deregisterInYellowPage(Agent agent) {
		try {
			DFService.deregister(agent);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}


	public static void sendReply(Agent agent, ACLMessage message, int performative, String content,
			String conversationId) {
		ACLMessage reply = message.createReply();
		reply.setPerformative(performative);
		reply.setContent(content);
		reply.setConversationId(conversationId);
		agent.send(reply);
	}
	
	public static void sendReply(Agent agent, ACLMessage message, int performative, String content) {
        ACLMessage reply = message.createReply();
        reply.setPerformative(performative);
        reply.setContent(content);
        agent.send(reply);
    }

	public static void sendMessage(Agent agent, AID receiver, int performative, String content, String conversationId) {
		ACLMessage message = new ACLMessage(performative);
		message.addReceiver(receiver);
		message.setConversationId(conversationId);
		message.setContent(content);
		agent.send(message);
		// waitForSometime(100);
	}
	
	public static void sendMessage(Agent agent, AID [] receiver, int performative, String content, String conversationId) {
        ACLMessage message = new ACLMessage(performative);
        for(int i = 0;i<receiver.length;i++){
			message.addReceiver(receiver[i]);
		}
        message.setConversationId(conversationId);
        message.setContent(content);
        agent.send(message);
    }

	public static void waitForSometime(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	public static TreeMap<String, Integer> sortMapByValue(HashMap<String, Integer> map) {
		Comparator<String> comparator = new ValueComparatorAscending(map);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}
	
	public static String buildOrderMessage(String guid, List<Order> orders, String agentGuid) {
		String msg = guid + ",";
		for (Order o: orders) {
			if (o.getGuid() == guid) {
				int orderDay = o.getOrder_date().getDay();
				int orderHour = o.getOrder_date().getHour();
				
				int delDay = o.getDelivery_date().getDay();
				int delHour = o.getDelivery_date().getHour();
				
				msg = msg + agentGuid + "," + orderDay + "." + orderHour + "," + delDay + "." + delHour + ",";
				Map<String, Integer> products = o.getProducts();
				for (String p : PRODUCTNAMES) {
					if (products.get(p) == null) {
						msg = msg + "0.";
					} else {
						msg = msg + products.get(p) + ".";
					}
				}
				msg = msg.substring(0, msg.length() - 1) + ";";
				break;
			}
		}
		return msg;
	}
	
	public static NeiGraph InitializeGraph() {
		
		BakeryJsonWrapper wrapper = getWrapper();
		StreetNetwork net = wrapper.getStreet_network();
		
		NeiGraph neig = new NeiGraph();
		for (Node n : net.getNodes()) {
			List<Node> m = new ArrayList<>();
			for (Link l : net.getLinks()) {
				if(l.getSource().equals(n.getGuid())) {
					String target = l.getTarget();
					for (Node n1 : net.getNodes()) {
						if (n1.getCompany().equals(target)) {
							m.add(n1);
							break;
						}
					}
				}
			}
			neig.AddEntry(n, m);
		}
		
		return neig;
		
	}

}
