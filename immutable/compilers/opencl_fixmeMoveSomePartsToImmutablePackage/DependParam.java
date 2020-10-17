package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

import mutable.util.Time;

/** metadata about a memory or a constant Number to use instead. Compare with ==. */
public class DependParam extends Mem implements Cloneable, Comparable<DependParam>{
	
	/** If null, this is a symbol referring to other memory (CLMem, FloatBuffer, float[], etc), else its just this constant Number */
	public final Number numOrNull;
	
	/** optional */
	public final String comment;
	
	public final long id = Time.timeId();
	
	public DependParam(String comment, Class elType, int size){
		super(elType, size);
		this.comment = comment;
		numOrNull = null;
	}
	
	public DependParam(String comment, Number n){
		super(n.getClass(), 1);
		this.comment = comment;
		numOrNull = n;
	}
	
	/** true if this is a key to lookup or store array/floatbuffer/etc.
	False if this is a literal Number stored here.
	*/
	public boolean lazy(){
		return numOrNull == null;
	}
	
	public Object clone(){
		return new DependParam(comment,elType,size);
	}
	
	public String toString(){
		return comment+"_"+id;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof DependParam)) return false;
		return id==((DependParam)obj).id;
	}
	
	public int compareTo(DependParam p){
		return Long.compare(id, p.id);
	}

}
