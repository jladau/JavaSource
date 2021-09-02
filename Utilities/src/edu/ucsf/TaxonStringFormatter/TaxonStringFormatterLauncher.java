package edu.ucsf.TaxonStringFormatter;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Formats taxonomic strings in file to k__;p__;... format
 * @author jladau
 */

public class TaxonStringFormatterLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data io
		//s1 = current taxon string, formatted
		
		ArgumentIO arg1;
		DataIO dat1;
		String s1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//looping through taxon strings
		for(int i=1;i<dat1.iRows;i++){
			s1 = formatTaxonString(dat1.getString(i, "TAXON").split(";"));
			dat1.setString(i, "TAXON", s1);
		}
		
		//outputting results
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static String formatTaxonString(String rgs1[]){
		
		//sbl1 = output
		//rgs2 = prefixes
		//rgs3 = genus in split form
		//i1 = counter
		//iLength = length
		
		int i1;
		int iLength;
		StringBuilder sbl1;
		String rgs2[];
		String rgs3[];
		
		sbl1 = new StringBuilder();
		rgs2 = new String[]{"k__","p__","c__","o__","f__","g__","s__"};
		i1 = 0;
		iLength = rgs1.length;
		if(iLength>7){
			iLength=7;
		}
		for(int i=0;i<iLength;i++){
			if(i>0){
				sbl1.append(";");
			}
			if(i==5 && rgs1.length==6){
				rgs3 = rgs1[i].split("_");
				sbl1.append("g__" + rgs3[0]);
				i1++;
				if(rgs3.length==2){
					sbl1.append(";s__" + rgs3[1]);
					i1++;
				}else{
					sbl1.append(";s__");
					i1++;
				}
			}else{
				sbl1.append(rgs2[i] + rgs1[i]);
				i1++;
			}
		}
		for(int i=i1;i<7;i++){
			if(i>0){
				sbl1.append(";");
			}
			sbl1.append(rgs2[i]);
		}
		return sbl1.toString();
	}
}
