package edu.ucsf.DateRangeHistogram;

import java.util.ArrayList;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.google.common.collect.Range;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Computes a histogram for a collection of date ranges. For each date, the number of ranges containing that date are reported in the histogram.
 * @author jladau
 */

public class DateRangeHistogramLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//lstOut = output
		//lstRanges = list of date ranges
		//timEnd = end date for histogram
		//rng1 = current date range
		//i1 = current frequency
		//d1 = expected mean
		//d2 = expected variance
		//dP = current probability
		
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<Range<LocalDate>> lstRanges;
		LocalDate timEnd;
		int i1;
		Range<LocalDate> rng1;
		double d1;
		double d2;
		double dP;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//loading list of ranges
		lstRanges = new ArrayList<Range<LocalDate>>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lstRanges.add(Range.closed(new LocalDate(dat1.getString(i, arg1.getValueString("sStartDateHeader"))), new LocalDate(dat1.getString(i, arg1.getValueString("sEndDateHeader")))));	
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("START_DATE,END_DATE,MID_DATE,OBSERVED_FREQUENCY,EXPECTED_FREQUENCY,EXPECTED_FREQUENCY_VARIANCE,Z");
		
		//loading frequencies
		timEnd = new LocalDate(arg1.getValueInt("iHistogramEndYear"),12,31);	
		rng1 = Range.closed(new LocalDate(arg1.getValueInt("iHistogramStartYear"),7,2),new LocalDate(arg1.getValueInt("iHistogramStartYear"),7,2));	
		while(!rng1.upperEndpoint().isAfter(timEnd)){
			i1 = 0;
			d1 = 0.;
			d2 = 0.;
			for(int i=0;i<lstRanges.size();i++){
				if(rng1.isConnected(lstRanges.get(i))){
					i1++;
				}
				dP = findProbabilityIntersection(rng1,lstRanges.get(i),new LocalDate(arg1.getValueInt("iHistogramStartYear"),1,1), timEnd);
				d1+=dP;
				d2+=dP*(1.-dP);
			}
			lstOut.add(rng1.lowerEndpoint() + "," + rng1.upperEndpoint() + "," + midDate(rng1.lowerEndpoint(),rng1.upperEndpoint()) + "," + i1 + "," + d1 + "," + d2 + "," + ((double) i1-d1)/Math.sqrt(d2));
			rng1 = Range.closed(rng1.lowerEndpoint().plusYears(arg1.getValueInt("iIntervalYears")), rng1.upperEndpoint().plusYears(arg1.getValueInt("iIntervalYears")));
		}
		
		//outputting results and terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static double findProbabilityIntersection(Range<LocalDate> rngBin, Range<LocalDate> rngInterval, LocalDate timHistogramStart, LocalDate timHistogramEnd){
		
		//d1 = days after histogram start to bin start (in days)
		//d2 = days after bin end to histogram end (in days)
		//dIntervalWidth = interval width (in days)
		//dHistogramLength = length of histogram (in days)
		//timStart = start of interval
		//timEnd = end of interval
		
		double d1;
		double d2;
		double dIntervalWidth;
		double dHistogramLength;
		LocalDate timStart;
		LocalDate timEnd;
		
		d1 = Days.daysBetween(timHistogramStart, rngBin.lowerEndpoint()).getDays();
		d2 = Days.daysBetween(rngBin.upperEndpoint(), timHistogramEnd).getDays();
		dIntervalWidth = Days.daysBetween(rngInterval.lowerEndpoint(), rngInterval.upperEndpoint()).getDays();
		dHistogramLength = Days.daysBetween(timHistogramStart,timHistogramEnd).getDays();
		
		if(d1<dIntervalWidth){
			timStart = timHistogramStart.plusDays((int) dIntervalWidth);
		}else{
			timStart = rngBin.lowerEndpoint();
		}
		if(d2<dIntervalWidth){
			timEnd = timHistogramEnd;
		}else{
			timEnd = rngBin.upperEndpoint().plusDays((int) dIntervalWidth);
		}
		return ((double) Days.daysBetween(timStart, timEnd).getDays())/(dHistogramLength-dIntervalWidth);
	}
	
	private static LocalDate midDate(LocalDate tim1, LocalDate tim2){
		
		//i1 = number of intervening days
		
		int i1;
		
		i1 = Days.daysBetween(tim1, tim2).getDays();
		return tim1.plusDays(i1/2);
	}
}
