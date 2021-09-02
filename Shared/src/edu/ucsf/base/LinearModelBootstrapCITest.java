package edu.ucsf.base;

import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;

/**
 * Fits linear model using apache math module.
 * @author jladau
 */


/*
#Test script for confirming results with R
library('MPV')
library('boot')
dat1 = read.csv('/home/jladau/Desktop/testdata2.csv')
prs=function(formula, data, indices) {
  d=data[indices,]
  lm1=lm(formula, data=d)
  return(PRESS(lm1))
} 
bot1 = boot(data=dat1, statistic=prs, R=5000, formula=y~x1+x2)
quantile(bot1$t,c(0.025,0.975))

cvr=function(formula, data, indices) {
  d=data[indices,]
  lm1=lm(formula, data=d)
  return(1-PRESS(lm1)/sum((d$y-mean(d$y))^2))
} 
bot2 = boot(data=dat1, statistic=cvr, R=5000, formula=y~x1+x2)
quantile(bot2$t,c(0.025,0.975))
*/


public class LinearModelBootstrapCITest{

	/**Linear model object**/
	private LinearModelBootstrapCI lnc1;
	
	/**Second linear model object**/
	private LinearModelBootstrapCI lnc2;
	
	public LinearModelBootstrapCITest() throws Exception{
		initialize();
	}
	
	private void initialize() throws Exception{
		
		//rgs1 = data in string format
		//tbl1 = data table
		//set1 = set of predictors
		
		HashSet<String> set1;
		String rgs1[][];
		HashBasedTable<String,String,Double> tbl1;
		
		//rgs1 = new String[][]{
		//		{"","pred1","pred2","pred3","resp1"},
		//		{"obs1","1","2","8","1"},
		//		{"obs2","2","2","5","3"},
		//		{"obs3","3","3","6","4"},
		//		{"obs4","4","4","9","9"},
		//		{"obs5","4","4","9","9"},
		//		{"obs6","8","4","9","12"},
		//		{"obs7","9","2","9","19"},
		//		{"obs8","12","4","12","25"},
		//		{"obs9","15","4","15","30"},
		//		{"obs10","18","-8","9","29"}};
		
		//rgs1 = new String[][]{
		//		{"","pred1","pred2","pred3","resp1"},
		//		{"obs1","1","5","8","6"},
		//		{"obs2","2","6","5","8"},
		//		{"obs3","3","7","6","10"},
		//		{"obs4","4","8","9","12"},
		//		{"obs5","5","9","9","14"},
		//		{"obs6","6","10","9","16"},
		//		{"obs7","7","11","9","18"},
		//		{"obs8","8","12","12","20"},
		//		{"obs9","9","13","15","22"},
		//		{"obs10","10","14","9","25"}};
		
		//rgs1 = new String[][]{
		//		{"","pred1","pred2","resp1"},
		//		{"obs1","1","5","6"},
		//		{"obs2","2","6","8"},
		//		{"obs3","3","7","10"},
		//		{"obs4","5","9","14"},
		//		{"obs5","4","8","13"}};

		//rgs1 = new String[][]{
		//		{"","pred1","pred2","resp1"},
		//		{"obs1","1","5","6"},
		//		{"obs2","2","6","8"},
		//		{"obs3","3","7","10"},
		//		{"obs4","5","9","14"},
		//		{"obs5","4","8","13"},
		//		{"obs6","6","10","16"}};

		
		//rgs1 = new String[][]{
		//		{"","pred1","resp1"},
		//		{"obs1","0","0"},
		//		{"obs2","1","0.9"},
		//		{"obs3","2","2.1"}};
		
		//rgs1 = new String[][]{
		//		{"","pred1","resp1"},
		//		{"obs1","0","0"},
		//		{"obs2","1","0.9"},
		//		{"obs3","2","2.1"},
		//		{"obs4","3","2.9"},
		//		{"obs5","4","4.1"},
		//		{"obs6","5","4.9"},
		//		{"obs7","6","6.1"},
		//		{"obs8","7","6.9"},
		//		{"obs9","8","8.1"},
		//		{"obs10","9","8.9"},
		//		{"obs111","10","9.1"}};
		
		rgs1 = new String[][]{
				{"","pred1","resp1"},
				{"obs1","0","0"},
				{"obs2","1","0.7"},
				{"obs3","2","2.3"},
				{"obs4","3","2.7"},
				{"obs5","4","4.3"},
				{"obs6","5","4.7"},
				{"obs7","6","6.3"},
				{"obs8","7","6.7"},
				{"obs9","8","8.3"},
				{"obs10","9","8.7"},
				{"obs111","10","9.3"}};
		
		
		tbl1 = HashBasedTable.create();
		for(int i=1;i<rgs1.length;i++){
			for(int j=1;j<rgs1[0].length;j++){
				tbl1.put(rgs1[0][j],rgs1[i][0], Double.parseDouble(rgs1[i][j]));
			}
		}
		
		set1 = new HashSet<String>();
		set1.add("pred1");
		lnc1 = new LinearModelBootstrapCI(tbl1, "resp1", set1);
		
		rgs1 = new String[][]{
				{"","pred1","pred2","resp1"},
				{"     1","-1.683333333333333","48.39166666666667","3.6530194510996132"},
				{"     2","-3.2999999999999994","46.86666666666667","3.6341748717626"},
				{"     3","-3.2999999999999994","46.86666666666667","3.6182573448404014"},
				{"     4","0.10000000000000024","45.275000000000006","3.369215857410143"},
				{"     5","-3.308333333333333","52.849999999999994","3.6776981814745104"},
				{"     6","1.941666666666667","54.625","3.6001012556913907"},
				{"     7","1.2666666666666666","53.475","3.66228551572213"},
				{"     8","-0.5916666666666667","49.83333333333332","3.661907292766021"},
				{"     9","3.8083333333333336","45.26666666666667","3.446070935701005"},
				{"    10","-2.6250000000000004","47.25000000000001","3.550595207489328"},
				{"    11","5.283333333333333","45.199999999999996","3.375114684692225"},
				{"    12","-3.0666666666666664","51.93333333333333","3.5871494982543437"},
				{"    13","-2.733333333333333","50.54999999999999","3.588159616383092"},
				{"    14","-4.466666666666666","50.875","3.684665864025861"},
				{"    15","-8.358333333333333","50.55833333333333","3.62293896921149"},
				{"    16","-5.158333333333333","50.79166666666668","3.680969718465897"},
				{"    17","-5.158333333333333","50.79166666666668","3.6602012013806817"},
				{"    18","-3.391666666666667","51.78333333333333","3.5908418347816027"},
				{"    19","-4.466666666666666","50.875","3.634678752178682"},
				{"    20","-2.375","48.916666666666664","3.5742628297070267"},
				{"    21","-4.125000000000001","48.958333333333336","3.635483746814912"},
				{"    22","-3.2333333333333343","49.19166666666667","3.6546577546495245"},
				{"    23","-2.375","48.916666666666664","3.6060587494103142"},
				{"    24","-4.533333333333334","48.40833333333333","3.6467956887784694"},
				{"    25","-5.483333333333334","48.35","3.6036855496146996"},
				{"    26","-3.258333333333334","51.59166666666667","3.611510887126656"},
				{"    27","-5.616666666666667","49.00833333333333","3.5833121519830775"},
				{"    28","-3.308333333333333","52.849999999999994","3.628899564420607"},
				{"    29","-6.75","48.849999999999994","3.6022770843001926"},
				{"    30","-1.4749999999999996","48.875","3.617943434828973"},
				{"    31","-1.7750000000000004","49.675000000000004","3.5991185650553628"},
				{"    32","-3.308333333333333","52.849999999999994","3.631950826259217"},
				{"    33","-1.591666666666667","53.083333333333336","3.651181062444688"},
				{"    34","-5.241666666666667","53.15","3.6245914591268478"},
				{"    35","-3.475","52.491666666666674","3.597695185925512"},
				{"    36","-3.475","52.491666666666674","3.5531545481696254"},
				{"    37","-1.6000000000000003","56.650000000000006","3.6807886115066824"},
				{"    38","-5.241666666666667","53.15","3.625723909525756"},
				{"    39","-1.675","52.125","3.6665179805548807"},
				{"    40","-3.258333333333334","51.59166666666667","3.5868122694433757"},
				{"    41","-5.008333333333333","47.49166666666667","3.640978057358332"},
				{"    42","-5.666666666666667","47.916666666666664","3.619406410886777"},
				{"    43","-3.6666666666666665","47.375","3.6609602917760835"},
				{"    44","-4.416666666666667","48.28333333333333","3.608739919068788"},
				{"    45","-4.533333333333334","48.40833333333333","3.63558426631123"},
				{"    46","-5.666666666666667","47.916666666666664","3.5727554651542195"},
				{"    47","-7.183333333333334","48.49166666666667","3.598899887063883"},
				{"    48","0.49166666666666686","57.975","3.6545615547417434"},
				{"    49","-0.008333333333333156","51.14166666666666","3.584670384464349"},
				{"    50","0.18333333333333313","57.0","3.6864575104691117"},
				{"    51","0.14999999999999977","57.55833333333333","3.582744965691277"},
				{"    52","-1.9750000000000003","49.71666666666667","3.5728716022004803"},
				{"    53","0.016666666666666607","49.433333333333344","3.5542468081661105"},
				{"    54","-1.6333333333333335","49.53333333333334","3.595055089759304"},
				{"    55","-1.9583333333333337","49.375","3.600428325732131"},
				{"    56","0.6749999999999998","54.6","3.657915936829955"},
				{"    57","-0.7666666666666667","49.35833333333334","3.6509870943834453"},
				{"    58","1.6166666666666671","54.25000000000001","3.6636067081245205"},
				{"    59","0.2666666666666669","53.23333333333334","3.658964842664435"},
				{"    60","1.6166666666666671","54.25000000000001","3.6918768225593315"}};
		
		tbl1 = HashBasedTable.create();
		for(int i=1;i<rgs1.length;i++){
			for(int j=1;j<rgs1[0].length;j++){
				tbl1.put(rgs1[0][j],rgs1[i][0], Double.parseDouble(rgs1[i][j]));
			}
		}
		set1 = new HashSet<String>();
		set1.add("pred1");
		set1.add("pred2");
		lnc2 = new LinearModelBootstrapCI(tbl1, "resp1", set1);
		
	}
	
	@Test
	public void getConfidenceInterval_CIGotten_CICorrect(){
		
		//set1 = set of predictors
		//rng1 = current ci
		
		HashSet<String> set1;
		Range<Double> rng1;
		
		//initializing first model
		set1 = new HashSet<String>();
		for(int i=1;i<=1;i++){		
			set1.add("pred" + i);
		}
		try {
			lnc1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//resampling
		lnc1.resample(10000);
		
		//checking PRESS CI
		rng1 = lnc1.getPRESSConfidenceInterval(0.95);
		assertEquals(rng1.lowerEndpoint(),0.537226,0.1);
		assertEquals(rng1.upperEndpoint(),2.372022,0.1);
		
		//checking cross validation R^2 CI
		rng1 = lnc1.getCVR2ConfidenceInterval(0.95);
		assertEquals(rng1.lowerEndpoint(),0.9600446,0.025);
		assertEquals(rng1.upperEndpoint(),0.9952610,0.025);
		
		set1 = new HashSet<String>();
		for(int i=1;i<=1;i++){		
			set1.add("pred" + i);
		}
		try {
			lnc1.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//initializing second model
		set1 = new HashSet<String>();
		for(int i=1;i<=2;i++){		
			set1.add("pred" + i);
		}
		try {
			lnc2.fitModel(set1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//resampling
		lnc2.resample(10000);
		
		//checking PRESS CI
		rng1 = lnc2.getPRESSConfidenceInterval(0.95);
		assertEquals(rng1.lowerEndpoint(),0.07837896,0.025);
		assertEquals(rng1.upperEndpoint(),0.20550878,0.025);
		
		//checking cross validation R^2 CI
		rng1 = lnc2.getCVR2ConfidenceInterval(0.95);
		assertEquals(rng1.lowerEndpoint(),-0.1108924,0.025);
		assertEquals(rng1.upperEndpoint(),0.6151928,0.025);
	}
	
	/*
	
	private void calculateExactExpectedPRESS(){
		
		//lnm1 = current model
		//rgs1 = overall array of data
		//rgs2 = current array of data
		//tbl1 = data table
		//set1 = set of predictors
		
		HashSet<String> set1;		
		LinearModel lnm1;
		Permutation<Integer> per1;
		String rgs1[][];
		String rgs2[][];
		HashBasedTable<String,String,Double> tbl1;
		
		rgs1 = new String[][]{
				{"","pred1","resp1"},
				{"obs1","0","0"},
				{"obs2","1","0.9"},
				{"obs3","2","2.1"}};
		
		rgs2 = new String[rgs1.length][3];
		rgs2[0] = rgs1[0];
		for(int i=1;i<=3;i++){
			for(int j=1;j<=3;j++){
				for(int k=1;k<=3;k++){
					
					//loading resample
					rgs2[1]=rgs1[i];
					rgs2[2]=rgs1[j];
					rgs2[3]=rgs1[k];
					tbl1 = HashBasedTable.create();
					for(int a=1;a<rgs2.length;a++){
						for(int b=1;b<rgs2[0].length;b++){
							tbl1.put(rgs2[0][b],rgs2[a][0], Double.parseDouble(rgs2[a][b]));
						}
					}
					
					
					lnm1 = new LinearModel(tbl1);
					set1 = new HashSet<String>();
					set1.add("pred1");
					try {
						lnm1.fitModel("resp1", set1);
					} catch (Exception e) {
						
						//**************************
						System.out.println("");
						for(int a=0;a<rgs2.length;a++){
							System.out.println(rgs2[a][0] + "," + rgs2[a][1] + "," + rgs2[a][2]);
						}
						//**************************
						
						
						e.printStackTrace();
					}
					System.out.println(lnm1.findPRESS());
				}
			}
		}

		//************************
		System.out.println("HERE");
		//************************
	}
	*/
	
}
