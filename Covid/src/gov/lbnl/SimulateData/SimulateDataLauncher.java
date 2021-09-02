package gov.lbnl.SimulateData;

import java.util.ArrayList;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class SimulateDataLauncher{

	
	public static void main(String rgsArgs[]) {
		
		
		//arg1 = arguments
		//dat1 = (merged) data
		//cnd1 = conditional distribution object
		//ucd1 = unconditional distribution object
		//sds1 = simulated data server
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		ConditionalDistribution cnd1;
		UnconditionalDistribution ucd1;
		SimulateDataServer sds1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		sds1 = new SimulateDataServer(
				100000,
				arg1.getValueDouble("dIntercept"),
				arg1.getValueDouble("dSlope"),
				dat1.getDoubleMap(new String[] {"SAMPLE_ID"}, new String[] {"BETA_DIVERSITY"}),
				dat1.getDoubleMap(new String[] {"SAMPLE_ID"}, new String[] {"RESPONSE"}));
		
		lstOut = null;
		if(arg1.getValueString("sMode").equals("conditional")) {
			cnd1 = new ConditionalDistribution(sds1);
			lstOut = cnd1.print();
		}else if(arg1.getValueString("sMode").equals("unconditional")) {
			ucd1 = new UnconditionalDistribution(sds1);
			lstOut = ucd1.print();
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
	/*
	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mapCFR = map from sample ids to reciprocal case fatality rates
		//d1 = counter
		//rgdSumC = total numbers of cases
		//d2 = current case fatality rate
		//lstOut = output
		//rgdY = response variables
		//sbl1 = current predictor string
		//iResponses = number of responses
		//rgdNorm = array of normal variates
		//map1 = map from sample indices to actual cfrs (for calculating parameter values -- 1/inverse)
		
		int iResponses;
		StringBuilder sbl1;
		ArgumentIO arg1;
		double d1;
		double d2;
		DataIO dat1;
		HashMap<String,Double> mapCFRReciprocal;
		double rgdSumC[][];
		double rgdY[][]=null;
		ArrayList<String> lstOut;
		double rgdNorm[];
		ArrayListMultimap<Integer,Double> map1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		iResponses = arg1.getValueInt("iResponses");
		
		//loading case fatality rates
		mapCFRReciprocal = new HashMap<String,Double>(dat1.iCols);
		d1 = 20;
		for(int j=0;j<dat1.iCols;j++){
			mapCFRReciprocal.put(dat1.getString(0,j),d1);
			d1+=10.;
		}
		
		//loading total numbers of cases
		rgdSumC = new double[dat1.iRows-1][iResponses];
		rgdY = new double[dat1.iRows-1][iResponses];
		lstOut = new ArrayList<String>(iResponses*dat1.iRows+1);
		lstOut.add("REGION_ID,OBSERVATION_ID,CASE_FATALITY_RATE");
		map1 = ArrayListMultimap.create(dat1.iCols,iResponses);
		for(int k=0;k<iResponses;k++){
			for(int i=1;i<dat1.iRows;i++){
				rgdNorm = ExtendedMath.normalRandomVector(0.,arg1.getValueDouble("dErrorVariance"),dat1.iCols);
				for(int j=0;j<dat1.iCols;j++){
					d2 = mapCFRReciprocal.get(dat1.getString(0,j));
					
					//adding noise
					d2 += rgdNorm[j];
					while(d2<0){
						d2 = mapCFRReciprocal.get(dat1.getString(0,j));
						d2 += ExtendedMath.normalRandomVector(0.,arg1.getValueDouble("dErrorVariance"),1)[0];
								
					}
					if(k==1){
						lstOut.add(dat1.getString(0,j) + "," + i + "," + 1./d2);
					}
					map1.put(j,1./d2);
					rgdSumC[i-1][k]+=dat1.getDouble(i,j)*d2;
				}
			}
			
			//finding response variables
			for(int i=0;i<rgdSumC.length;i++){
				rgdY[i][k]=rgdSumC[i][k];
			}
		}
		
		//outputting case fatality rates
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath").replace(".csv","-cfr.csv"));		
		
		//outputting case fatality rates
		lstOut = new ArrayList<String>(dat1.iCols+1);
		lstOut.add("REGION_ID,CASE_FATALITY_RATE_MEAN");
		for(int j=0;j<dat1.iCols;j++){
			lstOut.add(dat1.getString(0,j) + "," + ExtendedMath.mean(new ArrayList<Double>(map1.get(j))));		
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath").replace(".csv","-cfr-mean.csv"));		
		
		//outputting response variables
		lstOut = new ArrayList<String>(dat1.iRows+1);
		sbl1 = new StringBuilder();
		for(int j=0;j<iResponses-1;j++){
			sbl1.append("Y" + j + ",");
		}
		lstOut.add(sbl1.toString() + "Y" + (iResponses-1));
		for(int i=0;i<rgdY.length;i++){
			if(!Double.isNaN(rgdY[i][0])){
				sbl1 = new StringBuilder();
				for(int j=0;j<iResponses-1;j++){
					sbl1.append(rgdY[i][j] + ",");
				}
				lstOut.add(sbl1.toString() + rgdY[i][iResponses-1]);
			}
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath").replace(".csv","-responses.csv"));		
		System.out.println("Done.");
	}
	*/
}