package edu.ucsf.Trees.LineageTaxonomies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class LineageTaxonomiesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = tree
		//dat2 = leaf taxonomy path
		//map1 = map from child nodes to parent nodes
		//set1 = set of leaf vertices
		//lstOut = output
		//s2 = current node
		//map2 = keys are node ids, taxonomy levels (e.g., "3,FAMILY"), values are the values that have been found for children
		//rgsLevels = taxonomic levels
		//sStartLevel = starting level
		//iStartLevel = starting level index
		
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		HashMap<String,String> map1;
		HashSet<String> set1;
		ArrayList<String> lstOut;
		String s2;
		HashMultimap<String,String> map2;
		String rgsLevels[];
		String sStartLevel;
		int iStartLevel = 0;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sTreePath"));
		dat2 = new DataIO(arg1.getValueString("sLeafTaxonomyPath"));
		rgsLevels = new String[]{"kingdom","phylum","class","order","family","genus","species"};
		sStartLevel = arg1.getValueString("sStartingTaxonomicLevel");
		for(int i=0;i<rgsLevels.length;i++){
			if(rgsLevels[i].equals(sStartLevel)){
				iStartLevel = i;
				break;
			}
		}
		
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
		
		//loading classification information
		map2 = HashMultimap.create(7*dat1.iRows,10);
		lstOut = new ArrayList<String>(map1.size()*map1.size());
		lstOut.add("LINEAGE_ID,NODE_ID");
		for(int i=1;i<dat2.iRows;i++){
			s2 = dat2.getString(i, "NODE_ID");
			while(s2!=null){
				for(int j=0;j<=iStartLevel;j++){
					map2.put(s2 + "," + rgsLevels[j], dat2.getString(i, rgsLevels[j]));
				}
				
				if(map1.containsKey(s2)){
					s2 = map1.get(s2);
				}else{
					s2 = null;
				}
			}
		}
		
		//outputting classifications
		lstOut = new ArrayList<String>(map2.size()+1);
		lstOut.add("NODE_ID,TAXONOMIC_LEVEL,VALUE");
		for(String s:map2.keySet()){
			if(map2.get(s).size()==1){
				for(String t:map2.get(s)){
					lstOut.add(s + "," + t);
				}
			}else{
				lstOut.add(s + ",NA");
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
