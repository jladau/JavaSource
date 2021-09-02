package edu.ucsf.Trees.NodeDepths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class NodeDepthsLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from children to parents
		//map2 = current depth for each vertex
		//set1 = list of all vertices
		//i1 = current vertex
		//i2 = current depth
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<Integer,Integer> map1;
		HashSet<Integer> set1;
		int i1;
		int i2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = new HashMap<Integer,Integer>(dat1.iRows);
		set1 = new HashSet<Integer>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getInteger(i, "VERTEX_CHILD"), dat1.getInteger(i, "VERTEX_PARENT"));
			set1.add(dat1.getInteger(i, "VERTEX_CHILD"));
			set1.add(dat1.getInteger(i, "VERTEX_PARENT"));
		}
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("VERTEX,DEPTH");
		
		//loading depths
		for(Integer i:set1){
			i1 = i;
			i2 = 0;
			while(map1.containsKey(i1)){
				i1=map1.get(i1);
				i2++;
			}
			lstOut.add(i + "," + i2);
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
