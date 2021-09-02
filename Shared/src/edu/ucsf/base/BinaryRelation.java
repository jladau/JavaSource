package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashSet;
import com.google.common.collect.TreeBasedTable;

/**
 * Implements a binary relation
 * @author jladau
 */

@SuppressWarnings("rawtypes")
public class BinaryRelation<T extends Comparable> {

	/**Set of ordered pairs**/
	private HashSet<OrderedPair<T>> set1;
	
	/**Set of elements**/
	private HashSet<T> set2;
	
	public BinaryRelation(){
		set1 = new HashSet<OrderedPair<T>>();
		set2 = new HashSet<T>();
	}
	
	public void addOrderedPair(OrderedPair<T> orp1){
		set1.add(orp1);
		set2.add(orp1.o1);
		set2.add(orp1.o2);
	}
	
	public void addOrderedPair(T t1, T t2){
		addOrderedPair(new OrderedPair<T>(t1,t2));
	}
	
	public boolean contains(T t1, T t2){
		if(set1.contains(new OrderedPair<T>(t1,t2))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Checks if binary relation is transitive.
	 * @return True if transitive; false otherwise.
	 */
	public boolean isTransitive(){
		for(OrderedPair<T> orp1:set1){
			for(OrderedPair<T> orp2:set1){
				if(!orp1.equals(orp2)){
					if(orp1.o2.equals(orp2.o1)){
						if(!set1.contains(new OrderedPair<T>(orp1.o1,orp2.o2))){
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public ArrayList<ArrayList<OrderedPair<T>>> listTransitivityViolations(){
		
		//lstOut = output
		
		ArrayList<ArrayList<OrderedPair<T>>> lstOut;
		
		lstOut = new ArrayList<ArrayList<OrderedPair<T>>>();
		for(OrderedPair<T> orp1:set1){
			for(OrderedPair<T> orp2:set1){
				if(!orp1.equals(orp2)){
					if(orp1.o2.equals(orp2.o1)){
						if(!set1.contains(new OrderedPair<T>(orp1.o1,orp2.o2))){
							lstOut.add(new ArrayList<OrderedPair<T>>());
							lstOut.get(lstOut.size()-1).add(orp1);
							lstOut.get(lstOut.size()-1).add(orp2);
						}
					}
				}
			}
		}
		return lstOut;
	}
	
	public boolean isSymmetric(){
		for(OrderedPair<T> orp1:set1){
			if(!set1.contains(new OrderedPair<T>(orp1.o2,orp1.o1))){
				return false;
			}
		}
		return true;
	}
	
	public boolean isReflexive(){
		for(T t:set2){
			if(!set1.contains(new OrderedPair<T>(t,t))){
				return false;
			}
		}
		return true;
	}
	
	
	public boolean isStrictOrdering(){
		if(isTransitive() && !isSymmetric()){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isPartialOrdering(){
		if(isReflexive() && !isSymmetric() && isTransitive()){
			return true;
		}else{
			return false;
		}
	}
	
	public BinaryRelation<T> findTransitiveReduction(){
		
		//bin1 = output
		
		BinaryRelation<T> bin1;
		
		bin1 = new BinaryRelation<T>();
		for(OrderedPair<T> orp1:set1){
			if(cover(orp1)){
				bin1.addOrderedPair(orp1);
			}
		}
		return bin1;
	}
	
	public boolean cover(OrderedPair<T> orp1){
		
		if(!set1.contains(orp1)){
			return false;
		}
		for(T t:set2){
			if(set1.contains(new OrderedPair<T>(orp1.o1,t))){
				if(set1.contains(new OrderedPair<T>(t,orp1.o2))){
					return false;
				}
			}
		}
		return true;
	}
	
	public TreeBasedTable<T,T,Integer> toAdjacencyMatrix(){
		
		//tbl1 = output
		
		TreeBasedTable<T,T,Integer> tbl1;
		
		tbl1 = TreeBasedTable.create();
		for(T t:set2){
			for(T s:set2){
				tbl1.put(t, s, 0);
			}
		}
		for(OrderedPair<T> orp1:set1){
			tbl1.put(orp1.o1, orp1.o2, 1);
		}
		return tbl1;
	}
	
	
	public HashSet<T> getElements(){
		return set2;
	}
	
	public HashSet<OrderedPair<T>> getOrderedPairs(){
		return set1;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o1){
		
		//bin1 = coerced object
		
		BinaryRelation<T> bin1;
		
		if(!(o1 instanceof BinaryRelation)){
			return false;
		}else{
			bin1 = (BinaryRelation<T>) o1;
			if(!bin1.getElements().equals(getElements())){
				return false;
			}
			if(!bin1.getOrderedPairs().equals(getOrderedPairs())){
				return false;
			}
			return true;
		}
	}
}
