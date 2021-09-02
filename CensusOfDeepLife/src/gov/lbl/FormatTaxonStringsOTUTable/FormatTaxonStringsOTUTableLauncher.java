package gov.lbl.FormatTaxonStringsOTUTable;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Formats taxon strings in OTU table
 * @author jladau
 *
 */

public class FormatTaxonStringsOTUTableLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data

		ArgumentIO arg1;
		DataIO dat1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//outputting results
		for(int i=1;i<dat1.iRows;i++){
			dat1.setString(i, dat1.iCols-1, formatString(dat1.getString(i,dat1.iCols-1).split(";")));
		}
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static String formatString(String[] rgs1){
		
		//map1 = map from clades to taxon names
		//rgs2 = current clade-taxon string pair
		//rgs3 = map from input clade names to prefixes
		//sbl1 = output
		
		HashMap<String,String> map1;
		String rgs2[];
		String rgs3[][];
		StringBuilder sbl1;
		
		//loading map
		map1 = new HashMap<String,String>();
		for(String s:rgs1){
			rgs2 = s.replace("__", ",").replace("_unclassified", "").replaceAll("\\([0-9]*\\)", "").split(",");
			if(rgs2.length==2){
				map1.put(rgs2[0], rgs2[1]);
			}
		}
		
		//loading prefixes
		rgs3 = new String[][]{
				{"superkingdom","k__"},
				{"phylum","p__"},
				{"class","c__"},
				{"order","o__"},
				{"family","f__"},
				{"genus","g__"},
				{"species","s__"}};
		
		//creating string
		sbl1 = new StringBuilder();
		for(int i=0;i<rgs3.length;i++){
			if(i>0){
				sbl1.append(";");
			}
			sbl1.append(rgs3[i][1]);
			if(map1.containsKey(rgs3[i][0])){
				sbl1.append(map1.get(rgs3[i][0]));
			}
		}
		
		//outputting results
		return sbl1.toString();
	}	
}
