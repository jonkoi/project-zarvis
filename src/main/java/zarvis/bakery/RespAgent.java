package zarvis.bakery;

import jade.core.Agent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

public class RespAgent extends Agent {
	protected void setup() {
	  	System.out.println("Agent "+getLocalName()+" waiting for CFP...");
	  	MessageTemplate template = MessageTemplate.and(
	  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
	  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
	  		
			addBehaviour(new ContractNetResponder(this, template) {
				protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
					System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
					int proposal = evaluateAction();
					if (proposal > 2) {
						// We provide a proposal
						System.out.println("Agent "+getLocalName()+": Proposing "+proposal);
						ACLMessage propose = cfp.createReply();
						propose.setPerformative(ACLMessage.PROPOSE);
						propose.setContent(String.valueOf(proposal));
						return propose;
					}
					else {
						// We refuse to provide a proposal
						System.out.println("Agent "+getLocalName()+": Refuse");
						throw new RefuseException("evaluation-failed");
					}
				}
				
				protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
					System.out.println("Agent "+getLocalName()+": Proposal accepted");
					if (performAction()) {
						System.out.println("Agent "+getLocalName()+": Action successfully performed");
						ACLMessage inform = accept.createReply();
						inform.setPerformative(ACLMessage.INFORM);
						return inform;
					}
					else {
						System.out.println("Agent "+getLocalName()+": Action execution failed");
						throw new FailureException("unexpected-error");
					}	
				}
				
				protected void handleRejectProposal(ACLMessage reject) {
					System.out.println("Agent "+getLocalName()+": Proposal rejected");
				}
			} );
	  }
	  
	  private int evaluateAction() {
	  	// Simulate an evaluation by generating a random number
	  	return (int) (Math.random() * 10);
	  }
	  
	  private boolean performAction() {
	  	// Simulate action execution by generating a random number
	  	return (Math.random() > 0.2);
	  }
}
