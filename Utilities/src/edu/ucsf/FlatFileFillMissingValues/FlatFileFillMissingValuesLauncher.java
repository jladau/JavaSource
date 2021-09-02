package edu.ucsf.FlatFileFillMissingValues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Fills in missing values in flat file
 * @author jladau
 */

public class FlatFileFillMissingValuesLauncher {

	//TODO move most of this code to a separate flatfile class and add unit tests
	
	public static void main(String rgsArgs[]){
		
		//dat1 = flat file
		//arg1 = arguments
		//set1 = set of all included key combinations
		//map1 = returns sets of keys
		//sbl1 = current key combination
		//lstOut = output
		//lst1 = list of sets of keys (for cartesian product)
		//setCartesian = cartesian product
		//sValue = replacement value
		//s1 = current candidate output string
		
		String s1;
		DataIO dat1;
		ArgumentIO arg1;
		HashSet<String> set1;
		StringBuilder sbl1;
		HashMultimap<String,String> map1;
		ArrayList<String> lstOut;
		ArrayList<HashSet<String>> lst1;
		Set<List<String>> setCartesian;
		String sValue;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading keys and key combinations
		set1 = new HashSet<String>();
		map1 = HashMultimap.create();
		for(int i=1;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(String s:arg1.getValueStringArray("rgsKeyHeaders")){
				sbl1.append(dat1.getString(i, s) + ",");
				map1.put(s, dat1.getString(i, s));
			}
			set1.add(sbl1.toString());
		}
		
		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows*2);
		for(int i=0;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(String s:arg1.getValueStringArray("rgsKeyHeaders")){
				sbl1.append(dat1.getString(i,s) + ",");
			}
			sbl1.append(dat1.getString(i,arg1.getValueString("sValueHeader")));
			lstOut.add(sbl1.toString());
		}
		
		//loading cartesian product
		lst1 = new ArrayList<HashSet<String>>();
		for(String s:arg1.getValueStringArray("rgsKeyHeaders")){
			lst1.add(new HashSet<String>(map1.get(s)));
		}
		setCartesian = Sets.cartesianProduct(lst1);
		
		//looping through products and appending values if they are not included
		sValue = arg1.getValueString("sFillValue");
		for(List<String> lst:setCartesian){

			//loading product in string format
			s1=Joiner.on(",").join(lst);
			if(!set1.contains(s1 + ",")){
				lstOut.add(s1 + ","  + sValue);
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}