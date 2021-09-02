package edu.ucsf.MeansWithinCategories;

import java.util.ArrayList;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MeansWithinCategories{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data
		//sCategory = category field
		//mapSum = map from category values to sums
		//mapN = map from category values to counts
		//lst1 = list of category names
		//lst2 = output in stringbuilder form
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		String sCategory;
		HashMap_AdditiveDouble<String> mapSum;
		HashMap_AdditiveDouble<String> mapN;
		ArrayList<String> lst1;
		ArrayList<StringBuilder> lst2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategory");
		
		//loading category counts
		mapN = new HashMap_AdditiveDouble<String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			mapN.putSum(dat1.getString(i,sCategory),1.);
		}
		lst1 = new ArrayList<String>(mapN.keySet());
		lst2 = new ArrayList<StringBuilder>(lst1.size()+1);
		lst2.add(new StringBuilder());
		lst2.get(0).append(sCategory);
		for(int i=0;i<lst1.size();i++) {
			lst2.add(new StringBuilder());
			lst2.get(i+1).append(lst1.get(i));
		}
		
		//looping through data columns and outputting results
		for(String s:dat1.getHeaders()) {
			if(!s.equals("sCategory")){
				mapSum = new HashMap_AdditiveDouble<String>(dat1.iRows);
				for(int i=1;i<dat1.iRows;i++){
					mapSum.putSum(dat1.getString(i,sCategory),dat1.getDouble(i,s));
				}
				lst2.get(0).append("," + s);
				for(int i=0;i<lst1.size();i++) {
					lst2.get(i+1).append("," + mapSum.get(lst1.get(i))/mapN.get(lst1.get(i)));
				}
			}
		}
		
		
		
		//outputting results
		lstOut = new ArrayList<String>(lst2.size());
		for(int i=0;i<lst2.size();i++) {
			lstOut.add(lst2.get(i).toString());
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
