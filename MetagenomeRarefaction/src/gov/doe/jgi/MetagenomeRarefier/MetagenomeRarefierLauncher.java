package gov.doe.jgi.MetagenomeRarefier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.base.Metagenome;
import edu.ucsf.help.Usage.Usage;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.MetagenomesIO;

/**
 * This code rarefies metagenomes. It takes as input a list of metagenomes, the genes occurring in each one, their sequencing depths and lengths, and the number of reads in each metagenome. It outputs a list of rarefied metagenomes and also optionally rarefaction curves.
 * @author jladau
 */

public class MetagenomeRarefierLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//TODO add iHMMAverage to usage object

		//arg1 = input arguments object
		//usg1 = usage object
		//mio1 = initial metagenomes object
		//mio2 = rarefied metagenomes object
		//dat1 = data with probability of assembly as a function of read depth
		//lstDepths = list of depths
		//lstH = list of probabilities of assembly
		
		ArrayList<Double> lstDepths;
		ArrayList<Double> lstH;
		ArgumentIO arg1;
		Usage usg1;
		MetagenomesIO mio1;
		MetagenomesIO mio2;
		DataIO dat1;
		
		//initializing usage object
		//TODO update usage object
		usg1 = new Usage(new String[]{
			"MetagenomeRarefier"});
		usg1.printUsage(rgsArgs);
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		if(arg1.containsArgument("sOutputTestDataPath")){
			testData2(arg1.getValueString("sOutputTestDataPath"), arg1.getValueInt("iMinimumReadDepthForAssembly"));
			System.out.println("Done.");
			return;
		}
		if(!arg1.containsArgument("sRarefactionMode")){
			arg1.updateArgument("sRarefactionMode", "subsample");
		}
		if(!arg1.containsArgument("bIncludeEmptyMetagenomes")){
			arg1.updateArgument("bIncludeEmptyMetagenomes", false);
		}
		if(!arg1.containsArgument("bNaiveEstimates")){
			arg1.updateArgument("bNaiveEstimates", false);
		}
		
		//TODO add dCoverageThreshold to usage object
		mio1 = new MetagenomesIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments(), arg1.getValueDouble("dCoverageThreshold"));
		//TODO add sProbabilityOfAssemblyFcnPath to usage object
		dat1 = new DataIO(arg1.getValueString("sProbabilityOfAssemblyFcnPath"));
		lstDepths = new ArrayList<Double>(dat1.iRows);
		lstH = new ArrayList<Double>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lstDepths.add(dat1.getDouble(i, "READ_DEPTH"));
			lstH.add(dat1.getDouble(i, "PROBABILITY_OF_ASSEMBLY"));
		}
		
		//rarefying
		if(!arg1.getValueString("sOutputMode").equals("rarefaction_curves")){
			System.out.println("Rarefying...");
			mio2 = mio1.rarefy(
					arg1.getValueInt("iMetagenomeRarefactionDepth"), 
					lstDepths,
					lstH,
					arg1.getValueString("sOutputMode"),
					arg1.getValueInt("iRandomSeed"),
					arg1.getValueBoolean("bIncludeEmptyMetagenomes"),
					arg1.getValueBoolean("bNaiveEstimates"));
			
			//outputting rarefied metagenome
			DataIO.writeToFile(mio2.printJSON(), arg1.getValueString("sOutputPath"));
		}else{
			System.out.println("Generating rarefaction curves...");
			
			//TODO add sRarefactionMode to usage object
			
			DataIO.writeToFile(mio1.printRarefactionCurves(
					lstDepths,
					lstH,
					arg1.getValueInt("iRandomSeed"),
					arg1.getValueInt("iHMMAverage"),
					arg1.getValueInt("iMetagenomeRarefactionDepth"),
					arg1.getValueInt("iBootstrapIterations"),
					arg1.getValueString("sMetagenomeTaxonRank"),
					arg1.getValueString("sRarefactionMode")), 
					arg1.getValueString("sOutputPath"));
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	/*
	private static void testData1(String sOutputTestDataPath, int iMinimumReadDepthForAssembly) throws Exception{
		
		//rnd1 = random number object
		//map1 = map of gene lengths
		//iMetagenomes = number of metagenomes
		//iLength = current gene length
		//iDepth = current gene depth
		//mapTotal = total number of assembled reads in metagenome
		//iTotalReads = current total number of reads
		//mio1 = metagenomes io object
		//mapMetagenomes = map from metagenome ID to metagenomes
		//sMetagenome = current metagenome
		//sGene = current gene
		//iGenes = number of genes
		
		MetagenomesIO mio1;
		String sMetagenome;
		String sGene;
		int iMetagenomes;
		Random rnd1;
		HashMap<String,Integer> map1;
		HashMap_AdditiveInteger<String> mapTotal;
		int iLength;
		int iDepth;
		int iTotalReads;
		HashMap<String,Metagenome> mapMetagenomes;
		int iGenes;
		
		//initializing variables
		iMetagenomes=10;
		iGenes = 1000;
		rnd1 = new Random(1234);
		map1 = new HashMap<String,Integer>(iGenes);
		mapMetagenomes = new HashMap<String,Metagenome>(iMetagenomes);
		
		//initializing gene length map
		for(int j=0;j<iGenes;j++){
			sGene = "C" + j;
			map1.put(sGene, (int) (rnd1.nextDouble()*10000));
		}
		
		//writing gene data
		mapTotal = new HashMap_AdditiveInteger<String>();
		for(int i=0;i<iMetagenomes;i++){
			sMetagenome = "M" + i;
			mapMetagenomes.put(sMetagenome, new Metagenome(sMetagenome, 0, (i+1)*50, 150.));
			for(int j=0;j<(i+1)*50;j++){
				sGene = "C" + j;
				iLength = map1.get(sGene);
				iDepth = iMinimumReadDepthForAssembly + (int) (rnd1.nextDouble()*10);		
				mapMetagenomes.get(sMetagenome).addGene(sGene, iDepth, iLength);
				if(!mapTotal.containsKey(sMetagenome)){
					mapTotal.put(sMetagenome, iLength*iDepth);
				}else{
					mapTotal.putSum(sMetagenome, iLength*iDepth);
				}
			}
		}
		
		//writing metagenome data
		for(int i=0;i<iMetagenomes;i++){
			sMetagenome = "M" + i;
			iTotalReads=(int) (rnd1.nextDouble());
			if(iTotalReads<mapTotal.get(sMetagenome)){
				iTotalReads = mapTotal.get(sMetagenome);
			}
			mapMetagenomes.get(sMetagenome).updateTotalReads(iTotalReads);
		}
		mio1 = new MetagenomesIO(mapMetagenomes);
		DataIO.writeToFile(mio1.printJSON(), sOutputTestDataPath);	
	}
	
	*/

	private static void testData2(String sOutputTestDataPath, int iMinimumReadDepthForAssembly) throws Exception{
		
		//rnd1 = random number object
		//map1 = map of gene lengths
		//iMetagenomes = number of metagenomes
		//iLength = current gene length
		//iDepth = current gene depth
		//mapTotal = total number of assembled reads in metagenome
		//iTotalReads = current total number of reads
		//mio1 = metagenomes io object
		//mapMetagenomes = map from metagenome ID to metagenomes
		//sMetagenome = current metagenome
		//sGene = current gene
		//iGenes = number of genes
		//iReadLength = read length
		
		int iReadLength;
		MetagenomesIO mio1;
		String sMetagenome;
		String sGene;
		int iMetagenomes;
		Random rnd1;
		HashMap<String,Integer> map1;
		HashMap_AdditiveInteger<String> mapTotal;
		int iLength;
		int iDepth;
		int iTotalReads;
		HashMap<String,Metagenome> mapMetagenomes;
		int iGenes;
		
		//initializing variables
		iReadLength = 150;
		iMetagenomes=100;
		iGenes = 10;
		rnd1 = new Random(1234);
		map1 = new HashMap<String,Integer>(iGenes);
		mapMetagenomes = new HashMap<String,Metagenome>(iMetagenomes);
		
		//initializing gene length map
		for(int j=0;j<iGenes;j++){
			sGene = "C" + j;
			map1.put(sGene, (int) (rnd1.nextDouble()*10000));
		}
		
		//writing gene data
		mapTotal = new HashMap_AdditiveInteger<String>();
		for(int i=0;i<iMetagenomes;i++){
			sMetagenome = "M" + i;
			mapMetagenomes.put(sMetagenome, new Metagenome(sMetagenome, 0, (i+1)*50, 150., null));
			for(int j=0;j<iGenes;j++){
				sGene = "C" + j;
				iLength = map1.get(sGene);
				iDepth = iMinimumReadDepthForAssembly + j;		
				mapMetagenomes.get(sMetagenome).addGene(sGene, iDepth, iLength, null, 0, 0, null);
				if(!mapTotal.containsKey(sMetagenome)){
					mapTotal.put(sMetagenome, iLength*iDepth/iReadLength);
				}else{
					mapTotal.putSum(sMetagenome, iLength*iDepth/iReadLength);
				}
			}
		}
		
		//writing metagenome data
		for(int i=0;i<iMetagenomes;i++){
			sMetagenome = "M" + i;
			iTotalReads=1500000;
			if(iTotalReads<mapTotal.get(sMetagenome)){
				iTotalReads = mapTotal.get(sMetagenome);
			}
			mapMetagenomes.get(sMetagenome).updateTotalReads(iTotalReads);
		}
		mio1 = new MetagenomesIO(mapMetagenomes);
		DataIO.writeToFile(mio1.printJSON(), sOutputTestDataPath);	
	}
}