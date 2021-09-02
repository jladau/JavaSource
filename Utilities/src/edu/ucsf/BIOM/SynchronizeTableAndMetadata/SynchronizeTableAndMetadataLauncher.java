package edu.ucsf.BIOM.SynchronizeTableAndMetadata;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Synchronizes samples in OTU tale and metadata: checks that both have the same sets of samples
 * @author jladau
 */

public class SynchronizeTableAndMetadataLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datOTU = OTU table data
		//datMeta = metadata table
		//setOTUSamples = set of samples in OTU table
		//setMetaSamples = set of samples in metadata file
		//setIntersectSamples = set of samples in both OTU table and metadata file
		//lstMeta = metadata output
		//lstOTU = otu table output
		//sbl1 = current otu table line
		
		StringBuilder sbl1;
		ArgumentIO arg1;
		DataIO datOTU;
		DataIO datMeta;
		HashSet<String> setOTUSamples;
		HashSet<String> setMetaSamples;
		HashSet<String> setIntersectSamples;
		ArrayList<String> lstMeta;
		ArrayList<String> lstOTU;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datOTU = new DataIO(arg1.getValueString("sOTUTablePath"));
		datMeta = new DataIO(arg1.getValueString("sMetadataPath"));
		setOTUSamples = new HashSet<String>(datOTU.iCols);
		setMetaSamples = new HashSet<String>(datMeta.iRows);
		setIntersectSamples = new HashSet<String>(datMeta.iRows);
		lstMeta = new ArrayList<String>(datMeta.iRows);
		lstOTU = new ArrayList<String>(datOTU.iRows);
		
		//loading sets of samples
		for(int j=1;j<datOTU.iCols-1;j++){
			setOTUSamples.add(datOTU.getString(0, j));
		}
		for(int i=1;i<datMeta.iRows;i++){
			setMetaSamples.add(datMeta.getString(i, "SampleID"));
		}
		for(String s:setOTUSamples){
			if(setMetaSamples.contains(s)){
				setIntersectSamples.add(s);
			}
		}
		
		//loading metadata to output
		lstMeta.add(Joiner.on(",").join(datMeta.getRow(0)));
		for(int i=1;i<datMeta.iRows;i++){
			if(setIntersectSamples.contains(datMeta.getString(i, "SampleID"))){
				lstMeta.add(Joiner.on(",").join(datMeta.getRow(i)));
			}
		}
		
		//loading OTU table to output
		sbl1 = new StringBuilder();
		sbl1.append(datOTU.getString(0, 0));
		for(int j=1;j<datOTU.iCols-1;j++){
			if(setIntersectSamples.contains(datOTU.getString(0, j))){
				sbl1.append("," + datOTU.getString(0, j));
			}
		}
		sbl1.append("," + datOTU.getString(0, datOTU.iCols-1));
		lstOTU.add(sbl1.toString());
		for(int i=1;i<datOTU.iRows;i++){
			sbl1 = new StringBuilder();
			sbl1.append(datOTU.getString(i, 0));
			for(int j=1;j<datOTU.iCols-1;j++){
				if(setIntersectSamples.contains(datOTU.getString(0, j))){
					sbl1.append("," + datOTU.getString(i, j));
				}
			}
			sbl1.append("," + datOTU.getString(i, datOTU.iCols-1));
			lstOTU.add(sbl1.toString());
		}
		
		//outputting results
		DataIO.writeToFile(lstMeta, arg1.getValueString("sOutputPathMetadata"));
		DataIO.writeToFile(lstOTU, arg1.getValueString("sOutputPathOTUTable"));
		System.out.println("Done.");
	}	
}
