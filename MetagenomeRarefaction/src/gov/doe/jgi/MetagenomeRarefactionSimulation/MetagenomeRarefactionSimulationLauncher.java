package gov.doe.jgi.MetagenomeRarefactionSimulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.math3.distribution.BinomialDistribution;

import edu.ucsf.base.EmpiricalDistribution_Cumulative;
import edu.ucsf.base.FastBinomialDistribution;
import edu.ucsf.base.Function;
import edu.ucsf.base.Metagenome;
import edu.ucsf.base.Metagenome.Gene;
import edu.ucsf.base.MetagenomeRarefaction;
import edu.ucsf.base.MetagenomeRarefaction.RarefactionEstimates;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class MetagenomeRarefactionSimulationLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//iM = number of reads in metagenome 1
		//rgi1 = numers of reads in rarefied samples to consider
		//dPrGene = probability a read belongs to gene
		//rnd1 = random number generator
		//dT = mean read length
		//dL = length of gene
		//iIterations = number of iterations
		//lstOut = output
		//lstOut2 = variates output
		//bin1 = binomial distribution object
		//rgiVariates = variates
		//rgdVariates = variates (double format)
		//rgiNaiveVariates = naive variates
		//rgdNaiveVariates = naive variates (double format)
		//mgn1 = current metagenome
		//rar1 = rarefaction object
		//fcnH = function
		//est1 = rarefaction estimates
		//gen1 = current gene
		//iVariates = number of variates to generate
		//edf1 = emipircal distribution for variates
		//edf2 = empirical distribution for naive variates
		//par1 = parameters
		//lstX = list of x values for which to compute CDF
		
		MetagenomeRarefactionSimulationParameters par1;
		Gene gen1;
		MetagenomeRarefaction rar1;
		Metagenome mgn1;
		ArrayList<String> lstOut;
		ArrayList<String> lstOut2;
		ArgumentIO arg1;
		int iM;
		double dPrGene;
		Random rnd1;
		double dT;
		double dL;
		int iIterations;
		FastBinomialDistribution bin1;
		Integer rgi1[];
		int rgiVariates[];
		double rgdVariates[];
		int rgiNaiveVariates[];
		double rgdNaiveVariates[];
		Function fcnH;
		RarefactionEstimates est1;
		int iVariates;
		EmpiricalDistribution_Cumulative edf2;
		EmpiricalDistribution_Cumulative edf1;
		ArrayList<Integer> lstX;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		iM = arg1.getValueInt("iM");
		dPrGene = arg1.getValueDouble("dPrGene");
		dT = arg1.getValueDouble("dT");
		dL = arg1.getValueDouble("dL");
		rnd1 = new Random(arg1.getValueInt("iRandomSeed"));
		iIterations = arg1.getValueInt("iIterations");
		rgi1 = arg1.getValueIntegerArray("rgiSubsampleSizes");
		lstOut = new ArrayList<String>(10000);
		lstOut.add("VALUE_TYPE,VARIABLE,SUBSAMPLE_SIZE,VALUE");
		lstOut2 = new ArrayList<String>(10000);
		lstOut2.add("VALUE_TYPE,ITERATION,SUBSAMPLE_SIZE,DEPTH,DISTRIBUTION_FUNCTION_VALUE");
		fcnH = assemblyFunction();
		iVariates = 1000;
		lstX = loadCumulantX(dT,dL, rgi1, dPrGene);
		
		//System.out.println("Finding estimates...");
		bin1 = new FastBinomialDistribution(iM, dPrGene, 5678);
		for(int k=0;k<iIterations;k++){
			
			//simulating metagenome
			mgn1 = simulateMetagenome(iM, dT, dL, dPrGene, rnd1, bin1);
			for(int iN:rgi1){
				if(mgn1!=null){
					rar1 = new MetagenomeRarefaction(mgn1, fcnH, k+13,1000);
					gen1 = rar1.getGenes().get(0);
					est1 = rar1.getEstimates(gen1, iN);
					lstOut.add("estimate,assembly_probability," + iN + "," + est1.dRhoHat);
					lstOut.add("estimate,read_count_mean," + iN + "," + est1.dNuHat);
					lstOut.add("naive_estimate,assembly_probability," + iN + "," + est1.dPHat);
					lstOut.add("naive_estimate,read_count_mean," + iN + "," + est1.dXHat);
					rgiVariates = rar1.getVariates(gen1, est1, iVariates, iN, k+19);
					rgiNaiveVariates = rar1.getNaiveVariates(gen1, iVariates, iN, k+13);
				}else{
					lstOut.add("estimate,assembly_probability," + iN + "," + 0);
					lstOut.add("estimate,read_count_mean," + iN + "," + 0);
					lstOut.add("naive_estimate,assembly_probability," + iN + "," + 0);
					lstOut.add("naive_estimate,read_count_mean," + iN + "," + 0);
					rgiVariates = new int[iVariates];
					rgiNaiveVariates = new int[iVariates];
				}
				rgdVariates = integerToDoubleVariates(rgiVariates, dT, dL);
				rgdNaiveVariates = integerToDoubleVariates(rgiNaiveVariates, dT, dL);
				edf1 = new EmpiricalDistribution_Cumulative(rgdVariates);
				edf2 = new EmpiricalDistribution_Cumulative(rgdNaiveVariates);
				for(Integer i:lstX){
					lstOut2.add("estimate," + k + "," + iN + "," + ((double) i)*dT/dL + "," + edf1.cumulativeProbability(((double) i)*dT/dL));
					lstOut2.add("naive_estimate," + k + "," + iN + "," + ((double) i)*dT/dL + "," + edf2.cumulativeProbability(((double) i)*dT/dL));
				}
			}
		}
		
		for(int iN:rgi1){
			par1 = getParameters(iN, dPrGene, dT, dL, lstX);
			lstOut.add("parameter,assembly_probability," + iN + "," + par1.dRho);
			lstOut.add("parameter,read_count_mean," + iN + "," + par1.dNu);
			for(int k=0;k<lstX.size();k++){
				lstOut2.add("parameter," + 1 + "," + iN + "," + ((double) lstX.get(k))*dT/dL + "," + par1.lstCDF.get(k));
			}
		}
			
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		DataIO.writeToFile(lstOut2, arg1.getValueString("sOutputPath").replace(".csv", "-variate-distribution.csv"));
		System.out.println("Done.");
	}
	
	private static ArrayList<Integer> loadCumulantX(double dT, double dL, Integer[] rgiSampleSizes, double dPrGene){
		
		//iStep = step size
		//iMax = maximum
		//lst1 = output
		//dMaxDepth = maximum depth
		//d1 = current maximum sample size
		
		int iStep;
		int iMax;
		ArrayList<Integer> lst1;
		double dMaxDepth;
		double d1;
		
		d1 = 0;
		for(int i:rgiSampleSizes){
			if(i>d1){
				d1=i;
			}
		}
		dMaxDepth = d1*dPrGene + 3.1*Math.sqrt(d1*dPrGene*(1.-dPrGene));
		dMaxDepth = dMaxDepth*dT/dL;
		
		
		if(dT/dL>=0.02*dMaxDepth){
			iStep=1;
		}else{
			iStep = (int) Math.ceil(0.02*dMaxDepth*dL/dT);
		}
		iMax = (int) Math.ceil(dMaxDepth*dL/dT);
		lst1 = new ArrayList<Integer>(100);
		for(int i=0;i<iMax;i+=iStep){
			lst1.add(i);
		}
		return lst1;
	}
	
	private static double[] integerToDoubleVariates(int[] rgiVariates, double dT, double dL){
		
		//rgd1 = output
		
		double rgd1[];
		
		rgd1 = new double[rgiVariates.length];
		for(int i=0;i<rgiVariates.length;i++){
			rgd1[i] = ((double) rgiVariates[i])*dT/dL;
		}
		return rgd1;
	}
	
	public static MetagenomeRarefactionSimulationParameters getParameters(int iReadsTotal, double dPrGene, double dT, double dL, ArrayList<Integer> lstX){
		
		//rgd1 = output
		//dPr = current probability of number of reads
		//dPrAssemble = current probability of assembly
		//bin1 = binomial distribution object
		//dMean = mean
		//lstCDF = cdf
		//map1 = map of cumulant values
		//par1 = output
		
		double rgd1[];
		BinomialDistribution bin1;
		double dPr;
		double dPrAssembly;
		double dMean;
		ArrayList<Double> lstCDF;
		MetagenomeRarefactionSimulationParameters par1;
		HashMap<Integer,Double> map1;
		
		rgd1 = new double[2];
		bin1 = new BinomialDistribution(iReadsTotal, dPrGene);
		dMean = ((double) iReadsTotal)*dPrGene;
		map1 = new HashMap<Integer,Double>(lstX.size());
		for(Integer i:lstX){
			map1.put(i, 1.);
		}
		for(int i=0;i<=iReadsTotal;i++){
			dPr = bin1.probability(i);
			dPrAssembly = probabilityAssemble(i, dT, dL);
			rgd1[0]+=dPr*dPrAssembly;
			rgd1[1]+=dPr*dPrAssembly*i;
			if(map1.containsKey(i)){
				map1.put(i, rgd1[0]);
			}
			if(i>dMean && dPr<0.000000001){
				break;
			}
		}
		lstCDF = new ArrayList<Double>(lstX.size());
		for(int i=0;i<lstX.size();i++){
			lstCDF.add(Math.min(map1.get(lstX.get(i))+(1-rgd1[0]),1.));
		}
		par1 = new MetagenomeRarefactionSimulationParameters();
		par1.dRho = rgd1[0];
		par1.dNu = rgd1[1];
		par1.lstX = lstX;
		par1.lstCDF = lstCDF;
		return par1;
	}
	
	public static Metagenome simulateMetagenome(int iReadsTotal, double dT, double dL, double dPrGene, Random rnd1, FastBinomialDistribution bin1) throws Exception{
		
		//dPrAssemble = probability of assembly for current depth
		//i1 = current number of reads belonging to gene
		//mgn1 = output
		
		double dPrAssemble;
		int i1;
		Metagenome mgn1;
		
		i1 = bin1.sample();
		
		//checking for assembly in metagenome 1
		mgn1 = new Metagenome("na", iReadsTotal, 2, dT, null);
		dPrAssemble = probabilityAssemble(i1,dT,dL);
		if(rnd1.nextDouble()<dPrAssemble){
			mgn1.addGene("1", ((double) i1)*dT/dL, (int) dL, null, 0., 0., null);
		}else{
			mgn1 = null;
		}
		return mgn1;
	}
	
	private static Function assemblyFunction(){
		
		//lstX = list of x values
		//lstY = list of y values
		
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		
		lstX = new ArrayList<Double>(1000);
		lstY = new ArrayList<Double>(1000);		
		
		for(double d=0;d<25;d+=0.025){
			lstX.add(d);
			lstY.add(assemblyFunction(d));
		}
		return new Function(lstX,lstY);
	}
	
	private static double probabilityAssemble(int iReads, double dT, double dL){
		
		//d1 = depth
		
		double d1;
		
		if(iReads==0){
			return 0;
		}
		d1 = ((double) iReads)*dT/dL;
		return assemblyFunction(d1);
	}	
	
	private static double assemblyFunction(double d1){
		return 2./(1. + Math.exp(1./d1));
	}
}