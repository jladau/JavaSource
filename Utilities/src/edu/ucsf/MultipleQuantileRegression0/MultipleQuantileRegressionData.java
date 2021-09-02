package edu.ucsf.MultipleQuantileRegression0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.MultipleQuantileRegression0.YData.YDatum;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Ranks;

public class MultipleQuantileRegressionData{
	
	/**List of windows**/
	private ArrayList<Window> lstWindows;
	
	/**Number of responses**/
	private int iCols;
	
	//TODO add functionality to build y data in this object, then sort etc
	
	public MultipleQuantileRegressionData(ArrayList<Double> lstX, YData ydt1, int iWindowSize, Random rnd1) {
		iCols = ydt1.columns();
		initialize(lstX, ydt1, iWindowSize, rnd1);
	}
	
	private void initialize(ArrayList<Double> lstX, YData ydt1, int iWindowSize, Random rnd1){

		//map1 = tree map from x values to y values
		
		TreeMap<Double,YData> map1;
		
		//loading map from x values to y values
		map1 = new TreeMap<Double,YData>();
		for(int i=0;i<lstX.size();i++){
			if(!map1.containsKey(lstX.get(i))) {
				map1.put(lstX.get(i),new YData(lstX.size(),ydt1.columns()));
			}
			map1.get(lstX.get(i)).add(ydt1.get(i));
		}
		
		//loading windows
		loadWindows(map1, lstX.size(), iWindowSize, rnd1);
	}
	
	private void loadWindows(TreeMap<Double,YData> mapXY, int iRows, int iWindowSize, Random rnd1){
		
		//lstXOrdered = list of x values in ascending order
		//lstYOrdered = list of y values corresponding to x; y values random within x categories
		//lstY = current set of y values
		//lstX = current set of x values
		//d1 = current mean
		
		ArrayList<Double> lstXOrdered;
		YData ydtOrdered;
		YData ydt1;
		ArrayList<Double> lstX;
		double d1;
		
		//finding semi-ordered lists of x and y data
		lstXOrdered = new ArrayList<Double>(iRows);
		ydtOrdered = new YData(iRows, iCols);
		for(Map.Entry<Double,YData> ery1:mapXY.entrySet()){
			ydt1 = ery1.getValue();
			ydt1.shuffle(rnd1);
			for(int i=0;i<ydt1.rows();i++){
				lstXOrdered.add(ery1.getKey());
				ydtOrdered.add(ydt1.get(i));
			}
		}
		
		//loading windows
		lstWindows = new ArrayList<Window>(iRows);
		for(int i=0;i<lstXOrdered.size();i++){
			
			//exiting if done
			if(i+iWindowSize-1>iRows-1) {
				break;
			}
			
			//adding new window
			lstWindows.add(new Window(iWindowSize, iCols));
			
			//loading values
			lstX = new ArrayList<Double>(iWindowSize);
			for(int k=i;k<i+iWindowSize;k++){
				lstX.add(lstXOrdered.get(k));
				lstWindows.get(i).putY(ydtOrdered.get(k));
			}
			
			d1 = ExtendedMath.mean(lstX);
			lstWindows.get(i).putXMean(d1);
		}
	}
	
	public HashBasedTable<String, String, Double> toLinearModelData(){
		
		//tbl1 = output
		//s1 = current index in string format
		
		HashBasedTable<String,String,Double> tbl1;
		String s1;
		
		tbl1 = HashBasedTable.create(this.iCols+1,lstWindows.size());
		for(int i=0;i<lstWindows.size();i++){
			s1 = Integer.toString(i);
			for(String s:lstWindows.get(i).responses()){
				tbl1.put(s,s1,lstWindows.get(i).yPercentile(s));
			}
			tbl1.put("X",s1,lstWindows.get(i).xMean());
		}
		return tbl1;
	}
	
	public HashSet<String> responses(){
		return lstWindows.get(0).responses();
	}
	
	public String predictor(){
		return "X";
	}
	
	public void windowY(double dPercent){
		for(int i=0;i<lstWindows.size();i++){
			lstWindows.get(i).loadYPercentile(dPercent);
		}
	}
	
	public ArrayList<String> print(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(lstWindows.size());
		for(int i=0;i<lstWindows.size();i++){
			lst1.add(lstWindows.get(i).toString());
		}
		return lst1;
	}
	
	public class Window{
		
		/**Number of observations**/
		private int iWindowSize;
		
		/**Y variates**/
		private YData ydt1;
		
		/**X mean**/
		private double dXMean;
		
		/**Y percentile**/
		private HashMap<String,Double> mapYPercentile;
		
		public Window(int iWindowSize, int iCols){
			ydt1 = new YData(iWindowSize,iCols);
			this.iWindowSize = iWindowSize;
		}
		
		public String toString(){
			
			//sbl1 = output
			
			StringBuilder sbl1;
			
			sbl1 = new StringBuilder();
			sbl1.append("X:" + dXMean);
			for(String s:mapYPercentile.keySet()){
				sbl1.append("," + s + ":" + mapYPercentile.get(s));
			}
			return sbl1.toString();
		}
		
		public HashSet<String> responses(){
			return ydt1.names();
		}
		
		public void putXMean(double dXMean){
			this.dXMean = dXMean;
		}
		
		public void putY(YDatum ydm1){
			ydt1.add(ydm1);
		}
		
		public void loadYPercentile(double dPercent){
			
			//lst1 = ranks
			//lst2 = current column
			//d2 = rank
			//d1 = current percentile
			
			ArrayList<Double> lst2;
			ArrayList<Double> lst1;
			double d2;
			double d1;
	
			d2 = dPercent*((double) iWindowSize);
			mapYPercentile = new HashMap<String,Double>(ydt1.columns());
			for(String s:ydt1.names()){
				lst2 = new ArrayList<Double>(ydt1.rows());
				for(int k=0;k<ydt1.rows();k++){
					lst2.add(ydt1.get(k,s));
				}
				lst1 = Ranks.ranksAverage(lst2);
				d1 = Double.MAX_VALUE;
				for(int j=0;j<iWindowSize;j++){
					if(lst1.get(j)>=d2 && lst2.get(j)<d1){
						d1 = lst2.get(j);
					}
				}
				mapYPercentile.put(s,d1);
			}
		}
		
		public double yPercentile(String sResponse){
			return mapYPercentile.get(sResponse);
		}
		
		public double xMean(){
			return dXMean;
		}
		
		public int numberResponses(){
			return ydt1.columns();
		}
	}
}