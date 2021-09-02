package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;
import edu.ucsf.base.ExtendedMath;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.Metagenome.Gene;

/**
 * Code for rarefying metagenomes
 * @author jladau
 */

public class MetagenomeRarefaction {

	/**Unrarefied metagenome**/
	private Metagenome mgn1;
	
	/**Function mapping from read depth to probability of assembly**/
	private Function fcnH;
	
	/**Random number generator**/
	private Random rnd1;
	
	/**Number of iterations**/
	private int iIterations;
	
	public MetagenomeRarefaction(Metagenome mgn1, Function fcnH, int iRandomSeed, int iIterations){
		
		this.iIterations = iIterations;
		this.mgn1 = mgn1;
		this.fcnH = fcnH;
		if(mgn1.reads()>mgn1.mappedReads()){
			try{
				mgn1.addGene("unmapped", (mgn1.reads()-mgn1.mappedReads())*mgn1.readLength(), 1, null, Double.NaN, Double.NaN, null);
			}catch(Exception e){
				System.out.println("Error: umapped genes failed to add.");
			}
		}
		rnd1 = new Random(iRandomSeed);
	}

	/**
	 * Finds rarefaction curve
	 * @param lstN Read depths for finding richnesses
	 * @param bAverageHMM If true, then averaged over HMMs
	 */
	public RarefactionCurve rarefactionCurve(
			ArrayList<Integer> lstN, 
			int iHMMCount, 
			String sTaxonRank) throws Exception{
		
		//est1 = current estimates
		//itr1 = iterator
		//i1 = counter
		//i2 = number of genes
		//crv1 = rarefaction curve
		//crv2 = taxonomic rarefaction curve
		//sTaxon = taxon 
		
		RarefactionEstimates est1;
		int i1;
		int i2;
		RarefactionCurve crv1 = null;
		TaxonomicRarefactionCurve crv2 = null;
		String sTaxon = null;
		
		//rarefying: looping through genes
		est1 = new RarefactionEstimates();
		i1 = 0;
		i2 = mgn1.genes().size();
		if(sTaxonRank.equals("otu")){
			crv1 = new RarefactionCurve();
		}else{
			crv2 = new TaxonomicRarefactionCurve(lstN.size(),mgn1.genes().size());
		}
		for(Gene gen1:mgn1.genes()){
			i1++;
			if(i1 % 100 == 0){
				System.out.println("Analyzing gene " + i1 + " of " + i2 + "...");
			}
			
			if(!gen1.id().equals("unmapped")){
				if(!sTaxonRank.equals("otu")){
					sTaxon=gen1.taxon(sTaxonRank);
					if(sTaxon.endsWith("__")){
						continue;
					}
				}
				est1.loadEstimatesRarefactionCurve(gen1, lstN);
				for(Integer i:lstN){
					if(sTaxonRank.equals("otu")){
						crv1.addEstimate(i, est1);
					}else{
						crv2.addEstimate(i, sTaxon, est1);
					}
				}
			}
		}
		
		//computing total richness if necessary
		if(!sTaxonRank.equals("otu")){
			crv2.loadRichnesses();
			crv1 = (RarefactionCurve) crv2;
		}
		
		//normalizing by hmms if necessary
		if(iHMMCount>0){
			crv1.normalize(iHMMCount, lstN);
		}
		
		//removing erroneous values
		crv1.removeErroneousValues(lstN);
		
		//finding minimum and maximum assembled read depths
		crv1.loadMinMaxAssembledDepths();
			
		//returning output
		return crv1;
	}
	
	public RarefactionCurve individualBasedRarefactionCurve(
			ArrayList<Integer> lstN, 
			int iHMMCount, 
			String sTaxonRank,
			ArrayList<Integer> lstAssembledReadDepths) throws Exception{
		
		//crv1 = metagenomic rarefaction curve for finding appropriate total read count
		//map1 = map from assembled read depths to total read depths
		//dX = number of assembled reads in double format
		//dY = interpolated number of total reads
		//mgn1 = rarefied metagenome
		
		TreeMap<Double,Double> map1;
		RarefactionCurve crv1;
		double dX;
		double dY;
		Metagenome mgn1;
		
		//loading metagenome rarefaction curve
		crv1 = this.rarefactionCurve(lstN, iHMMCount, sTaxonRank);
		
		//loading read depths map
		map1 = new TreeMap<Double,Double>();
		for(Integer i:crv1.depths()){
			map1.put(crv1.assembledReads(i), (double) i);
		}
		
		//finding relevant total read count
		Collections.sort(lstAssembledReadDepths);
		dX = (double) (lstAssembledReadDepths.get(lstAssembledReadDepths.size()-1));
		dY = Double.NaN;
		if(map1.firstKey()<=dX){
			if(map1.lastKey()>=dX){					
				dY = ExtendedMath.linearInterpolation(
						dX, 
						(double) map1.floorKey(dX), 
						(double) map1.floorEntry(dX).getValue(), 
						(double) map1.ceilingKey(dX), 
						(double) map1.ceilingEntry(dX).getValue());
			}
		}
		if(Double.isNaN(dY)){
			return null;
		}
		
		//finding variates
		mgn1 = this.rarefy((int) dY, "subsample_counts", false);
		
		//finding individual based rarefaction curve
		return individualBasedRarefaction(mgn1, lstAssembledReadDepths);
	}
	
	private RarefactionCurve individualBasedRarefaction(Metagenome mgn1, ArrayList<Integer> lstAssembledReadDepths){
		
		//dReads = total number of reads
		//dPr = current probability
		//d1 = current total
		//crv1 = rarefaction curve
		
		double dReads;
		double dPr;
		double d1;
		RarefactionCurve crv1;
		
		dReads = 0;
		for(Gene gen1:mgn1.genes()){
			dReads+=gen1.readsDouble();
		}
		crv1 = new RarefactionCurve();
		for(Integer i:lstAssembledReadDepths){
			d1 = 0;
			for(Gene gen1:mgn1.genes()){
				dPr = gen1.readsDouble()/dReads;
				d1+=probabiltyOccur(i, dPr);
			}
			crv1.addEstimate(i, new RichnessEstimate(d1, (double) i));	
		}
		return crv1;
	}
	
	private double probabiltyOccur(int iN, double dPr){
		
		//d1 = output
		
		double d1;
		
		d1 = ((double) iN) * Math.log(1.-dPr);
		return 1.-Math.exp(d1);
	}
		
	/**
	 * Rarefies metagenome to specified number of reads
	 * @param iReads Number of reads to rarefy to
	 * @param sOutputMode Either 'subsample_depth', 'expected_depth', or 'probability_assembly' for depth with one sample, inferred expected depth, or inferred probability of assembly
	 * @param iRandomSeed Random seed
	 * @param bNaive Flag for outputting naive estimates
	 */
	public Metagenome rarefy(int iReads, String sOutputMode, boolean bNaive) throws Exception{
		
		//mgn2 = output metagenome
		//i1 = current number of sampled reads
		//est1 = current estimates
		//bin1 = fast binomial distribution
		//gen2 = current gene
		
		FastBinomialDistribution bin1;
		RarefactionEstimates est1;
		Metagenome mgn2;
		int i1;
		
		//initializing objects
		mgn2 = new Metagenome(
				mgn1.id() + "-rarefied-" + iReads, 
				iReads, 
				mgn1.geneIDs().size(), 
				mgn1.readLength(),
				mgn1.metadata());
		
		//rarefying: looping through genes
		est1 = new RarefactionEstimates();
		for(Gene gen1:mgn1.genes()){
			if(!gen1.id().equals("unmapped")){
				est1.loadEstimates(gen1, iReads);
				if(sOutputMode.equals("subsample")){
					bin1 = new FastBinomialDistribution(
						iReads, 
						gen1.readsDouble()/((double) mgn1.reads()), 
						rnd1.nextInt());
					if(bNaive==false){
						i1 = this.getVariates(gen1, est1, 1, bin1)[0];
					}else{
						i1 = this.getNaiveVariates(gen1, 1, bin1)[0];
					}
					if(i1>0){
						mgn2.addGene(gen1.id(), mgn2.depth(i1, gen1.length()), gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}
				}else if(sOutputMode.equals("subsample_counts")){
					bin1 = new FastBinomialDistribution(
						iReads, 
						gen1.readsDouble()/((double) mgn1.reads()), 
						rnd1.nextInt());
					if(bNaive==false){
						i1 = this.getVariates(gen1, est1, 1, bin1)[0];
					}else{
						i1 = this.getNaiveVariates(gen1, 1, bin1)[0];
					}
					if(i1>0){
						mgn2.addGene(gen1.id(), i1, gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}
				}else if(sOutputMode.equals("mean_depth")){
					if(bNaive==false){
						mgn2.addGene(gen1.id(), mgn2.depth(est1.dNuHat, gen1.length()), gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}else{
						mgn2.addGene(gen1.id(), mgn2.depth(est1.dXHat, gen1.length()), gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}
				}else if(sOutputMode.equals("probability_assembly")){
					if(bNaive==false && est1.dRhoHat>0){	
						mgn2.addGene(gen1.id(), est1.dRhoHat, gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}else if(est1.dPHat>0){
						mgn2.addGene(gen1.id(), est1.dPHat, gen1.length(), gen1.hmm(), gen1.hmmCoverage(), gen1.geneCoverage(), gen1.taxonomy());
					}
				}	
			}
		}
			
		//returning output
		return mgn2;
	}
	
	public int[] getVariates(Gene gen1, RarefactionEstimates est1, int iVariates, int iReads, int iRandomSeed){	
		return getVariates(
				gen1, 
				est1, 
				iVariates, 
				new FastBinomialDistribution(
						iReads, 
						gen1.readsDouble()/((double) mgn1.reads()), 
						iRandomSeed));
	}
	
	public int[] getNaiveVariates(Gene gen1, int iVariates, int iReads, int iRandomSeed){	
		return getNaiveVariates(
				gen1, 
				iVariates, 
				new FastBinomialDistribution(
						iReads, 
						gen1.readsDouble()/((double) mgn1.reads()), 
						iRandomSeed));
	}
	
	private int[] getNaiveVariates(Gene gen1, int iVariates, FastBinomialDistribution bin1){	
		
		//rgi1 = output
		//i1 = current variate
		//dPrAssemble = probability of assembly for current depth
		
		int rgi1[];
		int i1;
		double dPrAssemble;
		
		rgi1 = new int[iVariates];
		for(int k=0;k<iVariates;k++){
			i1 = bin1.sample();
			dPrAssemble = fcnH.image(mgn1.depth(i1, gen1.length()));
			if(rnd1.nextDouble()>dPrAssemble){
				i1 = 0;
			}
			rgi1[k] = i1;
		}
		return rgi1;
	}
	
	
	private int[] getVariates(Gene gen1, RarefactionEstimates est1, int iVariates, FastBinomialDistribution bin1){	
	
		//rgi1 = output
		//i1 = current variate
		//dPrAssemble = probability of assembly for current depth
		
		int rgi1[];
		int i1;
		double dPrAssemble;
		
		rgi1 = new int[iVariates];
		if(est1.dRhoHat==0){
			return rgi1;
		}
		for(int k=0;k<iVariates;k++){
			if(rnd1.nextDouble()<est1.dRhoHat){
				do{
					i1 = bin1.sample();
					dPrAssemble = fcnH.image(mgn1.depth(i1, gen1.length()));
					if(rnd1.nextDouble()>dPrAssemble){
						i1 = 0;
					}				
				}while(i1==0);
				rgi1[k] = i1;
			}
		}
		return rgi1;
	}
	
	public ArrayList<Gene> getGenes(){
		
		//lstOut = output
		
		ArrayList<Gene> lstOut;
		
		lstOut = new ArrayList<Gene>(mgn1.genes().size());
		for(Gene gen1:mgn1.genes()){
			lstOut.add(gen1);
		}
		return lstOut;
	}
	
	public RarefactionEstimates getEstimates(Gene gen1, int iReads){
		
		RarefactionEstimates est1;
		
		est1 = new RarefactionEstimates();
		est1.loadEstimates(gen1, iReads);
		return est1;
	}
	
	public class RichnessEstimate{
		
		/**Estimate for richness**/
		public double dRichness;
		
		/**Estimate for total number of assembled reads**/
		public double dAssembledReads;
		
		public RichnessEstimate(){
			dRichness=0;
			dAssembledReads=0;
		}
		
		public RichnessEstimate(double dRichness, double dAssembledReads){
			this.dRichness=dRichness;
			this.dAssembledReads=dAssembledReads;
		}
		
		public void addGene(RarefactionEstimates est1, int iReads){
			dRichness+=est1.mapRhoHat.get(iReads);
			dAssembledReads+=est1.mapNuHat.get(iReads);
		}
		
		public void updateRichness(double dRichness){
			this.dRichness=dRichness;
		}
		
		public void normalize(double d1){
			dRichness=dRichness/d1;
		}
		
		public double richness(){
			return dRichness;
		}
		public double assembledReads(){
			return dAssembledReads;
		}
	}
	
	public class TaxonomicRarefactionCurve extends RarefactionCurve{
		
		/**Map from read depths and taxon names to probability products (1-p1)*(1-p2)*...**/
		private HashBasedTable<Integer,String,Double> tblProduct;
		
		/**Set of taxa**/
		private HashSet<String> setTaxa;
		
		/**Set of read depths**/
		private HashSet<Integer> setDepths;
		
		
		public TaxonomicRarefactionCurve(int iDepths, int iTaxa){
			super();
			tblProduct = HashBasedTable.create(iDepths, iTaxa);
			setTaxa = new HashSet<String>(iTaxa);
			setDepths = new HashSet<Integer>(iDepths);
		}
		
		public void addEstimate(int iReads, String sTaxon, RarefactionEstimates est1){
			
			//d1 = current value
			
			double d1;
			
			if(!tblProduct.contains(iReads, sTaxon)){
				tblProduct.put(iReads,sTaxon,1.);
				setTaxa.add(sTaxon);
				setDepths.add(iReads);
			}
			d1 = tblProduct.get(iReads, sTaxon);
			tblProduct.put(iReads, sTaxon, d1*(1.-est1.mapRhoHat.get(iReads)));
			super.addEstimate(iReads, est1);
		}
		
		public void loadRichnesses(){
			
			//d1 = current richness
			//set1 = set of taxa
			
			double d1;
			
			//*************************
			//for(String s:setTaxa){
			//	System.out.println(s);
			//}
			//*************************
			
			for(Integer i:setDepths){
				d1 = 0;
				for(String s:setTaxa){
					if(tblProduct.contains(i, s)){
						d1+=(1.-tblProduct.get(i, s));
					}
				}
				super.updateRichness(i, d1);
			}
		}
	}
	
	public class RarefactionCurve{
		
		/**Rarefaction curve**/
		public TreeMap<Integer,RichnessEstimate> mapCurve;
		
		/**Minimum number of assembled reads**/
		public double dMinAssembledReads;
		
		/**Maximum number of assembled reads**/
		public double dMaxAssembledReads;
		
		public RarefactionCurve(){
			dMinAssembledReads=Double.MAX_VALUE;
			dMaxAssembledReads=-Double.MAX_VALUE;
			mapCurve = new TreeMap<Integer,RichnessEstimate>();
		}
		
		public void appendCurve(RarefactionCurve crv1){
			for(Integer i:crv1.depths()){
				this.mapCurve.put(i, crv1.get(i));
			}
			this.removeErroneousValues(new ArrayList<Integer>(mapCurve.keySet()));
			this.loadMinMaxAssembledDepths();
		}
		
		public RichnessEstimate get(int iReads){
			return mapCurve.get(iReads);
		}
		
		public void updateRichness(int iReads, double dRichness){
			mapCurve.get(iReads).updateRichness(dRichness);
		}
		
		public ArrayList<String> print(String sMetagenomeID){
			
			//lstOut = output
			
			ArrayList<String> lstOut;
			
			lstOut = new ArrayList<String>(mapCurve.size());
			for(Integer i:mapCurve.keySet()){
				lstOut.add(sMetagenomeID + "," + i + "," + this.assembledReads(i) + "," + this.richness(i));
			}
			return lstOut;
		}
		
		public ArrayList<Integer> depths(){
			return new ArrayList<Integer>(mapCurve.keySet());
		}
		
		public double assembledReads(int iDepth){
			return mapCurve.get(iDepth).dAssembledReads;
		}
		
		public double richness(int iDepth){
			return mapCurve.get(iDepth).dRichness;
		}
		
		public void addEstimate(int iReads, RichnessEstimate est1){
			mapCurve.put(iReads, est1);
		}
		
		public void addEstimate(int iReads, RarefactionEstimates est1){
			if(!mapCurve.containsKey(iReads)){
				mapCurve.put(iReads, new RichnessEstimate());
			}
			mapCurve.get(iReads).addGene(est1, iReads);
		}
		
		public void normalize(int iHMMCount, ArrayList<Integer> lstN){
			
			//d2 = number of hmms
			
			double d2;
			
			d2 = ((double) iHMMCount);
			for(Integer i:lstN){
				mapCurve.get(i).normalize(d2);
			}
		}
		
		public void removeErroneousValues(ArrayList<Integer> lstN){
			for(int i=1;i<lstN.size();i++){
				if(lstN.get(i-1)<lstN.get(i)){
					if(mapCurve.get(lstN.get(i-1)).assembledReads()>10*mapCurve.get(lstN.get(i)).assembledReads()){
						mapCurve.remove(lstN.get(i-1));
					}
					if(i==lstN.size()-1){
						if(lstN.get(i)/lstN.get(i-1)>5*lstN.get(i-1)/lstN.get(i-2)){
							mapCurve.remove(lstN.get(i));
						}
					}
				}
			}
		}
		
		public void loadMinMaxAssembledDepths(){
			
			//d1 = current assembled read depth
			
			double d1;
			
			for(Integer i:mapCurve.keySet()){
				d1 = mapCurve.get(i).dAssembledReads;
				if(d1<dMinAssembledReads){
					dMinAssembledReads = d1;
				}
				if(d1>dMaxAssembledReads){
					dMaxAssembledReads = d1;
				}
			}
		}
	}
	
	
	public class RarefactionEstimates{
		
		/**Estimate for the probability of assembly**/
		public double dRhoHat;
		
		/**Estimate for the mean number of assembled reads**/
		public double dNuHat;
	
		/**Naive estimate for the probability of assembly**/
		public double dPHat;
		
		/**Naive estimate for the mean number of assembled reads**/
		public double dXHat;
		
		/**Estimate for probability of assembly in observed metagenome**/
		private double dPHatM;
		
		/**Map from read counts to estimated probabilities of assembly**/
		public HashMap<Integer,Double> mapRhoHat;
		
		/**Map from read counts to expected number of assembled reads**/
		public HashMap<Integer,Double> mapNuHat;
		
		private void loadPrAssemblyObservedMetagenome(Gene gen1){
			
			//bin1 = binomial distribution
			//lstPr = list of bootstrapped assembly probabilities
			//iIterations = number of iterations
			//iReads = current number of reads
			//dPrGene = estimated gene observation probability
			//iIterations = number of iterations
			//d1 = sum
			
			FastBinomialDistribution bin1;
			int iReads;
			double dPrGene;
			double d1;
			
			dPrGene = gen1.readsDouble()/((double) mgn1.reads());
			bin1 = new FastBinomialDistribution(mgn1.reads(), dPrGene, rnd1);
			if(bin1.hasNormalApproximation() && mgn1.depth(bin1.minimumValue(),gen1.length())>10){
				dPHatM = 1;
			}else{
				d1 = 0;
				for(int k=0;k<iIterations;k++){
					iReads = bin1.sample();
					d1 += fcnH.image(mgn1.depth(iReads, gen1.length()));
				}
				dPHatM = d1 / ((double) iIterations);
			}
		}
		
		public void loadEstimatesRarefactionCurve(Gene gen1, ArrayList<Integer> lstN){
			
			//dPrGene = estimated gene observation probability
			//iIterations = number of iterations
			//dPrAssembly2 = subsample probability of assembly (estimate)
			//iReads = current number of reads
			//bin1 = binomial distribution
			//d1 = sum of probabilities
			//d2 = sum of read depths * probabilities
			//dPr = current probability
			//dXMean = mean number of reads
			//iN = current read count
			//bZero = flag for whether zero estimate has been found
			
			double dPrGene;
			double dPrAssembly2;
			int iReads;
			FastBinomialDistribution bin1;
			double d1;
			double d2;
			double dPr;
			double dXMean;
			int iN;
			boolean bZero;
			
			loadPrAssemblyObservedMetagenome(gen1);
			dPrGene = gen1.readsDouble()/((double) mgn1.reads());
			mapRhoHat = new HashMap<Integer,Double>(lstN.size());
			mapNuHat = new HashMap<Integer,Double>(lstN.size());
			bZero = false;
			for(int i=lstN.size()-1;i>=0;i--){
				iN = lstN.get(i);
				if(dPHatM==0 || bZero==true){
					mapRhoHat.put(iN, 0.);
					mapNuHat.put(iN, 0.);
				}else{
					bin1 = new FastBinomialDistribution(iN, dPrGene, rnd1);
					if(bin1.hasNormalApproximation() && mgn1.depth(bin1.minimumValue(),gen1.length())>10){
						dPrAssembly2 = 1.;
						dXMean = ((double) iN)*dPrGene;
					}else{
						d1 = 0.;
						d2 = 0.;
						for(int k=0;k<iIterations;k++){
							iReads = bin1.sample();
							dPr = fcnH.image(mgn1.depth(iReads, gen1.length()));
							d1 += dPr;
							d2 += ((double) iReads)*dPr;
						}
						dPrAssembly2 = d1/((double) iIterations);
						dXMean = d2/((double) iIterations);
					}
					mapRhoHat.put(iN, Math.min(dPrAssembly2/dPHatM, 1));
					mapNuHat.put(iN, dXMean/dPHatM);
					if(dPrAssembly2<0.0000000001){
						bZero=true;
					}
				}
			}
		}
		
		public void loadEstimates(Gene gen1, int iN){
			
			//dPrGene = estimated gene observation probability
			//iIterations = number of iterations
			//dPrAssembly1 = supersample probability of assembly (estimate)
			//dPrAssembly2 = subsample probability of assembly (estimate)
			//iReads = current number of reads
			//bin1 = binomial distribution
			//lstPr = list of bootstrapped assembly probabilities
			//lstXPr = list of bootrstrapped depths*assembly probabilities
			//dPr = current probability
			//dXMean = mean
			
			double dPr;
			double dPrGene;
			double dPrAssembly1;
			double dPrAssembly2;
			double dXMean;
			int iReads;
			FastBinomialDistribution bin1;
			ArrayList<Double> lstPr;
			ArrayList<Double> lstXPr;
			
			//returning 0 if appropriate
			if(gen1.readsDouble()==0){
				dRhoHat = 0;
				dNuHat = 0;
				dPHat = 0;
				dXHat = 0;
				return;
			}
			
			//loading variables
			dPrGene = gen1.readsDouble()/((double) mgn1.reads());
			
			//iterating
			//bin1 = new FastBinomialDistribution(mgn1.reads(), dPrGene, rnd1);
			//lstPr = new ArrayList<Double>(iIterations);
			//for(int k=0;k<iIterations;k++){
			//	iReads = bin1.sample();
			//	dPr = fcnH.image(mgn1.depth(iReads, gen1.length()));
			//	lstPr.add(dPr);
			//}
			//dPrAssembly1 = ExtendedMath.mean(lstPr);
			this.loadPrAssemblyObservedMetagenome(gen1);
			dPrAssembly1 = dPHatM;
			
			bin1 = new FastBinomialDistribution(iN, dPrGene, rnd1);
			lstPr = new ArrayList<Double>(iIterations);
			lstXPr = new ArrayList<Double>(iIterations);
			for(int k=0;k<iIterations;k++){
				iReads = bin1.sample();
				dPr = fcnH.image(mgn1.depth(iReads, gen1.length()));
				lstPr.add(dPr);
				lstXPr.add(((double) iReads)*dPr);
			}
			dPrAssembly2 = ExtendedMath.mean(lstPr);
			dXMean = ExtendedMath.mean(lstXPr);
			if(dPrAssembly1==0){
				dRhoHat = 0;
				dNuHat = 0;
			}else{
				dRhoHat = Math.min(dPrAssembly2/dPrAssembly1, 1);
				dNuHat = dXMean/dPrAssembly1;
			}
			dPHat = dPrAssembly2;
			dXHat = dXMean;
		}
	}
}