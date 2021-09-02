package edu.ucsf.Trees.NewickToTaxonStrings;

public class Node implements Comparable<Node>{
	
	private int iIndex;
	private String sID;
	private int iDepth;
	private boolean bLeaf;
	
	public Node(String sID, int iIndex, int iDepth, boolean bLeaf){
		this.iIndex = iIndex;
		this.sID = sID;
		this.iDepth = iDepth;
		this.bLeaf = bLeaf;
	}
	
	public boolean isLeaf(){
		return bLeaf;
	}
	
	public int depth(){
		return iDepth;
	}
	
	public String id(){
		return sID;
	}
	
	public int index(){
		return iIndex;
	}
	
	public String toString(){
		return sID + "," + iIndex + "," + iDepth + "," + bLeaf;
	}
	
	public int compareTo(Node nod1){
		
		return this.index()-nod1.index();
		
		/*
		if(this.index()<nod1.index() && this.depth()>nod1.depth()){
			return -1;
		}else{
			return 1;
		}
		*/
		
		/*
		if(this.index()<nod1.index() && this.depth()>nod1.depth()){
			return -1;
		}else if(this.index()>nod1.index() && this.depth()<nod1.depth()){
			return 1;
		}else{
			return 0;
		}
		*/
	}
	
	public boolean equals(Object obj1){
		
		//nod1 = object as node
		
		Node nod1;
		
		if(obj1 == null || obj1.getClass()!= this.getClass()){
			return false;
		}
		nod1 = (Node) obj1;
		if(this.isLeaf()==nod1.isLeaf()){
			if(this.depth()==nod1.depth()){
				if(this.id().equals(nod1.id())){
					if(this.index()==nod1.index()){	
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public int hashCode(){
		
		if(isLeaf()==true){	
			return 107*depth()+98*id().hashCode()+87*index()+19;
		}else{
			return 107*depth()+98*id().hashCode()+87*index();
		}
	}
}