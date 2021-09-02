package edu.ucsf.MannWhitneyU;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import com.google.common.collect.ArrayListMultimap;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.base.Ranks;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MannWhitneyULauncher {

	//TODO calculate U statistics and find bootstrap distributions -- gives the fraction of wins over other category
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sValue = value field
		//sCategory = category field
		//lst1 = list of values
		//lst3 = list of bootstrapped values
		//lstCategories = list of categories
		//map1 = categories to indices
		//map2 = map from categories to current mean differences
		//lstOut = output
		//iIterations = number of iterations
		//rnd1 = random number generator
		//lstUnique = list of unique categories
		//s1 = first category
		//s2 = second category
		//i1 = counter
		
		ArrayList<String> lstUnique;
		ArgumentIO arg1;
		DataIO dat1;
		String sValue;
		String sCategory;
		ArrayList<Double> lst1;
		ArrayList<Double> lst3;
		ArrayList<String> lstCategories;
		ArrayListMultimap<String,Integer> map1;
		HashMap<String,Double> map2;
		ArrayList<String> lstOut;
		int iIterations;
		Random rnd1;
		String s1;
		String s2;
		int i1;
		
		//TODO implement option for considering pairs of categories individually, probably create separate class?
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sValue = arg1.getValueString("sValueField");
		sCategory = arg1.getValueString("sCategoryField");
		iIterations = arg1.getValueInt("iIterations");
		lstOut = new ArrayList<String>((iIterations+1)*dat1.iRows+1);
		lstOut.add("TYPE,CATEGORY_PRIMARY,CATEGORY_COMPARISON,COMMON_LANGUAGE_EFFECT_SIZE");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		
		//loading list of unique categories
		lstUnique = new ArrayList<String>(100);
		for(int i=1;i<dat1.iRows;i++){
			if(!lstUnique.contains(dat1.getString(i, sCategory))){
				lstUnique.add(dat1.getString(i, sCategory));
			}
		}

		//looping through pairs of categories
		for(int k=1;k<lstUnique.size();k++){
			s1 = lstUnique.get(k);
			for(int j=0;j<k;j++){
				s2 = lstUnique.get(j);
				
				System.out.println("Analyzing " + s1 + " vs " + s2 + "...");
				
				//loading data				
				i1 = 0;
				lst1 = new ArrayList<Double>(dat1.iRows);
				lstCategories = new ArrayList<String>(dat1.iRows);
				map1 = ArrayListMultimap.create();
				for(int i=1;i<dat1.iRows;i++){
					if(dat1.getString(i, sCategory).equals(s1) || dat1.getString(i, sCategory).equals(s2)){
						lst1.add(dat1.getDouble(i,sValue));
						lstCategories.add(dat1.getString(i,sCategory));
						map1.put(dat1.getString(i, sCategory),i1);
						i1++;
					}
				}
			
				//finding and outputting observed distances
				map2 = proportionHigher(lst1, lstCategories);
				for(String s:map2.keySet()){
					if(s.equals(s1)){
						lstOut.add("observed," + s + "," + s2 + "," + map2.get(s));
					}else{
						lstOut.add("observed," + s + "," + s1 + "," + map2.get(s));
					}
				}
				
				//boostrapping
				for(int i=0;i<iIterations;i++){
					lst3 = bootstrapResample(lst1,lstCategories,map1,rnd1);
					map2 = proportionHigher(lst3, lstCategories);
					for(String s:map2.keySet()){
						if(s.equals(s1)){
							lstOut.add("bootstrap," + s + "," + s2 + "," + map2.get(s));
						}else{
							lstOut.add("bootstrap," + s + "," + s1 + "," + map2.get(s));
						}
					}
				}
			}
		}
				
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static HashMap<String,Double> proportionHigher(ArrayList<Double> lstValues, ArrayList<String> lstCategories){
		
		//lst1 = ranks
		//mapR = map from category name to sum of ranks
		//mapN = sample size for each category
		//mapOut = output
		//dSum = total number of pairs
		//dN = current value of n
		
		ArrayList<Double> lst1;
		HashMap_AdditiveDouble<String> mapR;
		HashMap_AdditiveInteger<String> mapN;
		HashMap<String,Double> mapOut;
		double dSum;
		double dN;
		
		lst1 = Ranks.ranksAverage(lstValues);
		mapR = new HashMap_AdditiveDouble<String>(2);
		mapN = new HashMap_AdditiveInteger<String>(2);
		for(int i=0;i<lst1.size();i++){
			mapR.putSum(lstCategories.get(i), lst1.get(i));
			mapN.putSum(lstCategories.get(i), 1);
		}
		dSum = 1;
		for(String s:mapN.keySet()){
			dSum*=mapN.get(s);
		}
		mapOut = new HashMap<String,Double>(2);
		for(String s:mapR.keySet()){
			dN = mapN.get(s);
			mapOut.put(s, (mapR.get(s)-dN*(dN+1)/2)/dSum);
		}
		return mapOut;
	}
	
	private static ArrayList<Double> bootstrapResample(ArrayList<Double> lst1, ArrayList<String> lstCategories, ArrayListMultimap<String,Integer> mapCategories, Random rnd1){
		
		//i1 = current random index
		//lst2 = output
		
		int i1;
		ArrayList<Double> lst2;
		
		lst2 = new ArrayList<Double>(lst1.size());
		for(String s:lstCategories){
			i1 = mapCategories.get(s).get(rnd1.nextInt(mapCategories.get(s).size()));
			lst2.add(lst1.get(i1));
		}
		return lst2;
	}
}