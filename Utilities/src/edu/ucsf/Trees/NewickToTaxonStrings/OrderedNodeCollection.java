package edu.ucsf.Trees.NewickToTaxonStrings;
import java.util.HashMap;
import java.util.TreeSet;

public class OrderedNodeCollection {

	/**Set of nodes**/
	private TreeSet<Node> setNodes;
	
	/**Map from node to node with the least index such that the depth is lower**/
	private HashMap<Node,Node> mapLeast;
	
	public OrderedNodeCollection(){
		setNodes = new TreeSet<Node>();
	}
	
	public boolean add(Node nod1){
		return setNodes.add(nod1);
	}
	
	/**
	 * Loads function from each node to the node with the least index with lower depth
	 */
	public void loadLeastDepthFunction(){
		
		//nod2 = current node being tested
		
		Node nod2;
		
		mapLeast = new HashMap<Node,Node>(setNodes.size());
		for(Node nod1:setNodes){
			nod2 = setNodes.higher(nod1);
			while(nod2!=null){
				if(nod2.depth()<nod1.depth()){
					mapLeast.put(nod1, nod2);
					break;
				}
				nod2 = setNodes.higher(nod2);
			}
		}
		mapLeast.put(null, null);
	}
	
	/**
	 * Returns node with smallest index greater than current index such that the depth is less than the current depth
	 */
	public Node nextNode(Node nod1){
		
		//nod2 = next closest node
		
		Node nod2;
		
		if(mapLeast.containsKey(nod1)){
			return mapLeast.get(nod1);
		}else{
			nod2 = setNodes.higher(nod1);
			return mapLeast.get(nod2);
		}
	}
}
