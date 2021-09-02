package edu.ucsf.Nestedness.EnvironmentPhylogenyHeatmap;

public class SortableTaxon implements Comparable<SortableTaxon>{

	/**Classification: kingdom, phylum, class, order, family, genus, otu**/
	private String[] rgsClassification;
	
	/**Values associated with classification**/
	private double[] rgdValues;
	
	/**Observation ID**/
	private String sObservationID;
	
	/**Rank below which start sorting; this rank and coarser ranks will be ignored; null for all ranks**/
	//private String sSortRank;
	
	/**Index to start sorting at**/
	private int iSortRank;
	
    public SortableTaxon(String rgsClassification[], double[] rgdValues, String sObservationID, String sSortRank) {
    	this.rgsClassification = rgsClassification;
    	this.rgdValues = rgdValues;
    	this.sObservationID = sObservationID;
    	//this.sSortRank = sSortRank;
    	if(sSortRank==null){
    		iSortRank = 0;
    	}else if(sSortRank.equals("kingdom")){
    		iSortRank = 1;
    	}else if(sSortRank.equals("phylum")){
    		iSortRank = 2;
    	}else if(sSortRank.equals("class")){
    		iSortRank = 3;
    	}else if(sSortRank.equals("order")){
    		iSortRank = 4;
    	}else if(sSortRank.equals("family")){
    		iSortRank = 5;
    	}else if(sSortRank.equals("genus")){
    		iSortRank = 6;
    	}else if(sSortRank.equals("otu") || sSortRank.equals("species")){
    		iSortRank = 6;
    	}
    }

    public boolean equals(Object o) {
    	
    	//stx1 = object coerced to sortable taxon
    	
    	SortableTaxon stx1;
    	
        if (!(o instanceof SortableTaxon))
            return false;
        stx1 = (SortableTaxon) o;
        
        if(!this.observationID().equals(stx1.observationID())){
        	return false;
        }
        
        for(int i=0;i<rgsClassification.length;i++){
        	if(!this.classification()[i].equals(stx1.classification()[i])){
        		return false;
        	}
        	if(this.values()[i]!=stx1.values()[i]){
        		return false;
        	}
        }
        return true;
    }

    public String observationID(){
    	return sObservationID;
    }
    
    public double[] values(){
    	return rgdValues;
    }
    
    public String[] classification(){
    	return rgsClassification;
    }
    
    public int hashCode() {
    	
    	//i1 = output
    	//rgi1 = hashcode multipliers
    	//rgi2 = more hashcode multipliers
    	
    	int rgi1[];
    	int rgi2[];
    	int i1;
    	
    	rgi1 = new int[]{2,5,7,11,13,17,19};
    	rgi2 = new int[]{23,29,31,37,41,43,47};
    	i1 = 0;
    	for(int i=0;i<7;i++){
    		i1+=rgsClassification[i].hashCode()*rgi1[i] + rgdValues[i]*(rgi2[i]+1);
    	}
    	i1+=sObservationID.hashCode();
    	return i1;
    }

    public String toString() {
    	
    	//sbl1 = stringbuilder
    	
    	StringBuilder sbl1;
    	
    	sbl1 = new StringBuilder();
    	
    	sbl1.append(this.observationID() + "," + rgsClassification[0]);
    	for(int i=1;i<rgsClassification.length;i++){
    		sbl1.append(";" + rgsClassification[i]);
    	}
    	sbl1.append(":" + rgdValues[0]);
    	for(int i=1;i<rgdValues.length;i++){
    		sbl1.append(";" + rgdValues[i]);
    	}
    	return sbl1.toString();
    }

    public int compareTo(SortableTaxon stx1) {
    	
    	for(int i=iSortRank;i<rgsClassification.length;i++){
    		
    		if(rgdValues[i]<stx1.values()[i]){
    			return -1;
    		}else if(rgdValues[i]>stx1.values()[i]){
    			return 1;
    		}else{
	    		if(!stx1.classification()[i].equals(this.classification()[i])){
	    			return this.classification()[i].compareTo(stx1.classification()[i]);
		    	}
    		}
    	}
    	return this.observationID().compareTo(stx1.observationID());
    }
}
