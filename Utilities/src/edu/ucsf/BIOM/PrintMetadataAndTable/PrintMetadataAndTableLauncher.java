package edu.ucsf.BIOM.PrintMetadataAndTable;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class PrintMetadataAndTableLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//lstOut = output
		//lstSampleMetadata = sample metadata
		//lstTable = table
		//rgl1 = string builder array
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		BiomIO bio1;
		ArrayList<String> lstSampleMetadata;
		ArrayList<String> lstTable;
		StringBuilder rgl1[];
		String rgs1[];
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		lstSampleMetadata = bio1.printMetadata(bio1.axsSample);
		lstTable = bio1.printTable();
		
		//outputting results
		rgl1 = new StringBuilder[lstSampleMetadata.size()];
		for(int i=0;i<rgl1.length;i++){
			rgl1[i] = new StringBuilder();
		}
		for(String s:lstTable){
			if(s.startsWith("#")){
				continue;
			}
			rgs1=s.split(",");
			for(int i=0;i<rgs1.length;i++){
				rgl1[i].append("," + rgs1[i]);
			}
		}
		lstOut = new ArrayList<String>(lstSampleMetadata.size());
		for(int i=0;i<rgl1.length;i++){
			lstOut.add(lstSampleMetadata.get(i) + rgl1[i].toString());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
