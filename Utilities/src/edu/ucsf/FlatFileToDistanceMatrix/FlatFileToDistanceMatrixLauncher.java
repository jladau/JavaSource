package edu.ucsf.FlatFileToDistanceMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class FlatFileToDistanceMatrixLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from names to row/column indices
		//rgd1 = output
		//i1 = counter
		//s1 = current name
		//rgi1 = current indices
		//lst1 = output
		//lst2 = mapping output
		//sbl1 = string builder
		
		StringBuilder sbl1;
		HashMap<String,Integer> map1;
		double rgd1[][];
		int rgi1[];
		int i1;
		ArgumentIO arg1;
		DataIO dat1;
		String s1;
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		
		//initializing arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = new HashMap<String,Integer>(dat1.iRows);
		
		//loading map
		i1 = 0;
		for(int i=1;i<dat1.iRows;i++){
			for(String s:new String[]{"SAMPLE_1","SAMPLE_2"}){	
				s1 = dat1.getString(i, s);
				if(!map1.containsKey(s1)){
					map1.put(s1, i1);
					i1++;
				}
			}
		}
		
		//loading output matrix
		rgd1 = new double[i1][i1];
		for(int i=1;i<dat1.iRows;i++){
			rgi1 = new int[]{
					map1.get(dat1.getString(i, "SAMPLE_1")),
					map1.get(dat1.getString(i, "SAMPLE_2"))};
			rgd1[rgi1[0]][rgi1[1]]=dat1.getDouble(i, arg1.getValueString("sDistanceHeader"));
			rgd1[rgi1[1]][rgi1[0]]=dat1.getDouble(i, arg1.getValueString("sDistanceHeader"));
		}
		
		//outputting results
		lst1 = new ArrayList<String>(rgd1.length);
		for(int i=0;i<rgd1.length;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<rgd1.length-1;j++){
				sbl1.append(rgd1[i][j] + ",");
			}
			sbl1.append(rgd1[i][rgd1.length-1]);
			lst1.add(sbl1.toString());
		}
		DataIO.writeToFile(lst1, arg1.getValueString("sOutputPath"));
		lst2 = new ArrayList<String>(map1.size()+1);
		lst2.add("SAMPLE,ROW_COLUMN_INDEX");
		for(String s:map1.keySet()){
			lst2.add(s + "," + map1.get(s));
		}
		DataIO.writeToFile(lst2, arg1.getValueString("sOutputPath").replaceAll(".csv", "-row-column-map.csv"));
		System.out.println("Done.");	
	}
}
