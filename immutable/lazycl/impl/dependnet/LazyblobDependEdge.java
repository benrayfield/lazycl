/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.dependnet;

import immutable.lazycl.spec.LazyBlob;

public strictfp class LazyblobDependEdge{
	
	/** If true can do both of these together for some advantage,
	such as doing multiple opencl ndrange kernels in gpu before returning to cpu for lower lag,
	or such as a cloud call thats faster if you send and receive multiple objects at once.
	*/
	public final boolean together;
	
	public final LazyBlob before, after;
	
	protected int hash;
	
	public LazyblobDependEdge(boolean together, LazyBlob before, LazyBlob after){
		this.together = true;
		this.before = before;
		this.after = after;
		hash = System.identityHashCode(before)+System.identityHashCode(after)+(together?49999:0);
	}
	
	public int hashCode(){ return hash; }
	
	public boolean equals(Object obj){
		if(!(obj instanceof LazyblobDependEdge)) return false;
		LazyblobDependEdge d = (LazyblobDependEdge)obj;
		return before==d.before && after==d.after && together==d.together;
	}

}
