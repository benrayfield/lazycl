/** Ben F Rayfield offers this software opensource MIT license */
package mutable.compilers.opencl.lwjgl;
import static mutable.util.Lg.*;
import org.lwjgl.BufferUtils;
//import static mutable.listweb.todoKeepOnlyWhatUsingIn.humanaicore.common.CommonFuncs.*;
//import java.util.regex.Pattern;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
//import org.pushingpixels.substance.internal.utils.SubstanceStripingUtils;
import org.lwjgl.opencl.CLMem;

import immutable.opencl.OpenCL;
import immutable.util.HashUtil;
//import immutable.compilers.opencl.Mem;
//import immutable.forestop.impl.OpenclMem;
import immutable.util.Text;
import mutable.dependtask.DependOp;
import mutable.dependtask.DependParam;
import mutable.dependtask.LockPar;
import mutable.dependtask.ParallelOp;
import mutable.dependtask.SyMem;
import mutable.dependtask.mem.FSyMem;
import mutable.dependtask.mem.Mem;
//import mutable.listweb.todoKeepOnlyWhatUsingIn.humanaicore.common.MathUtil;
//import mutable.listweb.todoKeepOnlyWhatUsingIn.humanaicore.common.Rand;
//import mutable.util.MultiPool;
//import mutable.util.ui.ScreenUtil;
//import mutable.util.ui.StretchVideo;
import mutable.util.MultiPool;

public class LwjglOpenCL implements OpenCL{
	private LwjglOpenCL(){}
	
	//TODO merge the Lwjgl and LwjglOpenCL classes.
	
	private static OpenCL instance;
	public static OpenCL instance(){
		if(instance == null){ //doubleCheckedLocking optimization avoids synchronized in most calls
			synchronized(LwjglOpenCL.class){
				if(instance == null){
					instance = new LwjglOpenCL();
				}
			}
		}
		return instance;
	}
	
	public String version(){ return "1.2"; }
	
	
	/** See comment in OpenCL.callOpencl */
	public synchronized Object[] callOpencl(String kernelCode, int[] globalSize, int[] localSizeOrNull, Object... params){
	//public static synchronized void callOpencl(String kernelCode, int[] ndRange, Object[] paramsRead, Object[] paramsWrite){
		//lg("java.library.path="+System.getProperty("java.library.path"));
		try{
			return Lwjgl.instance().callOpencl(kernelCode, globalSize, params);
			//Lwjgl.instance().callOpencl(kernelCode, ndRange, paramsRead, paramsWrite);
		}catch(Throwable t){
			throw new Error("kernelCode["+kernelCode+"]", t);
		}
	}
	
	public static final MultiPool<Integer,PoolCLMem> pool = new MultiPool<Integer,PoolCLMem>(
		(Integer sizeBytes)->{
			try{
				return new PoolCLMem(sizeBytes);
			}catch(Throwable t){
				lg("MultiPool factory says close others first");
				return (PoolCLMem)null;
			}
		}
	);
	
	/**
	FIXME use PooledCLMem to key by <eltype,size> mapping to set<PooledCLMem>
	so caller doesnt have to think about the pools internals
	reacting to a DependParam not being garbcolable.
	Also I might want DependParam to be equal by
	type, size, and a random string instead of by ==.
	*/
	
	
	
	
	
	
	
	/** see comment in OpenCL.callOpenclDependnet */
	public SortedMap<DependParam,Mem> callOpenclDependnet(
		Map<DependParam,Mem> ins,
		Set<DependOp> tasks,
		Set<DependParam> outs
	){
		
		//TODO optimize using "Device partitioning: the ability to partition a device into sub-devices so that
		//work assignments can be allocated to individual compute units. This is useful for reserving areas of the
		//device to reduce latency for time-critical tasks." it says in https://en.wikipedia.org/wiki/OpenCL#OpenCL_1.2
		//which may take the form of multiple CLQueue and depend edges making one wait on another,
		//especially using many CLQueue of 1 DependOp each, or something like that,
		//but I'm skeptical of that working reliably as some GPUs might have to all threads have to
		//run the same CompiledKernel? the opencl1.2 wikipedia page says it can be done,
		//so TODO those experiments. For example, half the gpu cores doing matmul while the other half do mandelbrot.
		
		if(tasks.isEmpty()) lg("WARNING: no DependOps in callOpenclDependnet");
		todo("TODO upgrade callOpenclDependnet to use multiple CLQueue to run multiple kernels in parallel when dependnet allows and when theres enough gpu threads. As of 2020-5-4 it looks at Set<DependOp> and chooses a sequence, only doing 1 kernel at a time, and a kernel can have many gpu threads, but sometimes a kernel has less gpu threads than the hardware supports or could be optimized by doing multiple kernels in parallel for some other reason.");
		
		//TODO redesign so the mem is not connected to the DependParam since thats done in OpenclUtil.callOpenclDependnet
		
		//FIXME maybe callOpencl works and this doesnt cuz callOpencl has an input array for every output array
		//as it takes Object[] and returns Object[] same size.
		//What is it doing in the case of TestOpencl.simplestTestWriteThreadId?
		//Is it creating a FloatBuffer for that float[1000]? Is it creating a CLMem?
		//Is it the same CLMem as it outputs to? Is it the same FloatBuffer for input as it outputs to?
		//Is it READONLY and COPYHOSTPTR?
		//I suspect that some bug in lwjgl2 opencl is workarounded by doing those extra things
		//in callOpencl that callOpenclDependnet doesnt do.
		//Test this by doing the extra things in callOpenclDependnet too and see if it works.
		//If that doesnt work, then leave it that way and continue making the 2 funcs
		//do ever closer to the same thing until they both work,
		//to the point of making them generate the exact same log outputs except
		//for the CLMem and FloatBuffer addresses,
		///then figure out why
		//whatever extra stuff (that may not be required by opencl spec) is needed,
		//then optimize it by removing whatever I can while keeping it working,
		//then continue to dependnet order optimizations instead of it choosing a sequence.
		//
		//This test code already works for multiple kernels at once:
		//immutable.forestop.impl.TestForestOp_1ClmemPerForestnodeAndReuseClmems.main
		//WAIT, it did the kernel calls, but did I test the result?
		//2020-4-3-230p modified that code and confirmed its data flowing all the way through
		//and reads it back to java at end:
		//"START: firstNodeStates[5]=0.16533566"
		//"END: totalCycles=3330 cyc/sec=1570.3532032905046"
		//and "nodeStatesBuf.get(5)=30.195332"
		//added 1.001 thru 30 kernels and did 1570 total kernels per second.
		//0.16533566+30*1.001=30.19533566. Its output is 30.195332.
		//
		//It also does a copyhostptr at the start but multiple kernels after that
		//before returning to java. Theory: existence of a single CLMem
		//created by copyhostptr, which is a dependency in the forest of opencl calls,
		//is required in lwjgl2 opencl (or at least on this old computer)
		//for it to do some boot code for connecting between opencl memory and java memory,
		//and if I just put such a dependency in then it will work.
		//TODO test that theory, first by removing the copyhostptr
		//and verifying it still gets a nonzero result.
		
		//FIXME I might need to use nonnull event_wait_list and event
		//as in https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/clEnqueueReadBuffer.html
		
		//TODO test this, but first test MultiPool
		
		/*TODO optimize: can this be done without synchronized (probably yes but needs
		multiple CLQueue etc and more testing),
		so can queue even more opencl kernels
		at once from multiple java threads that find more GPU work to do before the
		other GPU work finishes? Probably, but just to be safe I'm starting this
		as synchronized.
		I put the synchronized here instead of on the function,
		to not imply it has to be implemented that way.
		FIXME Until removed this synchronized, careful about other uses
		of Lwjgl class that dont synchronize.
		*/
		synchronized(Lwjgl.class){
			
			//pool.alloc(1000); //FIXME remove this
			
			
			//XXXX
			//PoolCLMem testPoolCLMem = null;
			
			//CL10.clEnqueueBarrier(Lwjgl.instance().queue());
			
			//TODO optimize: taskSequence should instead be done in parallel in opencl
			//using multiple CLCommandQueue with dependnet constraints between them,
			//but for now a single CLCommandQueue.
			List<DependOp> taskSequence = anySequenceOf_dependnetOp(tasks);
			
			PointerBuffer globalWorkSize = null; //FIXME also pool these? Or is it low lag to alloc?
			PointerBuffer localWorkSize_orNull = null; //FIXME also pool these? Or is it low lag to alloc?
			
			//Find DependParams. These are just symbols.
			Set<DependParam> dependParamsSet = new HashSet();
			dependParamsSet.addAll(ins.keySet());
			for(DependOp task : tasks){
				for(LockPar p : task.params){
					dependParamsSet.add(p.dp);
				}
			}
			dependParamsSet.addAll(outs);
			
			//Get CLMems from pool, 1 for each DependParam, 
			//but any of the correct size can be used.
			//java memory is copied into some of them, others do internal calculations,
			//then some are copied back to java memory.
			Map<DependParam,PoolCLMem> dpToPoolclmem = new HashMap();
			List<DependParam> dependParamsList = new ArrayList(dependParamsSet);
			lg("dependParamsList "+dependParamsList);
			List<Integer> dependParamsSizes = new ArrayList();
			for(DependParam dp : dependParamsList){
				if(dp.numOrNull == null){ //if its a Number, it goes in opencl ndrange kernel params as literal not CLMem
					dependParamsSizes.add(dp.size);
				}else{
					dependParamsSizes.add(null); //align to poolclmemsList
				}
			}
			List<Integer> dependParamsSizesInBytes = new ArrayList(dependParamsSizes);
			for(int i=0; i<dependParamsSizesInBytes.size(); i++){
				Integer dpSize = dependParamsSizesInBytes.get(i);
				if(dpSize != null){
					int primitiveSizeInBytes = 4; //FIXME this is size of float and int. get from DependParam to be more general.
					dependParamsSizesInBytes.set(i,primitiveSizeInBytes*dpSize);
				}
			}
			lg("dependParamSizes "+dependParamsSizes);
			//List<PoolCLMem> poolclmemsList = null;
			List<PoolCLMem> poolclmemsList = pool.alloc(dependParamsSizesInBytes);
			for(int i=0; i<dependParamsList.size(); i++){
				DependParam dp = dependParamsList.get(i);
				PoolCLMem pm = poolclmemsList.get(i);
				if(pm != null){ //null if param is a Number. Else observed it allocating a 1 byte CLMems for those.
					lg("dpToPoolclmem.put "+dp+" "+pm);
					dpToPoolclmem.put(dp, pm);
					//testPoolCLMem = pm; //FIXME
				}
			}
			
			//schedule opencl to copy primitive arrays etc into CLMems, for Map<DependParam,Object> ins
			lg("ins.size "+ins.size());
			for(Map.Entry<DependParam,Mem> entry : ins.entrySet()){
				DependParam dp = entry.getKey();
				Mem mem = entry.getValue();
				lg("In dp: "+dp);
				lg("In mem: "+mem);
				if(mem instanceof DependParam){
					lg("Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: "+mem);
				}else{
					if(!(mem instanceof SyMem)){
						throw new Error("TODO");
					}
					CLMem clmem = dpToPoolclmem.get(dp).mem;
					Object buf = ((SyMem)mem).mem();
					if(buf instanceof Buffer){
						((Buffer)buf).rewind();
						//TODO Lwjgl.instance().enqueueCopyBufferToCLMem((Buffer)buf, clmem);
						if(buf instanceof FloatBuffer){
							Lwjgl.instance().enqueueCopyFloatbufferToCLMem((FloatBuffer)buf, clmem);
						}else{
							throw new RuntimeException("TODO");
						}
					}else{
						throw new RuntimeException("TODO");
					}
				}
				
				
				/*FloatBuffer buf = ((FSyMem)mem).mem();
				buf.rewind();
				Lwjgl.instance().enqueueCopyFloatbufferToCLMem(buf, clmem);
				*/
			}
			
			//CL10.clEnqueueBarrier(Lwjgl.instance().queue());
			
			//schedule opencl to do taskSequence
			lg("taskSequence.size "+taskSequence.size());
			for(DependOp t : taskSequence){
				if(t instanceof ParallelOp){
					ParallelOp p = (ParallelOp)t;
					//String expectLang = "openclNdrangeKernel";
					String expectLang = "opencl1.2";
					if(!p.lang().equals(expectLang)) throw new Error("Not "+expectLang+": "+p);
					String kernelCodeStartingAtLparen = p.code(); //(global float* bdOut, int const bSize, int const cSize ... }
					String kernelCode = "kernel void "+deterministicKernelName(kernelCodeStartingAtLparen)+kernelCodeStartingAtLparen;
					CompiledKernel ck = null;
					try{
						ck = Lwjgl.instance().compiledOrFromCache(kernelCode);
					}catch(org.lwjgl.opencl.OpenCLException e){
						throw new RuntimeException("kernelCode="+kernelCode, e);
					}
					
					//int[] ndRange = new int[]{t.forkSize};
					int[] ndRangeGlobal = t.forkSize.globalToIntArray();
					int[] ndRangeLocal_orNull = t.forkSize.localToIntArrayOrNull();
					if(ndRangeLocal_orNull != null && ndRangeGlobal.length != ndRangeLocal_orNull.length) throw new RuntimeException(
						"Diff global and local num of dims");
					globalWorkSize = BufferUtils.createPointerBuffer(ndRangeGlobal.length);
					localWorkSize_orNull = ndRangeLocal_orNull!=null ? BufferUtils.createPointerBuffer(ndRangeLocal_orNull.length) : null;
					//FIXME free globalWorkSize
					for(int n=0; n<ndRangeGlobal.length; n++){
						lg("globalWorkSize.put "+n+" "+ndRangeGlobal[n]);
						globalWorkSize.put(n, ndRangeGlobal[n]);
					}
					for(int paramIndex=0; paramIndex<t.params.size(); paramIndex++){
						DependParam param = t.params.get(paramIndex).dp;
						if(param.numOrNull != null){ //param is a Number
							Number n = param.numOrNull;
							lg("set kernel param "+paramIndex+" to Number "+n);
							if(n instanceof Integer) ck.kernel.setArg(paramIndex, (int)n);
							else if(n instanceof Float) ck.kernel.setArg(paramIndex, (float)n);
							else if(n instanceof Double) ck.kernel.setArg(paramIndex, (double)n);
							else throw new Error("TODO type "+p.getClass().getName());
						}else{ //param is a CLMem
							
							//XXXX
							
							/*
							//boolean usePoolCorrectly = Rand.strongRand.nextBoolean(); //FIXME remove this. its only for testing.
							//lg("usePoolCorrectly="+usePoolCorrectly);
							//FIXME works only when !usePoolCorrectly. Why?
							boolean usePoolCorrectly = false;
							
							if(poolclmemsList == null){
								if(usePoolCorrectly){
									poolclmemsList = pool.alloc(dependParamsSizes);
								}else{
									poolclmemsList = Arrays.asList(pool.test(param.byteSize()));
								}
								lg("usePoolCorrectly="+usePoolCorrectly+" poolclmemsList="+poolclmemsList);
								//usePoolCorrectly=true poolclmemsList=[[PoolCLMem id=1586105031284615276 bytes=1000 clmem=CLMem pointer (0x6313AB50)]]
								//usePoolCorrectly=false poolclmemsList=[[PoolCLMem id=1586105049781571534 bytes=4000 clmem=CLMem pointer (0x62581280)]]
								//Problem appears to be its not multiplying byte size by primitiveSizeInBytes.
								for(int i=0; i<dependParamsList.size(); i++){
									DependParam dp = dependParamsList.get(i);
									PoolCLMem pm = poolclmemsList.get(i);
									if(pm != null){ //null if param is a Number. Else observed it allocating a 1 byte CLMems for those.
										lg("dpToPoolclmem.put "+dp+" "+pm);
										dpToPoolclmem.put(dp, pm);
										testPoolCLMem = pm; //FIXME
									}
								}
							}*/
							
							
							//testPoolCLMem = new PoolCLMem(param.byteSize());
							CLMem cm = dpToPoolclmem.get(param).mem;
							//CLMem cm = testPoolCLMem.mem; //FIXME
							//testPoolCLMem = new PoolCLMem(param.byteSize());
							//testCLMem = Lwjgl.instance()
							//	.newClmemReadableAndWritable(param.byteSize()); //FIXME
							//CLMem cm = testPoolCLMem.mem;
							
							//if(ck == null) ck = Lwjgl.instance().compiledOrFromCache(kernelCode); //FIXME
							
							lg("set kernel param "+paramIndex+" to CLMem "+cm);
							ck.kernel.setArg(paramIndex, cm);
						}
					}
					lg("clEnqueueNDRangeKernel for "+t);
					/*CL10.clEnqueueNDRangeKernel(
						Lwjgl.instance().queue(),
						ck.kernel,
						ndRange.length, null, globalWorkSize, null, null, null);
					*/
					CL10.clEnqueueNDRangeKernel(
						Lwjgl.instance().queue(),
						ck.kernel,
						ndRangeGlobal.length, null, globalWorkSize, localWorkSize_orNull, null, null);
				}else{
					throw new Error("Not a ParallelOp: "+t);
				}
				
				//CL10.clEnqueueBarrier(Lwjgl.instance().queue());
			}
			
			//CL10.clEnqueueBarrier(Lwjgl.instance().queue());
			
			//schedule opencl to copy CLMems to Buffer (then to primitive arrays),
			//but only for those in Set<DependParam> outs so dont waste it on middle steps.
			SortedMap<DependParam,Mem> ret = new TreeMap();
			lg("outs.size "+outs.size());
			for(DependParam dp : outs){
				if(dp.elType != float.class) throw new Error("TODO");
				Mem mem = new FSyMem(dp);
				ret.put(dp,mem);
				
				//XXXX
				//CLMem clmem = testPoolCLMem.mem; //FIXME
				CLMem clmem = dpToPoolclmem.get(dp).mem;
				//CLMem clmem = testPoolCLMem.mem; //FIXME
				FloatBuffer buf = ((FSyMem)mem).mem();
				buf.rewind();
				//FIXME if its written as input then read as output, would need to rewind twice,
				//but can only do it once before opencl starts,
				//so must only allow a FloatBuffer to be used once in callOpenclDependnet,
				//IF thats whats causing it to return all 0s as of 2020-4-3-11a.
				Lwjgl.instance().enqueueCopyClmemToFloatbuffer(clmem, buf);
			}
			
			//CL10.clEnqueueBarrier(Lwjgl.instance().queue());
			
			//do the queued work in GPU, all at once before returning to CPU, for extremely lower lag.
			lg("clFinish");
			CL10.clFinish(Lwjgl.instance().queue());
			
			for(PoolCLMem p : dpToPoolclmem.values()){
				//FIXME for testing, freeing the CLMem here
				//lg("clReleaseMemObject "+p.mem);
				//CL10.clReleaseMemObject(p.mem);
				//gives back to pool pool.free(p);
				lg("Returning to pool: "+p);
				pool.free(p);
				//removes from pool pool.close(p);
			}
			
			if(globalWorkSize != null){
				lg("FIXME does this need special code to free its mem?: "+globalWorkSize);
			}
			
			//FIXME release to MultiPool

			return Collections.unmodifiableSortedMap(ret);
		}
	}
	
	public static String deterministicKernelName(String kernelCodeStartingWithLparen){
		byte[] hash = HashUtil.sha3_256(Text.stringToBytes(kernelCodeStartingWithLparen));
		return "cl"+Text.bytesToHex(hash); //TODO base58? Either way, still needs a prefix in case starts with number.
		
	}
	
	public static void test_callOpenclDependnet(){
		throw new Error("TODO");
	}
	
	/** FIXME make this true after verify it works as false which uses "callOpencl(String kernelCode, int[] ndRange, Object... params)". */
	public static final boolean optimizeForestOpsByMultipleOpenclKernelsBeforeReturningToJava = false;
	//public static final boolean optimizeForestOpsByMultipleOpenclKernelsBeforeReturningToJava = true;
	
	/*static class Mem implements Closeable{
		public CLMem clmem;
		public final int byteSize;
		public Mem(int byteSize){
			this(
				Lwjgl.instance().newClmemReadableAndWritable(byteSize),
				byteSize
			);
		}
		public Mem(CLMem clmem, int byteSize){
			this.clmem = clmem;
			this.byteSize = byteSize;
		}
		public void close(){
			if(clmem != null) CL10.clReleaseMemObject(clmem);
			clmem = null;
		}
		protected void finalize(){
			close();
		}
	}*/
	
	//protected static final HashMap<Integer,Set<PoolCLMem>> reuseMems = new HashMap();
	
	/** key is ForestOp or DependParam. *
	protected static final WeakHashMap<Object,CLMem> reuseMems = new WeakHashMap();
	
	protected static synchronized CLMem mem(Object key, int typedSize){
		CLMem m = reuseMems.get(key);
		Class elType;
		if(key instanceof ForestOp){
			elType = ((ForestOp)key).ret;
			if(!elType.isArray()) throw new Error("ForestOp is not returning array");
			elType = elType.getComponentType();
		}else{
			elType = ((DependParam)key).elType;
		}
		int byteSize;
		if(elType == float.class) byteSize = 4*typedSize;
		else throw new Error("TODO elType "+elType);
		if(m == null){ //|| m.size != typedSize){
			m = Lwjgl.instance().newClmemReadableAndWritable(byteSize);
			reuseMems.put(key, m);
		}
		return m;
	}*/
	
	/** key is ForestOp or DependnetParam *
	protected static final WeakHashMap<Object,Mem> reuseMems = new WeakHashMap();
	
	/** each Mem is only to be used by 1 thread at a time.
	Reuses Mem for same ForestOp or DependnetParam until that is garbcoled.
	*
	protected static Mem mem(ForestOp f, int byteSize){
		Mem m = reuseMems.get(f);
		if(m == null || m.byteSize != byteSize){
			m = new Mem(byteSize);
			reuseMems.put(f, m);
		}
		return m;
	}*/
	
	/** ins.values() are, for example, float[][], float[], Float, int[][], int[], Integer.
	TODO throws if any of ins isnt reachable from outs or if any leaf is reachable from outs thats not in ins.
	If the ForestOp.nonsandboxedLangColonCode's are written correctly (which is not verified), does not modify
	any of the ins values or have any external effect except returning Map<ForestOp,Object>
	and its values should not be modified either. This software often uses arrays as
	immutable (after returned the first time) despite technically being able to modify their contents.
	<br><br>
	TODO optimize. This isnt 
	*
	public static synchronized Map<ForestOp<?>,Object> callOpenclForest(
			Set<ForestOp<?>> outs, Map<ForestOp<?>,Object> ins){
		if(optimizeForestOpsByMultipleOpenclKernelsBeforeReturningToJava){
			//TODO even if do this, still would be faster to use
			//multiple CLQueue so can do multiple opencl kernels in parallel.
			
			throw new Error("TODO");
			/*Map m = new HashMap();
			List<ForestOp> sequence = anySequence(outs, ins);
			for(ForestOp op : sequence){
				if(ins.containsKey(op)){
					if(!op.lang().equals("ForestOpLeaf")) throw new Error("Can only input at type ForestOpLeaf (code() should be empty string) but type is "+op.lang());
					m.put(op, ins.get(op));
				}else{
					if(!op.lang().equals("opencl")) throw new Error(
						"Lang must be opencl but is "+op.lang());
					boolean[] openclWritesParams = openclWritesParams(op.code());
					int kernelParams = openclWritesParams.length;
					for(int i=0; i<kernelParams; i++){
						if(i==0){
							if(!openclWritesParams[i]) throw new Error("In ForestOp, first opencl ndrange kernel param must be WRITE ONLY.");
						}else{
							if(openclWritesParams[i]) throw new Error("In ForestOp, all except first opencl ndrange kernel param must be READ ONLY.");
						}
					}
					
					TODO
					
					throw new Error("TODO");
				}
			}
			
			throw new Error("TODO");
			
			//throw new Error("TODO implement in multiple CLMem objects and multiple CLQueues with dependnet between them (forest async in parallel, with some parts required before other parts, not sequence)");
			//TODO use this API, and reuse CLMems of the same size, which are all CL_MEM_READ_WRITE,
			//between multiple calls but within the same call of the same forest
			//each ForestOp gets its own CLMem.
			//Then use this to build a Gru neuralnet
			//similar to how I got recurrentjava to learn 5 recordings of mouse movements in parallel.
			//Should the remembering of CLMems use SoftReference? Probably not cuz java doesnt
			//see all their memory. Could do WeakHashMap<ForestOp,CLMem> but would have to remake them
			//if the size changes.
		}else{
			Map m = new HashMap();
			List<ForestOp> sequence = anySequence(outs, ins);
			for(ForestOp op : sequence){
				if(ins.containsKey(op)){
					if(!op.lang().equals("ForestOpLeaf")) throw new Error("Can only input at type ForestOpLeaf (code() should be empty string) but type is "+op.lang());
					m.put(op, ins.get(op));
				}else{
					//if(!op.lang().equals("NonsandboxedOpenclNdrangeKernel")) throw new Error("Lang must be NonsandboxedOpenclNdrangeKernel but is "+op.lang());
					if(!op.lang().equals("opencl")) throw new Error("Lang must be opencl but is "+op.lang());
					
					boolean[] openclWritesParams = openclWritesParams(op.code());
					int kernelParams = openclWritesParams.length;
					for(int i=0; i<kernelParams; i++){
						if(i==0){
							if(!openclWritesParams[i]) throw new Error("In ForestOp, first opencl ndrange kernel param must be WRITE ONLY.");
						}else{
							if(openclWritesParams[i]) throw new Error("In ForestOp, all except first opencl ndrange kernel param must be READ ONLY.");
						}
					}
					
					//The int[] ndRange are the last n of op.childs. Before that are the opencl ndrange kernel params,
					//except the first op.child and first kernel param are about the return.
					//The first op.child tells the size of that array to return. The kernel param is that array.
					//childs = kernelParams+dims
					int[] ndRange = new int[op.childs.size()-kernelParams];
					if(ndRange.length != 1) throw new Error("TODO support up to 3d addressing (as in get_global_id(0) to (2), as opencl spec says 3d is max), but for now it must be 1d, but tried to be "+ndRange.length+"d");
					
					Object[] params = new Object[kernelParams];
					for(int p=0; p<params.length; p++){
						if(p==0){ //return into this array
							//TODO create array of type op.ret, but what size?
							//Its size is retsPerThread*ndRange[0] but I havent defined retsPerThread yet.
							//Or maybe I should just have a param for returned array size, like at op.childs.get(1).
							//Do I want that array to support more than 1 dim? At end I could alternate ndRange[0] retsPerThread[0] ndRange[1]...
							//Do I really plan to allow 2d and 3d arrays (as opencl spec allows up to 3d)? This would be simpler if always 1d.
							//Or retsPerThread could be viewed as the smallest dim in ndRange so it would support 2d-4d arrays (instead of 1d-3d),
							//but when converted to java would I want the array to be 1 less dim?
							//SOLUTION: To keep things more general than that, I define op.childs.get(1) to be size of (return into) op.childs.get(0),
							//so all others slide over 2 instead of 1.
							
							Class c = op.ret;
							while(c.isArray()) c = c.getComponentType();
							if(ndRange.length != 1) throw new Error("TODO");
							//op.childs are: returnArraySize, kernel read param 0, kernel read param1..., ndRange[0], ndRange[1]...
							int len = (Integer) m.get(op.childs.get(0));
							params[p] = Array.newInstance(c, len);
						}else{ //param to read
							params[p] = m.get(op.childs.get(p));
						}
					}
					for(int d=0; d<ndRange.length; d++){
						Object o = m.get(op.childs.get(kernelParams+d));
						if(!(o instanceof Integer)) throw new Error("ndRange param at end of op.childs (after opencl kernel params) is not Integer. It is "+o);
						ndRange[d] = (Integer) o;
					}
					
					//LWJGL, at least on my old computer as of Y2019, only supports 1d aka get_global_id(0) but not (1) or (2)
					//as opencl spec allows up to 3d addressing. We pay for this by using / and % to compute
					//as many dims as we want, such as matmul uses 1d array as 2d, which maybe makes matmul about half as efficient
					//in LWJGL (c++ called from java) compared to AMD c++ sample code,
					//which may be cuz AMD sample code (which supposedly runs on any opencl compatible hardware)
					//uses float4 instead of float1 or may be cuz of the / and %
					//or may be cuz of some inefficiency in LWJGL.
					//"callOpencl(String kernelCode, int[] ndRange, Object... params)" automatically converts
					//float[][] to float[] and back to float[][] if its written (else the return includes the original float[][]).
					
					
					//This is the inefficient part compared to optimizeForestOpsByMultipleOpenclKernelsBeforeReturningToJava
					//cuz this returns to java after every opencl ndrange kernel call instead of doing them all in opencl
					//in multiple CLMem and multiple CLQueue with dependent async order before returning to java.
					Object[] openclReturned = callOpencl(op.code(), ndRange, params);
					
					//TODO verify only first param in the opencl ndrange kernel code can be written.
					//The others in openclReturned are same as in params array.
					Object forestOpReturned = openclReturned[0];
					lg("ForestOp "+op+" returned "+forestOpReturned+". Its ndRange[0] is "+ndRange[0]+" and its lang:code is: "+op.nonsandboxedLangColonCode);
					if(!op.ret.isAssignableFrom(forestOpReturned.getClass())) throw new Error(
						"expected type "+op.ret+" but got "+forestOpReturned+" from "+op);
					
					m.put(op, forestOpReturned);
				}
			}
			Map ret = new HashMap();
			for(ForestOp op : outs){
				ret.put(op, m.get(op));
			}
			return Collections.unmodifiableMap(ret);
		}
	}*/
	
	/*public static void test_callOpenclForest(){
		//TODO implement n steps of conwaysgameoflife (which opencl image funcs are probably more efficient for than ndrange,
		//but so far this software only supports ndrange).
		String n = "\n";
		String conwaylifeKernel =
			"opencl:kernel void conwaylife(global float* out, int const height, int const width, global const float* in){"+n
			+"	int id = get_global_id(0);"+n
			+"	const int y = id/width;"+n
			+"	const int x = id%width;"+n
			+"	const int isEdge = max((y==0)+(y==height-1),(x==0)+(x==width-1));"+n
			+"	float here = in[id]; //0 or 1"+n
			+"	//0 to 8"+n
			+"	float end = height*width-1;"+n
			//+"	float a = 1-in[id];"+n //FIXME
			//+"	float a = 0;//in[max(0,min(id-width-1,end))]; //TODO could instead return an array size (height-2)*(width-2), wouldnt need min and max addressing"+n
			//+"	float b = 0;//in[max(0,min(id-width,end))];"+n
			//+"	float c = 0;//in[max(0,min(id-width+1,end))];"+n
			//+"	float d = 0;//in[max(0,min(id-1,end))];"+n
			//+"	float e = 0;//in[max(0,min(id+1,end))];"+n
			//+"	float f = 0;//in[max(0,min(id+width,end))];"+n
			//+"	float g = 0;//in[max(0,min(id+width+1,end))];"+n
			
			+"	float a = in[id-width-1]; //TODO could instead return an array size (height-2)*(width-2), wouldnt need min and max addressing"+n
			+"	float b = in[id-width]; //FIXME these go outside range of in[]. https://www.reddit.com/r/gpgpu/comments/ctye2l/is_it_ok_for_an_opencl_ndrange_kernel_to_try_to/"+n
			+"	float c = in[id-width+1];"+n
			+"	float d = in[id-1];"+n
			+"	float e = in[id+1];"+n
			+"	float f = in[id+width];"+n
			+"	float g = in[id+width+1];"+n
			+"	float sumAdjacent = a+b+c+d+e+f+g;"+n
			+"	out[id] = (1-isEdge)*((here*(sumAdjacent==2)) + (sumAdjacent==3)); //== returns 0 or 1 (or -1 if NaN etc, but that wont happen)"+n
			//+"	out[id] = (here*(sumAdjacent==2)) + (sumAdjacent==3); //== returns 0 or 1 (or -1 if NaN etc, but that wont happen)"+n
			//+"	out[id] = sumAdjacent;"+n //FIXME
			//+"	out[id] = 1-in[id];"+n //FIXME
			//+"	out[id] = in[id]*2;"+n
			+"}";
		ForestOp leaf = new ForestOp(float[].class);
		
		int height = 200, width = 150;
		ForestOp heightOp = new ForestOp(Integer.class);
		ForestOp widthOp = new ForestOp(Integer.class);
		ForestOp ndrange_dim0_op = new ForestOp(Integer.class);
		Map ins = new HashMap();
		ins.put(heightOp, height);
		ins.put(widthOp, width);
		ins.put(ndrange_dim0_op, height*width);
		
		float[] firstConwayState = new float[height*width];
		for(int i=0; i<firstConwayState.length; i++){
			if(MathUtil.weightedCoinFlip(.3)) firstConwayState[i] = 1;
		}
		ScreenUtil.testDisplayWithoutExitOnClose(new StretchVideo(array1dTo2d(firstConwayState,height)));
		ins.put(leaf, firstConwayState);
		ins = Collections.unmodifiableMap(ins);

		ForestOp top = leaf;
		for(int i=0; i<10000; i++){
		//for(int i=0; i<1; i++){ //TODO bigger loop
			top = new ForestOp(float[].class, conwaylifeKernel, ndrange_dim0_op, heightOp, widthOp, top, ndrange_dim0_op);
		}
		Set outs = Collections.unmodifiableSet(new HashSet(Arrays.asList(top)));
		Map<ForestOp,Object> openclReturned = callOpenclForest(outs, ins);
		float[] lastConwayState = (float[]) openclReturned.get(top);
		ScreenUtil.testDisplayWithoutExitOnClose(new StretchVideo(array1dTo2d(lastConwayState,height)));
	}*/
	
	public static List<DependOp> anySequenceOf_dependnetOp(Set<DependOp> tasks){
		List<DependOp> doneList = new ArrayList();
		Set<DependOp> doneSet = new HashSet();
		Set<DependOp> remain = new HashSet(tasks);
		//TODO optimize this is bigO of squared, but since its only expected to be called on at msot 100 at a time its ok
		while(!remain.isEmpty()){
			Iterator<DependOp> iter = remain.iterator();
			boolean removed = false;
			while(iter.hasNext()){
				DependOp d = iter.next();
				if(doneSet.containsAll(d.depends)){
					doneSet.add(d);
					doneList.add(d);
					iter.remove();
					removed = true;
				}
			}
			if(!removed){
				throw new Error("dependnet has cycle");
			}
		}
		return doneList;
	}
	
	/*static List<ForestOp> anySequence(Set<ForestOp<?>> outs, Map<ForestOp<?>,Object> ins){
		List<ForestOp<?>> list = new ArrayList();
		list.addAll(ins.keySet());
		Set<ForestOp<?>> added = new HashSet(list);
		for(ForestOp<?> o : outs) dependnetAdd(o, list, added);
		return Collections.unmodifiableList(list);
	}
	
	static void dependnetAdd(ForestOp<?> o, List<ForestOp<?>> list, Set<ForestOp<?>> added){
		if(!added.contains(o)){
			for(ForestOp<?> c : o.childs) dependnetAdd(c, list, added);
			list.add(o);
		}
	}*/
	
	
	public static String findKernelName(String kernelCode){
		int firstLparen = kernelCode.indexOf('(');
		if(firstLparen == -1) throw new Error("No lparen in "+kernelCode);
		String endsWithName = kernelCode.substring(0,firstLparen).trim();
		String[] tokens = endsWithName.split("\\s+");
		return tokens[tokens.length-1];
	}
	
	/** whats between the first ( and ).
	Example: "global const float* a, global const float* b, global float* result, int const size"
	*/
	public static String getParamsString(String kernelCode){
		int start = kernelCode.indexOf('(');
		if(start == -1) throw new Error("No ( found so must not be opencl kernel code: "+kernelCode);
		int end = kernelCode.indexOf(')',start);
		if(end == -1) throw new Error("No ) found after the ( so must not be opencl kernel code: "+kernelCode);
		return kernelCode.substring(start+1, end).trim();
	}
	
	public static int countIntParams(String kernelCode){
		return count("int ", getParamsString(kernelCode));
	}
	
	/** Includes params that are read and written */ 
	public static int countFloat1dParams(String kernelCode){
		return count("float* ", getParamsString(kernelCode));
	}
	
	public static List<String> getParamNames(String kernelCode){
		String p = getParamsString(kernelCode);
		List<String> ret = new ArrayList();
		for(String s : p.trim().split(",")){
			ret.add(Text.lastWhitespaceDelimitedToken(s));
		}
		return Collections.unmodifiableList(ret);
	}
	
	/** Example types: float* float int* int double* double.
	<br><br>
	FIXME it did this:
	(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd)
	-> [float*, const, const, const, float*, float*]*/
	public static List<String> getParamTypes(String kernelCode){
		String p = getParamsString(kernelCode);
		List<String> ret = new ArrayList();
		for(String s : p.trim().split(",")){
			String[] tokens = Text.splitByWhitespaceNoEmptyTokens(s);
			String type = null;
			for(String token : tokens){
				if(!token.equalsIgnoreCase("global") && !token.equalsIgnoreCase("const")){
					type = token;
					break;
				}
			}
			if(type == null) throw new RuntimeException("Couldnt find type in: "+s);
			//ret.add(tokens[tokens.length-2]); //Example: get "float*" from "global const float* b"
			ret.add(type);
		}
		return Collections.unmodifiableList(ret);
	}
	
	//static final Pattern splitComma = Pattern.compile("\\s*\\,\\s*");
	
	//static final Pattern splitWhitespace = Pattern.compile("\\s+");
	
	/** Every param is either read only or write only. FIXME Is that always true?
	Array is same size as number of params.
	*/
	public static boolean[] openclWritesParams(String kernelCode){
		String s = getParamsString(kernelCode); //is trimmed
		String[] sa = s.split("\\s*\\,\\s*");
		boolean[] write = new boolean[sa.length];
		for(int p=0; p<sa.length; p++){
			String[] paramTokens = sa[p].trim().split("\\s+"); //Example:
			//Example: ["global","const","float*","a"]
			//Example: ["global","float*","result"]
			//Example: ["int","const","size"]
			//boolean foundGlobal = false, foundLocal = false, foundConst = false;
			boolean foundConst = false;
			for(String token : paramTokens){
				//FIXME Are __global and __const also valid keywords? What are all the relevant keywords and alias of them?
				//if("global".equals(token)) foundGlobal = true;
				//if("local".equals(token)) foundLocal = true;
				if("const".equals(token)) foundConst = true;
			}
			write[p] = !foundConst;
		}
		return write;
	}
	
	static int count(String find, String inMe){
		int count = 0, i = 0;
		while(true){
			i = inMe.indexOf(find,i);
			if(i == -1) return count;
			count++;
		}
	}
	
	public static double[] array2dTo1d(double[][] in){
		int b = in.length, c = in[0].length;
		double[] out = new double[b*c];
		for(int i=0; i<b; i++){
			System.arraycopy(in[i], 0, out, i*c, c);
		}
		return out;
	}
	
	/** returns a double[firstDim][in.length/firstDim] where in.length%firstDim==0 */
	public static double[][] array1dTo2d(double[] in, int firstDim){
		int secondDim = in.length/firstDim;
		if(firstDim*secondDim != in.length) throw new Error(in.length+" not divisible by "+firstDim);
		double[][] out = new double[firstDim][secondDim];
		for(int i=0; i<firstDim; i++){
			System.arraycopy(in, i*secondDim, out[i], 0, secondDim);
		}
		return out;
	}
	
	public static float[] array2dTo1d(float[][] in){
		int b = in.length, c = in[0].length;
		float[] out = new float[b*c];
		for(int i=0; i<b; i++){
			System.arraycopy(in[i], 0, out, i*c, c);
		}
		return out;
	}
	
	/** returns a float[firstDim][in.length/firstDim] where in.length%firstDim==0 */
	public static float[][] array1dTo2d(float[] in, int firstDim){
		int secondDim = in.length/firstDim;
		if(firstDim*secondDim != in.length) throw new Error(in.length+" not divisible by "+firstDim);
		float[][] out = new float[firstDim][secondDim];
		for(int i=0; i<firstDim; i++){
			System.arraycopy(in, i*secondDim, out[i], 0, secondDim);
		}
		return out;
	}
	
	public static String newKernelName(){
		return Text.newJibberishWord(Math.pow(2, 128));
	}
	
	/** Deterministic. Code starts with "(" just before the params and ends with "}". */
	public static String kernelName(String code){
		return "fncl"+Text.bytesToHex(HashUtil.sha3_256(Text.stringToBytes(code)));
	}
	
	/** TODO check which params are read and written in kernel code and optimize by using
	CLMem thats only readable, only writable, or both, and in java only copy those modified,
	but for now copy all params since not checking which could be modified.
	*
	public Object[] copyAllParams(Object... params){
		
	}
	
	public Object copy(Object o){
		
	}*/
	
	public static float[][] copyRectArray(float[][] a){
		int innerSize = a[0].length;
		float[][] b = new float[a.length][innerSize];
		for(int i=0; i<a.length; i++){
			System.arraycopy(a[i], 0, b[i], 0, innerSize);
		}
		return b;
	}

}