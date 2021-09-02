package edu.ucsf.MannWhitneyUGrapher;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MannWhitneyUGrapherLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//iDepth1 = lower read depth
		//iDepth2 = upper read depth
		//i1 = current maximum x-coordinate
		//map1 = map from primary categories to x-coordinates
		//map2 = y-offsets
		//lst1 = output
		//i2 = current primary x-coordinate
		//i3 = current secondary x-coordinate
		//d1 = current y-coordinate
		//d2 = current maximum y-offset
		//d3 = current y-offset
		
		double d1;
		double d2;
		double d3;
		ArgumentIO arg1;
		DataIO dat1;
		int iDepth1;
		int iDepth2;
		HashMap<String,Integer> map1;
		HashMap<String,Double> map2;
		int i1;
		int i2;
		int i3;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iDepth1 = arg1.getValueInt("iReadDepth1");
		iDepth2 = arg1.getValueInt("iReadDepth2");
		map1 = new HashMap<String,Integer>(dat1.iRows);
		map2 = new HashMap<String,Double>(dat1.iRows);
		i1 = 1;
		d2 = 0;
		lstOut = new ArrayList<String>(dat1.iRows*3+1);
		lstOut.add("CATEGORY_PRIMARY,CATEGORY_COMPARISON,READ_DEPTH,SIGNIFICANCE,COMMON_LANGUAGE_EFFECT_SIZE_OBS,X,Y,COLOR");
		
		//looping through rows
		for(int i=1;i<dat1.iRows;i++){
			
			if(!map1.containsKey(dat1.getString(i, "CATEGORY_PRIMARY"))){
				map1.put(dat1.getString(i, "CATEGORY_PRIMARY"), i1);
				i1+=3;
			}
			i2 = map1.get(dat1.getString(i, "CATEGORY_PRIMARY"));
			
			if(!map2.containsKey(dat1.getString(i, "CATEGORY_COMPARISON"))){
				map2.put(dat1.getString(i, "CATEGORY_COMPARISON"), d2);
				d2+=0.1;
			}
			d3 = map2.get(dat1.getString(i, "CATEGORY_COMPARISON"));
			
			if(dat1.getInteger(i, "READ_DEPTH")==iDepth1){
				i3 = i2-1;
			}else{
				i3 = i2+1;
			}
			
			if(dat1.getString(i, "SIGNIFICANCE").equals("significant")){
				d1 = -(2.*dat1.getDouble(i, "COMMON_LANGUAGE_EFFECT_SIZE_OBS")-1.);
			}else{
				d1 = 0;
			}
			
			lstOut.add(
					dat1.getString(i, "CATEGORY_PRIMARY") + "," + 
					dat1.getString(i, "CATEGORY_COMPARISON") + "," + 
					dat1.getString(i, "READ_DEPTH") + "," + 
					dat1.getString(i, "SIGNIFICANCE") + "," + 
					dat1.getString(i, "COMMON_LANGUAGE_EFFECT_SIZE_OBS") + "," +
					i2 + "," + d3 + "," + d3);
			lstOut.add(
					dat1.getString(i, "CATEGORY_PRIMARY") + "," + 
					dat1.getString(i, "CATEGORY_COMPARISON") + "," + 
					dat1.getString(i, "READ_DEPTH") + "," + 
					dat1.getString(i, "SIGNIFICANCE") + "," + 
					dat1.getString(i, "COMMON_LANGUAGE_EFFECT_SIZE_OBS") + "," +
					i3 + "," + (d1+d3) + "," +
					d3);
			lstOut.add(
					dat1.getString(i, "CATEGORY_PRIMARY") + "," + 
					dat1.getString(i, "CATEGORY_COMPARISON") + "," + 
					dat1.getString(i, "READ_DEPTH") + "," + 
					dat1.getString(i, "SIGNIFICANCE") + "," + 
					dat1.getString(i, "COMMON_LANGUAGE_EFFECT_SIZE_OBS") + "," +
					"" + ",," + "");
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
