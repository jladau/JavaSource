package edu.ucsf.PartialLogScale;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code scales data for graphing using a partial log scale: value below or above a specified value are linear, while those outside are log. Negative values are also log scaled.
 * @author jladau
 *
 */

public class PartialLogScaleLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lst1 = list of absolute values
		//dThreshold = threshold for log scale
		//dValue = current value
		//lstOut = output
		//dMin = minimum value
		//dMax = maximum value
		//dTransValue = transformed value
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		double dThreshold = 0; double dValue; double dMin; double dMax; double dTransValue;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading data
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through headers
		dMin=Double.MAX_VALUE;
		dMax=Double.MIN_VALUE;
		dThreshold=arg1.getValueDouble("dThreshold");
		for(int j=0;j<arg1.getValueStringArray("rgsHeaders").length;j++){

			//appending header
			dat1.appendToLastColumn(0, arg1.getValueStringArray("rgsHeaders")[j] + "_PARTIAL_LOG_TRANSFORMED");
			
			//loading values
			for(int k=1;k<dat1.iRows;k++){
				dValue= dat1.getDouble(k, arg1.getValueStringArray("rgsHeaders")[j]);
				if(!Double.isNaN(dValue)){
					dTransValue=findTransformedValue(dValue,dThreshold);
					if(!Double.isInfinite(dTransValue)){
						dat1.appendToLastColumn(k, Double.toString(dTransValue));
						if(dTransValue<dMin){
							dMin=dTransValue;
						}
						if(dTransValue>dMax){
							dMax=dTransValue;
						}
					}
				}
			}
		}
		
		//outputting axis labels
		lstOut = new ArrayList<String>();
		lstOut.add("MAPPED_VALUE,ACTUAL_VALUE");
		for(int i=(int) Math.floor(dMin);i<=Math.ceil(dMax)+0.001;i++){
			lstOut.add(i + "," + findUntransformedValue((double) i, dThreshold));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace("csv","labels.csv"));
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
	
	private static double findUntransformedValue(double d1, double dAbsThreshold){
		
		if(d1==0){
			return 0;
		}else if(d1<0){
			if(d1<=-1){
				return -Math.pow(10.,-d1 - 1 + Math.log10(dAbsThreshold));
			}else{
				return d1*dAbsThreshold;
			}
		}else{ 
			if(d1>dAbsThreshold){
				return Math.pow(10.,d1 - 1 + Math.log10(dAbsThreshold));
			}else{
				return d1*dAbsThreshold;
			}
		}
	}
	
	private static double findTransformedValue(double d1, double dAbsThreshold){
		
		if(d1==0){
			return d1;
		}else if(d1<0){
			if(d1<-dAbsThreshold){
				return -Math.log10(-d1)+Math.log10(dAbsThreshold)-1;
			}else{
				return d1/dAbsThreshold;
			}
		}else{ 
			if(d1>dAbsThreshold){
				return Math.log10(d1)-Math.log10(dAbsThreshold)+1;
			}else{
				return d1/dAbsThreshold;
			}
		}
	}
}
