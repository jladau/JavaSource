package edu.ucsf.RandomEnvironmentalSequences;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Simulates random sequences based on the variability of loci at a fasta file. An analogous fasta file with the sequences replaced by random sequences is output.
 * @author Joshua Ladau, jladau@gmail.com
 */

public class RandomEnvironmentalSequencesLauncher {

	/**
	 * Simulates random, independent base changes.
	 * Outputs three files: (i) file listing observed base probabilities and sequence counts at each locus (*.probabilities-observed.csv), (ii) fasta file with simulated sequences according to probabilities in i, and (iii) a file listing simulated base probabilities and sequence counts at each locus (for error-checking; *.probabilities-simulated.csv).
	 * @param Arguments pass as --{argument name}={argument value}. Name-value pairs are:
	 * 				
	 * 				<p>
	 * 				<h4 class="list-heading">Required arguments</h4>
	 * 				<ul> 
	 * 				<li>sOutputPath [string] = Absolute path for output file. Output is in fasta format and should have a ".fasta" suffix.
	 * 				<p>
	 * 				<li>sFastaPath [string] = Path of file with fasta files.
	 * 				<p>
	 * 				<li>iRandomSeed [integer] = Random seed to use for simulating random sequences.
	 *				</ul>
	 **/
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//tblPr = for a given locus returns the probability of each base
		
		HashBasedTable<Integer,String,Double> tblPr = null;
		ArgumentIO arg1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading and printing table of probabilities
		System.out.println("Loading observed probabilities...");
		try{
			tblPr = loadProbabilities(arg1.getValueString("sFastaPath"));
		}catch(Exception e){
			e.printStackTrace();
		}
		printProbabilities(tblPr,arg1.getValueString("sOutputPath").replace("fasta","probabilities-observed.csv"));
		
		//simulating sequences
		System.out.println("Simulating sequences...");
		try{
			simulateSequences(tblPr, arg1.getValueString("sFastaPath"),arg1.getValueString("sOutputPath"),arg1.getValueInt("iRandomSeed"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//printing simulated probabilities
		System.out.println("Loading simulated probabilities...");
		try{
			tblPr = loadProbabilities(arg1.getValueString("sOutputPath"));
		}catch(Exception e){
			e.printStackTrace();
		}
		printProbabilities(tblPr,arg1.getValueString("sOutputPath").replace("fasta","probabilities-simulated.csv"));
		
		//terminating
		System.out.println("Done.");
	}
	
	/**
	 * Prints probabilities of each base occurring at each position.
	 * @param tblPr Table of probabilities with rows giving position and columns giving base ID or sequence count key.
	 * @param sOutputPath Path to print to.
	 */
	private static void printProbabilities(HashBasedTable<Integer,String,Double> tblPr, String sOutputPath){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		//loading output
		lstOut = new ArrayList<String>(tblPr.rowKeySet().size()+1);
		lstOut.add("POSITION,SEQUENCE_COUNT,A,T,G,C");
		for(int i=0;i<tblPr.rowKeySet().size();i++){
			lstOut.add(i + "," + tblPr.get(i, "SEQUENCE_COUNT") + "," + tblPr.get(i,"A") + "," + tblPr.get(i,"T") + "," + tblPr.get(i,"G") + "," + tblPr.get(i,"C"));
		}		
		
		//printing output
		DataIO.writeToFile(lstOut, sOutputPath);
	}
	
	/**
	 * Converts table giving probabilities of each base to cdf usable for efficient simulation.
	 * @param tblPr Table of probabilities with rows giving position and columns giving base ID or sequence count key.
	 */
	private static HashMap<Integer,TreeMap<Double,String>> convertToCDF(HashBasedTable<Integer,String,Double> tblPr){
		
		//mapOut = output
		//d1 = current cumulant
		
		HashMap<Integer,TreeMap<Double,String>> mapOut;
		double d1;
		
		mapOut = new HashMap<Integer,TreeMap<Double,String>>(tblPr.size());
		for(int i=0;i<tblPr.size();i++){
			mapOut.put(i,new TreeMap<Double,String>());
			d1 = 0.;
			for(String s:tblPr.columnKeySet()){
				if(!s.equals("SEQUENCE_COUNT")){	
					if(tblPr.contains(i, s)){
						mapOut.get(i).put(d1, s);
						d1+=tblPr.get(i, s);
					}
				}
			}
		}
		return mapOut;
	}
	
	/**
	 * Loads probability of each base at each location.
	 * @param sFastaPath Path to fasta file with sequences.
	 * @return A table giving the probability of each base at each location and also the sequence counts at each location.
	 */
	private static HashBasedTable<Integer,String,Double> loadProbabilities(String sFastaPath) throws Exception{
		
		//bfr1 = buffered reader
		//s1 = current line
		//rgc1 = current line as character array
		//tbl1 = output in table format
		//d1 = current count
		//tblOut = output
		//sBase = current base in string form
		//mapCounts = sequence counts
		//iPosition = current position
		
		BufferedReader bfr1;
		String s1;
		String sBase;
		char rgc1[];
		HashBasedTable<String,Integer,Double> tbl1=null;
		double d1;
		HashBasedTable<Integer,String,Double> tblOut;
		HashMap<Integer,Double> mapCounts;
		int iPosition=0;
		
		bfr1 = new BufferedReader(new FileReader(sFastaPath));
		mapCounts = new HashMap<Integer,Double>();
		while((s1=bfr1.readLine())!=null){
			
			//checking if line with sequence
			if(!s1.startsWith(">")){
			
				//converting sequence to character array
				rgc1 = s1.replace("U","T").toCharArray();
			
				//initializing probability table if necessary
				if(tbl1 == null){
					tbl1 = HashBasedTable.create();
				}
				
				//updating counts
				for(int i=0;i<rgc1.length;i++){
					sBase = Character.toString(rgc1[i]);
					
					//checking if base is resolved
					if(!sBase.equals("A") && !sBase.equals("T") && !sBase.equals("G") && !sBase.equals("C")){
						iPosition++;
						continue;
					}
					
					//updating base frequency
					if(tbl1.contains(sBase, iPosition)){
						d1=tbl1.get(sBase, iPosition);
					}else{
						d1 = 0.;
					}
					d1++;
					tbl1.put(sBase, iPosition, d1++);
					
					//updating sequence counts
					if(mapCounts.containsKey(iPosition)){
						d1=mapCounts.get(iPosition);
					}else{
						d1=0.;
					}
					d1++;
					mapCounts.put(iPosition, d1);
					iPosition++;
				}
			}else{
				iPosition=0;
			}
		}
		
		//closing reader
		bfr1.close();
		
		//loading results into treemap
		tblOut = HashBasedTable.create(mapCounts.size(), 4);
		for(Integer i:tbl1.columnKeySet()){
			for(String s:tbl1.rowKeySet()){
				if(tbl1.contains(s, i)){
					tblOut.put(i, s, tbl1.get(s, i)/mapCounts.get(i));
				}else{
					tblOut.put(i, s, 0.);
				}
			}
			tblOut.put(i, "SEQUENCE_COUNT", mapCounts.get(i));
		}
		
		//returning result
		return tblOut;
	}

	/**
	 * Simulates sequences according to specified probabilities of each base at each position.
	 * @param tblPr Table of probabilities with rows giving position and columns giving base ID or sequence count key. 
	 * @param sFastaPath Path to fasta file with sequences.
	 * @param sOutputPath Output path.
	 * @param iRandomSeed Random seed.
	 */
	private static void simulateSequences(HashBasedTable<Integer,String,Double> tblPr, String sFastaPath, String sOutputPath, int iRandomSeed) throws Exception{
		
		//bfr1 = buffered reader
		//s1 = current line
		//prt1 = print writer
		//sbl1 = stringbuilder for current line
		//rgs1 = output
		//d1 = current random number
		//mapCDF = cdf
		//iPosition = current position
		//rgc1 = current sequence
		//sBase = current base in string form
		
		PrintWriter prt1;
		BufferedReader bfr1;
		String s1;
		Random rnd1;
		String rgs1[];
		double d1;
		HashMap<Integer,TreeMap<Double,String>> mapCDF;
		int iPosition=0;
		String sBase;
		char rgc1[];
		
		//loading cdf
		mapCDF = convertToCDF(tblPr);
		
		//initializing reader and writer
		bfr1 = new BufferedReader(new FileReader(sFastaPath));
		prt1 = new PrintWriter(new FileWriter(sOutputPath, false));
		
		//initializing random number generator
		rnd1 = new Random(iRandomSeed);
		
		//looping through output
		while((s1=bfr1.readLine())!=null){
			if(s1.startsWith(">")){
				prt1.println(s1);
				iPosition=0;
			}else{
			
				//converting sequence to character array
				rgc1 = s1.replace("U","T").toCharArray();
				rgs1 = new String[rgc1.length];
				
				//looping through bases
				for(int i=0;i<rgc1.length;i++){
					sBase = Character.toString(rgc1[i]);
				
					//checking if base is resolved
					if(!sBase.equals("A") && !sBase.equals("T") && !sBase.equals("G") && !sBase.equals("C")){
						rgs1[i]="-";
					}else{
						d1 = rnd1.nextDouble();
						
						rgs1[i]=mapCDF.get(iPosition).get(mapCDF.get(iPosition).floorKey(d1));
					}
					iPosition++;
				}
				prt1.println(Joiner.on("").join(rgs1));
			}
		}
		
		//terminating
		bfr1.close();
		prt1.close();
	}
}