package edu.ucsf.base;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.HashBasedTable;

/**
 * Implementation of a sparse table with three axes.
 * @author jladau
 */

public class HashBasedTable4D<K1 extends Comparable<?>,K2 extends Comparable<?>,K3 extends Comparable<?>,K4 extends Comparable<?>>{

	/**Backing map.**/
	private HashMap<K1,HashBasedTable3D<K2,K3,K4>> map1;
	
	/**Maps of counts of values for each key.**/
	private HashMap_AdditiveInteger<K2> mapCount2;
	private HashMap_AdditiveInteger<K3> mapCount3;
	private HashMap_AdditiveInteger<K4> mapCount4;
	
	/**Number of entries in table**/
	private int iCount;
	
	/**
	 * Constructor
	 */
	public HashBasedTable4D(){
		initialize();
	}
	
	protected void initialize(){
		map1 = new HashMap<K1,HashBasedTable3D<K2,K3,K4>>(10000);
		mapCount2 = new HashMap_AdditiveInteger<K2>();
		mapCount3 = new HashMap_AdditiveInteger<K3>();
		mapCount4 = new HashMap_AdditiveInteger<K4>();
		iCount=0;
	}
	
	protected HashBasedTable<K3,K4,Double> getInnerTable(K1 key1, K2 key2){
		if(map1.containsKey(key1)){
			return map1.get(key1).getInnerTable(key2);
		}else{
			return null;
		}
	}
	
	/**
	 * Puts a value in the table
	 * @param key1 First key.
	 * @param key2 Second key.
	 * @param key3 Third key.
	 * @param key4 Fourth key.
	 * @param dValue Value.
	 */
	public void put(K1 key1, K2 key2, K3 key3, K4 key4, Double dValue){
		
		//tbl1 = table being added
		
		HashBasedTable3D<K2,K3,K4> tbl1;
		
		if(!map1.containsKey(key1)){
			tbl1 = new HashBasedTable3D<K2,K3,K4>();
			map1.put(key1, tbl1);
		}
		map1.get(key1).put(key2, key3, key4, dValue);
		mapCount2.putSum(key2, 1);
		mapCount3.putSum(key3, 1);
		mapCount4.putSum(key4, 1);
		iCount++;
	}
	
	/**
	 * Removes a value from the table
	 * @param key1 First key.
	 * @param key2 Second key.
	 * @param key3 Third key.
	 * @param key4 Fourth key.
	 */
	public void remove(K1 key1, K2 key2, K3 key3, K4 key4){
		if(contains(key1,key2,key3,key4)){
			map1.get(key1).remove(key2, key3, key4);
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
		mapCount4.putSum(key4, -1);
		if(mapCount4.get(key4)==0){
			mapCount4.remove(key4);
		}
		iCount--;
	}
	
	/**
	 * Gets a specified value.
	 * @return Value if keys found in table; Double.NaN otherwise.
	 */
	public double get(K1 key1, K2 key2, K3 key3, K4 key4){
		if(this.contains(key1,key2,key3,key4)){
			return map1.get(key1).get(key2, key3, key4);
		}else{
			return Double.NaN;
		}
	}

	/**
	 * Checks if table has specified coordinates.
	 * @return True if table has coordinates, false otherwise.
	 */
	public boolean contains(K1 key1, K2 key2, K3 key3, K4 key4){
		if(!map1.containsKey(key1)){
			return false;
		}else{
			if(!map1.get(key1).contains(key2, key3, key4)){
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
		
		HashBasedTable4D<K1,K2,K3,K4> tbl2;
		
		if(o1 instanceof HashBasedTable4D){
			tbl2 = (HashBasedTable4D<K1,K2,K3,K4>) o1;
			if(this.size()!=tbl2.size()){
				return false;
			}
			for(K1 k1:key1Set()){
				for(K2 k2:key2Set()){
					for(K3 k3:key3Set()){
						for(K4 k4:key4Set()){
							if(contains(k1,k2,k3,k4)){
								if(!tbl2.contains(k1,k2,k3,k4)){
									return false;
								}
								if(this.get(k1,k2,k3,k4)!=tbl2.get(k1, k2, k3,k4)){
									return false;
								}
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
	
	/**
	 * Returns set of keys for fourth axis
	 */
	public Set<K4> key4Set(){
		return mapCount4.keySet();
	}
	
	public String toString(){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		sbl1 = new StringBuilder();
		for(K1 k1:key1Set()){
			for(K2 k2:key2Set()){
				for(K3 k3:key3Set()){
					for(K4 k4:key4Set()){
						if(contains(k1,k2,k3,k4)){
							if(sbl1.length()>0){
								sbl1.append(",");
							}
							sbl1.append("(" + k1 + "," + k2 + "," + k3 + "," + k4 + ")=" + get(k1,k2,k3,k4));
						}
					}
				}
			}
		}
		return sbl1.toString();
	}
}
