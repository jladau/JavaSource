package edu.ucsf.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import edu.ucsf.io.BiomIO;

@SuppressWarnings("rawtypes")
public class BetaDiversityIterator implements Iterator{

	/**Samples in array format**/
	private String rgsSamples[];
	
	/**First counter**/
	private int iCounter1;
	
	/**Second counter**/
	private int iCounter2;
	
	/**Total number of samples**/
	private int iSamples;
	
	/**Total number of samples minus 1**/
	private int iSamplesMinus1;
	
	/**Total number of samples minus 2**/
	private int iSamplesMinus2;
	
	/**OTU table**/
	private BiomIO bio1;
	
	/**Counter**/
	public int iCounter=0;
	
	/**Total sample pairs**/
	public int iTotalSamplePairs;
	
	/**Sparse abundance map**/
	private HashMap<String,Set<String>> mapNonZero;
	
	public BetaDiversityIterator(BiomIO bio1, String sSample1, String sSample2){
		
		//saving table
		this.bio1 = bio1;

		//initializing array of samples and related variables
		iSamples = 2;
		iSamplesMinus1 = iSamples-1;
		iSamplesMinus2 = iSamples-2;
		rgsSamples = new String[iSamples];
		rgsSamples[0]=sSample1;
		rgsSamples[1]=sSample2;
		
		//initializing counters
		iCounter1 = 0;
		iCounter2 = 0;
		
		//initializing total sample pairs
		iTotalSamplePairs = iSamples*(iSamples-1)/2;
		
		//loading sparse matrix
		mapNonZero = new HashMap<String,Set<String>>(2);
		mapNonZero.put(sSample1, bio1.getNonzeroValues(bio1.axsSample, sSample1).keySet());
		mapNonZero.put(sSample2, bio1.getNonzeroValues(bio1.axsSample, sSample2).keySet());
	}
	
	public BetaDiversityIterator(BiomIO bio1){
		
		//i1 = counter for building array of strings
		
		int i1;
		
		//saving table
		this.bio1 = bio1;

		//initializing array of samples and related variables
		iSamples = bio1.axsSample.getIDs().size();
		iSamplesMinus1 = iSamples-1;
		iSamplesMinus2 = iSamples-2;
		rgsSamples = new String[iSamples];
		i1 = 0;
		for(String s:bio1.axsSample.getIDs()){
			rgsSamples[i1]=s;
			i1++;
		}
		
		//initializing counters
		iCounter1 = 0;
		iCounter2 = 0;
		
		//initializing total sample pairs
		iTotalSamplePairs = iSamples*(iSamples-1)/2;
		
		//loading sparse matrix
		mapNonZero = new HashMap<String,Set<String>>(bio1.axsSample.size());
		for(String s:bio1.axsSample.getIDs()){
			mapNonZero.put(s, bio1.getNonzeroValues(bio1.axsSample, s).keySet());
		}
	}
	
	@Override
	public BetaDiversity next(){
		
		//bet1 = output
		
		BetaDiversity bet1;
		
		//updating counters
		iCounter++;
		iCounter2++;
		if(iCounter2==iSamples){
			iCounter1++;
			if(iCounter1==iSamplesMinus1){
				try{
					throw new Exception("next() called but hasNext() == false");
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			iCounter2 = iCounter1+1;
		}
		
		//loading beta-diversity
		bet1 = new BetaDiversity(rgsSamples[iCounter1], rgsSamples[iCounter2]);
		
		//outputting beta-diversity
		return bet1;
	}
	
	@Override
	public boolean hasNext() {
		if(iCounter2 == iSamplesMinus1 && iCounter1==iSamplesMinus2){
			return false;
		}else{
			return true;
		}
	}
	
	public class BetaDiversity extends HashMap<String,Double>{
		
		private static final long serialVersionUID = 1L;

		/**First sample**/
		public String sSample1;
		
		/**Second sample**/
		public String sSample2;
		
		/**Jaccard dissimilarity**/
		public double dBetaJ = Double.NaN;
		
		/**Richness difference**/
		public double dBetaRich = Double.NaN;
		
		/**Simpson beta-diversity**/
		public double dBetaSim = Double.NaN;
		
		/**True turnover (Beta_-3 from Carvhalo)**/
		public double dBetaTurn = Double.NaN;
		
		/**Bray-Curtis**/
		public double dBetaBrayCurtis = Double.NaN;
		
		/**Number of shared species**/
		public double dA = Double.NaN;
		
		/**Number of species exclusive to first sample**/
		public double dB = Double.NaN;
		
		/**Number of species exclusive to second sample**/
		public double dC = Double.NaN;
		
		/**Weighted nestedness of sample 1 in sample 2**/
		public double dWNODF = Double.NaN;
		
		public BetaDiversity(String sSample1, String sSample2){
			this.sSample1 = sSample1;
			this.sSample2 = sSample2;
			loadBetaDiversity();
		}
		
		public String toString(){
			return sSample1 + "," + sSample2 + "," + dBetaBrayCurtis + "," + dA + "," + dB + "," + dC + "," + dBetaJ + "," + dBetaRich + "," + dBetaTurn + "," + dBetaSim + "," + dWNODF;
		}
		
		public boolean sameSamples(String sSample1, String sSample2){
			if(this.sSample1.equals(sSample1) && this.sSample2.equals(sSample2)){
				return true;
			}
			if(this.sSample2.equals(sSample1) && this.sSample1.equals(sSample2)){
				return true;
			}
			return false;
		}
		
		
		private void loadBetaDiversity(){
			
			//dA = value of 'a'
			//dB = value of 'b'
			//dC = value of 'c'
			//dW = number of taxa in sample 1 with lower values than those in sample 2
			//d1 = count for sample 1
			//d2 = count for sample 2
			//dSum = total
			//dNum = bray-curtis numerator
			//dDen = bray-curtis denominator
			//set1

			double d1;
			double d2;
			double dSum;
			double dNum=0;
			double dDen=0;
			double dW;
			
			if(!mapNonZero.containsKey(sSample1) || !mapNonZero.containsKey(sSample2)){
				return;
			}
			
			//initializing
			dA = 0;
			dB = 0;
			dC = 0;
			dW = 0;
			
			//loading components
			for(String s:mapNonZero.get(sSample1)){
				d1 = bio1.getValueByIDs(s, sSample1);
				if(mapNonZero.get(sSample2).contains(s)){
					d2 = bio1.getValueByIDs(s, sSample2);
				}else{
					d2 = 0;
				}
				dNum+=Math.abs(d1-d2);
				dDen+=(d1+d2);
				if(d1>0 && d2==0){
					dB++;
				}
				if(d1>0 && d2>0){
					dA++;
				}
				if(d1==0 && d2>0){
					dC++;
				}
				if(d1<d2){
					dW++;
				}
			}
			for(String s:mapNonZero.get(sSample2)){
				if(!mapNonZero.get(sSample1).contains(s)){
					d1 = 0;
					d2 = bio1.getValueByIDs(s, sSample2);
					dNum+=Math.abs(d1-d2);
					dDen+=(d1+d2);
					if(d1>0 && d2==0){
						dB++;
					}
					if(d1>0 && d2>0){
						dA++;
					}
					if(d1==0 && d2>0){
						dC++;
					}
					if(d1<d2){
						dW++;
					}
				}
			}
			
			//saving results
			dSum = dA + dB + dC;
			if(dSum>0){
				dBetaJ=(dB + dC)/dSum;
				dBetaRich = Math.abs(dB - dC)/dSum;
				dBetaTurn = 2.* Math.min(dB, dC)/dSum;
				if(dA + Math.min(dB, dC)>0){	
					dBetaSim = Math.min(dB, dC)/(dA + Math.min(dB, dC));
				}else{
					dBetaSim = 0;
				}
			}else{
				dBetaJ = 0;
				dBetaSim = 0;
			}
			if(dDen>0){
				dBetaBrayCurtis=dNum/dDen;
			}else{
				dBetaBrayCurtis=0;
			}
			if(dB > dC){
				dWNODF = 0;
			}else {
				if(dA + dB == 0) {
					dWNODF = 0;
				}else {
					dWNODF = dW/(dA+dB);
				}
			}
		}
	}
}