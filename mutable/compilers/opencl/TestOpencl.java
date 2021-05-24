package mutable.compilers.opencl;
import static mutable.util.Lg.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import immutable.dependtask.LockState;
import immutable.opencl.OpenCL;
//import immutable.rbm.learnloop.OpenclProgs;
import immutable.util.SetUtil;
import immutable.util.Text;
import mutable.compilers.opencl.lwjgl.LwjglOpenCL;
import mutable.dependtask.DependOp;
import mutable.dependtask.DependParam;
import mutable.dependtask.ForkSize;
import mutable.dependtask.LockPar;
import mutable.dependtask.mem.FSyMem;
import mutable.dependtask.mem.Mem;
import mutable.util.Rand;
import mutable.util.Time;

public class TestOpencl{
	
	public static OpenCL cl(){
		return LwjglOpenCL.instance();
	}
	
	public static LockPar readLock(DependParam d){
		return new LockPar(LockState.readLock, d);
	}
	
	public static LockPar writeLock(DependParam d){
		return new LockPar(LockState.writeLock, d);
	}
	
	public static LockPar readWriteLock(DependParam d){
		return new LockPar(LockState.readWriteLock, d);
	}
	
	/** TODO garbcol these */
	private static Map<Number,DependParam> numToDp = new HashMap();
	
	public static DependParam dp(Object o){
		if(o instanceof DependParam) return (DependParam)o;
		if(o instanceof Number){
			DependParam d = numToDp.get((Number)o);
			if(d == null){
				d = new DependParam("noComment_numberIs"+o, (Number)o);
				numToDp.put((Number)o, d);
			}
			return d;
		}
		throw new Error("Unknown type: "+o.getClass());
	}
	
	/** wraps Number in DependParam, or takes DependParam as it is */
	public static DependParam[] dps(Object... o){
		DependParam[] ret = new DependParam[o.length];
		for(int i=0; i<o.length; i++) ret[i] = dp(o[i]);
		return ret;
	}
	
	static final String simplestTestWriteThreadId_kernelCode =
		"kernel void "+Text.newJibberishWord()+"(global float* out){\r\n"+
		"	int id = get_global_id(0);\r\n"+
		"	out[id] = id;\r\n"+
		"}";
	
	/** the old way thats laggier for multiple kernels at once (this tes is only 1 kernel) */
	public static void simplestTestWriteThreadId_callOpenclNotDependnet(){
		int parallelSize = 1000;
		float[] sameSizeAsOut = new float[parallelSize];
		Object[] outs = cl().callOpencl(
			simplestTestWriteThreadId_kernelCode, new int[]{parallelSize}, null, sameSizeAsOut);
		float[] out = (float[]) outs[0]; //same param index as sameSizeAsOut
		lg("out[567]="+out[567]);
		lg("out[0]="+out[0]);
	}
	
	public static void simplestTestWriteThreadId(){
		lg("Starting simplestTestWriteThreadId");
		Map<DependParam,Mem> ins = new HashMap();
		Set<DependOp> tasks = new HashSet();
		Set<DependParam> outKeys = new HashSet();
		
		int parallelSize = 1000;
		DependParam outSy = new DependParam("out", float.class, parallelSize);
		
		outKeys.add(outSy);
		
		//FIXME use OpenclGraph instead of building a dependnet directly.
		
		DependOp a = new DependOp(
			Collections.EMPTY_SET, //depends
			"opencl1.2:"+simplestTestWriteThreadId_kernelCode,
			new ForkSize(parallelSize),
			writeLock(outSy)
		);
		
		tasks.add(a);
		
		Map<DependParam,Mem> outs = cl().callOpenclDependnet(ins, tasks, outKeys);
		FSyMem outMem = (FSyMem) outs.get(outSy);
		
		//outMem.mem.rewind();
		
		lg("outMem[567]="+outMem.get(567));
		lg("outMem[0]="+outMem.get(0));
	}
	
	public static void main(String[] args){
		
		//int w = 200, x = 300, y = 237, z=445;
		int w = 120, x = 130, y = 123, z=145;
		DependParam dpW = new DependParam("w",w);
		DependParam dpX = new DependParam("x",x);
		DependParam dpY = new DependParam("y",y);
		DependParam dpZ = new DependParam("z",z);
		FSyMem inputArrayWX = new FSyMem("inputArrayWX", w*x);
		FSyMem inputArrayXY = new FSyMem("inputArrayXY", x*y);
		FSyMem inputArrayYZ = new FSyMem("inputArrayYZ", y*z);
		
		Consumer<FSyMem> weakRandomizer = (FSyMem m)->{
			for(int i=0; i<m.size; i++){
				m.put(i, Rand.weakRand.nextFloat());
			}
		};
		weakRandomizer.accept(inputArrayWX);
		weakRandomizer.accept(inputArrayXY);
		weakRandomizer.accept(inputArrayYZ);
		
		//int const bSize, int const cSize, int const dSize, global const float* bc,
		//global const float* cd, global float* bdOut
		//String matmulCode = "opencl1.2:"+OpenclProgs.matmulCode1dAs2d;
		String matmulCode = "opencl1.2:(int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd, global float* bdOut){\r\n"+
			"	int bd = get_global_id(0);\r\n"+
			"	const int b = bd/dSize;\r\n"+ //TODO optimize allow get_global_id(more dims)?//
			"	const int d = bd%dSize;\r\n"+ //TODO optimize allow get_global_id(more dims)?
			//"	float sum = 200;\r\n"+ //FIXME
			"	float sum = 0;\r\n"+
			"	for(int c=0; c<cSize; c++){\r\n"+
			"		sum += bc[b*cSize+c]*cd[c*dSize+d];\r\n"+ //TODO optimize allow get_global_id(more dims)?
			"	}\r\n"+
			"	bdOut[bd] = sum;\r\n"+
			//"	bdOut[bd] = (float)dSize;\r\n"+
			//"	bdOut[bd] = (float)5;\r\n"+
			//"	bdOut[bd] = (float)bd;\r\n"+
			"}";
		
		double timeStart = Time.now();
		for(int repeat=0; repeat<10000; repeat++){
			double duration = Time.now()-timeStart;
			lg("repeat="+repeat+" callopencldependnetsPerSecond="+(repeat/duration));
		
			/*for(int i=0; i<1; i++){
				lg("");
				lg("");
				//simplestTestWriteThreadId_callOpenclNotDependnet();
				lg("");
				lg("");
				lg("");
				simplestTestWriteThreadId();
				lg("");
				lg("");
			}
			//if(1<2) return;
			*/
			
			lg("Starting TestOpencl, the OpenclUtil.testOpenclDependnet which does multiple opencl kernels before returning to java, for many times lower lag.");
			
			
			
			Map<DependParam,Mem> ins = new HashMap();
			Set<DependOp> tasks = new HashSet();
			Set<DependParam> outKeys = new HashSet();
			
			
			
			
			DependParam wySy = new DependParam("wySy", float.class, w*y);
			
			DependParam wzSy = new DependParam("wzSy", float.class, w*z);
			
			DependOp a = new DependOp(
				Collections.EMPTY_SET, //depends
				matmulCode,
				new ForkSize(wySy.size), //new ParallelSize(wySy.size),
				//(int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd, global float* bdOut)
				readLock(dpW),
				readLock(dpX),
				readLock(dpY),
				readLock(inputArrayWX.sy),
				readLock(inputArrayXY.sy),
				writeLock(wySy)
			);
			
			tasks.add(a);
			
			outKeys.add(wySy); //FIXME remove this
			
			DependOp b = new DependOp(
				SetUtil.set(a), //depends
				matmulCode,
				new ForkSize(wzSy.size), //new ParallelSize(wzSy.size),
				//(int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd, global float* bdOut)
				readLock(dpW),
				readLock(dpY),
				readLock(dpZ),
				readLock(wySy),
				readLock(inputArrayYZ.sy),
				writeLock(wzSy)
			);
			
			tasks.add(b);
			outKeys.add(wzSy);
			
			ins.put(inputArrayWX.sy, inputArrayWX);
			ins.put(inputArrayXY.sy, inputArrayXY);
			ins.put(inputArrayYZ.sy, inputArrayYZ);
			
			/*DependOp c = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);
			
			DependOp d = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);
			
			DependOp e = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);
			
	
			DependOp f = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);
			
			DependOp g = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);
			
	
			DependOp e = new DependOp(
				matmulCode,
				new DependParam[]{}, //params
				new DependOp[]{} //depends
			);*/
			
			Map<DependParam,Mem> outs = cl().callOpenclDependnet(ins, tasks, outKeys);
			
			Mem outEarlierWY = outs.get(wySy); //FIXME remove this
			//Mem outXV = outs.get(wzSy);
			Mem outEarlierWZ = outs.get(wzSy);
			
			//if(outXV instanceof SyMem){
			
				//FIXME implement (float)mutable.dependtask.SyMem.get(int)
				//such as outEarlierWY is a "java.nio.DirectByteBuffer[pos=0 lim=59040 cap=59040]" 2021-3-28
				//before the next line throws cuz havent been using get func in lazycl and this is old code
				//and probably redesigned it and this test stopped working.
				/*Use this instead[
					> node96_of_100 gpuOut[96]=2.5188996E-8 cpuOut[96]=2.5188996E-8
					> node97_of_100 gpuOut[97]=0.999992 cpuOut[97]=0.999992
					> node98_of_100 gpuOut[98]=0.99998206 cpuOut[98]=0.99998206
					> node99_of_100 gpuOut[99]=0.64454347 cpuOut[99]=0.64454347
					> firstNodeStates.bize = 3200
					> nodeStates.bize = 3200
					> testOpenclRecurrentNeuralnetNCyclesDeep(100,10) test pass.
					> Lacycl tests pass.
					> TestLazyclPrototype tests pass
					> END evalJava: immutable.lazycl.impl.TestLazyclPrototype.main(new String[0]);
				*/
				lg("outEarlierWY[567]="+outEarlierWY.get(567));
				lg("outEarlierWY[0]="+outEarlierWY.get(0));
				lg("outEarlierWZ[567]="+outEarlierWZ.get(567));
				lg("outEarlierWZ[0]="+outEarlierWZ.get(0));
				//lg("out[567]="+outXV.get(567));
			//}else{
			//	throw new Error("unknown type "+outXV.getClass());
			//}
			
			/*
			
			Calculate same on CPU then compare. also time the gpu doing it
			the second time around since first time it has to allocate mems and compile.
			
			
			TODO test OpenclUtil.callOpenclDependnet
			on a forest of at least 2 different kernel code strings.
			*/
			
		}
		
	}

}
