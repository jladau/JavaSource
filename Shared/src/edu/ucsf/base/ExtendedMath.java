package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import static java.lang.Math.*;

/**
 * Extended math functions. Wrapper for Apache functions.
 * @author jladau
 */

public class ExtendedMath {

	//TODO write unit test
	
	/**
	 * Performs numerical integration
	 * @param lstX List of x-values for function, in ascending order
	 * @param lstY List of y-values for function
	 * @param dMinX Lower bound for integration
	 * @param dMaxX Upper bound for integration
	 * @return Value of integral computed using approximating trapezoids
	 */
	public static double integrateNumerically(Function fcnF, double dMinX, double dMaxX) throws Exception{
		
		//d3 = area
		//lstX1 = values of domain for integrating
		//lstY1 = values of function for integrating
		//i1 = number of values to integrate over
		//dStep = step size
		//dX = current x value
		
		double dX;
		double d3;
		double dStep;
		int i1;
		ArrayList<Double> lstX1;
		ArrayList<Double> lstY1;
		
		i1 = 100;
		dStep = (dMaxX-dMinX)/((double) i1);
		if(!fcnF.inDomain(dMinX)){
			throw new Exception("Lower bound of integration outside of domain");
		}
		if(!fcnF.inDomain(dMaxX)){
			throw new Exception("Upper bound of integration outside of domain");
		}
		
		//initializing arrays for finding area
		lstX1 = new ArrayList<Double>(i1+2);
		lstY1 = new ArrayList<Double>(i1+2);
		dX = dMinX;
		do{
			lstX1.add(dX);
			lstY1.add(fcnF.image(dX));
			dX+=dStep;
			
		}while(dX<dMaxX);
		lstX1.add(dMaxX);
		lstY1.add(fcnF.image(dMaxX));
		
		//finding area
		d3 = 0.;
		for(int i=0;i<lstX1.size()-1;i++){
			d3+=0.5*(lstY1.get(i)+lstY1.get(i+1))*(lstX1.get(i+1)-lstX1.get(i));
		}
		return d3;
	}
	
	/**
	 * Computes unit normal probability density function
	 * @param dX Value at which PDF is to be computed
	 * @return Value of normal PDF
	 */
	public static double unitNormalPDF(double dX){
		return 1./(sqrt(2.*PI))*exp(-dX*dX/2.);
	}
	
	/**
	 * Computes normal probability density function
	 * @param dX Value at which PDF is to be computed
	 * @param dMu Mean of distribution
	 * @param dSigma2 Variance of distribution
	 * @return Value of normal PDF
	 */
	public static double normalPDF(double dX, double dMu, double dSigma2){
		return 1./(sqrt(2.*PI*dSigma2))*exp(-(dX-dMu)*(dX-dMu)/(2.*dSigma2));
	}
	
	/**
	 * Performs linear interpolation
	 * @param dX Value at which interpolation is to be performed
	 * @param dX0 Start x value
	 * @param dY0 Start y value
	 * @param dX1 End x value
	 * @param dY1 End y value
	 * @return Interpolated value
	 */
	public static double linearInterpolation(double dX, double dX0, double dY0, double dX1, double dY1) throws Exception{
		if(dX==dX0){
			return dY0;
		}
		if(dX==dX1){
			return dY1;
		}
		if(dX<dX0 || dX>dX1){
			throw new Exception("Interpolation bounds not valid.");
		}
		return dY0*(1.-(dX-dX0)/(dX1-dX0))+dY1*(dX-dX0)/(dX1-dX0);
	}
	
	/**
	 * Generates an identity matrix
	 * @param iSize Number of rows/columns
	 */
	public static double[][] matrixIdentity(int iSize){
		
		//rgd1 = output
		
		double rgd1[][];
		
		rgd1 = new double[iSize][iSize];
		for(int i=0;i<iSize;i++){
			rgd1[i][i]=1;
		}
		return rgd1;
	}
	
	//TODO write unit test
	/**
	 * Checks if angle C is obtuse
	 * @param da Length of side a
	 * @param db Length of side b
	 * @param dc Length of side c
	 * @return True if angle is obtuse, false otherwise
	 */
	public static boolean isAngleObtuse(double da, double db, double dc){
		
		//d1 = da^2 + db^2
		//dc2 = dc^2
		
		
		double dc2;
		double d1;
		
		d1 = da*da+db*db;
		dc2 = dc*dc;
		if(dc2*0.5<d1){
			if(d1 < dc2){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calculates the difference between two matrices
	 * @param rgd1 First matrix
	 * @param rgd2 Second matrix
	 * @return Difference between matrices.
	 * @throws Exception 
	 */
	public static double[][] matrixDifference(double rgd1[][], double rgd2[][]) throws Exception{
		
		//rgdOut = output
		
		double[][] rgdOut;
		
		if(rgd1.length!=rgd2.length || rgd1[0].length!=rgd2[0].length){
			throw new Exception("Matrices of different sizes.");
		}
		
		rgdOut = new double[rgd1.length][rgd1[0].length];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				rgdOut[i][j]=rgd1[i][j]-rgd2[i][j];
			}
		}
		return rgdOut;
	}
	
	/**
	 * Calculates the scalar product for a matrix
	 * @param rgd1 First matrix
	 * @param dScalar NUmber by which to multiply
	 */
	public static double[][] matrixScalarProduct(double rgd1[][], double dScalar){
		
		//rgdOut = output
		
		double[][] rgdOut;
		
		rgdOut = new double[rgd1.length][rgd1[0].length];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				rgdOut[i][j]=rgd1[i][j]*dScalar;
			}
		}
		return rgdOut;
	}
	
	/**
	 * Converts a Double array to a double array
	 * @param rgd1 Array to be converted
	 */
	public static double[][] toPrimitive(Double rgd1[][]){
		
		//rgd2 = output
		
		double rgd2[][];
		
		rgd2 = new double[rgd1.length][rgd1[0].length];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				rgd2[i][j]=rgd1[i][j];
			}
		}
		return rgd2;
	}
	
	//TODO write unit test
	/**
	 * Generates a vector with random integers
	 * @param iVectorLength Vector length
	 * @param iMax Maximum value (open interval -- values are between 0 and iMax-1)
	 * @return Random vector
	 */
	public static int[] randomIntegerVector(int iVectorLength, int iMax){
		
		//rgi1 = output
		//d1 = iMax in double form
		
		int rgi1[];
		double d1;
		
		rgi1 = new int[iVectorLength];
		d1 = (double) iMax;
		for (int i=0;i<iVectorLength;i++){
			rgi1[i] = (int) Math.floor(d1*Math.random());
		}
		return rgi1;
	}
	
	
	/**
	 * Generates a normal random vector of iid variates
	 * @param dMean Mean
	 * @param dVariance Variance
	 * @return Random vector
	 */
	public static double[] normalRandomVector(double dMean, double dVariance, int iLength){
		return normalRandomVector(dMean, dVariance, iLength, 1234);
	}
	
	/**
	 * Generates a normal random vector of iid variates
	 * @param dMean Mean
	 * @param dVariance Variance
	 * @return Random vector
	 */
	public static double[] normalRandomVector(double dMean, double dVariance, int iLength, int iRandomSeed){
		
		//rgd1 = output
		//gen1 = random number generator
		//d1 = square root of variance
		
		double rgd1[];
		RandomGenerator ran1;
		double d1;
		
		d1 = Math.sqrt(dVariance);
		rgd1 = new double[iLength];
		ran1 = new JDKRandomGenerator();
		ran1.setSeed(iRandomSeed);
		for(int i=0;i<iLength;i++){
			rgd1[i]=ran1.nextGaussian()*d1+dMean;
		}
		return rgd1;
	}
	
	/**
	 * Inverts a matrix
	 * @param rgd1 Matrix to be inverted.
	 */
	public static double[][] matrixInverse(double rgd1[][]){
		return MatrixUtils.inverse(new Array2DRowRealMatrix(rgd1)).getData();
	}
	
	/**
	 * Multiplies two matrices
	 * @param rgd1 First matrix
	 * @param rgd2 Second matrix
	 * @return Product of matrices. Error thrown if matrix dimensions don't match.
	 */
	public static double[][] matrixProduct(double rgd1[][], double rgd2[][]) throws Exception{
		
		//rgd3 = output
		
		double rgd3[][];
		
		if(rgd1[0].length!=rgd2.length){
			throw new Exception("Matrix dimensions do not match.");
		}
		rgd3 = new double[rgd1.length][rgd2[0].length];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd2[0].length;j++){
				for(int k=0;k<rgd1[0].length;k++){
					rgd3[i][j]+=rgd1[i][k]*rgd2[k][j];
				}
			}
		}
		return rgd3;		
	}
	
	/**
	 * Multiplies two matrices, the second of which is a vector
	 * @param rgd1 First matrix
	 * @param rgd2 Second matrix
	 * @return Product of matrices. Error thrown if matrix dimensions don't match.
	 */
	public static double[] matrixProduct(double rgd1[][], double rgd2[]) throws Exception{
		
		//rgd3 = output
		
		double rgd3[];
		
		if(rgd1[0].length!=rgd2.length){
			throw new Exception("Matrix dimensions do not match.");
		}
		rgd3 = new double[rgd1.length];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd2.length;j++){
				rgd3[i]+=rgd1[i][j]*rgd2[j];
			}
		}
		return rgd3;		
	}
	
	/**
	 * Converts ArrayList of doubles to array of doubles. 
	 * @param lst1 ArrayList to be converted.
	 * @return Array of values.
	 */
	private static double[] arrayListToDoubleArray(ArrayList<Double> lst1){
		
		//rgd1 = output
		
		double rgd1[];
		
		rgd1 = new double[lst1.size()];
		for(int i=0;i<rgd1.length;i++){
			rgd1[i]=lst1.get(i);
		}
		return rgd1;
	}
	
	/**
	 * Calculates intercept.
	 * @param rgdX Vector of independent variables.
	 * @param rgdY Vector of dependent variables
	 * @return Intercept of linear regression.
	 */
	public static double intercept(double rgdX[], double rgdY[]){
		
		//srg1 = simple regression object
		
		SimpleRegression srg1;
		
		srg1 = new SimpleRegression();
		for(int i=0;i<rgdX.length;i++){
			srg1.addData(rgdX[i],rgdY[i]);
		}
		return srg1.getIntercept();
	}
	
	/**
	 * Calculates slope.
	 * @param rgdX Vector of independent variables.
	 * @param rgdY Vector of dependent variables
	 * @return Slope of linear regression.
	 */
	public static double slope(double rgdX[], double rgdY[]){
		
		//srg1 = simple regression object
		
		SimpleRegression srg1;
		
		srg1 = new SimpleRegression();
		for(int i=0;i<rgdX.length;i++){
			srg1.addData(rgdX[i],rgdY[i]);
		}
		return srg1.getSlope();
	}
	
	/**
	 * Calculates Pearson correlation.
	 * @param rgdX Vector of independent variables.
	 * @param rgdY Vector of dependent variables
	 * @return Pearson correlation.
	 */
	public static double pearson(double rgdX[], double rgdY[]){	
		return (new PearsonsCorrelation()).correlation(rgdX, rgdY);
	}
	
	
	//TODO write unit test
	/**
	 * Calculates Spearman correlation.
	 * @param rgdX Vector of independent variables.
	 * @param rgdY Vector of dependent variables
	 * @return Spearman correlation.
	 */
	public static double spearman(double rgdX[], double rgdY[]){
		return (new SpearmansCorrelation()).correlation(rgdX, rgdY);
	}
	
	//TODO write unit test
	/**
	 * Calculates Spearman correlation.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Spearman correlation.
	 */
	public static double spearman(ArrayList<Double> lstX, ArrayList<Double> lstY){
		return spearman(arrayListToDoubleArray(lstX),arrayListToDoubleArray(lstY));
	}
	
	
	//TODO write unit test
	/**
	 * Calculates R^2 (1-ESS/TSS)
	 * @param lstObserved Vector of observed values
	 * @param lstPredicted Vector of predicted variables
	 * @return R^2
	 */
	public static double coefficientOfDetermination(ArrayList<Double> lstObserved, ArrayList<Double> lstPredicted){
		
		//dESS = error sum of squares
		//dTSS = total sum of squares
		//d1 = mean value
		//dPred = current predicted value
		//dObs = current observed value
		
		double dPred;
		double dObs;
		double d1;
		double dESS=0.;
		double dTSS=0.;
		
		d1 = ExtendedMath.mean(lstObserved);
		for(int i=0;i<lstObserved.size();i++){
			dObs = lstObserved.get(i);
			dPred = lstPredicted.get(i);
			dTSS+=(dObs-d1)*(dObs-d1);
			dESS+=(dPred-dObs)*(dPred-dObs);
		}
		return Math.max(0.,1.-dESS/dTSS);
	}
	
	/**
	 * Calculates Pearson correlation.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Pearson correlation.
	  */
	public static double pearson(ArrayList<Double> lstX, ArrayList<Double> lstY){
		return pearson(arrayListToDoubleArray(lstX),arrayListToDoubleArray(lstY));
	}
	
	/**
	 * Calculates intercept.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Intercept of linear regression.
	 */
	public static double intercept(ArrayList<Double> lstX, ArrayList<Double> lstY){
		
		return intercept(arrayListToDoubleArray(lstX),arrayListToDoubleArray(lstY));
	}
	
	/**
	 * Calculates slope.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Slope of linear regression.
	 */
	public static double slope(ArrayList<Double> lstX, ArrayList<Double> lstY){
		
		return slope(arrayListToDoubleArray(lstX),arrayListToDoubleArray(lstY));
	}
	
	//TODO write unit test
	/**
	 * Calculates bootstrap confidence interval.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Array: LB and UB for 95% confidence interval.
	 */
	public static double[] slopeBootstrapCI(ArrayList<Double> lstX, ArrayList<Double> lstY){
	
		//rgd1 = output
		//i1 = number of iterations
		//rgi1 = current random vector
		//rgdX1 = current bootstrap
		//rgdY1 = current bootstrap
		//rgd2 = list of bootstrap slope values
		//rgi1 = current resample
		
		double rgd1[];
		double rgd2[];
		int i1;
		double rgdX1[];
		double rgdY1[];
		int rgi1[];
		
		rgd1 = new double[2];
		i1 = 1000;
		rgd2 = new double[i1];
		for(int i=0;i<i1;i++){
			rgi1 = randomIntegerVector(lstX.size(), lstX.size());
			rgdX1 = new double[lstX.size()];
			rgdY1 = new double[lstY.size()];			
			for(int k=0;k<rgi1.length;k++){
				rgdX1[k] = lstX.get(rgi1[k]);
				rgdY1[k] = lstY.get(rgi1[k]);
			}
			rgd2[i] = slope(rgdX1,rgdY1);	
		}
		Arrays.sort(rgd2);
		rgd1[0] = rgd2[(int) (0.025*i1)];
		rgd1[1] = rgd2[(int) (0.975*i1)];
		return rgd1;
	}
		
	
	//TODO Write unit test
	/**
	 * Calculates significance values for slope.
	 * @param lstX Vector of independent variables.
	 * @param lstY Vector of dependent variables
	 * @return Array: P(slope<observed slope), P(observed slope<slope).
	 */
	public static double[] slopeSignificance(ArrayList<Double> lstX, ArrayList<Double> lstY){
		
		//rgd1 = output
		//i1 = number of iterations
		//d0 = actual slope
		//d1 = current permutation slope
		//lstX1 = permuted x values
		
		int i1;
		double rgd1[];
		double d0;
		double d1;
		ArrayList<Double> lstX1;
		
		
		rgd1 = new double[2];
		d0 = slope(lstX,lstY);
		lstX1 = new ArrayList<Double>(lstX.size());
		for(int i=0;i<lstX.size();i++){
			lstX1.add(lstX.get(i));
		}
		i1 = 1000;
		for(int i=0;i<i1;i++){
			Collections.shuffle(lstX1);
			d1 = slope(lstX1,lstY);
			if(d1<d0){
				rgd1[0]++;
			}
			if(d0<d1){
				rgd1[1]++;
			}
		}
		rgd1[0]=rgd1[0]/((double) i1);
		rgd1[1]=rgd1[1]/((double) i1);
		return rgd1;
	}
	
	/**
	 * Calculates sum of values raised to specified power.
	 * @param rgd1 Array of double values.
	 * @return Sum.
	 */
	public static double sumOfPowers(double rgd1[], double dPower){
		
		//rgd2 = values raised to specified power
		
		double rgd2[];
		
		rgd2 = new double[rgd1.length];
		for(int i=0;i<rgd1.length;i++){
			rgd2[i]=pow(rgd1[i], dPower);
		}
		return sum(rgd2);
	}
	
	/**
	 * Calculates sum of values raised to specified power.
	 * @param rgd1 ArrayList of double values.
	 * @return Sum.
	 */
	public static double sumOfPowers(ArrayList<Double> lst1, double dPower){
		
		//rgd2 = values raised to specified power
		
		double rgd2[];
		
		rgd2 = new double[lst1.size()];
		for(int i=0;i<lst1.size();i++){
			rgd2[i]=pow(lst1.get(i), dPower);
		}
		return sum(rgd2);
	}
	
	/**
	 * Calculates sum of values centered by the mean raised to specified power.
	 * @param rgd1 ArrayList of double values.
	 * @return Sum.
	 */
	public static double sumOfPowersMeanCentered(ArrayList<Double> lst1, double dPower){
		
		//rgd2 = values raised to specified power
		//dMean = mean value
		
		double rgd2[];
		double dMean;
		
		dMean = mean(lst1);
		rgd2 = new double[lst1.size()];
		for(int i=0;i<lst1.size();i++){
			rgd2[i]=pow(lst1.get(i)-dMean, dPower);
		}
		return sum(rgd2);
	}
	
	/**
	 * Calculates sum of values centered by the mean raised to specified power.
	 * @param rgd1 Array of double values.
	 * @return Sum.
	 */
	public static double sumOfPowersMeanCentered(double rgd1[], double dPower){
		
		//rgd2 = values raised to specified power
		//dMean = mean value
		
		double dMean;
		double rgd2[];
		
		dMean = mean(rgd1);
		rgd2 = new double[rgd1.length];
		for(int i=0;i<rgd1.length;i++){
			rgd2[i]=pow(rgd1[i]-dMean, dPower);
		}
		return sum(rgd2);
	}
	
	/**
	 * Calculates arithmetic mean.
	 * @param rgd1 Array of double values.
	 * @return Arithmetic mean.
	 */
	public static double mean(double rgd1[]){
		return (new Mean()).evaluate(rgd1);
	}
	
	/**
	 * Calculates arithmetic mean.
	 * @param rgd1 ArrayList of double values.
	 * @return Arithmetic mean.
	 */
	public static double mean(ArrayList<Double> lst1){
		return (new Mean()).evaluate(arrayListToDoubleArray(lst1));
	}
	
	public static double minimum(ArrayList<Double> lst1){
		
		//d1 = output
		
		double d1;
		
		d1 = Double.MAX_VALUE;
		for(Double d:lst1){
			if(d<d1){
				d1 = d;
			}
		}
		return d1;
	}

	public static double maximum(ArrayList<Double> lst1){
		
		//d1 = output
		
		double d1;
		
		d1 = -Double.MAX_VALUE;
		for(Double d:lst1){
			if(d>d1){
				d1 = d;
			}
		}
		return d1;
	}
	
	/**
	 * Calculates sum.
	 * @param rgd1 Array of double values.
	 * @return Sum.
	 */
	public static double sum(double rgd1[]){
		return (new Sum()).evaluate(rgd1);
	}

	/**
	 * Calculates sum.
	 * @param rgd1 ArrayList of double values.
	 * @return Sum.
	 */
	public static double sum(ArrayList<Double> lst1){
		return (new Sum()).evaluate(arrayListToDoubleArray(lst1));
	}
	
	public static double standardDeviation(ArrayList<Double> lst1) {
		
		//dN = n
	
		double dN;
		
		dN = (double) lst1.size();
		return Math.sqrt(dN/(dN-1))*standardDeviationP(lst1);
	}
	
	
	public static double standardizedEffectSize(double dObs, double dSxNull, double dSx2Null, double dNNull){
		
		//dMean = null mean
		//dStDev = null standard deviation
		
		double dMean;
		double dStDev;
		
		dMean = dSxNull/dNNull;
		dStDev = Math.sqrt(dNNull/(dNNull-1.)*(dSx2Null/dNNull - dMean*dMean));
		return (dObs - dMean)/dStDev;
	}
	
	/**
	 * Calculates population standard deviation.
	 * @param rgd1 Array of double values.
	 * @return Population standard deviation.
	 */
	public static double standardDeviationP(double rgd1[]){
		return sqrt((new Variance(false)).evaluate(rgd1));
	}
	
	/**
	 * Calculates population standard deviation.
	 * @param rgd1 ArrayList of double values.
	 * @return Population standard deviation.
	 */
	public static double standardDeviationP(ArrayList<Double> lst1){
		return sqrt((new Variance(false)).evaluate(arrayListToDoubleArray(lst1)));
	}
}
