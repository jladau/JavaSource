package edu.ucsf.FindIdenticalColumns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class FindIdenticalColumnsLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//set1 = set of columns
		//itr1 = iterator
		//s1 = current element
		//set2 = set of sets of identical columns
		//set3 = current identical columns
		//set4 = set of used columns
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		HashSet<String> set1;
		HashSet<String> set3;
		HashSet<String> set4;
		Iterator<String> itr1;
		String s1;
		HashSet<HashSet<String>> set2;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		set1 = dat1.getHeaders();
		itr1 = set1.iterator();
		set2 = new HashSet<HashSet<String>>(dat1.iRows);
		set4 = new HashSet<String>(dat1.iRows);
		while(itr1.hasNext()){
			s1 = itr1.next();
			if(set4.contains(s1)){
				continue;
			}
			set3 = new HashSet<String>(dat1.iRows);
			for(String s:set1){
				if(!s.equals(s1) && !set4.contains(s)){
					if(columnsIdentical(dat1,s,s1)){
						set3.add(s1);
						set3.add(s);
						set4.add(s1);
						set4.add(s);
					}
				}
			}
			set2.add(set3);
			set4.add(s1);
		}
		
		lstOut = new ArrayList<String>(set2.size()+1);
		for(HashSet<String> set:set2){
			lstOut.add(Joiner.on(",").join(set));
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static boolean columnsIdentical(DataIO dat1, String s1, String s2){
		
		for(int i=1;i<dat1.iRows;i++){
			//if(!dat1.getString(i,s1).equals(dat1.getString(i,s2))){
			if(dat1.getDouble(i,s1)!=dat1.getDouble(i,s2)){
				return false;
			}
		}
		return true;
	}
}
