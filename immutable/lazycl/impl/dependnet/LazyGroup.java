package immutable.lazycl.impl.dependnet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import immutable.lazycl.spec.LazyBlob;

/** a set of LazyBlob where DependEdge.together is true
(caused by "Code" starts with the same prefix colon such as "opencl1.2:" or "download:"
AND there being enough GPU memory etc to run them all together)
which can be evaled after any dependent LazyGroups are evaled.
This is a level above DependEdge.
*/
public class LazyGroup{
	
	/** immutable */
	public final Set<LazyBlob> lazyblobs;
	
	/** from and to LazyBlobs in this group */
	public final Set<LazyblobDependEdge> internalDepends;
	
	/*public LazyGroup(Set<LazyblobDependEdge> internalDepends){
		internalDepends = Collections.unmodifiableSet(new HashSet(internalDepends));
		Set<LazyBlob> lbs = new HashSet();
		for(LazyblobDependEdge e : internalDepends){ //FIXME what if theres no depend edge between some of them
			lbs.add(e.before);
			lbs.add(e.after);
		}
		lazyblobs = Collections.unmodifiableSet(lbs)
	}*/
	public LazyGroup(Set<LazyBlob> lazyblobs, Set<LazyblobDependEdge> internalDepends){
		this.lazyblobs = Collections.unmodifiableSet(new HashSet(lazyblobs));
		this.internalDepends = Collections.unmodifiableSet(new HashSet(internalDepends));
	}

}
