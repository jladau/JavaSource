package gov.lbnl.IndividualBetaDiversity;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import gov.lbnl.IndividualBetaDiversity.OccurrenceModel.Estimate;
import gov.lbnl.IndividualBetaDiversity.OccurrenceModel.Estimates;

public class IndividualBetaDiversityLauncher{

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = pairs of samples and response variable
		//bio1 = biom object
		//setTaxa = set of taxa
		//lstIndoor = indoor sample list
		//lstOutdoor = outdoor sample list
		//lstIndoorOccur = occurrence values indoors
		//lstOutdoorOccur = occurrence values outdoors
		//d1 = current value
		//ocm1 = current occurrence model
		//lstResponses = list of response variables
		//est1 = current estimate
		//lstOut = output
		//iCounter = output counter
		
		int iCounter;
		ArrayList<String> lstOut;
		double d1;
		ArgumentIO arg1;
		DataIO dat1;
		BiomIO bio1;
		HashSet<String> setTaxa;
		ArrayList<String> lstIndoor;
		ArrayList<String> lstOutdoor;
		ArrayList<Boolean> lstIndoorOccur;
		ArrayList<Boolean> lstOutdoorOccur;
		ArrayList<Double> lstResponses;
		OccurrenceModel ocm1;
		Estimate est1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sSampleDataPath"));
		bio1 = new BiomIO(arg1.getValueString("sBIOMPath"), arg1.getAllArguments());
		setTaxa = bio1.axsObservation.getIDs();
		lstIndoor = dat1.getStringColumn("INDOOR_SAMPLE");
		lstOutdoor = dat1.getStringColumn("OUTDOOR_SAMPLE");
		lstResponses = dat1.getDoubleColumn(arg1.getValueString("sResponseHeader"));
		
		//looping through taxa
		lstOut = new ArrayList<String>(setTaxa.size()*4+1);
		lstOut.add("TAXON,PARAMETER,ESTIMATE,SES,PROBABILITY_GTE,PROBABILITY_LTE");
		iCounter = 0;
		for(String sTaxon:setTaxa){
			iCounter++;
			System.out.println("Analyzing taxon " + iCounter + " of " + setTaxa.size() + "...");
			
			lstIndoorOccur = new ArrayList<Boolean>(lstIndoor.size());
			lstOutdoorOccur = new ArrayList<Boolean>(lstOutdoor.size());
			for(int i=0;i<lstIndoor.size();i++){
				d1 = bio1.getValueByIDs(sTaxon,lstIndoor.get(i));
				if(d1>0){
					lstIndoorOccur.add(true);
				}else{
					lstIndoorOccur.add(false);
				}
				d1 = bio1.getValueByIDs(sTaxon,lstOutdoor.get(i));
				if(d1>0){
					lstOutdoorOccur.add(true);
				}else{
					lstOutdoorOccur.add(false);
				}
			}
			ocm1 = new OccurrenceModel(lstIndoorOccur, lstOutdoorOccur, lstResponses);
			ocm1.loadEstimates(arg1.getValueInt("iNullIterations"));
			for(String s:new String[]{"d0","dI","dO","dIO","dIPlusIO"}) {
				est1 = ocm1.getEstimate(s);
				lstOut.add(sTaxon + "," + s + "," + est1.toString());
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}	
}