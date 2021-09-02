package edu.ucsf.base;

import java.util.HashMap;

/**
 * This class adds functions to hashmap
 * @author jladau
 *
 */

public class HashMap_AdditiveInteger<K> extends HashMap<K,Integer>{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public HashMap_AdditiveInteger(){
	}
	
	/**
	 * Constructor
	 */
	public HashMap_AdditiveInteger(int iInitialCapacity){
		super(iInitialCapacity);
	}
	
	/**
	 * Adds integer value to a hashmap
	 */
	public void putSum(K Key, int iValue){
		if(!this.containsKey(Key)){
			this.put(Key, iValue);
		}else{
			this.put(Key, this.get(Key) + iValue);
		}
	}
}
