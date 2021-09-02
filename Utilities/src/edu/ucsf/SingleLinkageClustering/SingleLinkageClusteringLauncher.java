package edu.ucsf.SingleLinkageClustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class SingleLinkageClusteringLauncher{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data
		//sObj1 = object 1 header
		//sObj2 = object 2 header
		//sValue = value header
		//map1 = map from objects to clusters
		//i1 = cluster counter
		//dThreshold = threshold for clustering
		//s1 = current first object
		//s2 = current second object
		//iCluster1 = cluster of first object
		//iCluster2 = cluster of second object
		//set1 = set of objects
		//lstOut = output
		//lst1 = list of objects
		
		ArrayList<String> lst1;
		ArrayList<String> lstOut;
		HashSet<String> set1;
		String s1;
		String s2;
		double dThreshold;
		ArgumentIO arg1;
		DataIO dat1;
		String sObj1;
		String sObj2;
		String sValue;
		HashMap<String,Integer> map1;
		int i1;
		int iCluster1;
		int iCluster2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sObj1 = arg1.getValueString("sObjectHeader1");
		sObj2 = arg1.getValueString("sObjectHeader2");
		sValue = arg1.getValueString("sValueHeader");
		map1 = new HashMap<String,Integer>(dat1.iRows);
		dThreshold = arg1.getValueDouble("dThreshold");
		i1 = 1;
		set1 = new HashSet<String>(dat1.iRows);
		
		//looping through pairs of observations
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getDouble(i,sValue)>dThreshold) {
				s1 = dat1.getString(i,sObj1);
				s2 = dat1.getString(i,sObj2);
				iCluster1 = -9999;
				iCluster2 = -9999;
				if(map1.containsKey(s1)) {
					iCluster1 = map1.get(s1);
				}
				if(map1.containsKey(s2)) {
					iCluster2 = map1.get(s2);
				}
				if(iCluster1>0 && iCluster2<0) {
					map1.put(s2,iCluster1);
				}else if(iCluster1<0 && iCluster2>0) {
					map1.put(s1,iCluster2);
				}else if(iCluster1<0 && iCluster2<0) {
					map1.put(s1,i1);
					map1.put(s2,i1);
					i1++;
				}else if(iCluster1>0 && iCluster2>0) {
					if(iCluster1!=iCluster2){
						for(String s:set1) {
							if(map1.get(s)==iCluster1 || map1.get(s)==iCluster2) {
								map1.put(s,i1);
							}
						}
					}
					i1++;
				}
				set1.add(s1);
				set1.add(s2);
			}
		}
		
		//updating other observations
		lst1 = dat1.getStringColumn(sObj1);
		lst1.addAll(dat1.getStringColumn(sObj2));
		for(String s:lst1) {
			if(!map1.containsKey(s)){
				map1.put(s,i1);
				i1++;
			}
		}
		
		
		//outputting results
		lstOut = new ArrayList<String>(map1.size());
		lstOut.add("OBJECT,CLUSTER");
		for(String s:map1.keySet()) {
			lstOut.add(s + ",c" + map1.get(s));
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}