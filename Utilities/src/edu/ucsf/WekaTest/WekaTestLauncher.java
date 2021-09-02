package edu.ucsf.WekaTest;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import edu.ucsf.io.DataIO;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTestLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		
		Random rnd1;
		
		rnd1 = new Random(1234);
		
	    
	/*
		
      // load CSV
      CSVLoader loader = new CSVLoader();
      loader.setSource(new File("/home/jladau/Desktop/temp.1.csv"));
      Instances data = loader.getDataSet();

      // save ARFF
      ArffSaver saver = new ArffSaver();
      saver.setInstances(data);
      saver.setFile(new File("/home/jladau/Desktop/temp.2.arff"));
      saver.setDestination(new File("/home/jladau/Desktop/temp.2.arff"));
      saver.writeBatch();
	*/
		
		
		//DataSource source = new DataSource("/home/jladau/Desktop/weka-3-8-5/data/vote.arff");
		DataSource source = new DataSource("/home/jladau/Desktop/weka-test-data.arff");
		Instances ist1 = source.getDataSet();
		if(ist1.classIndex() == -1){
			ist1.setClassIndex(ist1.numAttributes() - 1);
		}
		
		System.out.println(ist1.classIndex());
		
		
		ArrayList<Attribute> lst1 = new ArrayList<Attribute>(2);
		lst1.add(new Attribute("beta-diversity"));
		ArrayList<String> lst2 = new ArrayList<String>(2);
		lst2.add("no");
		lst2.add("yes");
		lst1.add(new Attribute("y1", lst2));
		DataIO dat1 = new DataIO("/home/jladau/Desktop/weka-test-data.csv");
		Instances ist2 = new Instances("test-instances", lst1, dat1.iRows);
		
		for(int i=1;i<dat1.iRows;i++) {
			//ist2.add(new DenseInstance(1., new double[] {dat1.getDouble(i,0), 1-dat1.getDouble(i,1)}));
			if(rnd1.nextDouble()<dat1.getDouble(i,0)){
				ist2.add(new DenseInstance(1., new double[] {dat1.getDouble(i,0), 1.}));
				System.out.println(dat1.getDouble(i,0) + "," + 0);
			}else {
				ist2.add(new DenseInstance(1., new double[] {dat1.getDouble(i,0), 0.}));
				System.out.println(dat1.getDouble(i,0) + "," + 1);
			}
		}
		ist2.setClassIndex(1);
		
		Logistic log1 = new Logistic();
		log1.setDoNotStandardizeAttributes(false);
		
		
		for(int i=0;i<1;i++) {
			Evaluation eval = new Evaluation(ist2);
			eval.crossValidateModel(log1, ist2, 10, new Random(System.currentTimeMillis()));
		    ThresholdCurve tc = new ThresholdCurve();
		    int classIndex = 0;
		    Instances result = tc.getCurve(eval.predictions(), classIndex);
		    //System.out.println(ThresholdCurve.getROCArea(result));
		    if(i % 100 == 0){	
		    	System.out.println(i);
		    }
		} 
		    
		
		log1.buildClassifier(ist2);
		System.out.println(log1.getDoNotStandardizeAttributes());
		double[][] rgd1 = log1.coefficients();
		System.out.println(rgd1[0][0]);
		System.out.println(rgd1[1][0]);
		System.out.println("HERE");
		
	}
	
	
}
