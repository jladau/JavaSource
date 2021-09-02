package edu.ucsf.base;

import java.util.ArrayList;
import static edu.ucsf.base.ExtendedMath.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for MantelTest
 * @author jladau
 */


public class MantelTestTest {

	/**Mantel test object**/
	private MantelTest mnt1;
	
	/**True probability less than or equal to observed value**/
	private double dPrLE;

	/**True probability greter than or equal to observed value**/
	private double dPrGE;
	
	/**True correlation**/
	private double dR;

	public MantelTestTest(){
		
		//rgdX = x values
		//rgdY = y values
		//tblX = table of x values
		//tblY = table of y values
		
		double[][] rgdX;
		double[][] rgdY;
		Table<String,String,Double> tblX;
		Table<String,String,Double> tblY;
		
		rgdX = new double[][]{
				{0.0000000,0.3937326,0.4088031,0.6144127,0.1854888},
				{0.3937326,0.0000000,0.3749446,0.2206810,0.5743590},
				{0.4088031,0.3749446,0.0000000,0.5116772,0.4994034},
				{0.6144127,0.2206810,0.5116772,0.0000000,0.7944601},
				{0.1854888,0.5743590,0.4994034,0.7944601,0.0000000}};
		rgdY = new double[][]{
				{0.000000,1.326612,3.172921,0.044354,1.149193},
				{1.326612,0.000000,1.846309,1.282258,0.177419},
				{3.172921,1.846309,0.000000,3.128567,2.023728},
				{0.044354,1.282258,3.128567,0.000000,1.104839},
				{1.149193,0.177419,2.023728,1.104839,0.000000}};
		rgdX = new double[][]{
				{0.0000000,0.3937326,0.4088031},
				{0.3937326,0.0000000,0.3749446},
				{0.4088031,0.3749446,0.0000000}};
		rgdY = new double[][]{
				{0.000000,1.326612,3.172921},
				{1.326612,0.000000,1.846309},
				{3.172921,1.846309,0.000000}};
		
		tblX = HashBasedTable.create();
		tblY = HashBasedTable.create();
		for(int i=0;i<rgdX.length;i++){
			for(int j=0;j<rgdX[0].length;j++){
				tblX.put(Integer.toString(i), Integer.toString(j), rgdX[i][j]);
				tblY.put(Integer.toString(i), Integer.toString(j), rgdY[i][j]);
			}
		}
		mnt1 = new MantelTest(tblX, tblY, 99999);
		loadCorrectResults(rgdX);
	}
	
	private void loadCorrectResults(double[][] rgdX){
		
		//rgdPer = permutations
		//lst1 = first array for calculating pearson
		//lst2 = second array for calculating pearson
		//d1 = current correlation
		
		double d1;
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		double[][][] rgdPer;
		
		rgdPer = new double[6][3][3];
		rgdPer[0] = new double[][]{
				{0,1.326612,3.172921},
				{1.326612,0,1.846309},
				{3.172921,1.846309,0}};
		rgdPer[1] = new double[][]{
				{0,1.326612,1.846309},
				{1.326612,0,3.172921},
				{1.846309,3.172921,0}};
		rgdPer[2] = new double[][]{
				{0,1.846309,3.172921},
				{1.846309,0,1.326612},
				{3.172921,1.326612,0}};
		rgdPer[3] = new double[][]{
				{0,3.172921,1.326612},
				{3.172921,0,1.846309},
				{1.326612,1.846309,0}};
		rgdPer[4] = new double[][]{
				{0,3.172921,1.846309},
				{3.172921,0,1.326612},
				{1.846309,1.326612,0}};
		rgdPer[5] = new double[][]{
				{0,1.846309,1.326612},
				{1.846309,0,3.172921},
				{1.326612,3.172921,0}};
	
		lst1 = new ArrayList<Double>();
		for(int i=0;i<rgdX.length;i++){
			for(int j=0;j<rgdX[0].length;j++){
				lst1.add(rgdX[i][j]);
			}
		}
		
		dPrLE=0.;
		dPrGE=0.;
		for(int k=0;k<rgdPer.length;k++){
			lst2 = new ArrayList<Double>();
			for(int i=0;i<rgdPer[k].length;i++){
				for(int j=0;j<rgdPer[k][0].length;j++){
					lst2.add(rgdPer[k][i][j]);
				}
			}
			d1=pearson(lst1,lst2);
			if(k==0){
				dR=d1;
			}
			if(d1<=dR){
				dPrLE++;
			}
			if(d1>=dR){
				dPrGE++;
			}
		}
		dPrLE/=6.;
		dPrGE/=6.;
	}
	
	
	@Test
	public void getObservedCorrelation_CorrelationGotten_CorrelationCorrect(){
		
		assertEquals(mnt1.getObservedCorrelation(),dR,0.0000001);
		//assertEquals(mnt1.getObservedCorrelation(),0.86331409429829,0.0000001);
		//assertEquals(mnt1.getObservedCorrelation(),0.31933586065346,0.0000001);
	}

	@Test
	public void getPrGreaterThanObs_PrGotten_PrCorrect(){
		assertEquals(mnt1.getPrGreaterThanObs(),dPrGE,0.1);
	}
	
	@Test
	public void getPrLessThanObs_PrGotten_PrCorrect(){
		assertEquals(mnt1.getPrLessThanObs(),dPrLE,0.1);
	}
}
