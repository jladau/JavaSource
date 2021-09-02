package edu.ucsf.sdm;

import org.joda.time.LocalDate;

/**
 * Transforms predictor response variables.
 * @author jladau
 */

public class PredictorTransformDifferences {
	
	public static double apply(String sSample1, String sDateAlias1, double dValue1, String sSample2, String sDateAlias2, double dValue2, String sResponseDifferenceTransform) throws Exception{
		
		//d1,d2 = intermediate values
		
		double d1;
		double d2;
		
		//ordering samples
		if(!sDateAlias1.equals(sDateAlias2)){
			if(sDateAlias1.equals("current")){
				d1 = dValue1;
				d2 = dValue2;
			}else if(sDateAlias2.equals("current")){
				d1 = dValue2;
				d2 = dValue1;
			}else{
				if((new LocalDate(sDateAlias1)).isBefore(new LocalDate(sDateAlias2))){
					d1 = dValue1;
					d2 = dValue2;
				}else{
					d1 = dValue2;
					d2 = dValue1;
				}
			}
		}else{
			if(sSample1.compareTo(sSample2)<0){
				d1 = dValue1;
				d2 = dValue2;
			}else{
				d1 = dValue2;
				d2 = dValue1;
			}
		}
		if(sResponseDifferenceTransform.startsWith("abs") || sResponseDifferenceTransform.equals("logitbraycurtis")){
			return Math.abs(d2-d1);	
		}else{
			return d2-d1;
		}
	}
	
	public static String getOrderedID(String sSample1, String sDateAlias1, String sSample2, String sDateAlias2){
		
		//ordering samples
		if(!sDateAlias1.equals(sDateAlias2)){
			if(sDateAlias1.equals("current")){
				return sSample1 + "," + sSample2;
			}else if(sDateAlias2.equals("current")){
				return sSample2 + "," + sSample1;
			}else{
				if((new LocalDate(sDateAlias1)).isBefore(new LocalDate(sDateAlias2))){
					return sSample1 + "," + sSample2;
				}else{
					return sSample2 + "," + sSample1;
				}
			}
		}else{
			if(sSample1.compareTo(sSample2)<0){
				return sSample1 + "," + sSample2;
			}else{
				return sSample2 + "," + sSample1;
			}
		}			
	}
	
	public static String getOrderedDates(String sSample1, String sDateAlias1, String sSample2, String sDateAlias2){
		
		//ordering samples
		if(!sDateAlias1.equals(sDateAlias2)){
			if(sDateAlias1.equals("current")){
				return sDateAlias1 + "," + sDateAlias2;
			}else if(sDateAlias2.equals("current")){
				return sDateAlias2 + "," + sDateAlias1;
			}else{
				if((new LocalDate(sDateAlias1)).isBefore(new LocalDate(sDateAlias2))){
					return sDateAlias1 + "," + sDateAlias2;
				}else{
					return sDateAlias2 + "," + sDateAlias1;
				}
			}
		}else{
			if(sSample1.compareTo(sSample2)<0){
				return sDateAlias1 + "," + sDateAlias2;
			}else{
				return sDateAlias2 + "," + sDateAlias1;
			}
		}			
	}
}
