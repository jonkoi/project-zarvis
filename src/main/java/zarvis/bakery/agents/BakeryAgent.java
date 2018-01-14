package zarvis.bakery.agents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
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
import zarvis.bakery.utils.Util;
import jade.proto.ContractNetResponder;

public class BakeryAgent extends TimeAgent {

	private Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
	private Bakery bakery;
	private List<Product> products;
	
	private MessageTemplate orderTemplate;

	public BakeryAgent(Bakery bakery, long globalStartTime) {
		super(globalStartTime);
		this.bakery = bakery;
		this.products = bakery.getProducts();
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
		
		orderTemplate = MessageTemplate.and(
		  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
		  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		
		FSMBehaviour fb = new FSMBehaviour();
		fb.registerFirstState(new WaitSetup(), "WaitSetup-state");
		fb.registerState(new ContractResponse(this, orderTemplate), "ContractResponse-state");
		
		fb.registerDefaultTransition("WaitSetup-state", "ContractResponse-state");
		fb.registerDefaultTransition("ContractResponse-state", "ContractResponse-state");
		
		addBehaviour(fb);
//		addBehaviour(new ProcessOrderBehaviour(bakery));
	}

	protected void takeDown() {
	}
	
	private class ContractResponse extends ContractNetResponder{

		public ContractResponse(Agent a, MessageTemplate mt) {
			super(a, mt);
		}
		
		protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
			System.out.print(cfp.getContent());
			ACLMessage reply = cfp.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent("Hey");
			System.out.print("Hey");
			return reply;
		}
//		public int onEnd() {
//			System.out.println("END REsP");
//			return 0;
//		}
		
	}
	
	private class DummyReceive extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
			  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
			  		MessageTemplate.MatchPerformative(ACLMessage.CFP));
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(msg.getContent());
			}
			
		}
	}
	
	private long processProposal() {
		return 1000;
	}
}
