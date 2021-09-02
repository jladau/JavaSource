package edu.ucsf.RegionalDiversityCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.Range;
import static edu.ucsf.geospatial.GeospatialRaster.*;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Transforms per-sampling region results to regional results.
 * @author jladau
 *
 */

public class RegionalDiversityCalculatorLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data, in pivot table format
		//ras1 = raster
		//lstOut = output
		//i1 = counter
		//itr1 = raster iterator
		//cel1 = current cell
		//set2 = set of variables to map
		//lst1 = list of cells
		//cap1 = current cap
		//map4 = map of mean values
		
		HashMap<String,Double> map4;
		SphericalCapEarth cap1;
		HashSet<String> set2;
		GeospatialRasterCell cel1;
		GeospatialRaster.LatLonIterator itr1;
		ArrayList<String> lstOut;
		int i1;
		GeospatialRaster ras1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<BetaDiversityRasterCell> lst1;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		
		//initializing list of cells
		lst1 = new ArrayList<BetaDiversityRasterCell>((int) (360*180/(arg1.getValueDouble("dCellSize")*arg1.getValueDouble("dCellSize"))));
		ras1 = new GeospatialRaster(
				arg1.getValueDouble("dCellSize"),
				arg1.getValueDouble("dCellSize"),
				Range.closed(-90.,90.),
				Range.closed(-180.,180.),
				new GeospatialRasterMetadata(null, null, null, null, null, null, null, null, null));
		ras1.addNullTime();
		ras1.addNullVert();
		itr1 = ras1.getLatLonIterator(NULL_TIME, NULL_VERT);
		i1 = 0;
		while(itr1.hasNext()){
			cel1 = itr1.next();
			i1++;
			lst1.add(new BetaDiversityRasterCell(cel1.axeLat.ID, cel1.axeLon.ID,i1));
		}
		
		//loading variables to map
		if(!arg1.containsArgument("rgsVariablesToMap")){
			set2 = new HashSet<String>(Arrays.asList(new String[]{
					"beta_j_given_regional_range",
					"beta_j_given_local_range",
					"beta_j_observed",
					"beta_s_given_regional_range",
					"beta_s_given_local_range",
					"beta_s_observed",
					"beta_w_given_regional_range",
					"beta_w_given_local_range",
					"beta_w_observed",
					"regional_area_mean_observed",
					"regional_perimeter_mean_observed",
					"regional_scaled_perimeter_area_ratio_observed",
					"local_area_mean_observed",
					"local_perimeter_mean_observed",
					"local_perimeter_area_ratio_observed",
					"local_area_mean_given_regional_range",
					"local_perimeter_mean_given_regional_range",
					"alpha_observed"}));
		}else{
			set2 = new HashSet<String>();
			for(String s:arg1.getValueStringArray("rgsVariablesToMap")){
				set2.add(s);
			}
		}
		
		//looping through sampling regions
		for(int i=1;i<dat1.iRows;i++){
			
			//loading region
			cap1 = new SphericalCapEarth(dat1.getDouble(i,"region_radius"),dat1.getDouble(i,"region_latitude_center"),dat1.getDouble(i,"region_longitude_center"),1234);
			
			//looping through cells and adding values as appropriate
			for(int k=0;k<lst1.size();k++){
				if(cap1.contains(lst1.get(k).latitude(), lst1.get(k).longitude())){
					for(int j=0;j<dat1.iCols;j++){
						if(set2.contains(dat1.getString(0, j))){
							if(!dat1.getString(i, j).equals("null")){
								lst1.get(k).put(dat1.getString(0, j), dat1.getDouble(i, j));
							}
						}
					}
				}
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(400000);
		lstOut.add("CELL,VALUE_TYPE,VALUE");
		for(int k=0;k<lst1.size();k++){
			if(lst1.get(k).hasData()){
				lstOut.add(lst1.get(k).id() + ",latitude," + lst1.get(k).latitude());
				lstOut.add(lst1.get(k).id() + ",longitude," + lst1.get(k).longitude());
				lstOut.add(lst1.get(k).id() + ",sample_region_count," + lst1.get(k).samplingRegionCount());
				map4 = lst1.get(k).getMeans();
				for(String s:map4.keySet()){
					lstOut.add(lst1.get(k).id() + "," + s + "," + map4.get(s));
				}
			}
		}
		
		//writing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
