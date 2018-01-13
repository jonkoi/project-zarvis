package zarvis.bakery.agents;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import zarvis.bakery.behaviors.bakery.ProcessOrderBehaviour;
import zarvis.bakery.models.Bakery;
import zarvis.bakery.models.Product;
import zarvis.bakery.utils.Util;
import jade.proto.ContractNetResponder;

public class BakeryAgent extends Agent {

	private Logger logger = LoggerFactory.getLogger(BakeryAgent.class);
	private Bakery bakery;
	private List<Product> products;

	public BakeryAgent(Bakery bakery) {
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

		addBehaviour(new ProcessOrderBehaviour(bakery));
	}

	protected void takeDown() {
	}
}
