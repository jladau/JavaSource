package edu.ucsf.RankDifferencesConfidenceInterval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import com.google.common.collect.ArrayListMultimap;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Ranks;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class RankDifferencesConfidenceIntervalLauncher {

	//TODO calculate U statistics and find bootstrap distributions -- gives the fraction of wins over other category
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sValue1 = first value field
		//sValue2 = second value field
		//sCategory = category field
		//lst1 = first list of values
		//lst2 = second list of values
		//lst3 = first list of bootstrapped values
		//lst4 = second list of bootstrapped values
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
		String sValue1;
		String sValue2;
		String sCategory;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		ArrayList<Double> lst4;
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
		sValue1 = arg1.getValueString("sValueField1");
		sValue2 = arg1.getValueString("sValueField2");
		sCategory = arg1.getValueString("sCategoryField");
		iIterations = arg1.getValueInt("iIterations");
		lstOut = new ArrayList<String>((iIterations+1)*dat1.iRows+1);
		lstOut.add("CATEGORY,TYPE,PAIR_1,PAIR_2,MEAN_DIFFERENCE");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		
		//loading list of unique categories
		if(arg1.getValueString("sMode").equals("all")){
			lstUnique = new ArrayList<String>(2);
			lstUnique.add("all");
			lstUnique.add("all");
		}else if(arg1.getValueString("sMode").equals("pairs")){
			lstUnique = new ArrayList<String>(100);
			for(int i=1;i<dat1.iRows;i++){
				if(!lstUnique.contains(dat1.getString(i, sCategory))){
					lstUnique.add(dat1.getString(i, sCategory));
				}
			}
		}else{
			lstUnique=null;
		}
		
		//looping through pairs of categories
		for(int k=1;k<lstUnique.size();k++){
			s1 = lstUnique.get(k);
			for(int j=0;j<k;j++){
				s2 = lstUnique.get(j);
				
				System.out.println("Analyzing " + s1 + " vs " + s2 + "...");
				
				//loading data
				if(s1.equals("all-9999") && s2.equals("all-9999")){
					lst1 = dat1.getDoubleColumn(sValue1);
					lst2 = dat1.getDoubleColumn(sValue2);
					lstCategories = dat1.getStringColumn(sCategory);
					map1 = ArrayListMultimap.create();
					for(int i=1;i<dat1.iRows;i++){
						map1.put(dat1.getString(i, sCategory),i-1);
					}
				}else{
					i1 = 0;
					lst1 = new ArrayList<Double>(dat1.iRows);
					lst2 = new ArrayList<Double>(dat1.iRows);
					lstCategories = new ArrayList<String>(dat1.iRows);
					map1 = ArrayListMultimap.create();
					for(int i=1;i<dat1.iRows;i++){
						if(dat1.getString(i, sCategory).equals(s1) || dat1.getString(i, sCategory).equals(s2)){
							lst1.add(dat1.getDouble(i,sValue1));
							lst2.add(dat1.getDouble(i,sValue2));
							lstCategories.add(dat1.getString(i,sCategory));
							map1.put(dat1.getString(i, sCategory),i1);
							i1++;
						}
					}
				}
				
				//finding and outputting observed distances
				map2 = meanDifferences(lst1, lst2, map1);
				for(String s:map2.keySet()){
					lstOut.add(s + ",observed," + s1 + "," + s2 + "," + map2.get(s));
				}
				
				//boostrapping
				for(int i=0;i<iIterations;i++){
					lst3 = bootstrapResample(lst1,lstCategories,map1,rnd1);
					lst4 = bootstrapResample(lst2,lstCategories,map1,rnd1);
					map2 = meanDifferences(lst3, lst4, map1);
					for(String s:map2.keySet()){
						lstOut.add(s + ",bootstrap," + s1 + "," + s2 + "," + map2.get(s));
					}
				}
			}
		}
				
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
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
	
	private static HashMap<String,Double> meanDifferences(ArrayList<Double> lst1, ArrayList<Double> lst2, ArrayListMultimap<String,Integer> mapCategories){
		
		//lst4 = first list of ranks
		//lst5 = second list of ranks
		//map1 = map of differences
		//map2 = output
		
		ArrayList<Double> lst4;
		ArrayList<Double> lst5;
		ArrayListMultimap<String,Double> map1;
		HashMap<String,Double> map2;
		
		lst4 = Ranks.ranksAverage(lst1);
		lst5 = Ranks.ranksAverage(lst2);
		map1 = ArrayListMultimap.create();
		for(String s:mapCategories.keySet()){
			for(Integer i:mapCategories.get(s)){
				map1.put(s, lst4.get(i)-lst5.get(i));
			}
		}
		map2 = new HashMap<String,Double>(map1.size());
		for(String s:map1.keySet()){
			map2.put(s, ExtendedMath.mean(new ArrayList<Double>(map1.get(s))));
		}
		return map2;
	}
}