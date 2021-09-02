package edu.ucsf.Trees.ExtractLineages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class ExtractLineagesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = tree
		//map1 = map from child nodes to parent nodes
		//set1 = set of leaf vertices
		//lstOut = output
		//s1 = current lineage id
		//s2 = current node
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,String> map1;
		HashSet<String> set1;
		ArrayList<String> lstOut;
		String s1;
		String s2;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading tree
		map1 = new HashMap<String,String>(dat1.iRows-1);
		set1 = new HashSet<String>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, "VERTEX_CHILD"), dat1.getString(i, "VERTEX_PARENT"));
			set1.add(dat1.getString(i, "VERTEX_CHILD"));
			if(set1.contains(dat1.getString(i, "VERTEX_PARENT"))){
				set1.remove(dat1.getString(i, "VERTEX_PARENT"));
			}
		}
		
		//loading lineages
		lstOut = new ArrayList<String>(map1.size()*map1.size());
		lstOut.add("LINEAGE_ID,NODE_ID");
		for(String s:set1){
			s1 = "lineage_" + s;
			s2 = s;
			while(s2!=null){
				lstOut.add(s1 + "," + s2);
				if(map1.containsKey(s2)){
					s2 = map1.get(s2);
				}else{
					s2 = null;
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
