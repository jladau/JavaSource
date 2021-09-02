package edu.ucsf.SarEarSlopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import static java.lang.Math.*;

/**
 * Calculates SAR/EAR slopes. Input must be sorted by study region, sampling plot radiuss
 * @author jladau
 */

public class SarEarSlopesLauncher {
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstRadii = set of radii
		//lstOut = output
		//cap1 = current first cap
		//cap2 = current second cap
		//dArea1 = area 1 log-transformed
		//dArea2 = area 2 log-transformed
		//dObs1 = observed value 1
		//dObs2 = observed value 2
		//dPred1 = predicted value 1
		//dPred2 = predicted value 2
		
		SphericalCapEarth cap1;
		SphericalCapEarth cap2;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<Double> lstRadii;
		ArrayList<String> lstOut;
		double dArea1;
		double dArea2;
		double dObs1;
		double dObs2;
		double dPred1;
		double dPred2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("STUDY_REGION_ID,"
				+ "SAMPLING_PLOT_RADIUS_START,"
				+ "SAMPLING_PLOT_RADIUS_END,"
				+ "SAMPLING_PLOT_AREA_START,"
				+ "SAMPLING_PLOT_AREA_END,"
				+ "SAMPLING_PLOT_AREA_MEAN,"
				+ "LOG_SAR_OBSERVED_SLOPE,"
				+ "LOG_SAR_PREDICTED_SLOPE,"
				+ "SAR_OBSERVED_SLOPE,"
				+ "SAR_PREDICTED_SLOPE");
		
		//looping through values and outputting results
		for(int i=2;i<dat1.iRows;i++){
			if(dat1.getString(i, "STUDY_REGION_ID").equals(dat1.getString(i-1, "STUDY_REGION_ID"))){
				cap1 = new SphericalCapEarth(dat1.getDouble(i-1, "SAMPLING_PLOT_RADIUS"),0.,0.,1234);
				cap2 = new SphericalCapEarth(dat1.getDouble(i, "SAMPLING_PLOT_RADIUS"),0.,0.,1234);
				dArea1 = cap1.area();
				dArea2 = cap2.area();
				dObs1 = dat1.getDouble(i-1, "SAR_OBSERVED");
				dObs2 = dat1.getDouble(i, "SAR_OBSERVED");
				dPred1 = dat1.getDouble(i-1, "SAR_PREDICTED");
				dPred2 = dat1.getDouble(i, "SAR_PREDICTED");
				
				//checking that observations and predictions are greater than 0
				if(dObs1>0 && dObs2>0 && dPred1>0 && dPred2>0){
					lstOut.add(dat1.getString(i, "STUDY_REGION_ID") + ","
							+ cap1.radius() + ","
							+ cap2.radius() + ","
							+ dArea1 + ","
							+ dArea2 + ","
							+ 0.5*(dArea1 + dArea2) + ","
							+ (log10(dObs2)-log10(dObs1))/(log10(dArea2)-log10(dArea1)) + ","
							+ (log10(dPred2)-log10(dPred1))/(log10(dArea2)-log10(dArea1)) + ","
							+ (dObs2-dObs1)/(dArea2-dArea1) + ","
							+ (dPred2-dPred1)/(dArea2-dArea1));
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
