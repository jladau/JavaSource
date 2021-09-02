package edu.ucsf.MergeSamplesOTUTable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.base.Joiner;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Merges samples in an OTU table so that counts are summed
 * @author jladau
 *
 */

public class MergeSamplesOTUTableLauncher {

	public static void main(String rgsArgs[]){
		
		//dat1 = table giving old sample IDs (first column) and new sample IDs (second column)
		//arg1 = arguments
		//bfr1 = buffered file reader
		//s1 = current line
		//mapIndex(sOldSampleID) = returns new index for given old sample ID
		//lstNewSampleIDs = list of new sample IDs
		//map1(sNewSampleID) = returns new index for given new sample ID
		//lstOut = output
		//rgs1 = current output line
		//rgd1 = current merged line
		//rgsOldHeader = old header
		//iIndex = current index
		
		int iIndex;
		ArgumentIO arg1;
		DataIO dat1;
		BufferedReader bfr1;
		String s1;
		ArrayList<String> lstNewSampleIDs;
		HashMap<String,Integer> mapIndex;
		HashMap<String,Integer> map1;
		ArrayList<String> lstOut;
		String rgs1[];
		String rgsOldHeader[];
		Double rgd1[];
		
		//loading input
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSampleMapPath"));
		
		//loading list of new sample IDs
		mapIndex = new HashMap<String,Integer>(dat1.iRows);
		lstNewSampleIDs = new ArrayList<String>(dat1.iRows);
		map1 = new HashMap<String,Integer>(dat1.iRows);
		for(int i=0;i<dat1.iRows;i++){
			if(!map1.containsKey(dat1.getString(i,1))){
				lstNewSampleIDs.add(dat1.getString(i, 1));
				map1.put(dat1.getString(i, 1), lstNewSampleIDs.size()-1);
			}
			mapIndex.put(dat1.getString(i, 0), map1.get(dat1.getString(i,1)));
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("#\t" + Joiner.on("\t").join(lstNewSampleIDs) + "\t" + "taxonomy");
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		lstOut = new ArrayList<String>(1000);
		
		//looping through lines of data
		try{
			
			//initializing output	
			bfr1 = new BufferedReader(new FileReader(arg1.getValueString("sTablePath")));
			
			//loading header
			rgsOldHeader = bfr1.readLine().split("\\t");
			
			//loading additional lines
			while((s1=bfr1.readLine())!=null){
			
				rgs1 = s1.split("\\t");
				rgd1 = new Double[lstNewSampleIDs.size()];
				for(int j=1;j<rgs1.length-1;j++){
					iIndex = mapIndex.get(rgsOldHeader[j]);
					if(rgd1[iIndex]==null){
						rgd1[iIndex]=Double.parseDouble(rgs1[j]);
					}else{
						rgd1[iIndex]+=Double.parseDouble(rgs1[j]);
					}
				}
				lstOut.add(rgs1[0] + "\t" + Joiner.on("\t").join(rgd1) + "\t" + rgs1[rgs1.length-1]);
				if(lstOut.size()==1000){
					DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
					lstOut = new ArrayList<String>(1000);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//terminating
		if(lstOut.size()>0){
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
		}
		System.out.println("Done.");
	}
	
}
