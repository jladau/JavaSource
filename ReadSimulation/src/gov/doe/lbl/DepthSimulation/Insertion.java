package gov.doe.lbl.DepthSimulation;

public class Insertion{
	
	/**Insertion start position**/
	private int iStart;
	
	/**Insertion end position**/
	private int iEnd;
	
	/**Insertion length**/
	private int iLength;
	
	/**
	 * Constructor
	 * @param iStart Start position
	 * @param iEnd End position
	 */
	public Insertion(int iStart, int iEnd){
		this.iStart = iStart;
		this.iEnd = iEnd;
		this.iLength = iEnd-iStart+1;
	}
	
	/**
	 * Checks if a base is within an insertion
	 * @param iIndex Index of base
	 * @return True if within insertion, false otherwise
	 */
	public boolean contains(int iIndex){
		if(iIndex>=iStart && iIndex<=iEnd){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Finds the distance from the start or end of the insertion
	 * @param iIndex Location to consider
	 * @return Distance in base pairs
	 */
	public int distance(int iIndex){
		if(iIndex<this.iStart){
			return iStart-iIndex;
		}else if(iIndex>this.iEnd){
			return iIndex-iEnd;
		}else{
			return 0;
		}
	}
	
	
	/**
	 * Position relative to the insertion
	 * @param iIndex Base location
	 * @return "within", "before", or "after" if position is within, before, or after the insertion
	 */
	public String relativePosition(int iIndex){
		if(contains(iIndex)){
			return "within";
		}else if(iIndex<iStart){
			return "before";
		}else{
			return "after";
		}
	}
	
	/**
	 * @return Insertion length
	 */
	public int length(){
		return iLength;
	}
	
	/**
	 * @return Insertion start
	 */
	public int start(){
		return iStart;
	}
	
	/**
	 * @return Insertion End
	 */
	public int end(){
		return iEnd;
	}
	
}

