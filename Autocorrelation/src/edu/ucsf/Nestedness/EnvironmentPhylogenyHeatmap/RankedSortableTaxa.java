package edu.ucsf.Nestedness.EnvironmentPhylogenyHeatmap;

import java.util.Arrays;
import java.util.HashMap;

public class RankedSortableTaxa {

	/**Array of taxa**/
	private SortableTaxon[] rgt1;
	
	/**Map from observation ID to sortable taxon ID**/
	private HashMap<String,SortableTaxon> map1;
	
	/**Map from observation ID to ranks (kingdom, phylum, class, order, family, genus, otu)**/
	private HashMap<String,Integer[]> map2;
	
	/**Map from observation ID to taxonomic rank names**/
	private HashMap<String,String[]> map4;
	
	/**Taxon counter**/
	private int iCounter;
	
	/**Flag for whether sorted**/
	private boolean bSorted;
	
	/**Map from taxonomic rank to rank indices**/
	private HashMap<String,Integer> map3;
	
	
	public RankedSortableTaxa(int iTaxa){	
		rgt1 = new SortableTaxon[iTaxa];
		iCounter = 0;
		bSorted = false;
		map1 = new HashMap<String,SortableTaxon>();
		map3 = new HashMap<String,Integer>();
		map3.put("kingdom", 0);
		map3.put("phylum", 1);
		map3.put("class", 2);
		map3.put("order", 3);
		map3.put("family", 4);
		map3.put("genus", 5);
		map3.put("otu", 6);
	}
	
	public void addTaxon(SortableTaxon txn1){
		rgt1[iCounter] = txn1;
		iCounter++;
		bSorted = false;
		map1.put(txn1.observationID(), txn1);
	}
	
	public void sort(){
		
		//rgi1 = current ranks
		//rgi2 = copy
		
		Integer rgi1[];
		Integer rgi2[];
		
		Arrays.sort(rgt1);
		bSorted = true;
		map2 = new HashMap<String,Integer[]>(rgt1.length);
		map4 = new HashMap<String,String[]>(rgt1.length);
		rgi1 = new Integer[]{1,1,1,1,1,1,1};
		for(int i=0;i<rgt1.length;i++){
			if(i>0){	
				for(int j=0;j<6;j++){
					if(!rgt1[i].classification()[j].equals(rgt1[i-1].classification()[j])){
						rgi1[j]++;
					}
				}
				rgi1[6]++;
			}
			
			//*********************
			//System.out.println(rgi1[2]);
			//*********************
			
			rgi2 = new Integer[rgi1.length];
			for(int k=0;k<rgi1.length;k++){
				rgi2[k]=rgi1[k];
			}
			
			map2.put(rgt1[i].observationID(), rgi2);
			
			//*********************
			//System.out.println(rgt1[i].observationID() + "," + map2.get(rgt1[i].observationID())[2]);
			//*********************
			
			map4.put(rgt1[i].observationID(), rgt1[i].classification());
		}
		
		//*********************
		//for(String s:map2.keySet()){
		//	if(map2.get(s)[2]!=44){	
		//		System.out.println(s + "," + map2.get(s)[2]);
		//	}
		//}
		//System.out.println("HERE");
		
		//*********************
		
		
		
	}
	
	public int rank(String sObservationID, String sTaxonomicRank){
		
		//i1 = column
	
		if(bSorted == false){
			return -9999;
		}
		
		int i1;
		
		i1 = map3.get(sTaxonomicRank);
		
		//*********************
		//for(String s:map2.keySet()){
		//	System.out.println(s + "," + map2.get(s)[2]);
		//}
		//*********************
		
		
		return map2.get(sObservationID)[i1];
	}

	public String taxon(String sObservationID, String sTaxonomicRank){
		
		//i1 = column
	
		int i1;
		
		i1 = map3.get(sTaxonomicRank);
		return map4.get(sObservationID)[i1];
	}
	
	public SortableTaxon taxon(String sObservationID){
		return map1.get(sObservationID);
	}	
}