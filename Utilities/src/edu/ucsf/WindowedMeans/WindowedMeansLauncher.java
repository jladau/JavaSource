package edu.ucsf.WindowedMeans;
import java.util.ArrayList;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.base.ExtendedMath;

/**
 * Finds successive slopes within windows. Assumes that data are sorted in ascending x order.
 * @author jladau
 *
 */

public class WindowedMeansLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//i1 = window size
		//lst1 = current list of x-values
		//lst2 = current list of y-values
		//lstOut = output
		//sX = x header
		//sY = y header
		//tbl1 = current data
		//sCat = category header
		//iMin = current minimum index
		//dMin = minimum value
		
		double dMin;
		int iMin;
		ArgumentIO arg1;
		DataIO dat1;
		int i1;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		ArrayList<String> lstOut;
		String sX;
		String sY;
		String sCat;
		HashBasedTable<Integer,String,Double> tbl1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		i1 = arg1.getValueInt("iWindowSize");
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		if(arg1.containsArgument("sCategoryHeader")){
			sCat = arg1.getValueString("sCategoryHeader");
		}else{
			sCat = null;
		}

		//initializing output
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("CATEGORY,X_MEAN,X_MIN,X_MAX,MEAN"); 
				
		//looping through categories
		tbl1 = HashBasedTable.create(dat1.iRows,3);
		iMin = Integer.MAX_VALUE;
		for(int i=1;i<dat1.iRows;i++){
			tbl1.put(i,sX,dat1.getDouble(i,sX));
			tbl1.put(i,sY,dat1.getDouble(i,sY));
			if(iMin==Integer.MAX_VALUE){
				iMin = i;
			}	
			if(i==dat1.iRows-1 || (sCat!=null && !dat1.getString(i,sCat).equals(dat1.getString(i+1,sCat)))){
				for(int l=iMin;l<=i;l++){
					if(l + i1 > i){
						break;
					}
					lst1 = new ArrayList<Double>(i1);
					lst2 = new ArrayList<Double>(i1);
					dMin = Double.MAX_VALUE;
					for(int k=0;k<i1;k++){
						lst1.add(tbl1.get(l+k, sX));
						if(tbl1.get(l+k, sX)<dMin){
							dMin = tbl1.get(l+k, sX);
						}
						lst2.add(tbl1.get(l+k, sY));
					}
					lstOut.add(dat1.getString(i,sCat) + "," + ExtendedMath.mean(lst1) + "," + dMin + "," + ExtendedMath.maximum(lst1) + "," + ExtendedMath.mean(lst2));
				}
				tbl1 = HashBasedTable.create(dat1.iRows,3);
				iMin = Integer.MAX_VALUE;
			}	
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
