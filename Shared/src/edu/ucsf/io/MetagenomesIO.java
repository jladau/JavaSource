package edu.ucsf.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.Function;
import edu.ucsf.base.Metagenome;
import edu.ucsf.base.MetagenomeRarefaction;
import edu.ucsf.base.MetagenomeRarefaction.RarefactionCurve;

/**
 * Metagenomes input/output object
 * @author jladau
 */
public class MetagenomesIO {

	/**Map from metagenome ID to metagenomes object**/
	private HashMap<String,Metagenome> mapMetagenomes;
	
	/**Number of genes**/
	private int iGenes;
	
	public MetagenomesIO(String sBIOMPath, Map<String,String> mapOptions, double dCoverageThreshold) throws Exception{
		initialize(new BiomIO(sBIOMPath, mapOptions), dCoverageThreshold);	
	}
		
	public MetagenomesIO(BiomIO bio1, double dCoverageThreshold) throws Exception{
		initialize(bio1, dCoverageThreshold);
	}
		
	private void initialize(BiomIO bio1, double dCoverageThreshold) throws Exception{
		
		//bio1 = biom object
		//mapGeneCounts = keys are metagenome IDs, values are numbers of genes per metagenome
		//iReads = number of reads in current metagenome
		//dDepth = current depth
		//dReadLength = read length mean
		//mapMetadata = additional metadata
		//sHMM = hmm
		//dHMMCoverage = hmm coverage
		//dGeneCoverage = gene coverage
		//sTaxonomy = taxonomy
		
		HashMap<String,Double> mapGeneCounts;
		int iReads;
		double dDepth;
		double dReadLength;
		HashMap<String,String> mapMetadata;
		String sHMM;
		double dHMMCoverage;
		double dGeneCoverage;
		String sTaxonomy;
		
		//initializing variables
		mapMetagenomes = new HashMap<String,Metagenome>(bio1.axsSample.size());
		
		//loading number of genes
		iGenes = bio1.axsObservation.size();
		
		//loading metagenomes
		mapGeneCounts = bio1.getRichness();
		for(String sMetagenomeID:bio1.axsSample.getIDs()){
			iReads = Integer.parseInt(bio1.axsSample.getMetadata(sMetagenomeID).get("total_reads"));
			dReadLength = Double.parseDouble(bio1.axsSample.getMetadata(sMetagenomeID).get("mean_read_length"));
			mapMetadata = new HashMap<String,String>(25);
			for(String s:bio1.axsSample.getMetadata(sMetagenomeID).keySet()){
				if(!s.equals("total_reads") && !s.equalsIgnoreCase("mean_read_length")){
					mapMetadata.put(s, bio1.axsSample.getMetadata(sMetagenomeID).get(s));
				}
			}
			if(!mapMetagenomes.containsKey(sMetagenomeID)){
				mapMetagenomes.put(sMetagenomeID, new Metagenome(sMetagenomeID, iReads, mapGeneCounts.get(sMetagenomeID).intValue(), dReadLength, mapMetadata));
			}
			for(String sGeneID:bio1.axsObservation.getIDs()){
				dDepth = bio1.getValueByIDs(sGeneID, sMetagenomeID);
				if(dDepth>0){
					if(bio1.axsObservation.getMetadata(sGeneID).containsKey("hmm")){
						sHMM=bio1.axsObservation.getMetadata(sGeneID).get("hmm");
					}else{
						sHMM=null;
					}
					if(bio1.axsObservation.getMetadata(sGeneID).containsKey("taxonomy")){
						sTaxonomy=bio1.axsObservation.getMetadata(sGeneID).get("taxonomy");
					}else{
						sTaxonomy=null;
					}
					if(bio1.axsObservation.getMetadata(sGeneID).containsKey("gene_coverage")){
						dGeneCoverage=Double.parseDouble(bio1.axsObservation.getMetadata(sGeneID).get("gene_coverage"));
					}else{
						dGeneCoverage=0.;
					}
					if(dGeneCoverage<dCoverageThreshold){
						continue;
					}
					if(bio1.axsObservation.getMetadata(sGeneID).containsKey("hmm_coverage")){
						dHMMCoverage=Double.parseDouble(bio1.axsObservation.getMetadata(sGeneID).get("hmm_coverage"));
					}else{
						dHMMCoverage=0.;
					}
					if(dHMMCoverage<dCoverageThreshold){
						continue;
					}
					mapMetagenomes.get(sMetagenomeID).addGene(
						sGeneID,
						dDepth,
						Integer.parseInt(bio1.axsObservation.getMetadata(sGeneID).get("gene_length")),
						sHMM,
						dHMMCoverage,
						dGeneCoverage,
						sTaxonomy);
				}
			}
		}
	}
	
	public MetagenomesIO(HashMap<String,Metagenome> mapMetagenomes){
		
		//set1 = set of genes
		
		HashSet<String> setGenes;
		
		this.mapMetagenomes = mapMetagenomes;
		setGenes = new HashSet<String>(iGenes);
		for(String s:mapMetagenomes.keySet()){
			setGenes.addAll(mapMetagenomes.get(s).geneIDs());
		}
		iGenes = setGenes.size();
	}
	
	public ArrayList<String> printJSON(){
		return BiomIO.printJSON(this.toTable(), this.sampleMetadata(), this.geneMetadata(), "Gene table");
	}
	
	public HashMap<String,String> sampleMetadata(){
		
		//map1 = output
		//sbl1 = current output
		
		HashMap<String,String> map1;
		StringBuilder sbl1;
		
		map1 = new HashMap<String,String>(mapMetagenomes.size());
		for(String s:mapMetagenomes.keySet()){
			sbl1 = new StringBuilder();
			sbl1.append("{\"total_reads\":\"" + mapMetagenomes.get(s).reads() + "\",\"mean_read_length\":\"" + mapMetagenomes.get(s).readLength() + "\"");
			if(mapMetagenomes.get(s).metadata()!=null && mapMetagenomes.get(s).metadata().size()>0){
				sbl1.append("," + mapMetagenomes.get(s).printMetadata());
			}
			map1.put(s, sbl1.toString() + "}");
			//map1.put(s, "{\"total_reads\":\"" + mapMetagenomes.get(s).reads() + "\",\"mean_read_length\":\"" + mapMetagenomes.get(s).readLength() + "\"}");
		}
		return map1;	
	}
	
	public HashMap<String,String> geneMetadata(){
		
		//map1 = output
		//set1 = set of genes that have been considered
		//sbl1 = current output line
		
		HashMap<String,String> map1;
		HashSet<String> set1;
		StringBuilder sbl1;

		map1 = new HashMap<String,String>(mapMetagenomes.size());
		set1 = new HashSet<String>(iGenes);
		
		for(String sSample:mapMetagenomes.keySet()){
			for(String sGene:mapMetagenomes.get(sSample).geneIDs()){
				if(!set1.contains(sGene)){
					sbl1 = new StringBuilder();
					sbl1.append("{\"gene_length\":\"" + mapMetagenomes.get(sSample).gene(sGene).length() + "\"" +
							",\"hmm\":\"" + mapMetagenomes.get(sSample).gene(sGene).hmm() + "\"" +
							",\"taxonomy\":[\"" + mapMetagenomes.get(sSample).gene(sGene).taxonomy().replace(";","\",\"") + "\"]" + "}");
					map1.put(sGene, sbl1.toString());
					set1.add(sGene);
				}	
			}
		}
		return map1;
	}
	
	public HashBasedTable<String,String,Double> toTable(){
		
		//tbl1 = output
		
		HashBasedTable<String,String,Double> tbl1;
		
		tbl1 = HashBasedTable.create(iGenes, this.size());
		for(String sMetagenome:mapMetagenomes.keySet()){
			for(String sGene:mapMetagenomes.get(sMetagenome).geneIDs()){
				tbl1.put(sGene, sMetagenome, mapMetagenomes.get(sMetagenome).gene(sGene).depth());
			}
		}
		return tbl1;
	}
	
	public int size(){
		return mapMetagenomes.size();
	}

	public HashMap<String,RarefactionCurve> rarefactionCurves(
			ArrayList<Double> lstDepths, 
			ArrayList<Double> lstH, 
			int iRandomSeed, 
			int iHMMAverage, 
			int iMetagenomeRarefactionDepth, 
			int iBootstrapIterations,
			String sTaxonRank,
			String sRarefactionMode) throws Exception{
		
		//map2 = output
		//rar1 = rarefaction object
		//lstN = list of assembled read depths
		//fcn1 = probabilities function
		//dMax = maximum in log units
		//iMin = minimum
		//crv1 = rarefaction curve
		//lstN2 = list of individual-based read depths
		
		HashMap<String,RarefactionCurve> map2;
		MetagenomeRarefaction rar1;
		ArrayList<Integer> lstN;
		ArrayList<Integer> lstN2;
		Function fcn1;
		double dMax;
		int iMin;
		RarefactionCurve crv1 = null;
		
		fcn1 = new Function(lstDepths, lstH);
		map2 = new HashMap<String,RarefactionCurve>(this.size());
		for(Metagenome mgn1:this.mapMetagenomes.values()){		
			if(mgn1.reads()<iMetagenomeRarefactionDepth){
				continue;
			}
			rar1 = new MetagenomeRarefaction(mgn1, fcn1, iRandomSeed, iBootstrapIterations);
			iMin = 10000;
			if(iMetagenomeRarefactionDepth==-1 || sRarefactionMode.equals("individual_based")){
				dMax = Math.log((double) mgn1.reads());
			}else{
				dMax = Math.log((double) iMetagenomeRarefactionDepth);
			}
			lstN = rarefactionDepths(Math.log((double) iMin),dMax);
			if(sRarefactionMode.equals("metagenome")){
			
				crv1 = rar1.rarefactionCurve(lstN, iHMMAverage, sTaxonRank);
				while(crv1.dMinAssembledReads>1){
					dMax = Math.log((double) iMin);
					iMin = iMin/100;
					lstN = rarefactionDepths(Math.log((double) iMin),dMax);
					crv1.appendCurve(rar1.rarefactionCurve(lstN, iHMMAverage, sTaxonRank));
				}
			}else if(sRarefactionMode.equals("individual_based")){
				lstN2 = rarefactionDepths(0., Math.log((double) iMetagenomeRarefactionDepth));
				crv1 = rar1.individualBasedRarefactionCurve(lstN, iHMMAverage, sTaxonRank, lstN2);
			}
				
			map2.put(mgn1.id(),crv1);
		}
		return map2;
	}
	
	private ArrayList<Integer> rarefactionDepths(double dMin, double dMax){
		
		//lstN = list of read depths
		//dStep = step in log units
		//d1 = current value in log units
		//dSteps = number of steps
		
		ArrayList<Integer> lstN;
		double dStep;
		double d1;
		double dSteps = 250.;
		
		dSteps = 100.;
		dStep = (dMax-dMin)/dSteps;
		lstN = new ArrayList<Integer>((int) dSteps + 10);
		d1 = dMin;
		do{
			lstN.add((int) Math.floor(Math.exp(d1)));
			d1+=dStep;
		}while(d1<dMax);
		lstN.add((int) Math.round(Math.exp(dMax)));
		return lstN;
	}
	
	
	public ArrayList<String> printRarefactionCurves(
			ArrayList<Double> lstDepths, 
			ArrayList<Double> lstH, 
			int iRandomSeed, 
			int iHMMAverage, 
			int iMetagenomeRarefactionDepth, 
			int iBootstrapIterations,
			String sTaxonRank,
			String sRarefactionMode) throws Exception{
		
		//lstOut = output
		//map1 = map rarefaction curves
		
		ArrayList<String> lstOut;
		HashMap<String,RarefactionCurve> map1;
		
		lstOut = new ArrayList<String>(this.size()*50+1);
		lstOut.add("METAGENOME,NUMBER_READS,NUMBER_READS_ASSEMBLED,RICHNESS");
		map1 = rarefactionCurves(lstDepths, lstH, iRandomSeed, iHMMAverage, iMetagenomeRarefactionDepth, iBootstrapIterations, sTaxonRank, sRarefactionMode);
		for(String s:map1.keySet()){
			if(map1.get(s)!=null){
				lstOut.addAll(map1.get(s).print(s));
			}
		}
		return lstOut;
	}
	
	public MetagenomesIO rarefy(int iReads, ArrayList<Double> lstDepths, ArrayList<Double> lstH, String sOutputMode, int iRandomSeed, boolean bIncludeEmptyMetagenomes, boolean bNaive) throws Exception{
		
		//map1 = rarefied map
		//mgn2 = rarefied metagenome
		//rar1 = rarefaction object
		
		HashMap<String,Metagenome> map1;
		Metagenome mgn2;
		MetagenomeRarefaction rar1;
		
		map1 = new HashMap<String,Metagenome>(this.size());
		for(Metagenome mgn1:this.mapMetagenomes.values()){		
			if(mgn1.reads()>=iReads){
				rar1 = new MetagenomeRarefaction(mgn1, new Function(lstDepths, lstH), iRandomSeed, 1000);
				mgn2 = rar1.rarefy(
						iReads,
						sOutputMode,
						bNaive);
				if(mgn2.mappedReads()>0 || bIncludeEmptyMetagenomes){
					map1.put(mgn1.id() + "-rarefied-" + iReads, mgn2);
				}
			}
		}
		return new MetagenomesIO(map1);
	}	
	
	public HashMap<String,Metagenome> metagenomeMap(){
		return mapMetagenomes;
	}
	
	
}