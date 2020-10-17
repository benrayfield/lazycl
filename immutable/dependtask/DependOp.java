package immutable.dependtask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.DependParam;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.LockPar;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.ParallelOp;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.ParallelSize;
import mutable.compilers.opencl.OpenclUtil;

/** Used with OpenclUtil.callOpenclDependnet. This is a node in a dependnet.
Each node has a readLock, writeLock, or readWriteLock on DependParams (which wrap Mem or Number).
It can only ReadLock a Number cuz Number is Immutable. Mem normally wraps a CLMem,
which may be used as various primitive types such as floats or ints or longs,
but as of 2020-4-19 its only floats since I'm using this system to opencl optimize RecurrentJava
and its Matrix objects (which were doubles, but I changed to floats for efficiency)
now use FSyMem which are a kind of Mem which has a DependParam and some of them
also have a FloatBuffer. Most of them dont have a FloatBuffer and only use
CLMems for internal opencl calculations. A few Matrixs will trigger lazy create of FloatBuffer
so floats can be copied into opencl, then do lots of internal calculations, then copy floats out.
Its very low lag cuz of reusing CLMems from a pool (a MultiPool field in OpenclUtil)
which otherwise are slow to allocate from LWJGL, and cuz of reusing CompiledKernel objects,
and cuz of doing multiple opencl ndrange kernels before returning to java
so much less memory has to be copied between CPU and GPU.
On an old computer I got it to run 30 kernels 50 times per second (1500 kernels/sec),
but it cant efficiently switch between CPU and GPU nearly 1500 times per second.
Its limit seems to be about 100 times per second before it starts doing much less flops.
It can switch between CPU and GPU 1500 times per second but is about 10 times less flops.
On newer computers its expected to get closer to 10000 kernels/sec.
<br><br>
Unlike ForestOp, this (TODO) is designed to allow simultaneous
reading and writing of the same CLMem in the same kernel
and can write multiple CLMems in same kernel,
and of course read multiple CLMems.
*/
public class DependOp extends ParallelOp{
	
	/** immutable List */
	public final Set<DependOp> depends;
	
	/** Immutable List. Includes wrappers of Mem and Number, each with a plan to readLock, readWriteLock, or writeLock. */
	public final List<LockPar> params;
	
	/** Index in params. -1 if not found */
	public int indexOf(DependParam d){
		for(int i=0; i<params.size(); i++) if(params.get(i).dp == d) return i;
		return -1;
	}
	
	public DependOp(String nonsandboxedLangColonCode, ParallelSize parallelSize, LockPar... params) {
		this(Collections.EMPTY_SET, nonsandboxedLangColonCode, parallelSize, params);
	}
	
	public DependOp(Set<DependOp> depends, String nonsandboxedLangColonCode, ParallelSize parallelSize, LockPar... params){
		super(nonsandboxedLangColonCode, parallelSize);
		this.depends = Collections.unmodifiableSet(new HashSet(Arrays.asList(params)));
		this.params = Collections.unmodifiableList(new ArrayList(Arrays.asList(params)));
	}
	
	public String toString(){
		return "[DependOp nonsandboxedLangColonCode="+nonsandboxedLangColonCode+" parallelSize="+parallelSize+" numDepends="+depends.size()+" param="+params+"]";
	}
	
	/** When calling OpenclUtil.callOpenclDependnet you need to give Mem objects for these (ins) */
	public static SortedSet<DependParam> dependparamsReadBeforeWritten(Set<DependOp> ops){
		List<DependOp> list = OpenclUtil.anySequenceOf_dependnetOp(ops); //any dependnet order. Will get the same result regardless of which one.
		SortedSet<DependParam> readBeforeFirstWrite = new TreeSet();
		SortedSet<DependParam> writtenYet = new TreeSet();
		for(DependOp op : list){
			for(LockPar lp : op.params){
				DependParam dp = lp.dp;
				boolean read = lp.ls.read; //may read, write, or both (read then write, actually in parallel but each index is read before first written)
				boolean write = lp.ls.write;
				if(!writtenYet.contains(dp) && read) readBeforeFirstWrite.add(dp);
				if(write) writtenYet.add(dp);
			}
		}
		return Collections.unmodifiableSortedSet(readBeforeFirstWrite);
	}
	
	/** Is either a temporary/middle calculation or the outputs you want. */
	public static SortedSet<DependParam> dependparamsWritten(Set<DependOp> ops){
		SortedSet<DependParam> written = new TreeSet();
		for(DependOp op : ops){
			for(LockPar lp : op.params){
				if(lp.ls.write) written.add(lp.dp);
			}
		}
		return Collections.unmodifiableSortedSet(written);
	}

}
