package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

import immutable.dependtask.LockState;

/** Locked DependParam. Compare with .equals(Object).
 Example: a readWriteLock on a CLMem, used with a readLock on another 2 CLMems to sum derivatives
into the first CLMem during backprop of matrix multiply.
*/
public class LockPar {
	
	public final LockState ls;
	
	public final DependParam dp;
	
	public LockPar(LockState lockState, DependParam dependParam){
		this.ls = lockState;
		this.dp = dependParam;
	}
	
	public int hashCode() {
		return ls.hashCode()+dp.hashCode();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof LockPar)) return false;
		LockPar l = (LockPar)o;
		return l.ls==ls && l.dp==dp;
	}

}
