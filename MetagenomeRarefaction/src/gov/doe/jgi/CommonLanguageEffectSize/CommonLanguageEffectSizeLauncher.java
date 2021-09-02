package gov.doe.jgi.CommonLanguageEffectSize;

import java.util.ArrayList;

import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class CommonLanguageEffectSizeLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sCategoryField = category field
		//map1 = map from read counts, categories to sample counts
		//map3 = map from read counts to total counts
		//map2 = map from read counts, categories to sum of ranks
		//s1 = current read count, category pair
		//d1 = current count in double format
		//d2 = current total count
		
		double d1;
		double d2;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		String sCategoryField;
		HashMap_AdditiveInteger<String> map1;
		HashMap_AdditiveDouble<String> map2;
		HashMap_AdditiveInteger<String> map3;
		String s1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategoryField = arg1.getValueString("sCategoryField");
		map1 = new HashMap_AdditiveInteger<String>(dat1.iRows);
		map2 = new HashMap_AdditiveDouble<String>(dat1.iRows);
		map3 = new HashMap_AdditiveInteger<String>(dat1.iRows);
		
		//loading sums
		for(int i=1;i<dat1.iRows;i++){
			s1 = dat1.getString(i, sCategoryField) + "," + dat1.getString(i, "READ_DEPTH");
			map1.putSum(s1, 1);
			map2.putSum(s1, dat1.getDouble(i, "RICHNESS_RANK"));
			map3.putSum(dat1.getString(i, "READ_DEPTH"), 1);
		}
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add(sCategoryField + ",READ_DEPTH,COMMON_LANGUAGE_EFFECT_SIZE");
		for(String s:map1.keySet()){
			d1 = (double) map1.get(s);
			d2 = (double) map3.get(s.split(",")[1]);
			lstOut.add(s + "," + (map2.get(s)-d1*(d1+1.)/2.)/(d1*d2));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}

	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sMode = 'greater-than' or 'less-than'
		//d1 = first value
		//d2 = second value
		//i1 = number of reads in first metagenome
		//s1 = first metagenome category
		//s2 = second metagenome category
		//map1 = total pairs
		//map2 = total pairs passing test
		//lstOut = output
		//sCategoryField = category field
		//iCounter = counter
		//iTotal = total number of pairs
		
		//TODO needs to be by category!!
		
		ArgumentIO arg1;
		DataIO dat1;
		String sMode;
		double d1;
		double d2;
		int i1;
		String s1;
		String s2;
		HashMap_AdditiveInteger<String> map2;
		HashMap_AdditiveInteger<String> map1;
		ArrayList<String> lstOut;
		String sCategoryField;
		int iTotal;
		int iCounter;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sMode = arg1.getValueString("sMode");
		map1 = new HashMap_AdditiveInteger<String>(dat1.iRows);
		map2 = new HashMap_AdditiveInteger<String>(dat1.iRows);
		sCategoryField = arg1.getValueString("sCategoryField");
		
		//looping through pairs of samples
		iTotal = (dat1.iRows-1)*(dat1.iRows-2)/2;
		iCounter = 0;
		for(int i=2;i<dat1.iRows;i++){
			d1 = dat1.getDouble(i, "RICHNESS");
			i1 = dat1.getInteger(i, "NUMBER_READS");
			s1 = dat1.getString(i, sCategoryField);
			for(int k=1;k<i;k++){
				
				iCounter++;
				System.out.println("Analyzing sample pair " + iCounter + " of " + iTotal + "...");
				
				
				if(dat1.getInteger(k, "NUMBER_READS")==i1){
					s2 = dat1.getString(k, sCategoryField);
					if(!s1.equals(s2)){
						d2 = dat1.getDouble(k, "RICHNESS");
						if(sMode.equals("less-than") && d1<d2){
							map2.putSum(s1 + "," + i1, 1);
						}else if(sMode.equals("less-than") && d2<d1){
							map2.putSum(s2 + "," + i1, 1);
						}else if(sMode.equals("greater-than") && d1>d2){
							map2.putSum(s1 + "," + i1, 1);
						}else if(sMode.equals("greater-than") && d2>d1){
							map2.putSum(s2 + "," + i1, 1);
						}
						map1.putSum(s1 + "," + i1, 1);
						map1.putSum(s2 + "," + i1, 1);
					}
				}
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size()+1);
		lstOut.add("CATEGORY,NUMBER_OF_READS,COMMON_LANGUAGE_EFFECT_SIZE");
		for(String s:map1.keySet()){
			if(map2.containsKey(s)){
				lstOut.add(s + "," + ((double) map2.get(s))/((double) map1.get(s)));
			}else{
				lstOut.add(s + "," + 0);
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}