package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;
import java.util.Collections;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Ranks;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InequalitySignificanceTestLauncher1{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sX = x header
		//sY = y header
		//lstX = list of x values
		//lstY = list of y values
		//rgdXRange = range of x values
		//rgdYRange = range of y values
		//lstOut = output
		//lstStart = list of possible start points
		//lstEnd = list of possible end points
		//iSteps = number of steps
		//sSlopeSign = sign of slope
		//inqMaxSES = inequality with maximum SES
		//sDirection = direction
		//iIterations = number of iterations
		//inq1 = current shuffled inequality
		//i1 = counter
		//shf1 = response shuffler
		//iCounter = output counter
		
		String sDirection;
		Inequality0 inqMaxSES;
		Inequality0 inq1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String sX;
		String sY;
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		double rgdXRange[];
		double rgdYRange[];
		ArrayList<Double[]> lstStart;
		ArrayList<Double[]> lstEnd;
		int iSteps;
		String sSlopeSign;
		int iIterations;
		int i1;
		int iCounter;
		ResponseShuffler shf1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		lstX = dat1.getDoubleColumn(sX);
		lstY = dat1.getDoubleColumn(sY);
		sSlopeSign = arg1.getValueString("sSlopeSign");
		sDirection = arg1.getValueString("sCountingDirection");
		iIterations = arg1.getValueInt("iIterations");
		
		//loading ranges
		rgdXRange = new double[]{ExtendedMath.minimum(lstX), ExtendedMath.maximum(lstX)};
		rgdYRange = new double[]{ExtendedMath.minimum(lstY), ExtendedMath.maximum(lstY)};
		
		//updating data if negative slope
		if(sSlopeSign.equals("negative")){
			lstX = new ArrayList<Double>(dat1.iRows);
			for(int i=1;i<lstX.size();i++){
				lstX.add(rgdXRange[1] + (rgdXRange[1]-dat1.getDouble(i,sX)));
			}
			rgdXRange = new double[]{ExtendedMath.minimum(lstX), ExtendedMath.maximum(lstX)};
		}
		
		//loading possible start and end points
		//iSteps = 10;
		//dStepX = (rgdXRange[1]-rgdXRange[0])/((double) iSteps);
		//dStepY = (rgdYRange[1]-rgdYRange[0])/((double) iSteps);
		//lstStart = new ArrayList<Double[]>(3*iSteps);
		//lstEnd = new ArrayList<Double[]>(3*iSteps);
		//*************************
		
		//Just minima and maxima
		//lstStart.add(new Double[]{rgdXRange[0],rgdYRange[0]});
		//lstEnd.add(new Double[]{rgdXRange[1],rgdYRange[1]});
		
		//All points
		//for(double dY=rgdYRange[0];dY<=rgdYRange[1];dY+=dStepY){
		//	lstStart.add(new Double[]{rgdXRange[0],dY});
		//	lstEnd.add(new Double[]{rgdXRange[1],dY});
		//}
		//for(double dX=rgdXRange[0];dX<=rgdXRange[1];dX+=dStepX){
		//	lstStart.add(new Double[]{dX,rgdYRange[0]});
		//	lstEnd.add(new Double[]{dX,rgdYRange[1]});
		//}
		
		//Quantiles
		lstStart = new ArrayList<Double[]>(lstX.size());
		lstEnd = new ArrayList<Double[]>(lstY.size());
		for(double d=0;d<0.5;d+=0.1) {
			double[] rgdXQ = quantiles(d,lstX);
			double[] rgdYQ = quantiles(d,lstY);
			lstStart.add(new Double[]{rgdXQ[0],rgdYQ[0]});
			lstEnd.add(new Double[]{rgdXQ[1],rgdYQ[1]});
		}
		
		//loading shuffler
		if(arg1.containsArgument("sCategoryHeader")){
			shf1 = new ResponseShuffler(lstY,dat1.getStringColumn(arg1.getValueString("sCategoryHeader")));
		}else {
			shf1 = new ResponseShuffler(lstY,null);
		}
		//*************************
		
		//looping through pairs of start and end points and testing
		inqMaxSES = Inequality0.maximizeSES(lstX, lstY, iIterations, lstStart, lstEnd, sDirection, shf1);
		//inqMaxSES = Inequality.maximizeSESSerial(lstX, lstY, iIterations, lstStart, lstEnd, sDirection, shf1);
		
		//finding pvalue
		i1 = 0;
		iCounter = 0;
		for(int i=0;i<100;i++){
			
			iCounter++;
			System.out.println("Randomization " + i + " of 100...");
			
			//*****************************
			//inq1 = Inequality.maximizeSES(lstX, shf1.nextShuffle(), iIterations, lstStart, lstEnd, sDirection, shf1);
			Collections.shuffle(lstY);
			//inq1 = Inequality.maximizeSESSerial(lstX, lstY, iIterations, lstStart, lstEnd, sDirection, shf1);
			inq1 = Inequality0.maximizeSES(lstX, lstY, iIterations, lstStart, lstEnd, sDirection, shf1);
			//*****************************
			
			//*****************************
			//System.out.println(inqMaxSES.ses() + "," + inq1.ses());
			//*****************************
			
			if(inq1.ses()>=inqMaxSES.ses()){
				i1++;
			}
		}
		inqMaxSES.setSignificance(((double) i1)/100.);
		
		
		//initializing output
		lstOut = new ArrayList<String>(25);
		lstOut.add("STATISTIC_TYPE,STATISTIC_VALUE");
		
		//outputting results
		//TODO update x start and end output if negative slope
		lstOut.add("test_statistic," + inqMaxSES.testStatistic());
		lstOut.add("n," + lstX.size());
		lstOut.add("ses," + inqMaxSES.ses());
		lstOut.add("significance_value," + inqMaxSES.significance());
		if(sSlopeSign.equals("positive")){
			lstOut.add("x_start," + inqMaxSES.line()[0]);
		}else if(sSlopeSign.equals("negative")){
			lstOut.add("x_start," + (rgdXRange[0]-(inqMaxSES.line()[0]-rgdXRange[0])));
		}
		lstOut.add("y_start," + inqMaxSES.line()[1]);
		if(sSlopeSign.equals("positive")){
			lstOut.add("x_end," + inqMaxSES.line()[2]);
		}else if(sSlopeSign.equals("negative")){
			lstOut.add("x_end," + (rgdXRange[0]-(inqMaxSES.line()[1]-rgdXRange[0])));
		}
		lstOut.add("y_end," + inqMaxSES.line()[3]);
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double[] quantiles(double dPercentLower, ArrayList<Double> lstData){
		
		//lst1 = ranks
		//d1 = lower rank value
		//d2 = upper rank value
		//rgd1 = output; lower and upper quantiles
		
		ArrayList<Double> lst1;
		double d1;
		double d2;
		double rgd1[];
		
		lst1 = Ranks.ranksAverage(lstData);
		d1 = dPercentLower*((double) lstData.size());
		d2 = (1.-dPercentLower)*((double) lstData.size());
		
		rgd1 = new double[]{-Double.MAX_VALUE,Double.MAX_VALUE};
		for(int i=0;i<lstData.size();i++){
			if(lst1.get(i)<d1 && lstData.get(i)>rgd1[0]){
				rgd1[0] = lstData.get(i);
			}
			if(lst1.get(i)>d2 && lstData.get(i)<rgd1[1]){
				rgd1[1] = lstData.get(i);
			}
		}
		if(rgd1[0]==-Double.MAX_VALUE){
			rgd1[0]=ExtendedMath.minimum(lstData);
		}
		if(rgd1[1]==Double.MAX_VALUE){
			rgd1[1]=ExtendedMath.maximum(lstData);
		}
		return rgd1;
	}

	
	/*
	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sX = x header
		//sY = y header
		//lstX = list of x values
		//lstY = list of y values
		//rgdXRange = range of x values
		//rgdYRange = range of y values
		//lstOut = output
		//lstStart = list of possible start points
		//lstEnd = list of possible end points
		//dStepX = step size for x start and end points
		//dStepY = step size for x start and end points
		//iSteps = number of steps
		//sSlopeSign = sign of slope
		//inq1 = current inequality
		//inqMaxSES = inequality with maximum SES
		//sDirection = direction
		//iIterations = number of iterations
		//iLines = number of lines
		//iCounter = counter
		
		int iCounter;
		int iLines;
		String sDirection;
		Inequality inq1;
		Inequality inqMaxSES;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String sX;
		String sY;
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		double rgdXRange[];
		double rgdYRange[];
		ArrayList<Double[]> lstStart;
		ArrayList<Double[]> lstEnd;
		double dStepX;
		double dStepY;
		int iSteps;
		String sSlopeSign;
		int iIterations;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		lstX = dat1.getDoubleColumn(sX);
		lstY = dat1.getDoubleColumn(sY);
		sSlopeSign = arg1.getValueString("sSlopeSign");
		sDirection = arg1.getValueString("sCountingDirection");
		iIterations = arg1.getValueInt("iIterations");
		
		//*******************
		//Collections.shuffle(lstX);
		//*******************
		
		//loading ranges
		rgdXRange = quantiles(0,lstX);
		rgdYRange = quantiles(0,lstY);
		
		//loading possible start and end points
		iSteps = 15;
		dStepX = (rgdXRange[1]-rgdXRange[0])/((double) iSteps);
		dStepY = (rgdYRange[1]-rgdYRange[0])/((double) iSteps);
		lstStart = new ArrayList<Double[]>(3*iSteps);
		lstEnd = new ArrayList<Double[]>(3*iSteps);
		for(double dY=rgdYRange[0];dY<=rgdYRange[1];dY+=dStepY){
			lstStart.add(new Double[]{rgdXRange[0],dY});
			lstEnd.add(new Double[]{rgdXRange[1],dY});
		}
		for(double dX=rgdXRange[0];dX<=rgdXRange[1];dX+=dStepX){
			if(sSlopeSign.equals("positive")){
				lstStart.add(new Double[]{dX,rgdYRange[0]});
				lstEnd.add(new Double[]{dX,rgdYRange[1]});
			}else if(sSlopeSign.equals("negative")){
				lstStart.add(new Double[]{dX,rgdYRange[1]});
				lstEnd.add(new Double[]{dX,rgdYRange[0]});
			}
		}
		
		//looping through pairs of start and end points and testing
		inqMaxSES = null;
		iLines = lstStart.size()*lstEnd.size();
		iCounter = 0;
		for(Double[] rgdStart:lstStart){
			for(Double[] rgdEnd:lstEnd){
				iCounter++;
				if(iCounter % 100 == 0){	
					System.out.println("Analyzing line " + iCounter + " of " + iLines + "...");
				}
				if((sSlopeSign.equals("positive") && rgdStart[1]<rgdEnd[1]) || (sSlopeSign.equals("negative") && rgdStart[1]>rgdEnd[1])){
					inq1 = new Inequality(rgdStart[0],rgdStart[1],rgdEnd[0],rgdEnd[1],sDirection);
					inq1.nullDistribution(lstX,lstY,iIterations);
					if(!Double.isNaN(inq1.ses())){
						if(inqMaxSES==null || inqMaxSES.ses()<inq1.ses()){
							inqMaxSES = new Inequality(inq1);
						}
					}
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>(25);
		lstOut.add("STATISTIC_TYPE,STATISTIC_VALUE");
		
		//outputting results
		lstOut.add("test_statistic," + inqMaxSES.testStatistic());
		lstOut.add("n," + lstX.size());
		lstOut.add("ses," + inqMaxSES.ses());
		lstOut.add("significance_value," + inqMaxSES.significance());
		lstOut.add("x_start," + inqMaxSES.line()[0]);
		lstOut.add("y_start," + inqMaxSES.line()[1]);
		lstOut.add("x_end," + inqMaxSES.line()[2]);
		lstOut.add("y_end," + inqMaxSES.line()[3]);
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	*/
}
