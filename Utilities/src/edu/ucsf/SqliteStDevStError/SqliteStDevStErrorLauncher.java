package edu.ucsf.SqliteStDevStError;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates standard deviation and standard error on sqlite output. Required columns headers are Sx (sum of values), Sx2 (sum of squared values), and n (counts)
 * @author jladau
 *
 */

public class SqliteStDevStErrorLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//sbl1 = current output line
		//sPrefix = output header prefix
		//b1 = flag for whether string 
		//lstSx = sum of values
		//lstSx2 = sum of squared values
		//lstN = total number of values
		//d1 = calculation intermediate
		
		double d1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		String sPrefix;
		ArrayList<Double> lstSx;
		ArrayList<Double> lstSx2;
		ArrayList<Double> lstN;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sPrefix = arg1.getValueString("sOutputHeaderPrefix");
		
		lstSx = dat1.getDoubleColumn("Sx");
		lstSx2 = dat1.getDoubleColumn("Sx2");
		lstN = dat1.getDoubleColumn("n");
		
		lstOut = new ArrayList<String>(dat1.iRows);
		sbl1 = new StringBuilder();
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0, j).equals("Sx") && !dat1.getString(0, j).equals("Sx2") && !dat1.getString(0, j).equals("n")){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(0, j));
			}
		}
		sbl1.append("," + sPrefix + "_STDEV," + sPrefix + "_STERROR");
		lstOut.add(sbl1.toString());
		
		for(int i=1;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(int j=0;j<dat1.iCols;j++){
				if(!dat1.getString(0, j).equals("Sx") && !dat1.getString(0, j).equals("Sx2") && !dat1.getString(0, j).equals("n")){
					if(sbl1.length()>0){
						sbl1.append(",");
					}
					sbl1.append(dat1.getString(i, j));
				}
			}
			d1 = lstSx2.get(i-1) + lstSx.get(i-1)*lstSx.get(i-1)/lstN.get(i-1);
			d1 = d1/(lstN.get(i-1) - 1.);
			d1 = Math.sqrt(d1);
			sbl1.append("," + d1 + "," + d1/Math.sqrt(lstN.get(i-1)));
			lstOut.add(sbl1.toString());
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}