package edu.ucsf.geospatial;

import java.util.HashMap;

/**
 * Metadata for geospatial rasters. Follows conventions from http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html
 * @author jladau
 *
 */

public class GeospatialRasterMetadata {
	
	/**Short description of the file contents**/
	public String title;
	
	/**References that describe the data or methods used to produce it**/
	public String references;
	
	/**Method of production of the original data**/
	public String source;
	
	/**Where the original data was produced**/
	public String institution;
	
	/**See: http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html#cell-methods**/
	public String cell_methods;
	
	/**Name of variable in raster**/
	public String variable;
	
	/**Long name of variable in raster. See http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html#long-name**/
	public String long_name;
	
	/**Units of variable in raster**/
	public String units;
	
	/**History: creation date of data and raster. Provides an audit trail for modifications to the original data.**/
	public String history;
	
	public GeospatialRasterMetadata(
			String title,
			String institution,
			String references,
			String source,
			String history,
			String variable, 
			String units, 
			String long_name, 
			String cell_methods){
	
		this.title = title;
		this.institution = institution;
		this.references = references; 
		this.source = source;
		this.history = history;
		this.variable = variable; 
		this.units = units; 
		this.long_name = long_name; 
		this.cell_methods = cell_methods;	
	}
	
	public GeospatialRasterMetadata(GeospatialRasterMetadata gmt1){
		this.title = gmt1.title;
		this.institution = gmt1.institution;
		this.references = gmt1.references; 
		this.source = gmt1.source;
		this.history = gmt1.history;
		this.variable = gmt1.variable; 
		this.units = gmt1.units; 
		this.long_name = gmt1.long_name; 
		this.cell_methods = gmt1.cell_methods;
	}
	
	public boolean equals(Object obj1){
		
		//gmt1 = object coerced to metadata object
		
		GeospatialRasterMetadata gmt1;
		
		if(obj1 instanceof GeospatialRasterMetadata){
			
			gmt1 = (GeospatialRasterMetadata) obj1;
			
			if(!gmt1.title.equals(title)){
				return false;
			}
			if(!gmt1.institution.equals(institution)){
				return false;
			}
			if(!gmt1.references.equals(references)){
				return false;
			}
			if(!gmt1.source.equals(source)){
				return false;
			}
			if(!gmt1.variable.equals(variable)){
				return false;
			}
			if(!gmt1.units.equals(units)){
				return false;
			}
			if(!gmt1.long_name.equals(long_name)){
				return false;
			}
			if(!gmt1.cell_methods.equals(cell_methods)){
				return false;
			}
			if(!gmt1.history.equals(history)){
				return false;
			}
			return true;
		}else{
			return false;
		}
	}
	
	public HashMap<String,String> getGlobalAttributes(){
		
		//map1 = output
		
		HashMap<String,String> map1;
		
		map1 = new HashMap<String,String>();
		map1.put("title",title);
		map1.put("institution", institution);
		map1.put("references", references);
		map1.put("source", source);
		map1.put("history", history);
		return map1;
	}
	
	public void putGlobal(String sKey, String sValue){
		
		if(sKey.equals("title")){
			title=sValue;
		}
		if(sKey.equals("institution")){
			institution=sValue;
		}
		if(sKey.equals("references")){
			references=sValue;
		}
		if(sKey.equals("source")){
			source=sValue;
		}
		if(sKey.equals("history")){
			history=sValue;
		}
	}
}
