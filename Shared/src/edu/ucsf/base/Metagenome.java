package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Object representing a metagenome
 * @author jladau
 */

public class Metagenome {

	/**Set of genes**/
	private HashMap<String, Gene> mapGenes;
	
	/**Total number of reads**/
	private int iTotalReads;
	
	/**Mean read length**/
	private double dReadLength;
	
	/**ID**/
	private String sID;
	
	/**Total mapped reads**/
	private int iMappedReads;
	
	/**Metadata map**/
	private HashMap<String,String> mapMetadata;
	
	/**
	 * Constructor
	 * @param sID String, metagenome ID
	 * @param iReads Integer, number of reads that were input in the assembler
	 * @param iExpectedGenes Integer, expected number of genees
	 */
	public Metagenome(String sID, int iReads, int iExpectedGenes, double dReadLength, HashMap<String,String> mapMetadata){
		this.iTotalReads = iReads;
		this.sID = sID;
		this.dReadLength = dReadLength;
		this.mapGenes = new HashMap<String,Gene>(iExpectedGenes);
		this.mapMetadata = mapMetadata;
	}
	
	public void addMetadata(String sKey, String sValue){
		mapMetadata.put(sKey, sValue);
	}
	
	public HashMap<String,String> metadata(){
		return mapMetadata;
	}
	
	public String printMetadata(){
		
		//sbl1 = output
		
		StringBuilder sbl1;
		
		if(mapMetadata.size()==0){
			return null;
		}
		sbl1 = new StringBuilder();
		for(String s:mapMetadata.keySet()){
			if(sbl1.length()>0){
				sbl1.append(",");
			}
			sbl1.append("\"" + s + "\":\"" + mapMetadata.get(s) + "\"");
		}
		return sbl1.toString();
	}
	
	public static String printMetagenomeHeader(){
		return "METAGENOME_ID,TOTAL_READS,MEAN_READ_LENGTH";
	}

	public String printMetagenome(){
		return this.id() + "," + this.reads() + "," + this.readLength();
	}
	
	public void updateTotalReads(int iReads){
		this.iTotalReads = iReads;
	}
	
	public static String printGenesHeader(){
		return "METAGENOME_ID,GENE_ID,GENE_LENGTH,GENE_SEQUENCING_DEPTH";
	}
	
	public ArrayList<String> printGenes(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(this.geneIDs().size()+1);
		for(Gene ctg1:this.genes()){
			lst1.add(
					this.id() + "," +
					ctg1.id() + "," +
					ctg1.length() + "," +
					ctg1.depth());
					
		}
		return lst1;
	}
	
	public double depth(int iNumberReads, int iGeneLength){
		return ((double) iNumberReads)*dReadLength/((double) iGeneLength);
	}
	
	public double depthDouble(double dNumberReads, int iGeneLength){
		return dNumberReads*dReadLength/((double) iGeneLength);
	}
	
	public double depth(double dNumberReads, int iGeneLength){
		return dNumberReads*dReadLength/((double) iGeneLength);
	}
	
	
	public int mappedReads(){
		return this.iMappedReads;
	}
	
	public void addGene(String sID, double dDepth, int iLength, String sHMM, double dHMMCoverage, double dGeneCoverage, String sTaxonomy) throws Exception{
		mapGenes.put(sID, new Gene(sID, dDepth, iLength, sHMM, dHMMCoverage, dGeneCoverage, sTaxonomy));
		iMappedReads+=mapGenes.get(sID).readsInteger();
		if(iMappedReads>this.reads()){
			this.iTotalReads=iMappedReads;
		}
	}
	
	public int reads(){
		return iTotalReads;
	}
	
	public double readLength(){
		return dReadLength;
	}
	
	public String id(){
		return sID;
	}
	
	public Gene gene(String sGeneID){
		return mapGenes.get(sGeneID);
	}
	
	public Set<String> geneIDs(){
		return mapGenes.keySet();
	}
	
	public Collection<Gene> genes(){
		return mapGenes.values();
	}
	
	public class Gene{
		
		/**Read depth**/
		private double dDepth;
		
		/**Length**/
		private int iLength;
		
		/**ID**/
		private String sID;
		
		/**HMM**/
		private String sHMM;
		
		/**HMM coverage**/
		private double dHMMCoverage;
		
		/**Gene coverage**/
		private double dGeneCoverage;
		
		/**Taxonomy**/
		private String sTaxonomy;
		
		public Gene(String sID, double dDepth, int iLength, String sHMM, double dHMMCoverage, double dGeneCoverage, String sTaxonomy) throws Exception{
			
			if(dDepth==0){
				throw new Exception("Gene depth must exceed 0.");
			}
			if(iLength==0){
				throw new Exception("Gene length must exceed 0.");
			}
			
			this.dDepth = dDepth;
			this.sID = sID;
			this.iLength = iLength;
			this.sHMM = sHMM;
			this.dHMMCoverage = dHMMCoverage;
			this.dGeneCoverage = dGeneCoverage;
			this.sTaxonomy = sTaxonomy;
		}
		
		public String taxonomy(){
			return sTaxonomy;
		}
		
		public double hmmCoverage(){
			return dHMMCoverage;
		}
		
		public double geneCoverage(){
			return dGeneCoverage;
		}
		
		public String taxon(String sTaxonRank){
			
			//rgs1 = taxon string in split format
			
			String rgs1[];
			
			if(sTaxonomy==null){
				return null;
			}
			rgs1 = sTaxonomy.split(";");
			if(sTaxonRank.equals("kingdom")){
				return rgs1[0];
			}else if(sTaxonRank.equals("phylum")){
				return rgs1[1];
			}else if(sTaxonRank.equals("class")){
				return rgs1[2];
			}else if(sTaxonRank.equals("order")){
				return rgs1[3];
			}else if(sTaxonRank.equals("family")){
				return rgs1[4];
			}else if(sTaxonRank.equals("genus")){
				return rgs1[5];
			}else if(sTaxonRank.equals("species")){
				return rgs1[6];
			}else{
				return null;
			}
		}
		
		public double depth(){
			return dDepth;
		}
		
		public String hmm(){
			return sHMM;
		}
		
		public Integer readsInteger(){
			return (int) Math.round(dDepth*((double) iLength)/dReadLength);
		}
		
		public Double readsDouble(){
			return dDepth*((double) iLength)/dReadLength;
		}
		
		public int length(){
			return iLength;
		}
		
		public String id(){
			return sID;
		}
		
		public double depth(int iNumberReads, int iGeneLength){
			return ((double) iNumberReads)*dReadLength/((double) iGeneLength);
		}
		
		public double depth(double dNumberReads, int iGeneLength){
			return dNumberReads*dReadLength/((double) iGeneLength);
		}
	}
}
