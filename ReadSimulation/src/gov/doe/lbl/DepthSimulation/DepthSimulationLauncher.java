package gov.doe.lbl.DepthSimulation;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class DepthSimulationLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datReads = read counts
		//datGenes = gene data
		//gen1 = sequenced genome (sequenced)
		//gen2 = second genome (simulated)
		//ins1 = sequenced insertion
		//ins2 = simulated insertion
		//iInsLength = length of new insertion
		//iReadLength = read length
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO datReads;
		DataIO datGenes;
		Genome gen1;
		Genome gen2;
		Insertion ins1 = null;
		Insertion ins2;
		int iInsLength = 0;
		int iReadLength;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datReads = new DataIO(arg1.getValueString("sReadFrequenciesPath"));
		datGenes = new DataIO(arg1.getValueString("sGeneDataPath"));
		iReadLength = arg1.getValueInt("iReadLength");
		
		//loading insertions
		for(int i=1;i<datGenes.iRows;i++){
			if(datGenes.getString(i, "GENE_ID").equals(arg1.getValueString("sOriginalGeneID"))){
				ins1 = new Insertion(datGenes.getInteger(i, "POSITION_START")-1,datGenes.getInteger(i, "POSITION_END")-1); 
			}
			if(datGenes.getString(i, "GENE_ID").equals(arg1.getValueString("sSimulatedGeneID"))){
				iInsLength = datGenes.getInteger(i, "LENGTH");
			}
		}
		ins2 = new Insertion(ins1.start(),ins1.start()+iInsLength-1);
		
		//loading sequenced genome
		gen1 = new Genome(datReads.iRows, ins1, "sequenced");
		for(int i=1;i<datReads.iRows;i++){
			gen1.setReadStarts(i-1, datReads.getInteger(i, "READ_START_FREQUENCY"));
		}
		gen2 = new Genome(datReads.iRows-ins1.length()+ins2.length(), ins2, "simulated");
		
		//finding simulated genome
		gen1.mapTo(gen2);
		gen1.loadDepths(iReadLength);
		gen2.loadDepths(iReadLength);
		
		//outputting results
		lstOut = new ArrayList<String>(2*arg1.getValueInt("iTrimDistance")+10000);
		lstOut.addAll(gen1.print(arg1.getValueInt("iTrimDistance"), true));
		lstOut.addAll(gen2.print(arg1.getValueInt("iTrimDistance"), false));
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	/*
	
	public static void main(String[] rgsArgs){
		
		//arg1 = arguments
		//lst1 = read start frequencies
		//lst2 = list of depths
		//lst3 = list of new depths
		//lstOut = output
		//rgiInitial = start and end index of initial insertion
		//rgiInsertions = start (0) and end (1) index of initial insertion, and end (2) index of new insertion
		//iLength = read length
		//iBases = number of bases
		
		ArgumentIO arg1;
		ArrayList<Integer> lst1;
		ArrayList<Integer> lst2;
		ArrayList<Integer> lst3;
		int iLength;
		int iBases;
		Integer[] rgiInsertions;
		ArrayList<String> lstOut;
		
		//loading initial variables
		arg1 = new ArgumentIO(rgsArgs);
		iLength = arg1.getValueInt("iReadLength");
		iBases = arg1.getValueInt("iBases");
		rgiInsertions = arg1.getValueIntegerArray("rgiInsertions");
		
		//loading read start frequencies
//		lst1 = loadReadStartFrequenciesSinCos(
//				iLength,
//				iBases);
		lst1 = loadReadStartFrequenciesPoisson(
				iLength,
				iBases,
				arg1.getValueInt("iRandomSeed"));
		
		//loading depths
		lst2 = loadDepths(lst1, iLength);
		lst3 = loadDepthsShorter(lst1, iLength, rgiInsertions);
		
		//outputting results
		lstOut = new ArrayList<String>(lst2.size()+1);
		lstOut.add("POSITION,INSERTION_INITIAL,DEPTH_INITIAL,INSERTION_NEW,DEPTH_NEW");
		for(int i=0;i<lst2.size();i++){
			if(i<lst3.size()){
				lstOut.add(
						i + 
						"," + isInInitialInsertion(i, rgiInsertions) +
						"," + lst2.get(i) +
						"," + isInNewInsertion(i, rgiInsertions) +
						"," + lst3.get(i));
			}else{
				lstOut.add(
						i + 
						"," + isInInitialInsertion(i, rgiInsertions) +
						"," + lst2.get(i) +
						"," + "NA" +
						"," + "NA");
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
	
	private static boolean isInInitialInsertion(int i1, Integer[] rgiInsertions){
		if(rgiInsertions[0]<=i1 && i1<=rgiInsertions[1]){
			return true;
		}else{
			return false;
		}
	}
	
	private static boolean isInNewInsertion(int i1, Integer[] rgiInsertions){
		if(rgiInsertions[0]<=i1 && i1<=rgiInsertions[2]){
			return true;
		}else{
			return false;
		}
	}
	
	private static ArrayList<Integer> loadDepthsLonger(ArrayList<Integer> lstStartFrequencies, int iReadLength, Integer[] rgiInsertions){
		
		
		
	}
	
	
	private static ArrayList<Integer> loadDepthsShorter(ArrayList<Integer> lstStartFrequencies, int iReadLength, Integer[] rgiInsertions){
		
		//lst2 = edited start frequencies list
		
		ArrayList<Integer> lst2;
		
		lst2 = new ArrayList<Integer>(lstStartFrequencies.size());
		for(int i=0;i<lstStartFrequencies.size();i++){
			if(isInInitialInsertion(i, rgiInsertions) && !isInNewInsertion(i, rgiInsertions)){
				continue;
			}
			lst2.add(lstStartFrequencies.get(i));
		}
		return loadDepths(lst2,iReadLength);
	}
	
	private static ArrayList<Integer> loadDepths(ArrayList<Integer> lstStartFrequencies, int iReadLength){
		
		//lst1 = output
		//i1 = current start
		//i2 = current depth
		
		ArrayList<Integer> lst1;
		int i1;
		int i2;
		
		lst1 = new ArrayList<Integer>(lstStartFrequencies.size());
		for(int i=0;i<lstStartFrequencies.size();i++){
			i1 = i-iReadLength+1;
			if(i1<0){
				i1 = 0;
			}
			i2 = 0;
			for(int j=i1;j<i;j++){
				i2+=lstStartFrequencies.get(j);
			}
			lst1.add(i2);
		}
		return lst1;
	}
	
	private static ArrayList<Integer> loadReadStartFrequenciesSinCos(int iReadLength, int iBases){
		
		//lst1 = output
		//d1 = mean depth
		//d2 = current value
		//d3 = current index
		
		ArrayList<Integer> lst1;
		double d1;
		double d2;
		double d3;
		
		lst1 = new ArrayList<Integer>(iBases);
		d1 = 1000./((double) iReadLength);
		for(int i=0;i<iBases;i++){
			d3 = (double) i;
			d2 = d1+d1/2.*(Math.sin(d3*0.02) + Math.cos(d3*0.07));
			lst1.add(Math.max(1, (int) d2));
		}
		return lst1;
	}
	
	
	private static ArrayList<Integer> loadReadStartFrequenciesPoisson(int iReadLength, int iBases, int iRandomSeed){
		
		//lst1 = output
		//d1 = mean depth
		//rnd1 = random number generator
		
		ArrayList<Integer> lst1;
		double d1;
		Random rnd1;
		
		lst1 = new ArrayList<Integer>(iBases);
		d1 = 1000./((double) iReadLength);
		rnd1 = new Random(iRandomSeed);
		for(int i=0;i<iBases;i++){
			lst1.add(poissonVariate(d1,rnd1));
		}
		return lst1;
	}
	
	private static int poissonVariate(double dLambda, Random rnd1){
		
		//d1 = product
		//d2 = exp(-lambda)
		//i1 = counter
		
		double d1;
		double d2;
		int i1;
		
		d2 = Math.exp(-dLambda);
		d1 = rnd1.nextDouble();
		i1 = 0;
		while(d1>d2){
			d1*=rnd1.nextDouble();
			i1++;
		}
		return i1;
	}
	
	*/
}
