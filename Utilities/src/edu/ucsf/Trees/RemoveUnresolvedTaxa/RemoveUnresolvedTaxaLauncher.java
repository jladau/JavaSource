package edu.ucsf.Trees.RemoveUnresolvedTaxa;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code removes all taxa that are formatted as 'XXXX; XXXX; ...' from a Newick file and individual taxa listed in these strings as well (these are indicative of unresolved taxa)
 * @author jladau
 *
 */

public class RemoveUnresolvedTaxaLauncher {

	
	
	public static void main(String rgsArgs[]) throws Exception{
		
		//lst1 = input list
		//arg1 = arguments
		//lstOut = output
		//set1 = list of taxa to omit
		//rgs1 = current set of taxa
		//s1 = current string
		//map1 = count for current taxon
		
		String rgs1[];
		HashSet<String> set1;
		ArrayList<String> lst1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		String s1;
		HashMap_AdditiveInteger<String> map1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		lst1 = DataIO.readFileNoDelimeter(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(lst1.size());
		set1 = new HashSet<String>(1000);
		map1 = new HashMap_AdditiveInteger<String>(1000);
		
		//loading list of taxa to exclude
		for(String s:lst1){
			rgs1 = extractTaxa(s);
			if(rgs1!=null){
				if(rgs1.length>1){
					for(String t:rgs1){
						set1.add(t);
					}
				}
				for(String t:rgs1){
					map1.putSum(t, 1);
				}
			}
		}
		for(String s:map1.keySet()){
			if(map1.get(s)>1){
				set1.add(s);
			}
		}
		
		//looping through lines
		for(int i=0;i<lst1.size();i++){
			s1 = lst1.get(i);
			rgs1 = extractTaxa(s1);
			if(rgs1!=null){
				if(rgs1.length>1){
					s1 = removeTaxa(s1);
				}else{
					if(set1.contains(rgs1[0])){
						s1 = removeTaxa(s1);
					}
				}
			}
			lstOut.add(s1);
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static String removeTaxa(String s1){
		
		//rgs1 = s1 in split format
		//sbl1 = output
		
		String rgs1[];
		StringBuilder sbl1;
		
		rgs1 = s1.split(":");
		sbl1 = new StringBuilder();
		sbl1.append(")");
		for(int i=1;i<rgs1.length;i++){
			sbl1.append(":" + rgs1[i]);
		}
		return sbl1.toString();
	}
	
	
	private static String[] extractTaxa(String s1){
		
		//rgs1 = output
		//s2 = current string
		
		String rgs1[];
		String s2;
		
		if(!s1.contains("__")){
			return null;
		}
		rgs1 = s1.split(";");
		for(int i=0;i<rgs1.length;i++){
			s2 = rgs1[i];
			s2 = s2.split(":")[0];
			s2 = s2.replace(")","");
			s2 = s2.replace("(","");
			s2 = s2.trim();
			rgs1[i] = s2;
		}
		return rgs1;
	}
}
