package edu.ucsf.BIOM.MetagenomeCollapseObservations;

import java.util.HashMap;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.base.Metagenome;
import edu.ucsf.base.Metagenome.Gene;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.MetagenomesIO;

/**
 * Merges observations
 * @author jladau
 *
 */


public class MetagenomeCollapseObservationsLauncher {

	public static void main(String[] rgsArgs) throws Exception{
		
		//arg1 = arguments
		//dat1 = mapping file from old observation (OBSERVATION_ID_CURRENT) names to new names (OBSERVATION_ID_NEW)
		//mio1 = metagenomes IO object
		//mapMetagenomes = collapsed metagenomes map
		//map1 = uncollapsed metagenomes map
		//mgn1 = current metagenome
		//map2 = map from current observation ids to new observation ids
		//map3 = map from new observation ids to total number of reads for observations
		//map4 = map from new observation ids to total length for observation
		//sIDNew = new ID
		
		HashMap<String,String> map2;
		ArgumentIO arg1;
		DataIO dat1;
		MetagenomesIO mio1;
		HashMap<String,Metagenome> mapMetagenomes;
		HashMap<String,Metagenome> map1;
		Metagenome mgn1;
		HashMap_AdditiveInteger<String> map3;
		HashMap_AdditiveInteger<String> map4;
		String sIDNew;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sIDMappingPath"));
		mio1 = new MetagenomesIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments(),0.);
		mapMetagenomes = new HashMap<String,Metagenome>(mio1.size());
		map1 = mio1.metagenomeMap();
		map2 = new HashMap<String,String>();
		for(int i=1;i<dat1.iRows;i++){
			map2.put(dat1.getString(i, "OBSERVATION_ID_CURRENT"), dat1.getString(i, "OBSERVATION_ID_NEW"));
		}
		
		//looping through metagenomes
		for(String s:map1.keySet()){
			
			//initializing new metagenome
			mgn1 = map1.get(s);
			mapMetagenomes.put(s, new Metagenome(
					s, 
					mgn1.reads(), 
					mgn1.genes().size(), 
					mgn1.readLength(),
					mgn1.metadata()));
			
			//loading total read count and gene length for new observations
			map3 = new HashMap_AdditiveInteger<String>(mgn1.genes().size());
			map4 = new HashMap_AdditiveInteger<String>(mgn1.genes().size());
			for(Gene gen1:mgn1.genes()){
				sIDNew = map2.get(gen1.id());
				map3.putSum(sIDNew, gen1.readsInteger());
				map4.putSum(sIDNew, gen1.length());
			}
			
			//loading new genes
			for(String t:map3.keySet()){
				mapMetagenomes.get(s).addGene(
						t, 
						((double) (map3.get(t)*mgn1.readLength())/((double) map4.get(t))), 
						map4.get(t),
						null,
						0.,
						0.,
						"k__;p__;c__;o__;f__;g__;s__");
			}
		}
		
		//terminating
		mio1 = new MetagenomesIO(mapMetagenomes);
		DataIO.writeToFile(mio1.printJSON(), arg1.getValueString("sOutputPath"));	
		System.out.println("Done.");
	}	
}