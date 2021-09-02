package edu.ucsf.CorrelationsPairwise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Ranks;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Finds pairwise correlations
 * @author jladau
 *
 */

public class CorrelationsPairwiseLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgs1 = list of first set of variables
		//rgs2 = list of second set of variables
		//lst1 = first set of values
		//lst2 = second set of variables
		//lst3 = transformed variables
		//lst4 = null values
		//lstOut = output
		//sMode = mode
		//sResponseTransform = response transform
		//sPredictorTransform = predictor transform
		//d1 = coordinate counter
		//map1 = map from observed correlations to variable pair names
		//map2 = map from observed correlations to null correlations
		//dCor = current correlation
		
		double dCor;
		double d1;
		ArgumentIO arg1;
		DataIO dat1;
		String rgs1[];
		String rgs2[];
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<Double> lst3;
		ArrayList<Double> lst4;
		ArrayList<String> lstOut;
		String sMode;
		String sTransform1;
		String sTransform2;
		TreeMap<Double,String> map1;
		HashMap<Double,ArrayList<Double>> map2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgs1 = arg1.getValueStringArray("rgsVariables1");
		rgs2 = arg1.getValueStringArray("rgsVariables2");
		if(arg1.containsArgument("sMode")){
			sMode=arg1.getValueString("sMode");
		}else{
			sMode="pearson";
		}
		lstOut = new ArrayList<String>(1000);
		if(sMode.equals("pearson")){
			lstOut.add("VARIABLE_1,VARIABLE_2,TYPE,COLOR_CODE,COORDINATE,PEARSON_CORRELATION");
		}else if(sMode.equals("spearman")){
			lstOut.add("VARIABLE_1,VARIABLE_2,TYPE,COLOR_CODE,COORDINATE,SPEARMAN_CORRELATION");
		}
		if(arg1.containsArgument("sTransform1")){
			sTransform1=arg1.getValueString("sTransform1");
		}else{
			sTransform1="identity";
		}
		if(arg1.containsArgument("sTransform2")){
			sTransform2=arg1.getValueString("sTransform2");
		}else{
			sTransform2="identity";
		}
		
		//looping through correlations
		map1 = new TreeMap<Double,String>();
		map2 = new HashMap<Double,ArrayList<Double>>(100*rgs1.length*rgs2.length);
		for(String s:rgs1){
			lst1 = dat1.getDoubleColumn(s);
			if(sTransform1.equals("log")){
				lst3 = new ArrayList<Double>(lst1.size());
				for(int i=0;i<lst1.size();i++){
					lst3.add(Math.log(lst1.get(i)+1.));
				}
				lst1 = lst3;
			}
			for(String t:rgs2){
				
				System.out.println("Finding correlation between " + s + " and " + t + "...");
				
				lst2 = dat1.getDoubleColumn(t);
				if(sTransform1.equals("log")){
					lst3 = new ArrayList<Double>(lst2.size());
					for(int i=0;i<lst2.size();i++){
						lst3.add(Math.log(lst2.get(i)+1.));
					}
					lst2 = lst3;
				}
				dCor = Double.NaN;
				try{
					if(sMode.equals("pearson")){
						dCor = ExtendedMath.pearson(lst1, lst2);	
					}else if(sMode.equals("spearman")){
						dCor = ExtendedMath.spearman(lst1, lst2);
					}
				}catch(Exception e){
				}
				if(!Double.isNaN(dCor)){
					map2.put(dCor, nullValues(lst1,lst2,sMode,arg1.getValueInt("iNullIterations")));
					map1.put(dCor, s + "," + t);
				}
			}
		}
		
		d1 = 1;
		for(Double d: map1.descendingKeySet()){
			for(Double e:map2.get(d)){
				lstOut.add(map1.get(d) + ",null_value,0," + (d1 + Math.random() - 0.5) + "," + e);	
			}
			d1+=2;
		}
		d1 = 1;
		for(Double d: map1.descendingKeySet()){
			lstOut.add(map1.get(d) + ",observation,1," + (d1 + Math.random() - 0.5) + "," + d);
			d1+=2;
		}
	
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<Double> nullValues(ArrayList<Double> lst1, ArrayList<Double> lst2, String sMode, int iIterations){

		//lst3 = list of null correlation statistics
		
		ArrayList<Double> lst3;
		
		lst3 = new ArrayList<Double>(iIterations);
		for(int i=0;i<iIterations;i++){
			Collections.shuffle(lst1);
			if(sMode.equals("pearson")){
				lst3.add(ExtendedMath.pearson(lst1, lst2));
			}else if(sMode.equals("spearman")){
				lst3.add(ExtendedMath.spearman(lst1, lst2));
			}
		}
		return lst3;
	}
	
	private static HashMap<String,Double> nullDistribution(ArrayList<Double> lst1, ArrayList<Double> lst2, String sMode, int iIterations){
		
		//map1 = output
		//lst3 = list of null correlation statistics
		//lst4 = list of ranks
		//map2 = map from ranks to values
		
		HashMap<String,Double> map1;
		ArrayList<Double> lst3;
		ArrayList<Double> lst4;
		TreeMap<Double,Double> map2;
		
		map1 = new HashMap<String,Double>(10);
		lst3 = nullValues(lst1, lst2, sMode, iIterations);
		map1.put("mean", ExtendedMath.mean(lst3));
		lst4 = Ranks.ranksAverage(lst3);
		map2 = new TreeMap<Double,Double>();
		for(int i=0;i<lst4.size();i++){
			map2.put(lst4.get(i), lst3.get(i));
		}
		map1.put("minimum", map2.firstEntry().getValue());
		map1.put("maximum", map2.lastEntry().getValue());
		map1.put("97.5_quartile", map2.get(map2.ceilingKey(0.975*iIterations)));
		map1.put("2.5_quartile", map2.get(map2.floorKey(0.025*iIterations)));
		return map1;
	}
}