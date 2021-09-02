package edu.ucsf.base;

import java.util.HashSet;

/**
 * This class is for a pair of strings such that two objects are equal iff the pair of strings (unordered) is unordered 
 * @author jladau
 */

@SuppressWarnings("rawtypes")
public class SemiOrderedPair<T extends Comparable> extends OrderedPair<T> implements Comparable {
	
	public SemiOrderedPair(T o1, T o2){
		super(o1,o2);
	}
	
	public int hashCode(){
		return o1.hashCode()+o2.hashCode();
	}
	
	public HashSet<T> toHashSet(){
		
		//set1 = output
		
		HashSet<T> set1;
		
		set1 = new HashSet<T>();
		set1.add((T) o1);
		set1.add((T) o2);
		return set1;
	}
	
	public boolean equals(Object obj1){
		
		//sop1 = object coerced to semi-ordered pair
		
		SemiOrderedPair sop1;
				
		if(!(obj1 instanceof SemiOrderedPair)){
			return false;
		}else{
			sop1 = (SemiOrderedPair) obj1;
			if(sop1.o1.equals(this.o1)){
				if(sop1.o2.equals(this.o2)){
					return true;
				}
			}
			if(sop1.o2.equals(this.o1)){
				if(sop1.o1.equals(this.o2)){
					return true;
				}
			}
			return false;
		}
	}
}
