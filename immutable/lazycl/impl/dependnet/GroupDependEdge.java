/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.dependnet;

public strictfp class GroupDependEdge{
	
	public final LazyGroup before, after;
	
	protected int hash;
	
	public GroupDependEdge(LazyGroup before, LazyGroup after){
		this.before = before;
		this.after = after;
		hash = System.identityHashCode(before)+System.identityHashCode(after);
	}
	
	public int hashCode(){ return hash; }
	
	public boolean equals(Object obj){
		if(!(obj instanceof GroupDependEdge)) return false;
		GroupDependEdge d = (GroupDependEdge)obj;
		return before==d.before && after==d.after;
	}

}
