package edu.ucsf.InequalitySignificanceTest;

import java.util.ArrayList;
import java.util.Collections;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Ranks;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InequalitySignificanceTestLauncher0{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//sX = x header
		//sY = y header
		//lstX = list of x values
		//lstY = list of y values
		//rgdXQuantiles = x value quantiles
		//rgdYQuantiles = y value quantiles
		//i1 = current count
		//lstOut = output
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		String sX;
		String sY;
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		double rgdXQuantiles[];
		double rgdYQuantiles[];
		int i1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		lstX = dat1.getDoubleColumn(sX);
		lstY = dat1.getDoubleColumn(sY);
	
		//loading quantiles
		//TODO make the quantiles a varying quantity between 0 and 0.5
		rgdXQuantiles = quantiles(0.15,lstX);
		rgdYQuantiles = quantiles(0,lstY);
		
		//initializing output
		lstOut = new ArrayList<String>(arg1.getValueInt("iIterations") + 2);
		lstOut.add("STATISTIC_TYPE, STATISTIC_VALUE");
		
		//finding observed count
		i1 = countPoints(lstX, lstY, rgdXQuantiles, rgdYQuantiles, arg1.getValueString("sDiagonalType"), arg1.getValueString("sDirection"));
		lstOut.add("observed," + i1);
		
		//finding randomized counts
		for(int i=0;i<arg1.getValueInt("iIterations");i++){
			Collections.shuffle(lstX);
			i1 = countPoints(lstX, lstY, rgdXQuantiles, rgdYQuantiles, arg1.getValueString("sDiagonalType"), arg1.getValueString("sDirection"));
			lstOut.add("randomized," + i1);
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static int countPoints(
			ArrayList<Double> lstX, 
			ArrayList<Double> lstY, 
			double rgdXQuantiles[], 
			double rgdYQuantiles[], 
			String sDiagonalType,
			String sDirection){
		
		//i1 = output
		//dX0 = x start
		//dY0 = y start
		//dX1 = x end
		//dY1 = y end
		//bAbove = indicator for whether current point is above the line
		
		int i1;
		double dX0 = Double.NaN;
		double dY0 = Double.NaN;
		double dX1 = Double.NaN;
		double dY1 = Double.NaN;
		boolean bAbove;
		
		if(sDiagonalType.equals("ul-lr")){
			dX0 = rgdXQuantiles[0];
			dY0 = rgdYQuantiles[1];
			dX1 = rgdXQuantiles[1];
			dY1 = rgdYQuantiles[0];
		}else if(sDiagonalType.equals("ll-ur")){
			dX0 = rgdXQuantiles[0];
			dY0 = rgdYQuantiles[0];
			dX1 = rgdXQuantiles[1];
			dY1 = rgdYQuantiles[1];
		}
		i1 = 0;
		
		for(int i=0;i<lstX.size();i++){
			bAbove = isAbove(sDiagonalType, dX0, dY0, dX1, dY1, lstX.get(i), lstY.get(i));	
			if(bAbove==true && sDirection.equals("above")){
				i1++;
			}else if(bAbove==false && sDirection.equals("below")){
				i1++;
			}
		}
		return i1;
	}
	
	private static boolean isAbove(String sDiagonalType, double dX0, double dY0, double dX1, double dY1, double dXTest, double dYTest){
		
		//bLeft = boolean for whether point is to the left of the line
		
		boolean bLeft;
		
		if((dX1-dX0)*(dYTest-dY0)-(dY1-dY0)*(dXTest-dX0)>0){
			bLeft = true;
		}else{
			bLeft = false;
		}
		
		if(sDiagonalType.equals("ul-lr")){
			if(bLeft==true){
				return false;
			}else{
				return true;
			}
		}else if(sDiagonalType.equals("ll-ur")){
			if(bLeft==true){
				return true;
			}else {
				return false;
			}
		}
		return false;
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
}
