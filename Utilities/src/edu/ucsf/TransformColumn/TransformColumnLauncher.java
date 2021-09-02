package edu.ucsf.TransformColumn;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Transforms column
 * @author jladau
 *
 */

public class TransformColumnLauncher {
	
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//d1 = current value
		
		double d1;
		ArgumentIO arg1;
		DataIO dat1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));

		//appending transform
		if(arg1.getValueString("sTransform").equals("log10")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_LOG10");
			for(int i=1;i<dat1.iRows;i++){
				dat1.appendToLastColumn(i,Math.log10(dat1.getDouble(i, arg1.getValueString("sValueKey"))));
			}
		}else if(arg1.getValueString("sTransform").equals("multiplybyscalar")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_MULTIPLIED_SCALAR");
			for(int i=1;i<dat1.iRows;i++){
				dat1.appendToLastColumn(i,arg1.getValueDouble("dMultiplicationFactor")*dat1.getDouble(i, arg1.getValueString("sValueKey")));
			}
		}else if(arg1.getValueString("sTransform").equals("sign")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_SIGN");
			for(int i=1;i<dat1.iRows;i++){
				if(dat1.getDouble(i, arg1.getValueString("sValueKey"))<=0){
					dat1.appendToLastColumn(i,0);
				}else{
					dat1.appendToLastColumn(i,1);
				}
			}
		}else if(arg1.getValueString("sTransform").equals("bound")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_BOUNDED");
			for(int i=1;i<dat1.iRows;i++){
				if(dat1.getDouble(i, arg1.getValueString("sValueKey"))<arg1.getValueDouble("dMinimum")){
					dat1.appendToLastColumn(i,arg1.getValueDouble("dMinimum"));
				}else if(dat1.getDouble(i, arg1.getValueString("sValueKey"))>arg1.getValueDouble("dMaximum")){
					dat1.appendToLastColumn(i,arg1.getValueDouble("dMaximum"));
				}else{
					dat1.appendToLastColumn(i,dat1.getDouble(i, arg1.getValueString("sValueKey")));
				}
			}
		}else if(arg1.getValueString("sTransform").equals("inverselogit")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_INVERSE_LOGIT");
			for(int i=1;i<dat1.iRows;i++){
				d1 = dat1.getDouble(i, arg1.getValueString("sValueKey"));
				dat1.appendToLastColumn(i,1./(1.+Math.exp(-d1)));
			}
		}else if(arg1.getValueString("sTransform").equals("inverselog10")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_INVERSE_LOG10");
			for(int i=1;i<dat1.iRows;i++){
				d1 = dat1.getDouble(i, arg1.getValueString("sValueKey"));
				dat1.appendToLastColumn(i,Math.pow(10., d1));
			}
		}else if(arg1.getValueString("sTransform").equals("logratiotopercentchange")){
			dat1.appendToLastColumn(0,arg1.getValueString("sValueKey") + "_PERCENT_CHANGE");
			for(int i=1;i<dat1.iRows;i++){
				d1 = dat1.getDouble(i, arg1.getValueString("sValueKey"));
				dat1.appendToLastColumn(i,Math.pow(10., d1)-1.);
			}
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}