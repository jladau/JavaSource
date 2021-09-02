package edu.ucsf.base;

import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.ucsf.base.ExtendedMath;

/**
 * Extended math functions. Wrapper for Apache functions.
 * @author jladau
 */

public class ExtendedMathTest {

	/**Array of values.**/
	private double rgd1[];
	
	/**Array of values.**/
	private double rgd2[];
	
	/**ArrayList of values.**/
	private ArrayList<Double> lst1;
	
	/**ArrayList of values.**/
	private ArrayList<Double> lst2;
	
	
	public ExtendedMathTest(){
		initialize();
	}
	
	private void initialize(){
		lst1 = new ArrayList<Double>();
		lst1.add(4.5);
		lst1.add(0.);
		lst1.add(79.34);
		lst1.add(19.);
		rgd1 = new double[lst1.size()];
		for(int i=0;i<lst1.size();i++){
			rgd1[i]=lst1.get(i);
		}
		
		lst2 = new ArrayList<Double>();
		lst2.add(11.);
		lst2.add(13.);
		lst2.add(99.);
		lst2.add(-20.13);
		rgd2 = new double[lst2.size()];
		for(int i=0;i<lst2.size();i++){
			rgd2[i]=lst2.get(i);
		}
	}
	
	@Test
	public void toPrimitive_ArrayConverted_ConvertedCorrectly(){
		
		Double rgd1[][];
		double rgd2[][];
		
		rgd1 = new Double[10][10];
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				rgd1[i][j]=Math.random();
			}
		}
		rgd2 = ExtendedMath.toPrimitive(rgd1);
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				assertEquals(rgd1[i][j],rgd2[i][j],0.000000001);
			}
		}
	}
	
	@Test
	public void matrixIdentity_IdentityFound_Correct(){
		
		//rgd2 = identity
		//rgd3 = correct identity
		
		double rgd2[][];
		double rgd3[][];
		
		rgd2 = ExtendedMath.matrixIdentity(3);
		rgd3 = new double[][]{
				{1,0,0},
				{0,1,0},
				{0,0,1}};
		for(int i=0;i<rgd2.length;i++){
			for(int j=0;j<rgd2[0].length;j++){
				assertEquals(rgd2[i][j],rgd3[i][j],0.000000001);
			}
		}
	}
	
	
	@Test
	public void matrixScalarProduct_ProductFound_ProductCorrect(){
		
		//rgd1 = matrix
		//rgd2 = product
		//rgd3 = correct product
		
		double rgd1[][];
		double rgd2[][];
		double rgd3[][];
		
		rgd1 = new double[][]{
				{1,2,3},
				{0,4,5},
				{1,0,6}};
		rgd2 = ExtendedMath.matrixScalarProduct(rgd1,2.);
		rgd3 = new double[][]{
				{2,4,6},
				{0,8,10},
				{2,0,12}};
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				assertEquals(rgd2[i][j],rgd3[i][j],0.000000001);
			}
		}
	}
	
	@Test
	public void matrixInverse_MatrixInverted_InversionCorrect(){
		
		//rgd1 = matrix to inverted
		//rgd2 = inverted matrix
		//rgd3 = correct inverted matrix
		
		double rgd1[][];
		double rgd2[][];
		double rgd3[][];
		
		rgd1 = new double[][]{
				{1,2,3},
				{0,4,5},
				{1,0,6}};
		rgd2 = ExtendedMath.matrixInverse(rgd1);
		rgd3 = new double[][]{
				{1.09090909090909,-0.54545454545455,-0.09090909090909},
				{0.22727272727273,0.13636363636364,-0.22727272727273},
				{-0.18181818181818,0.09090909090909,0.18181818181818}};
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				assertEquals(rgd2[i][j],rgd3[i][j],0.000000001);
			}
		}
	}
	
	@Test
	public void normalRandomVector_VectorObtained_VectorCorrect(){
		
		//rgd1 = random vector
		
		double rgd1[];
		rgd1 = ExtendedMath.normalRandomVector(17, 23, 100000);
		assertEquals(ExtendedMath.mean(rgd1),17,0.1);
		assertEquals(ExtendedMath.standardDeviationP(rgd1),Math.sqrt(23),1);
	}
	
	@Test
	public void matrixProduct_MatrixAndVectorAreMultiplied_AnswerCorrect(){
		
		//rgd1 = matrix1
		//rgd2 = matrix2
		//rgd3 = product
		//rgd4 = correct product
		
		double rgd1[][];
		double rgd2[];
		double rgd3[] = null;
		double rgd4[];
		
		rgd1 = new double[][]{
				{1,2,3},
				{0,4,5},
				{1,0,6}};
		rgd2 = new double[]{1,2,3};
		try {
			rgd3 = ExtendedMath.matrixProduct(rgd1, rgd2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rgd4 = new double[]{14,23,19};
		for(int i=0;i<rgd1.length;i++){
			assertEquals(rgd3[i],rgd4[i],0.000000001);
		}
	}
	
	@Test
	public void matrixProduct_MatricesMultiplied_AnswerCorrect(){
		
		//rgd1 = matrix1
		//rgd2 = matrix2
		//rgd3 = product
		//rgd4 = correct product
		
		double rgd1[][];
		double rgd2[][];
		double rgd3[][] = null;
		double rgd4[][];
		
		rgd1 = new double[][]{
				{1,2,3},
				{0,4,5},
				{1,0,6}};
		rgd2 = new double[][]{
				{1.09090909090909,-0.54545454545455,-0.09090909090909},
				{0.22727272727273,0.13636363636364,-0.22727272727273},
				{-0.18181818181818,0.09090909090909,0.18181818181818}};
		try {
			rgd3 = ExtendedMath.matrixProduct(rgd1, rgd2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rgd4 = new double[][]{
				{1,0,0},
				{0,1,0},
				{0,0,1}};
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				assertEquals(rgd3[i][j],rgd4[i][j],0.000000001);
			}
		}
	}
	
	@Test
	public void matrixDifference_MatricesDifferenced_AnswerCorrect(){
		
		//rgd1 = matrix1
		//rgd2 = matrix2
		//rgd3 = difference
		//rgd4 = correct difference
		
		double rgd1[][];
		double rgd2[][];
		double rgd3[][] = null;
		double rgd4[][];
		
		rgd1 = new double[][]{
				{1,2,3},
				{0,4,5},
				{1,0,6}};
		rgd2 = new double[][]{
				{1,2,3},
				{0,4,5},
				{11,0,7}};
		try {
			rgd3 = ExtendedMath.matrixDifference(rgd1, rgd2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		rgd4 = new double[][]{
				{0,0,0},
				{0,0,0},
				{-10,0,-1}};
		for(int i=0;i<rgd1.length;i++){
			for(int j=0;j<rgd1[0].length;j++){
				assertEquals(rgd3[i][j],rgd4[i][j],0.000000001);
			}
		}
	}
	
	@Test
	public void slope_Evaluated_CorrectResult(){
		assertEquals(1.20952842277608,ExtendedMath.slope(rgd1,rgd2),0.0000001);
		assertEquals(1.20952842277608,ExtendedMath.slope(lst1,lst2),0.0000001);
	}
	
	@Test
	public void pearson_Evaluated_CorrectResult(){
		assertEquals(0.8668162544815,ExtendedMath.pearson(rgd1,rgd2),0.0000001);
		assertEquals(0.8668162544815,ExtendedMath.pearson(lst1,lst2),0.0000001);
	}
	
	
	
	@Test
	public void sumOfPowers_Evaluated_CorrectResult(){
		assertEquals(ExtendedMath.sumOfPowers(lst1,2.),6676.0856,0.00000001);
		assertEquals(ExtendedMath.sumOfPowers(rgd1,2.),6676.0856,0.00000001);
		assertEquals(ExtendedMath.sumOfPowers(lst1,3.),506382.381504,0.00000001);
		assertEquals(ExtendedMath.sumOfPowers(rgd1,3.),506382.381504,0.00000001);

	}
	
	@Test
	public void sumOfPowersMeanCentered_Evaluated_CorrectResult(){
		assertEquals(ExtendedMath.sumOfPowersMeanCentered(lst1,2.),4032.0692,0.00000001);
		assertEquals(ExtendedMath.sumOfPowersMeanCentered(rgd1,2.),4032.0692,0.00000001);
		assertEquals(ExtendedMath.sumOfPowersMeanCentered(lst1,3.),127411.222464,0.00000001);
		assertEquals(ExtendedMath.sumOfPowersMeanCentered(rgd1,3.),127411.222464,0.00000001);

	}
	
	
	@Test
	public void mean_Evaluated_CorrectResult(){
		assertEquals(ExtendedMath.mean(lst1),25.71,0.00000001);
		assertEquals(ExtendedMath.mean(rgd1),25.71,0.00000001);
	}
	
	@Test
	public void sum_Evaluated_CorrectResult(){
		assertEquals(ExtendedMath.sum(lst1),102.84,0.00000001);
		assertEquals(ExtendedMath.sum(rgd1),102.84,0.00000001);
	}
	
	@Test
	public void standardDeviationP_Evaluated_CorrectResult(){
		assertEquals(ExtendedMath.standardDeviationP(lst1),31.7492881809971,0.00000001);
		assertEquals(ExtendedMath.standardDeviationP(rgd1),31.7492881809971,0.00000001);
	}
}
