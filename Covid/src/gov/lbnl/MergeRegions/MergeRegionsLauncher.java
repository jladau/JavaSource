package gov.lbnl.MergeRegions;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MergeRegionsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = mortality data
		//rgd1 = merged mortalities
		//tbl1 = output
		//set1 = list of regions to merge
		//set2 = list of regions not to merge
		//lst2 = output list of merged regions
		
		ArgumentIO arg1;
		DataIO dat1;
		double rgd1[];
		HashBasedTable<Integer,String,Double> tbl1;
		HashSet<String> set1;
		HashSet<String> set2;
		ArrayList<String> lst2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		set1 = null;
		
		//loading regions to merge based on mortality threshold
		if(arg1.containsArgument("dMortalityThreshold")){
			set1 = loadRegionsToMerge(arg1.getValueDouble("dMortalityThreshold"),dat1);
		}
		
		//loading regions based on positive list
		if(arg1.containsArgument("setRegionsToMerge")){
			set1 = arg1.getValueHashSet("setRegionsToMerge");
		}
		
		//loading regions based on negative list
		if(arg1.containsArgument("setRegionsNotToMerge")){
			set2 = arg1.getValueHashSet("setRegionsNotToMerge");
			set1 = new HashSet<String>(dat1.iCols);
			for(String s:dat1.getHeaders()){
				if(!set2.contains(s)){
					set1.add(s);
				}
			}
		}
		
		//merging columns as appropriate
		rgd1 = new double[dat1.iRows-1];
		tbl1 = HashBasedTable.create(dat1.iRows,dat1.iCols);
		for(String s:dat1.getHeaders()){
			if(set1.contains(s)){
				for(int i=1;i<dat1.iRows;i++){
					rgd1[i-1]+=dat1.getDouble(i,s);
				}
			}else{
				for(int i=1;i<dat1.iRows;i++){
					tbl1.put(i,s,dat1.getDouble(i,s));
				}
			}
		}
		if(set1.size()>0){
			for(int i=0;i<rgd1.length;i++){
				tbl1.put(i+1,"MERGED",rgd1[i]);
			}
		}
		
		//outputting results
		lst2 = new ArrayList<String>(dat1.iCols);
		lst2.add("REGION_ID");
		for(String s:set1){
			lst2.add(s);		
		}
		DataIO.writeToFile(lst2,arg1.getValueString("sOutputPath").replace(".csv","-merged-regions.csv"));
		DataIO.writeToFile((new DataIO(tbl1)).getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static HashSet<String> loadRegionsToMerge(double dThreshold, DataIO dat1){
		
		//set1 = output
		//d1 = current value
		
		HashSet<String> set1;
		double d1;
		
		set1 = new HashSet<String>(dat1.iCols);
		for(String s:dat1.getHeaders()){
			d1 = ExtendedMath.mean(dat1.getDoubleColumn(s));
			if(d1<dThreshold){
				set1.add(s);
			}
		}
		return set1;
	}
	
	
}