package edu.ucsf.sdm;

import org.joda.time.LocalDate;

/**
 * Transforms difference response variables.
 * @author jladau
 */

public class ResponseTransformDifferences {

	public static double apply(String sSample1, String sDateAlias1, double dValue1, String sSample2, String sDateAlias2, double dValue2, String sResponseDifferenceTransform) throws Exception{
		
		//d1,d2,d3 = intermediate values
		
		double d1;
		double d2;
		double d3;
		
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
		
		if(sResponseDifferenceTransform.equals("percentdifference")){
			if(Math.abs(d1)<0.00000001){
				d3 = Double.NaN;
			}else{
				d3 = (d2-d1)/d1;
			}
		}else if(sResponseDifferenceTransform.equals("difference")){
			d3 = d2 - d1;
		}else if(sResponseDifferenceTransform.equals("logoddsratio")){
			if(Math.abs(1.-d2)<0.00000001 || Math.abs(1.-d1)<0.00000001 || Math.abs(d2)<0.00000001 || Math.abs(d1)<0.00000001){
				d3 = Double.NaN;
			}else{
				d3 = Math.log((d2/(1.-d2))/(d1/(1.-d1)));
			}
		}else if(sResponseDifferenceTransform.equals("abslogoddsratio")){
			if(Math.abs(1.-d2)<0.00000001 || Math.abs(1.-d1)<0.00000001 || Math.abs(d2)<0.00000001 || Math.abs(d1)<0.00000001){
				d3 = Double.NaN;
			}else{
				d3 = Math.abs(Math.log((d2/(1.-d2))/(d1/(1.-d1))));
			}
		}else if(sResponseDifferenceTransform.equals("logitbraycurtis")){
			if((d1+d2)<0.00000001){
				d3 = Double.NaN;
			}else{
				d3 = Math.log(Math.abs(d1-d2)/((d1+d2)-Math.abs(d1-d2)));
			}
		}else{
			throw new Exception("Response transform for differences not specified.");
		}
		return d3;
	}
}
