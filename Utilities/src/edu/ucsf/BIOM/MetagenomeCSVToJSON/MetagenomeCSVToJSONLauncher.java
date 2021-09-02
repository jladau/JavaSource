package edu.ucsf.BIOM.MetagenomeCSVToJSON;

import java.util.HashMap;
import edu.ucsf.base.Metagenome;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.MetagenomesIO;

/**
 * Converts metagenomic data in CSV format to JSON format. 
 * @author jladau
 */

public class MetagenomeCSVToJSONLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments object
		//datGenes = genes data object
		//datMetagenomes = metagenomes data object
		//mio1 = metagenomes io object
		//mapMetagenomes = map from metagenome ID to metagenomes
		//sMetagenome = current metagenome
		//mapMetadata = metadata for current metagenome
		
		HashMap<String,String> mapMetadata;
		ArgumentIO arg1;
		DataIO datGenes;
		DataIO datMetagenomes;
		MetagenomesIO mio1;
		String sMetagenome;
		HashMap<String,Metagenome> mapMetagenomes;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		datGenes = new DataIO(arg1.getValueString("sGeneDataPath"));
		datMetagenomes = new DataIO(arg1.getValueString("sMetagenomesDataPath"));
		mapMetagenomes = new HashMap<String,Metagenome>(datMetagenomes.iRows);
		
		//loading metagenomes
		for(int i=1;i<datMetagenomes.iRows;i++){	
			if(arg1.containsArgument("rgsMetadataFields")){
				mapMetadata = new HashMap<String,String>(25);
				for(String s:arg1.getValueStringArray("rgsMetadataFields")){
					mapMetadata.put(s, datMetagenomes.getString(i, s));
				}
			}else{
				mapMetadata=null;
			}
			mapMetagenomes.put(datMetagenomes.getString(i, "METAGENOME_ID"), new Metagenome(
					datMetagenomes.getString(i, "METAGENOME_ID"), 
					datMetagenomes.getInteger(i, "READS_TOTAL"), 
					datGenes.iRows, 
					datMetagenomes.getDouble(i, "READ_LENGTH_MEAN"),
					mapMetadata));
		}
		
		//loading genes
		for(int i=1;i<datGenes.iRows;i++){
			sMetagenome=datGenes.getString(i, "METAGENOME_ID");
			if(datGenes.hasHeader("HMM")){
				mapMetagenomes.get(sMetagenome).addGene(
						datGenes.getString(i, "GENE_ID"), 
						datGenes.getDouble(i, "READ_DEPTH"), 
						datGenes.getInteger(i, "GENE_LENGTH"),
						datGenes.getString(i, "HMM"),
						datGenes.getDouble(i, "HMM_COVERAGE"),
						datGenes.getDouble(i, "GENE_COVERAGE"),
						datGenes.getString(i, "TAXONOMY"));
			}else{
				mapMetagenomes.get(sMetagenome).addGene(
						datGenes.getString(i, "GENE_ID"), 
						datGenes.getDouble(i, "READ_DEPTH"), 
						datGenes.getInteger(i, "GENE_LENGTH"),
						null,
						0.,
						datGenes.getDouble(i, "GENE_COVERAGE"),
						datGenes.getString(i, "TAXONOMY"));
			}
		}
				
		//terminating
		mio1 = new MetagenomesIO(mapMetagenomes);
		DataIO.writeToFile(mio1.printJSON(), arg1.getValueString("sOutputPath"));	
		System.out.println("Done.");
	}	
}