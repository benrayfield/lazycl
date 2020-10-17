package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

import java.util.Arrays;

/** Immutable. This is more general than but compatible with opencl. It could do a parallel calculation in javassist compiled code.
The 1-3 dimensions of opencl ndrange kernel, which you use with [get_local_id(int) and get_group_id(int)] or get_global_id(int).
See TestOpenclLocalMem.java for an example of using get_local_id(int) and get_group_id(int) for faster matrix multiply
which I copied and slightly modified from the MIT licensed code at https://cnugteren.github.io/tutorial/pages/page4.html 
*/
public class ParallelSize{
	
	private final int[] globalSize;
	
	private final int[] localSize;
	
	public int dims(){ return globalSize.length; }
	
	public int globalSize(int dim){ return globalSize[dim]; }
	
	public int localSize(int dim){ return localSize[dim]; }
	
	public ParallelSize(int... globalSize){
		this(globalSize,intArrayOfAllOnes(globalSize.length));
	}
	
	public ParallelSize(int[] globalSize, int[] groupSize){
		if(globalSize.length != groupSize.length) throw new Error("Diff sizes");
		this.globalSize = globalSize.clone();
		this.localSize = groupSize.clone();
	}
	
	public int globalSize(){
		int i = 1;
		for(int size : globalSize) i *= size;
		return i;
	}
	
	public int localSize(){
		int i = 1;
		for(int size : localSize) i *= size;
		return i;
	}
	
	/** nonbacking */
	public int[] globalToIntArray(){
		return globalSize.clone();
	}
	
	/** nonbacking */
	public int[] localToIntArray(){
		return localSize.clone();
	}
	
	static int[] intArrayOfAllOnes(int dims){
		int[] ret = new int[dims];
		Arrays.fill(ret, 1);
		return ret;
	}

}
