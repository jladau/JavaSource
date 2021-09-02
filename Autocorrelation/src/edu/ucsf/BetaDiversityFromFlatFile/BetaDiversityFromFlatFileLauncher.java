package edu.ucsf.BetaDiversityFromFlatFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Loads beta-diversity from a flat file
 * @author jladau
 *
 */

public class BetaDiversityFromFlatFileLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//tbl1 = rows represent taxa, columns represent samples
		//set1 = set of pairs of samples that have been examined
		//lstOut = output
		//mapTotal = total
		
		HashMap_AdditiveDouble<String> mapTotal;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		HashBasedTable<String,String,Double> tbl1;
		HashSet<String> set1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
	
		//loading totals, if requested
		mapTotal = new HashMap_AdditiveDouble<String>();
		for(int i=1;i<dat1.iRows;i++){
			mapTotal.putSum(
					dat1.getString(i, arg1.getValueString("sSampleHeader")), 
					dat1.getDouble(i, arg1.getValueString("sValueHeader")));
		}
		
		//loading data table
		tbl1 = HashBasedTable.create();
		for(int i=1;i<dat1.iRows;i++){
			if(arg1.getValueBoolean("bNormalize")==false || dat1.getDouble(i, arg1.getValueString("sValueHeader"))==0){
				tbl1.put(
						dat1.getString(i, arg1.getValueString("sTaxonHeader")), 
						dat1.getString(i, arg1.getValueString("sSampleHeader")), 
						dat1.getDouble(i, arg1.getValueString("sValueHeader")));
			}else{
				tbl1.put(
						dat1.getString(i, arg1.getValueString("sTaxonHeader")), 
						dat1.getString(i, arg1.getValueString("sSampleHeader")), 
						dat1.getDouble(i, arg1.getValueString("sValueHeader"))/mapTotal.get(dat1.getString(i, arg1.getValueString("sSampleHeader"))));
			}
		}
				
		//looping through pairs of samples
		set1 = new HashSet<String>(tbl1.columnKeySet().size()*tbl1.columnKeySet().size());
		lstOut = new ArrayList<String>(tbl1.columnKeySet().size()*tbl1.columnKeySet().size());
		lstOut.add("SAMPLE_1,SAMPLE_2,BRAY_CURTIS");
		for(String s:tbl1.columnKeySet()){
			for(String t:tbl1.columnKeySet()){
				if(!s.equals(t)){
					if(!set1.contains(s + "," + t) && !set1.contains(t + "," + s)){
						lstOut.add(s + "," + t + "," + calculateBrayCurtis(s,t,tbl1));
						set1.add(s + "," + t);
						set1.add(t + "," + s);
					}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double calculateBrayCurtis(String sSample1, String sSample2, HashBasedTable<String,String,Double> tbl1){
		
		//d1 = count for sample 1
		//d2 = count for sample 2
		//dNum = bray-curtis numerator
		//dDen = bray-curtis denominator
		//map1 = first column
		//map2 = second column
		
		Map<String,Double> map1;
		Map<String,Double> map2;
		double d1;
		double d2;
		double dNum=0;
		double dDen=0;
		
		//loading components
		map1 = tbl1.column(sSample1);
		map2 = tbl1.column(sSample2);
		
		for(String s:map1.keySet()){
			d1 = map1.get(s);
			if(map2.containsKey(s)){
				d2=map2.get(s);
			}else{
				d2=0;
			}
			dNum+=Math.abs(d1-d2);
			dDen+=(d1+d2);
		}
		for(String s:map2.keySet()){
			if(!map1.containsKey(s)){
				d1 = 0;
				d2 = map2.get(s);
				dNum+=Math.abs(d1-d2);
				dDen+=(d1+d2);
			}
		}
				
		if(dDen>0){
			return dNum/dDen;
		}else{		
			return 0;
		}
	}
}
