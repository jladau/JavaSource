package edu.ucsf.base;

import java.util.HashMap;
import java.util.Set;
import com.google.common.collect.HashBasedTable;

/**
 * Implementation of a sparse table with three axes.
 * @author jladau
 */

public class HashBasedTable3D<K1 extends Comparable<?>,K2 extends Comparable<?>,K3 extends Comparable<?>> {

	/**Backing map.**/
	private HashMap<K1,HashBasedTable<K2,K3,Double>> map1;
	
	/**Maps of counts of values for each key.**/
	private HashMap_AdditiveInteger<K2> mapCount2;
	private HashMap_AdditiveInteger<K3> mapCount3;
	
	/**Number of entries in table**/
	private int iCount;
	
	/**
	 * Constructor
	 */
	public HashBasedTable3D(){
		map1 = new HashMap<K1,HashBasedTable<K2,K3,Double>>();
		mapCount2 = new HashMap_AdditiveInteger<K2>();
		mapCount3 = new HashMap_AdditiveInteger<K3>();
		iCount=0;
	}
	
	/**
	 * Puts a value in the table
	 * @param key1 First key.
	 * @param key2 Second key.
	 * @param key3 Third key.
	 * @param dValue Value.
	 */
	public void put(K1 key1, K2 key2, K3 key3, Double dValue){
		
		//tbl1 = table being added
		
		HashBasedTable<K2,K3,Double> tbl1;
		
		if(!map1.containsKey(key1)){
			tbl1 = HashBasedTable.create();
			map1.put(key1, HashBasedTable.create(tbl1));
		}
		map1.get(key1).put(key2, key3, dValue);
		mapCount2.putSum(key2, 1);
		mapCount3.putSum(key3, 1);
		iCount++;
	}
	
	protected HashBasedTable<K2,K3,Double> getInnerTable(K1 key1){
		if(map1.containsKey(key1)){
			return map1.get(key1);
		}else{
			return null;
		}
	}
	
	/**
	 * Removes a value from the table
	 * @param key1 First key.
	 * @param key2 Second key.
	 * @param key3 Third key.
	 */
	public void remove(K1 key1, K2 key2, K3 key3){
		if(contains(key1,key2,key3)){
			map1.get(key1).remove(key2, key3);
			if(map1.get(key1).size()==0){
				map1.remove(key1);
			}
		}
		mapCount2.putSum(key2, -1);
		if(mapCount2.get(key2)==0){
			mapCount2.remove(key2);
		}
		mapCount3.putSum(key3, -1);
		if(mapCount3.get(key3)==0){
			mapCount3.remove(key3);
		}
		iCount--;
	}
	
	/**
	 * Gets a specified value.
	 * @return Value if keys found in table; Double.NaN otherwise.
	 */
	public double get(K1 key1, K2 key2, K3 key3){
		if(this.contains(key1,key2,key3)){
			return map1.get(key1).get(key2, key3);
		}else{
			return Double.NaN;
		}
	}

	/**
	 * Checks if table has specified coordinates.
	 * @return True if table has coordinates, false otherwise.
	 */
	public boolean contains(K1 key1, K2 key2, K3 key3){
		if(!map1.containsKey(key1)){
			return false;
		}else{
			if(!map1.get(key1).contains(key2, key3)){
				return false;
			}else{
				return true;
			}
		}
	}
	
	/**
	 * Gets number of entries in table.
	 * @return Number of entries.
	 */
	public int size(){
		return iCount;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o1){
		
		//tbl2 = object coerced to table
		
		HashBasedTable3D<K1,K2,K3> tbl2;
		
		if(o1 instanceof HashBasedTable3D){
			tbl2 = (HashBasedTable3D<K1,K2,K3>) o1;
			if(this.size()!=tbl2.size()){
				return false;
			}
			for(K1 k1:key1Set()){
				for(K2 k2:key2Set()){
					for(K3 k3:key3Set()){
						if(contains(k1,k2,k3)){
							if(!tbl2.contains(k1,k2,k3)){
								return false;
							}
							if(this.get(k1,k2,k3)!=tbl2.get(k1, k2, k3)){
								return false;
							}
						}
					}
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Returns set of keys for first axis
	 */
	public Set<K1> key1Set(){
		return map1.keySet();
	}

	/**
	 * Returns set of keys for second axis
	 */
	public Set<K2> key2Set(){
		return mapCount2.keySet(); 
	}

	/**
	 * Returns set of keys for third axis
	 */
	public Set<K3> key3Set(){
		return mapCount3.keySet(); 
	}
}