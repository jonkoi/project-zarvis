package zarvis.bakery.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zarvis.bakery.models.Node;

public class NeiGraph {
	
	private Map<Node, List<Node>> adjacencyMap;
	
	public NeiGraph() {
		this.adjacencyMap = new HashMap<>();
	}
	
	public void AddEntry(Node n, List<Node> m) {
		adjacencyMap.put(n, m);
	}
	
	public Map<Node, List<Node>> GetMap(){
		return adjacencyMap;
	}

}
