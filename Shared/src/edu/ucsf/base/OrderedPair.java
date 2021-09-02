package edu.ucsf.base;

/**
 * This class is for a pair of strings such that two objects are equal iff the pair of strings (unordered) is unordered 
 * @author jladau
 */

@SuppressWarnings("rawtypes")
public class OrderedPair<T extends Comparable> implements Comparable{

	/**First object**/
	public T o1;
	
	/**Second object**/
	public T o2;
	
	public OrderedPair(T o1, T o2){
		this.o1 = o1;
		this.o2 = o2;
	}
	
	@SuppressWarnings("unchecked")
	public int compareTo(Object obj1){
		
		//sop1 = object coerced to string pair
		
		OrderedPair<T> sop1;
		
		if(!(obj1 instanceof OrderedPair)){
			return -1;
		}else{
			sop1 = (OrderedPair) obj1;
			if(sop1.equals(this)){
				return 0;
			}else{
				if(!sop1.o1.equals(o1)){
					return sop1.o1.compareTo(o1);
				}else{
					return sop1.o2.compareTo(o2);
				}
			}
		}
	}
	
	public int hashCode(){
		return 7*o1.hashCode()+19*o2.hashCode();
	}
	
	public String toString(){
		return o1 + "," + o2;
	}
	
	public boolean equals(Object obj1){
		
		//sop1 = object coerced to semi-ordered pair
		
		OrderedPair sop1;
				
		if(!(obj1 instanceof OrderedPair)){
			return false;
		}else{
			sop1 = (OrderedPair) obj1;
			if(sop1.o1.equals(this.o1)){
				if(sop1.o2.equals(this.o2)){
					return true;
				}
			}
			return false;
		}
	}
}
