package edu.ucsf.PercentileRankComparison;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import com.google.common.base.Joiner;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Computes percentile ranks of specified comparison columns within a reference column
 * @author jladau
 *
 */

public class PercentileRankComparisonLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = dataio object
		//rgd1 = current set of data
		//lstOut = output
		//rgdRank = rank of reference values
		//rsb1 = string builder array
		//rgdFrac = fractions
		
		StringBuilder[] rsb1;
		ArgumentIO arg1;
		DataIO dat1;
		double rgd1[];
		ArrayList<String> lstOut;
		double rgdRank[];
		double rgdFrac[];
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//initializing reference data
		rgd1 = new double[dat1.iRows-1];
		for(int i=1;i<dat1.iRows;i++){
			rgd1[i-1]=dat1.getDouble(i,arg1.getValueString("sReferenceColumn"));
		}
		
		//loading ranking for reference data
		Arrays.sort(rgd1);
		rgdRank = (new NaturalRanking()).rank(rgd1);
		
		//initializing output
		lstOut = new ArrayList<String>();
		rsb1 = new StringBuilder[dat1.iRows];
		rsb1[0] = new StringBuilder();
		rsb1[0].append(arg1.getValueString("sReferenceColumn") + "," + arg1.getValueString("sReferenceColumn") + "_RANK" + "," + Joiner.on(",").join(arg1.getValueStringArray("rgsComparisonColumns")));
		for(int i=0;i<rgd1.length;i++){
			rsb1[i+1] = new StringBuilder();
			rsb1[i+1].append(rgd1[i] + "," + (rgdRank.length-rgdRank[i]));
		}
		
		//looping through comparison columns
		for(String s:arg1.getValueStringArray("rgsComparisonColumns")){
			rgdFrac = findFractionGreater(rgd1,dat1.getDoubleColumn(s));
			for(int i=0;i<rgdFrac.length;i++){
				rsb1[i+1].append("," + rgdFrac[i]);
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(rsb1.length);
		for(int i=0;i<rsb1.length;i++){
			lstOut.add(rsb1[i].toString());
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double[] findFractionGreater(double[] rgdValues, ArrayList<Double> lstDistribution){
		
		//rgdOut = output
		
		double rgdOut[];
		
		rgdOut = new double[rgdValues.length];
		for(int i=0;i<rgdValues.length;i++){
			for(double d:lstDistribution){
				if(d>rgdValues[i]){
					rgdOut[i]++;
				}
			}
		}
		for(int i=0;i<rgdOut.length;i++){
			rgdOut[i]=rgdOut[i]/((double) lstDistribution.size());
		}
		return rgdOut;
	}
}
