package edu.ucsf.MulitpleQuantileRegression;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import edu.ucsf.MulitpleQuantileRegression.MultipleQuantileRegressionData.Neighborhood;

public class MultipleQuantileRegression{

	/**Map from neighborhood index to observed response values**/
	private HashMap<Integer,Double> mapObserved;
	
	/**Map from neighborhood index to predicted response values (left out)**/
	private HashMap<Integer,Double> mapPredicted;
	
	/**Multiple regression object**/
	private OLSMultipleLinearRegression ols1;
	
	/**Data object**/
	private MultipleQuantileRegressionData mqd1;
	
	/**Observed mean**/
	private double dObservedMean;
	
	/**Coefficient estimates**/
	private HashMap<String,Double> mapCoefficients;
	
	//**********************
	private boolean containsZero(double[] rgd1) {
		for(int i=0;i<rgd1.length;i++) {
			if(rgd1[i]==0) {
				return true;
			}
		}
		return false;
	}
	//**********************
	
	
	public MultipleQuantileRegression(MultipleQuantileRegressionData mqd1, ArrayList<String> lstPredictors){
		
		//i1 = counter
		
		int i1;
		
		this.mqd1 = mqd1;
		mapObserved = new HashMap<Integer,Double>(this.mqd1.neighborhoods().size());
		mapPredicted = new HashMap<Integer,Double>(this.mqd1.neighborhoods().size());
		ols1 = new OLSMultipleLinearRegression();
		dObservedMean = 0;
		i1 = 0;
		for(Neighborhood ngb1:this.mqd1.neighborhoods()) {
			this.mqd1.loadData(ngb1, lstPredictors);
			
			
			//***********************
			/*
			ArrayList<Integer> lstNonzeroRows = new ArrayList<Integer>(1000);
			double[][] rgd0;
			rgd0 = this.mqd1.x();
			for(int i=0;i<rgd0.length;i++) {
				if(!containsZero(rgd0[i])) {
					lstNonzeroRows.add(i);
				}
			}
			double[][] rgdXNew = new double[lstNonzeroRows.size()][rgd0[0].length];
			double[] rgdYNew = new double[lstNonzeroRows.size()];
			int i2=0;
			for(Integer i:lstNonzeroRows) {
				for(int j=0;j<rgd0[0].length;j++) {
					rgdXNew[i2][j]=Math.log(rgd0[i][j]);
					System.out.print(rgdXNew[i2][j] + ",");
				}
				rgdYNew[i2]=this.mqd1.y()[i];
				System.out.println(rgdYNew[i2]);
				i2++;
			}
			
			ols1.newSampleData(rgdYNew,rgdXNew);
			*/
			ols1.newSampleData(this.mqd1.y(),this.mqd1.x());
			//***********************
			
			mapObserved.put(i1,this.mqd1.yOmitted()[0]);
			dObservedMean+=this.mqd1.yOmitted()[0];
			mapPredicted.put(i1,predictedValue());
			i1++;
		}
		dObservedMean = dObservedMean/((double) this.mqd1.neighborhoods().size());
		this.mqd1.loadData(null, lstPredictors);
		ols1.newSampleData(this.mqd1.y(),this.mqd1.x());
		loadCoefficients();
		
		//*********************************
		//System.out.println("");
		//mqd1.print();
		//System.out.println("");
		//*********************************
	
	}
	
	public HashMap<String,Double> coefficients() {
		return mapCoefficients;
	}
	
	private void loadCoefficients() {
		
		//rgd1 = regression parameter estimates
		
		double rgd1[];
		
		rgd1 = ols1.estimateRegressionParameters();
		mapCoefficients = new HashMap<String,Double>(rgd1.length + 1);
		mapCoefficients.put("(INTERCEPT)",rgd1[0]);
		for(int i=1;i<rgd1.length;i++) {
			mapCoefficients.put(mqd1.predictor(i-1), rgd1[i]);
		}	
	}
	
	public ArrayList<String> predictors(){
		return mqd1.predictors();
	}
	
	private double predictedValue() {
		
		//rgd1 = regression parameter estimates
		//d1 = prediction
		
		double rgd1[];
		double d1;
		
		rgd1 = ols1.estimateRegressionParameters();
		d1 = rgd1[0];
		for(int i=1;i<rgd1.length;i++) {
			d1+=rgd1[i]*mqd1.xOmitted()[0][i-1];
		}
		return d1;
	}
	
	public double crossValidationR2() {
		
		//dSSResidual = residual sum of squares
		//dSSTotal = total sum of squares
		//dObs = current predicted value
		//dPred = current predicted value
		
		double dSSResidual;
		double dSSTotal;
		double dObs;
		double dPred;
		
		dSSResidual = 0.;
		dSSTotal = 0.;
		
		for(Integer i:mapObserved.keySet()){
			dObs = mapObserved.get(i);
			dPred = mapPredicted.get(i);
			dSSResidual+=(dObs-dPred)*(dObs-dPred);
			dSSTotal+=(dObs-dObservedMean)*(dObs-dObservedMean);
		}
		return 1.-dSSResidual/dSSTotal;
	}
}
