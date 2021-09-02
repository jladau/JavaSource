package edu.ucsf.FlatFileDifferences;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Computes absolute differences between each pair of observations in a flat file
 * @author jladau
 */

public class FlatFileDifferencesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgsKeys = key headers
		//sValue = value header
		//lstOut = output
		//sbl1 = current first keys
		//sbl2 = current second keys
		//rgs1 = sorted array of strings
		
		String rgs1[];
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String rgsKeys[];
		String sValue;
		StringBuilder sbl1;
		StringBuilder sbl2;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgsKeys = arg1.getValueStringArray("rgsKeyHeaders");
		sValue = arg1.getValueString("sValueHeader");
		
		//loading output
		lstOut = new ArrayList<String>(dat1.iRows*dat1.iRows/2);
		sbl1 = new StringBuilder();
		for(int j=1;j<3;j++){	
			for(String s:rgsKeys){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(s + "_" + j);
			}
		}
		sbl1.append("," + sValue + "_ABS_DIFFERENCE");
		lstOut.add(sbl1.toString());
		for(int i=2;i<dat1.iRows;i++){
			for(int k=1;k<i;k++){
				sbl1 = new StringBuilder();
				sbl2 = new StringBuilder();
				for(String s:rgsKeys){
					if(sbl1.length()>0){
						sbl1.append(",");
						sbl2.append(",");
					}
					sbl1.append(dat1.getString(i, s));
					sbl2.append(dat1.getString(k, s));
				}
				rgs1=new String[]{sbl1.toString(),sbl2.toString()};
				Arrays.sort(rgs1);
				lstOut.add(Joiner.on(",").join(rgs1) + "," + Math.abs(dat1.getDouble(i, sValue) - dat1.getDouble(k, sValue)));
			}
		}
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}