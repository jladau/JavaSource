package edu.ucsf.LinearModelsWithinCategories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.LinearModel;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class LinearModelsWithinCategoriesLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//sCategory = category header
		//set1 = set of predictors
		//sResponse = response
		//tbl1 = current table of data
		//lnm1 = current linear model
		//bNoIntercept = flag for whether to include intercept
		//map1 = current coefficients
		//lstOut = output
		//s1 = current category
		
		LinearModel lnm1;
		ArgumentIO arg1;
		DataIO dat1;
		String sCategory;
		HashSet<String> set1;
		String sResponse;
		HashBasedTable<String,String,Double> tbl1;
		boolean bNoIntercept;
		HashMap<String,Double> map1;
		String s1;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategory");
		sResponse = arg1.getValueString("sResponse");
		set1 = arg1.getValueHashSet("setPredictors");
		bNoIntercept = arg1.getValueBoolean("bNoIntercept");
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("CATEGORY,COEFFICIENT,VALUE");
		s1 = null;
		tbl1 = HashBasedTable.create(3,365);
		
		//looping through categories
		for(int i=1;i<dat1.iRows;i++){
			if(i!=1 && !dat1.getString(i,sCategory).equals(dat1.getString(i-1,sCategory)) ){
				if(tbl1.columnKeySet().size()>2){
					lnm1 = new LinearModel(tbl1,sResponse,set1,bNoIntercept);
					lnm1.fitModel(set1);
					map1 = lnm1.findCoefficientEstimates();
					for(String s:map1.keySet()){
						lstOut.add(s1 + "," + s + "," + map1.get(s));
					}
				}
				tbl1 = HashBasedTable.create(3,365);
			}
			tbl1.put(sResponse,Integer.toString(i),dat1.getDouble(i,sResponse));
			for(String s:set1){
				tbl1.put(s,Integer.toString(i),dat1.getDouble(i,s));
			}
			s1 = dat1.getString(i,sCategory);
		}
		lnm1 = new LinearModel(tbl1,sResponse,set1,bNoIntercept);
		lnm1.fitModel(set1);
		map1 = lnm1.findCoefficientEstimates();
		for(String s:map1.keySet()){
			lstOut.add(s1 + "," + s + "," + map1.get(s));
		}
		
		//outputting results
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
