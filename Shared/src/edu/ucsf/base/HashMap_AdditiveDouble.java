package edu.ucsf.base;

import java.util.HashMap;

/**
 * This class adds functions to hashmap
 * @author jladau
 *
 */

public class HashMap_AdditiveDouble<K> extends HashMap<K,Double>{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public HashMap_AdditiveDouble(){
	}
	
	/**
	 * Constructor
	 */
	public HashMap_AdditiveDouble(int iInitialCapacity){
		super(iInitialCapacity);
	}
	
	/**
	 * Adds double value to a hashmap
	 */
	public void putSum(K Key, Double dValue){
		if(!this.containsKey(Key)){
			this.put(Key, dValue);
		}else{
			this.put(Key, this.get(Key) + dValue);
		}
	}
}
