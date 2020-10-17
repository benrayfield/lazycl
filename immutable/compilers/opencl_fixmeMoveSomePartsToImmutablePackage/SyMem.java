package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

import java.nio.FloatBuffer;
import java.util.function.IntFunction;

/** symbol (DependParam) and memory.
The symbol is used in OpenclUtil.callOpenclDependnet
to refer to memory that is used by potentially multiple DependOps
in the same dependnet of DependOps before returning from GPU to CPU.
Doing multiple opencl kernel calls before returning to CPU extremely reduces lag.
It reads andOr writes a pooled CLMem multiple times
when that symbol is used in multiple DependOp,
and a single DependOp may read andOr write that.
For example, 50 times per second, do 30 opencl ndrange kernels in 1 callOpenclDependnet.
*/
public class SyMem<T> extends Mem{
	
	//TODO redesign so the mem is not connected to the DependParam since thats done in OpenclUtil.callOpenclDependnet

	public final DependParam sy;
	
	protected T mem;
	
	/** Param is size in units of elType such as floats.
	At most once, creates memory such as FloatBuffer.
	For many Mems, only the DependParam is ever used such as to
	abstractly define but not get a copy of internal opencl calculations.
	*/
	protected IntFunction<T> memFactory;
	
	
	public SyMem(DependParam sy, IntFunction<T> memFactory){
		super(sy.elType, sy.size);
		this.sy = sy;
		this.memFactory = memFactory;
	}
	
	/** after calling this once, can use this.mem */
	public T mem(){
		if(mem == null) mem = memFactory.apply(sy.size);
		return mem;
	}
	
	public void put(float[] a){
		if(mem() instanceof FloatBuffer){
			((FloatBuffer)mem).position(0);
			((FloatBuffer)mem).put(a);
		}else{
			throw new UnsupportedOperationException();
		}
	}

}
