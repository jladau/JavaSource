package gov.doe.jgi.RarefactionIntersections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * Counts the number of crossing points of rarefaction curves. Note: curves must have the same x-values
 * @author jladau
 *
 */

public class RarefactionIntersectionsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments

		ArgumentIO arg1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		if(arg1.getValueString("sMode").equals("from-biom-table")){
			intersectionsFromBIOM(arg1);
		}else if(arg1.getValueString("sMode").equals("from-rarefaction-curves")){
			intersectionsFromCurves(arg1);
		}
				
		//terminating
		System.out.println("Done.");	
	}
	
	private static void intersectionsFromCurves(ArgumentIO arg1){

		//lstOut = output
		//dat1 = curves data
		//lstX = list of read counts
		//lstY1 = list of richnesses from first sample 
		//lstY2 = list of richnesses from second sample
		//dThreshold = threshold for differencing
		//map1 = current map of intersections
		//mapRich = total richness for each metagenome
		//iMax = maximum read count for rarefaction curves
		//i1 = maximum row index
		//i2 = counter
		//i3 = total pairs
		
		HashMap<Integer,Double[]> map1;
		ArrayList<String> lstOut;
		DataIO dat1;
		ArrayList<Double> lstY1;
		ArrayList<Double> lstY2;
		ArrayList<Integer> lstX;
		double dThreshold;
		HashMap<String,Double> mapRich;
		int iMax;
		int i1;
		int i2;
		int i3;
		
		//loading variables
		dat1 = new DataIO(arg1.getValueString("sRarefactionCurvesPath"));
		lstOut = new ArrayList<String>(dat1.iCols*(dat1.iCols-1)/2*2);
		lstOut.add("SAMPLE_1,SAMPLE_2,INTERSECTION_READ_DEPTH,INTERSECTION_RICHNESS_SAMPLE_1,INTERSECTION_RICHNESS_SAMPLE_2,SAMPLE_1_RICHNESS,SAMPLE_2_RICHNESS");
		lstX = dat1.getIntegerColumn("NUMBER_READS");
		dThreshold = 0.1;
		
		//loading sample richnesses
		iMax = -Integer.MAX_VALUE;
		i1 = -9999;
		for(int i=1;i<dat1.iRows;i++){
			if(dat1.getInteger(i, "NUMBER_READS")>iMax){
				iMax=dat1.getInteger(i, "NUMBER_READS");
				i1 = i;
			}
		}
		mapRich = new HashMap<String,Double>();
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0, j).equals("NUMBER_READS")){
				mapRich.put(dat1.getString(0, j), dat1.getDouble(i1, j));
			}
		}
		
		//looping through pairs of samples
		i2 = 0;
		i3 = (dat1.iCols-1)*(dat1.iCols-2)/2;
		for(int j=1;j<dat1.iCols;j++){
			if(!dat1.getString(0, j).equals("NUMBER_READS")){
				lstY1 = dat1.getDoubleColumn(dat1.getString(0, j));
				for(int k=0;k<j;k++){
					if(!dat1.getString(0, k).equals("NUMBER_READS")){
						i2++;
						if(i2 % 100 == 0){
							System.out.println("Finding intersection for sample pair " + i2 + " of " + i3 + "...");
						}
						lstY2 = dat1.getDoubleColumn(dat1.getString(0, k));
						map1 = intersectionsFromCurves(lstX, lstY1, lstY2, dThreshold);
						for(Integer l:map1.keySet()){
							lstOut.add(dat1.getString(0, j) +
									"," + dat1.getString(0, k) +
									"," + l +
									"," + map1.get(l)[0] +
									"," + map1.get(l)[1] +
									"," + mapRich.get(dat1.getString(0, j)) +
									"," + mapRich.get(dat1.getString(0, k)));
						}
					}
				}
			}
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
	}
	
	private static HashMap<Integer,Double[]> intersectionsFromCurves(ArrayList<Integer> lstX, ArrayList<Double> lstY1, ArrayList<Double> lstY2, double dThreshold){
		
		//map1 = output
		//i1 = previous non-zero difference
		//d1 = current difference
		//lst1 = list of difference signs
		//lst2 = list of thresholded difference signs
		
		HashMap<Integer,Double[]> map1;
		int i1;
		double d1;
		ArrayList<Integer> lst1;
		ArrayList<Integer> lst2;
		
		map1 = new HashMap<Integer,Double[]>(10);
		lst1 = new ArrayList<Integer>(lstX.size());
		lst2 = new ArrayList<Integer>(lstX.size());
		for(int i=0;i<lstX.size();i++){
			d1 = lstY1.get(i)-lstY2.get(i);
			lst1.add((int) Math.signum(d1));
			if(Math.abs(d1)>dThreshold){
				lst2.add((int) Math.signum(d1));
			}else{
				lst2.add(0);
			}
		}
		i1 = -9999;
		for(int i=0;i<lst1.size();i++){
			if(lst2.get(i)!=0){
				if(i1==-9999){
					i1 = lst2.get(i);
				}else{
					if(lst2.get(i)!=i1){
						for(int j=i;j>=0;j--){
							if(lst1.get(j)!=lst2.get(i)){
								map1.put(lstX.get(j), new Double[]{lstY1.get(j),lstY2.get(j)});
								break;
							}
						}
						i1 = lst2.get(i);
					}
				}
			}
		}
		return map1;
	}
	
	private static void intersectionsFromBIOM(ArgumentIO arg1) throws Exception{
		
		//lstOut = output
		//bio1 = biom table
		//mapRichness = richness map
		//set1 = first set of relative abundances
		//set2 = second set of relative abundances
		//rgd1 = current crossing point
		//dReadsMax = maximum number of reads (rarefaction depth)
		//map1 = map of sample relative abundances
		//lst1 = list of sample ids
		//s1 = first sample
		//s2 = second sample
		//i1 = current counter
		//i2 = total count
		
		String s1;
		String s2;
		ArrayList<String> lst1;
		ArrayList<String> lstOut;
		BiomIO bio1;
		HashSet<Double> set1;
		HashSet<Double> set2;
		double rgd1[];
		double dReadsMax;
		HashMap<String,HashSet<Double>> map1;
		int i1;
		int i2;
		
		//loading variables
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		dReadsMax = (double) bio1.randomSampleDepth();
		System.out.println("Normalizing to relative abundance...");
		bio1.normalize();
		lstOut = new ArrayList<String>(bio1.axsSample.size()*(bio1.axsSample.size()-1)/2+1);
		lstOut.add("SAMPLE_1,SAMPLE_2,INTERSECTION_READ_DEPTH,INTERSECTION_RICHNESS_SAMPLE_1,INTERSECTION_RICHNESS_SAMPLE_2,SAMPLE_1_RICHNESS,SAMPLE_2_RICHNESS");
		map1 = new HashMap<String,HashSet<Double>>(bio1.axsSample.size());
		i2 = bio1.axsSample.size();
		i1 = 1;
		for(String s:bio1.axsSample.getIDs()){
			System.out.println("Loading non-zero values for sample " + i1 + " of " + i2 + "...");
			i1++;
			map1.put(s, loadRelativeAbundances(bio1.getNonzeroValues(bio1.axsSample, s)));
		}
		lst1 = new ArrayList<String>(map1.keySet());
		
		//looping through pairs of samples
		i2 = lst1.size()*(lst1.size()-1)/2;
		i1 = 1;
		for(int i=1;i<lst1.size();i++){
			s1 = lst1.get(i);
			set1 = map1.get(s1);
			for(int j=0;j<i;j++){
				s2 = lst1.get(j);
				set2 = map1.get(s2);
				if(i1%100==0){
					System.out.println("Finding intersection for sample pair " + i1 + " of " + i2 + "...");
				}
				i1++;
				rgd1 = rootBisection(set1, set2, dReadsMax);
				if(rgd1!=null){
					lstOut.add(s1 + "," + s2 + "," + rgd1[0] + "," + rgd1[1] + "," + rgd1[2] + "," + set1.size() + "," + set2.size());
				}
			}	
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));	
	}	
	
	private static HashSet<Double> loadRelativeAbundances(HashMap<String,Double> map1){
		
		//set1 = output
		
		HashSet<Double> set1;
		
		set1 = new HashSet<Double>(map1.size());
		for(String u:map1.keySet()){
			if(!u.equals("unclassified")){
				set1.add(map1.get(u));
			}
		}
		return set1;
	}
	
	private static double[] rootBisection(HashSet<Double> set1, HashSet<Double> set2, double dReadsMax){
		
		//dReads = current number of reads
		//rgdCurrent = current richnesses
		//dStep = current step size
		//iSigFinal = final difference

		int iSigFinal;
		double dReads;
		double dStep;
		double rgdCurrent[];
		
		iSigFinal = signum(set1.size(), set2.size());
		if(iSigFinal==0){
			return null;
		}
		dStep = dReadsMax/2.;
		dReads = dReadsMax/2.;
		rgdCurrent = new double[]{richness(dReads,set1), richness(dReads,set2)};
		do{
			dStep = dStep/2.;
			if(iSigFinal!=signum(rgdCurrent[0],rgdCurrent[1])){
				dReads+=dStep;
			}else{
				dReads-=dStep;
			}
			rgdCurrent = new double[]{richness(dReads,set1), richness(dReads,set2)};
			if(dReads<=1){
				return null;
			}
		}while(dStep>1);
		return new double[]{dReads,rgdCurrent[0],rgdCurrent[1]};
	}
	
	private static int signum(int i1, int i2){
		if(i1==i2){
			return 0;
		}else if(i1<i2){
			return -1;
		}else{
			return 1;
		}
	}
	
	private static int signum(double d1, double d2){
		if(Math.abs(d1-d2)<0.000000001){
			return 0;
		}else{
			if(d1<d2){
				return -1;
			}else{
				return 1;
			}
		}
		
	}
	
	private static double richness(double dReads, HashSet<Double> set1){
		
		//d1 = sum
		//d2 = current term
		
		double d1;
		double d2;
		
		d1 = 0;
		for(double d:set1){
			d2 = dReads*Math.log(1.-d);
			d1+= 1. - Math.exp(d2);
		}
		return d1;
	}	
}