package edu.ucsf.Climatologies.BestModelFlagger;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;

/**
 * Flags the best models within each climatology and across all climatologies. Removes models with cross validation below specified threshold.
 * @author jladau
 *
 */

public class BestModelFlaggerLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//rgs1 = current line
		//iCVR2 = column with cross validation r^2
		//iResponse = column with response variable
		//iLength = column with climatology length
		//tbl1(iClimatologyLength,sResponseVar) = returns the best model for given climatology length and response variable 
		//bfr1 = buffered reader
		//s1 = current line
		//dCV = current cross validation r^2
		//iClim = current climatology
		//sResp = current response variable
		
		double dCV;
		String rgs1[];
		String s1;
		BufferedReader bfr1;
		ArgumentIO arg1;
		int iCVR2 = -9999;
		int iLength = -9999;
		int iResponse = -9999;
		HashBasedTable<Integer,String,Double> tbl1;
		int iClim;
		String sResp;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading best models
		try{
			
			//initializing reader
			bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sModelsPath")));
			
			//loading columns
			rgs1 = bfr1.readLine().split(",");
			for(int j=0;j<rgs1.length;j++){
				if(rgs1[j].equals("CV_R2")){
					iCVR2=j;
				}
				if(rgs1[j].equals("CLIMATOLOGY_LENGTH")){
					iLength=j;
				}
				if(rgs1[j].equals("RESPONSE_VAR")){
					iResponse=j;
				}
			}
			
			//loading best models
			tbl1 = HashBasedTable.create(1000, 1000);
			s1 = bfr1.readLine();
			while(s1 != null){
				rgs1 = s1.split(",");
				dCV = Double.parseDouble(rgs1[iCVR2]);
				if(dCV>0.1){
					sResp = rgs1[iResponse];
					iClim = Integer.parseInt(rgs1[iLength]);
					if(!tbl1.contains(iClim, sResp)){
						tbl1.put(iClim, sResp, dCV);
					}else{
						if(tbl1.get(iClim, sResp)<dCV){
							tbl1.put(iClim, sResp, dCV);
						}
					}
				}
				s1 = bfr1.readLine();
			}
			
			
			
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	
}
