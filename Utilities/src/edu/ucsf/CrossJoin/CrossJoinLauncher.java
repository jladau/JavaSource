package edu.ucsf.CrossJoin;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * This code appends data to a given file by matching data from a source file to it
 * @author jladau
 */

public class CrossJoinLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat0 = data set to be appended to
		//dat1 = data set to append
		//sKey = join key
		//map1 = map from join field values to rows with values for dat1
		//rgs1 = list of headers from dat1 to output
		//i1 = counter
		//s1 = current value
		//s2 = current line from dat1 being output
		
		String s1;
		String s2;
		ArgumentIO arg1;
		DataIO dat0; 
		DataIO dat1;
		String sKey;
		HashMultimap<String,Integer> map1;
		String rgs1[];
		int i1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		sKey = arg1.getValueString("sJoinField");
		
		//loading data sets
		dat0 = new DataIO(arg1.getValueString("sDataPath1"));
		dat1 = new DataIO(arg1.getValueString("sDataPath2"));
		
		//loading map to indices with key field values
		map1 = HashMultimap.create(dat1.iRows, dat1.iRows/2);
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, sKey), i);
		}
		
		//loading list of headers to output
		i1 = 0;
		rgs1 = new String[dat1.iCols-1];
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0,j).equals(sKey)){
				rgs1[i1] = dat1.getString(0, j);
				i1++;
			}
		}
		
		//outputting header
		System.out.println(Joiner.on(",").join(dat0.getRow(0)) + "," + Joiner.on(",").join(rgs1));
		
		//looping through pairs of rows
		for(int i=1;i<dat0.iRows;i++){
			s1 = dat0.getString(i,sKey);
			if(map1.containsKey(s1)){
				for(int l:map1.get(s1)){
					s2 = dat1.getString(l, rgs1);
					System.out.println(Joiner.on(",").join(dat0.getRow(i)) + "," + s2);
				}
			}
		}
	}
}