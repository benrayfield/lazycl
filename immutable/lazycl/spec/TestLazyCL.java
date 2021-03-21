/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import static mutable.util.Lg.*;
import java.util.Arrays;

import data.lib.fdlibm53.Fdlibm53Exp;
import immutable.opencl.OpenCL;
import immutable.util.MathUtil;
import immutable.util.Pair;
import mutable.compilers.opencl.lwjgl.LwjglOpenCL;
import mutable.util.Rand;
import mutable.util.ui.ScreenUtil;

public strictfp class TestLazyCL{
	
	/** throws if fail */
	public static void runTests(Lazycl lz){
		runTests(lz,true);
	}
	
	/** throws if fail. If !includeDoubles then only float not double, cuz old GPUs dont support double. Also floats are faster. */
	public static void runTests(Lazycl lz, boolean includeDoubles){
		
		for(int i=0; i<10; i++){
			lg("randomNormedDouble: "+randomNormedDouble());
		}
		
		for(int i=0; i<10; i++){
			long g = Rand.strongRand.nextLong();
			lg("randLong: "+MathUtil.bitsToString(g)+" "+Double.longBitsToDouble(g));
		}
		
		//works but dont download too often... testDownload(lz); //FIXME dont do this every time. Dont want to download too many times and get local address blocked. TODO robots.txt
		//testDownload(lz);
		testOpencl(lz.opencl(),includeDoubles);
		testOpenclMatmul(lz);
		if(includeDoubles){
			testDoublesInCode(lz);
			testDoublesInParams(lz);
			testDoubleLiterals(lz.opencl());
			testOpencl_matmulDouble(lz.opencl());
			testCpuAndGpuOfExponentsOfEOnDoubles(lz);
		}
		
		/*
		> Test pass: testDoublesInParams ins.d(1) should be 4.0
		> Test pass: testDoublesInParams ins.d(2) should be 5.0
		> in FloatBuffer[0]=2.125
		> in FloatBuffer[1]=0.0
		> in FloatBuffer[2]=2.25
		> in FloatBuffer[3]=0.0
		> in FloatBuffer[4]=2.3125
		> in FloatBuffer[5]=0.0
		> dependParamsList [dp_outs_1610892734599150900_double_siz3, dp_ins_1610892734599156700_double_siz3]
		> dependParamSizes [3, 3]
		> dpToPoolclmem.put dp_outs_1610892734599150900_double_siz3 [PoolCLMem id=1610892734599270600 bytes=12 clmem=CLMem pointer (0x1897BC4B260)]
		> dpToPoolclmem.put dp_ins_1610892734599156700_double_siz3 [PoolCLMem id=1610892734599276300 bytes=12 clmem=CLMem pointer (0x1897BC4B470)]
		> ins.size 1
		> In dp: dp_ins_1610892734599156700_double_siz3
		> In mem: mutable.dependtask.SyMem@57e1b0c
		> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=6 cap=6] CLMem=CLMem pointer (0x1897BC4B470)
		> taskSequence.size 1
		> globalWorkSize.put 0 3
		> set kernel param 0 to CLMem CLMem pointer (0x1897BC4B260) dp=dp_outs_1610892734599150900_double_siz3
		> set kernel param 1 to CLMem CLMem pointer (0x1897BC4B470) dp=dp_ins_1610892734599156700_double_siz3
		> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global double* outs, global const double* ins){
			int id = get_global_id(0);
			outs[id] = ins[id]*ins[id];
		} forkSize=mutable.dependtask.ForkSize@4232c52b numDepends=0 param=[mutable.dependtask.LockPar@61e0b7c8, mutable.dependtask.LockPar@a7b97583]]
		> outs.size 1
		> clEnqueueReadBuffer CLMem=CLMem pointer (0x1897BC4B260) Buffer=java.nio.DirectByteBuffer[pos=0 lim=24 cap=24], using JReflect.call...
		> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1897DBA2F80), CLMem pointer (0x1897BC4B260), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=24 cap=24], null, null]
		> clFinish
		> Returning to pool: [PoolCLMem id=1610892734599270600 bytes=12 clmem=CLMem pointer (0x1897BC4B260)]
		> Returning to pool: [PoolCLMem id=1610892734599276300 bytes=12 clmem=CLMem pointer (0x1897BC4B470)]
		> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
		Exception in thread "main" java.lang.RuntimeException: TEST FAIL: testDoublesInParams squares3 cuz 0.0 not .equals 9.0
			at immutable.lazycl.spec.TestLazyCL.testEq(TestLazyCL.java:39)
			at immutable.lazycl.spec.TestLazyCL.testDoublesInParams(TestLazyCL.java:256)
			at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:18)
			at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
		*/
		
		testOpenclRecurrentNeuralnetNCyclesDeep(lz, 5, 1);
		testOpenclRecurrentNeuralnetNCyclesDeep(lz, 5, 2);
		testOpenclRecurrentNeuralnetNCyclesDeep(lz, 100, 10);
		testOpenclRecurrentNeuralnetNCyclesDeep(lz, 300, 20);
		testAcylicFlow(lz);
		
	}
	
	public static void test(String testName, boolean z){
		if(!z) throw new RuntimeException("TEST FAIL: "+testName);
		lg("Test pass: "+testName);
	}
	
	/** by == */
	public static void testEqq(String testName, Object x, Object y){
		if(x != y) throw new RuntimeException("TEST FAIL: "+testName+" cuz "+x+" != "+y);
		lg("Test pass: "+testName);
	}
	
	/** by .equals or for nulls uses == */
	public static void testEq(String testName, Object x, Object y){
		boolean equal = (x==null && y==null) || (x!=null && x.equals(y));
		if(!equal) throw new RuntimeException("TEST FAIL: "+testName+" cuz "+x+" not .equals "+y);
		lg("Test pass: "+testName);
	}
	
	/*TODO choose javassist andOr beanshell, cuz beanshell claims to have a compiled mode. Also, can it put debug breakpoints?
	Also, do i want the code that finds jdk or openjdk etc and uses it for compiling with debug info so can breakpoint in runtime generated code?
	*/
	
	public static void testDownload(Lazycl lz){
		LazyBlob bytes = lz.lazycl("Code", "download:https://upload.wikimedia.org/wikipedia/commons/a/a3/Ice_water_vapor.jpg");
		lg("testDownload got "+bytes.bize()+" bits. DisplayImage...");
		ScreenUtil.displayImage(bytes.arr(byte[].class));
	}
	
	/** an optimization useful for music tools or simple loop bodies */
	public static void testAcylicFlow(Lazycl lz){
		/*String acyclicFlow = "java:...TODO copy that static func that takes int[] and double[] params from the acyclicflow dir in earlier project, which already works...";
		int[] opcodes = TODO; //FIXME
		double[] in = TODO; //FIXME
		LazyBlob outBlob = lz.lazycl(
			//is default "IsTemp", false,
			"Code", acyclicFlow,
			"opcodes", opcodes,
			"in", in
		); //TODO it also needs to know inSize, tempSize, outSize, andOr totalSize? 
		double[] outArray = null; //FIXME get from outblob.d(int);
		*/
		throw new RuntimeException("TODO");
	}
	
	/** test wrapping of IntToDoubleFunction, for example. TODO other primitive lambdas. */
	public static void testJavaPrimitiveLambdas(Lazycl lz){
		test("i*i*i-7*i*i+3 i*i*i-7*i*i+3",
			lz.wrap(float[].class, 100, (int i)->i*i*i-7*i*i+3).arr(float[].class)[5] == 5f*5*5-7*5*5+3);
	}
	
	public static final String matmulCode1dAs2d = //todo generate kernel void hashNameBasedOnKernelCodeString
		"opencl1.2:(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd){\n"+
		"	int bd = get_global_id(0);\n"+
		"	const int b = bd/dSize;\n"+
		"	const int d = bd%dSize;\n"+
		"	float sum = 0;\n"+
		"	for(int c=0; c<cSize; c++){\n"+
		"		sum += bc[b*cSize+c]*cd[c*dSize+d];\n"+
		"	}\n"+
		"	bdOut[bd] = sum;\n"+
		"}";
	
	public static void testOpenclMatmul(Lazycl lz){
		int bSize = 20;
		int cSize = 30;
		int dSize = 50;
		
		/*how does LazyclPrototype.vm_evalOneTheSlowWayNotInGroups know how big bdOut should be? It knows its float[something],
			but its allowed foir get_global_id not to match 1-to-1 with the output array, such as if each writes a continuous
			5 indexs in the output array.
			Put something like "OutSize" or "Size(bdOut)" or "bdOut.size" or "bdOut.length" or "OutBize". in params. TODO choose.
			I choose "Bize" which is in units of bits.
		*/
		
		/*TODO include IntToDoubleFunction but used as floats, in lz.wrap,
		but still need to include int size and which primitive type,
		and for it to be that or IntToLongFunction,
		or maybe another wrap func that has size and primitive type.
		LazyBlob bc = lz.wrap(b*c, float.class, (int i)->i*i*i-7*i*i+3);
		/*LazyBlob bc = lz.lazycl(
			"Code",
			"java8WithJava4Syntax:float[] ret = new float["+(b*c)+"];"
			+" for(int i=0; i<ret.length; i++) ret[i] = i*i*i-7*i*i+3;"
			+" return TODO_HOW_TO_GET_THE_LAZYCL_INSTANCE.wrap(ret);"
		); //size b*c
		*/
		//LazyBlob bc = floats(b*c, (int i)->i*i*i-7*i*i+3); //size b*c
		//LazyBlob cd = floats(c*d, (int i)->i^(i*i-23)); //size c*d
		
		//FIXME are those IntToLongFunction instead of IntToDoubleFunction? does it matter in these 2 cases?
		LazyBlob bc = lz.wrap(float.class, bSize*cSize, (int i)->i*i*i-7*i*i+3); //size bSize*cSize
		LazyBlob cd = lz.wrap(float.class, cSize*dSize, (int i)->i^(i*i-23)); //size cSize*dSize
		
		long bize = 32L*bSize*dSize; //32==sizeof(float)
		LazyBlob bd = lz.lazycl(
			//is default "IsTemp", false,
			"Code", matmulCode1dAs2d,
			"Bize", bize,
			//could be 1 2 or 3 ints as opencl supports get_global_id in 3d in some GPUs but not older GPUs
			//which is why some of the code uses 1d and does / and % (slower) to turn it into more dims.
			"GlobalSize", lz.wrapc(bSize*dSize), //TODO just include bSize*dSize as it is, will be automatically wrapped. then test again.
			//"LocalSize", lz.wrapc(32),
			//TODO local id for multiple threads sharing local memory together
			//todo will get default in this? unsure about that. "TODO local id, see how i did that 32x32x32 matmul cache optimization, and give url i copied it from before slightly modifying it", ints(1) or leave this as a default? but how many id dims (max 3 in cl),
			"bSize", bSize,
			"cSize", cSize,
			"dSize", dSize,
			"bc", bc,
			"cd", cd
		); //size b*d. multiplied (or waits until you observe the floats in bd)
		int testB = 7, testD = 15;
		float correctSum = 0;
		for(int c=0; c<cSize; c++){
			correctSum += bc.f(testB*cSize+c)*cd.f(c*dSize+testD);
		}
		float observedSum = bd.f(testB*dSize+testD);
		//strictfp and strict opencl compiler params and opencl1.2 supports strict IEEE754 floats so == is ok.
		testEq("matmul bc cd, testB="+testB+" testD="+testD, correctSum, observedSum);
	}
	
	/* Use StrictMath.exps(Lazycl,LazyBlob) instead of...
	public static final String sigmoidOfFloatArrayOpenclCode = TODO get it working in cpu (float)sigmoid(float) first.
			"opencl1.2:(global float* outs, global const float* ins){\n"+
					"	int id = get_global_id(0);\n"+
					"	outs[id] = 1.0f/(1.0f+exp(-ins[id]));\n"+
					"}";
	*/
	
	public static double norm(double d){
		return Double.longBitsToDouble(Double.doubleToLongBits(d));
	}
	
	/** randomly sampled from all nonnormed doubles the normed, so each exponent occurs near equally often,
	and based on the below 2 quotes, I'm unsure if +Infinity -Infinity and NaN all occur often vs are only a few out of 2^64 possible values.
	<br><br>
	https://en.wikipedia.org/wiki/Double-precision_floating-point_format
	0 00000000000 0000000000000000000000000000000000000000000000000000 ≙ 0000 0000 0000 000016 ≙ +0
	1 00000000000 0000000000000000000000000000000000000000000000000000 ≙ 8000 0000 0000 000016 ≙ −0
	0 11111111111 0000000000000000000000000000000000000000000000000000 ≙ 7FF0 0000 0000 000016 ≙ +∞ (positive infinity)
	1 11111111111 0000000000000000000000000000000000000000000000000000 ≙ FFF0 0000 0000 000016 ≙ −∞ (negative infinity)
	0 11111111111 0000000000000000000000000000000000000000000000000001 ≙ 7FF0 0000 0000 000116 ≙ NaN (sNaN on most processors, such as x86 and ARM)
	0 11111111111 1000000000000000000000000000000000000000000000000001 ≙ 7FF8 0000 0000 000116 ≙ NaN (qNaN on most processors, such as x86 and ARM)
	0 11111111111 1111111111111111111111111111111111111111111111111111 ≙ 7FFF FFFF FFFF FFFF16 ≙ NaN (an alternative encoding of NaN)
	<br><br>
	From java.lang.Double sourcecode:
	**
     * A constant holding the positive infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)}.
     *
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;
    **
     * A constant holding the negative infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0xfff0000000000000L)}.
     *
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;
    **
     * A constant holding a Not-a-Number (NaN) value of type
     * {@code double}. It is equivalent to the value returned by
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)}.
     *
    public static final double NaN = 0.0d / 0.0;
	*/
	public static double randomNormedDouble() {
		return norm(Double.longBitsToDouble(Rand.strongRand.nextLong())); 
	}
	
	public static strictfp void testCpuAndGpuOfExponentsOfEOnDoubles(Lazycl lz){
		lg("Starting testCpuAndGpuOfExponentsOfEOnDoubles");
		/*
		These test are slightly nondeterministic probably cuz using Math.pow func,
		but they can still generate random tests of the determinism of 2 implementations of exp getting the same answer every time.
		
		> i=65450 exp(4.0001065305886537E307)
		> i=65460 exp(5.198471405664417E307)
		> i=65470 exp(6.755846312806505E307)
		> i=65480 exp(8.779784640640494E307)
		> i=65490 exp(Infinity)
		> i=65500 exp(Infinity)
		> i=65510 exp(Infinity)
		*/
		double[] ins = new double[500000]; 
		int j = 0;
		ins[j++] = 0;
		ins[j++] = Double.POSITIVE_INFINITY;
		ins[j++] = Double.NEGATIVE_INFINITY;
		ins[j++] = Double.NaN;
		ins[j++] = Double.MIN_NORMAL;
		ins[j++] = -Double.MIN_NORMAL;
		ins[j++] = Double.MAX_VALUE;
		ins[j++] = -Double.MAX_VALUE;
		ins[j++] = Math.PI;
		ins[j++] = Math.E;
		ins[j++] = Math.sqrt(2);
		ins[j++] = -Math.PI;
		ins[j++] = -Math.E;
		ins[j++] = -Math.sqrt(2);
		for(int i=0; i<100; i++){
			ins[j++] = Math.pow(2,i)-1;
			ins[j++] = Math.pow(2,i);
			ins[j++] = Math.pow(2,i)+1;
			ins[j++] = -Math.pow(2,i)-1;
			ins[j++] = -Math.pow(2,i);
			ins[j++] = -Math.pow(2,i)+1;
			ins[j++] = Math.pow(2,-i)-1;
			ins[j++] = Math.pow(2,-i);
			ins[j++] = Math.pow(2,-i)+1;
			ins[j++] = -Math.pow(2,-i)-1;
			ins[j++] = -Math.pow(2,-i);
			ins[j++] = -Math.pow(2,-i)+1;
		}
		double[] cpuJavaStrictmathOuts = new double[ins.length];
		double[] cpuFdlibm53ExpOuts = new double[ins.length];
		double[] openclOuts;
		
		//double[] javaOutsLowBitAs0 = new double[ins.length];
		//double[] fdlibm53Exp_raw_outs = new double[ins.length];
		//double[] fdlibm53Exp_normed_outs = new double[ins.length];
		//Pair<Double,Long>[] fdlibm53Exp_normed_outs_pairs = new Pair[ins.length];
		
		for(int i=j; i<ins.length/2; i++){
			ins[i] = -4.353121424351 + i*.0003123217;
		}
		for(int i=Math.max(j,ins.length/2); i<ins.length; i+=4){
			ins[i] = (ins[i-4]+ins[i-8])*.51;
			ins[i+1] = -ins[i];
			ins[i+2] = 1./ins[i];
			ins[i+3] = -1./ins[i];
		}
		for(int i=0; i<ins.length/5; i++){
			ins[ins.length/2+Rand.strongRand.nextInt(ins.length/2)] = randomNormedDouble();
		}
		
		/*
		> inBits                        1100000010000111000100101010011110110100010011010111010000110000 -738.3318868685801 get exp of this
		>  . . 
		> cpuOutBits                    0000000000000000000000000000000000000000000000000000000111000010 2.223E-321 an exp output
		> cpu2OutBits                   0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
		> gpuOutBits                    0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
		>  . . 
		> diffFirst2                    0000000000000000000000000000000000000000000000000000000111000010
		> diffSecond2                   0000000000000000000000000000000000000000000000000000000000000000
		> as doubles do first 2 ==:     false
		> as doubles do secopnd 2 ==:   true
		> as doubles do 1st and 3rd ==: false
		> 
		> FIXME: 2021-3-21 since changing the c= line changed the output of Fdlibm53, todo compute it using BigDecimal (or compute it using boolean[] which I can do very slowly) etc and find which is more precise in math, which is probably StrictMath, but verify that and figure out what order of ops its using and write them in that order. Make all 3 match.
		Exception in thread "main" java.lang.RuntimeException: Test fail at i=62631
			at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:368)
		
		"2.223E-321" an exp output. Very near "4.9406564584124654 × 10−324".
		https://en.wikipedia.org/wiki/Double-precision_floating-point_format
		0 00000000000 00000000000000000000000000000000000000000000000000012
		≙ 0000 0000 0000 000116 ≙ +2−1022 × 2−52 = 2−1074
		≈ 4.9406564584124654 × 10−324 (Min. subnormal positive double) 
		*/
		ins[ins.length-1] = -738.3318868685801;
		
		
		Arrays.sort(ins);
		
		//boolean convertDoublesToFloatsBeforeInput = true; //cuz doubles are slightly nondeterministic in opencl code as of 2021-3-17. may find a way to fix it.
		//if(convertDoublesToFloatsBeforeInput) for(int i=0; i<ins.length; i++){
		//	ins[i] = (float)ins[i];
		//}
		
		for(int i=0; i<ins.length; i++){
			cpuJavaStrictmathOuts[i] = StrictMath.exp(ins[i]);
			
			cpuFdlibm53ExpOuts[i] = Fdlibm53Exp.exp(ins[i]);
			//javaOutsLowBitAs0[i] = LazyclStrictMath.cpuExp(ins[i]);
			//fdlibm53Exp_normed_outs[i] = Fdlibm53Exp.expUsingNormedDoubleLongTransform(ins[i]);
			
			//fdlibm53Exp_normed_outs_pairs[i] = Fdlibm53Exp.expUsingNormedDoubleLongTransform_withExtraOutputForDebug(ins[i]);
			
		}
		openclOuts = LazyclStrictMath.exps(lz, ins);
		//Pair<double[],long[]> openclOutsWithDebug = LazyclStrictMath.exp_withExtraOutputForDebug(lz, ins);
		//double[] openclOuts = openclOutsWithDebug.l;
		//long[] openclDebug = openclOutsWithDebug.r;
		long countErrors = 0;
		long maxDiff = 0;
		for(int i=0; i<ins.length; i++){
			
			long cpuBits = Double.doubleToRawLongBits(cpuJavaStrictmathOuts[i]);
			long cpu2Bits = Double.doubleToRawLongBits(cpuFdlibm53ExpOuts[i]);
			long gpuBits = Double.doubleToRawLongBits(openclOuts[i]);
			long cpuBitsNorm = Double.doubleToLongBits(cpuJavaStrictmathOuts[i]);
			long cpu2BitsNorm = Double.doubleToLongBits(cpuFdlibm53ExpOuts[i]);
			long gpuBitsNorm = Double.doubleToLongBits(openclOuts[i]);
			//if(cpuBitsNorm != gpuBits || gpuBitsNorm != cpu2BitsNorm){ //if all 3 exp functions dont give exactly the same 64 bits of output
			if(cpuBits != gpuBits || cpuBits != cpu2Bits){ //if all 3 exp functions dont give exactly the same 64 bits of output
			//if(cpu2Bits != gpuBits){
				lg("inBits                        "+MathUtil.bitsToString(Double.doubleToRawLongBits(ins[i]))+" "+ins[i]+" get exp of this");
				lg(" . . ");
				lg("cpuOutBits                    "+MathUtil.bitsToString(cpuBits)+" "+cpuJavaStrictmathOuts[i]+" an exp output");
				lg("cpu2OutBits                   "+MathUtil.bitsToString(cpu2Bits)+" "+cpuFdlibm53ExpOuts[i]+" an exp output");
				lg("gpuOutBits                    "+MathUtil.bitsToString(gpuBits)+" "+openclOuts[i]+" an exp output");
				lg(" . . ");
				lg("diffFirst2                    "+MathUtil.bitsToString(cpuBits^cpu2Bits));
				lg("diffSecond2                   "+MathUtil.bitsToString(cpu2Bits^gpuBits));
				lg("as doubles do first 2 ==:     "+(cpuJavaStrictmathOuts[i]==cpuFdlibm53ExpOuts[i]));
				lg("as doubles do secopnd 2 ==:   "+(cpuFdlibm53ExpOuts[i]==openclOuts[i]));
				lg("as doubles do 1st and 3rd ==: "+(cpuJavaStrictmathOuts[i]==openclOuts[i]));
				lg(" . . NORMS...");
				lg("cpuOutBits                    "+MathUtil.bitsToString(cpuBitsNorm)+" "+cpuJavaStrictmathOuts[i]+" an exp output");
				lg("cpu2OutBits                   "+MathUtil.bitsToString(cpu2BitsNorm)+" "+cpuFdlibm53ExpOuts[i]+" an exp output");
				lg("gpuOutBits                    "+MathUtil.bitsToString(gpuBitsNorm)+" "+openclOuts[i]+" an exp output");
				lg(" . . ");
				lg("diffFirst2                    "+MathUtil.bitsToString(cpuBitsNorm^cpu2BitsNorm));
				lg("diffSecond2                   "+MathUtil.bitsToString(cpu2BitsNorm^gpuBitsNorm));
				lg("as doubles do first 2 ==:     "+(cpuJavaStrictmathOuts[i]==cpuFdlibm53ExpOuts[i]));
				lg("as doubles do secopnd 2 ==:   "+(cpuFdlibm53ExpOuts[i]==openclOuts[i]));
				lg("as doubles do 1st and 3rd ==: "+(cpuJavaStrictmathOuts[i]==openclOuts[i]));
				lg("");
				lg("FIXME: 2021-3-21 since changing the c= line changed the output of Fdlibm53, todo compute it using BigDecimal (or compute it using boolean[] which I can do very slowly) etc and find which is more precise in math, which is probably StrictMath, but verify that and figure out what order of ops its using and write them in that order. Make all 3 match.");
				lg("First "+i+" of "+ins.length+" testCpuAndGpuOfExponentsOfEOnDoubles tests passed.");
				throw new RuntimeException("Test fail at i="+i);
				
				/** Is this a nonnormed zero?
				> inBits                  1100000010000111000100101010011110110100010011010111010000110000 -738.3318868685801 get exp of this
				> cpuOutBits              0000000000000000000000000000000000000000000000000000000111000010 2.223E-321 an exp output
				> gpuOutBits              0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
				> diff                    0000000000000000000000000000000000000000000000000000000111000010
				> 
				Exception in thread "main" java.lang.RuntimeException: Test fail at i=62456
					at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:358)
					at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:42)
					at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:18)
					at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
					
				https://www.tutorialspoint.com/compile_java_online.php confirms StrictMath.exp(-738.331886868580) -> 2.223E-321
				Firefox 86.0.1 (64-bit) in win10 says Math.exp(-738.3318868685801) -> 2.223e-321,
					but I'm unsure if Firefox uses the same Fdlibm53 and strictfp etc.
				*/
				
				/*
				> inBits                        1100000010000110011110001001001001110111011111110010100000000100 -719.0715169843756 get exp of this
				>  . . 
				> cpuOutBits                    0000000000000000000000000001100000111100011101000001001000001100 5.14290000033E-313 an exp output
				> cpu2OutBits                   0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
				> gpuOutBits                    0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
				>  . . 
				> diffFirst2                    0000000000000000000000000001100000111100011101000001001000001100
				> diffSecond2                   0000000000000000000000000000000000000000000000000000000000000000
				> as doubles do first 2 ==:     false
				> as doubles do secopnd 2 ==:   true
				> as doubles do 1st and 3rd ==: false
				
				https://www.tutorialspoint.com/compile_java_online.php confirms StrictMath.exp(-719.0715169843756) -> 5.14290000033E-313
				Firefox 86.0.1 (64-bit) in win10 says Math.exp(-719.0715169843756) -> 5.14290000033e-313
				*/
			}
			
			/*try{
				/*long diff = Math.abs(Double.doubleToLongBits(fdlibm53Exp_normed_outs[i])-Double.doubleToLongBits(openclOuts[i]));
				maxDiff = Math.max(maxDiff, diff);
				//testEq("testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp("+ins[i]+"), and fdlibm53Exp_raw_outs["+i+"]="
				//	+fdlibm53Exp_raw_outs[i]+" normed="+fdlibm53Exp_normed_outs[i], fdlibm53Exp_normed_outs[i], openclOuts[i]);
				long inBits = Double.doubleToRawLongBits(ins[i]);
				long a = Double.doubleToRawLongBits(fdlibm53Exp_normed_outs[i]);
				long b = Double.doubleToRawLongBits(openclOuts[i]);
				//boolean err = a!=b;
				boolean err = openclDebug[i]!=fdlibm53Exp_normed_outs_pairs[i].r; //ignore output of exp and compare debug output
				if(err){
					lg("inBits                  "+MathUtil.bitsToString(inBits));
					lg("fdlibm53Exp_normed_outs "+MathUtil.bitsToString(a)+" "+(err?"X "+(a-b)+" "+fdlibm53Exp_normed_outs[i]+" exp("+ins[i]+") i="+i+" ihex="+MathUtil.bitsToHex(i):""));
					lg("openclOuts              "+MathUtil.bitsToString(b)+" "+(err?"X "+(b-a)+" "+openclOuts[i]+" exp("+ins[i]+") i="+i:""));
					lg("diff                    "+MathUtil.bitsToString(a^b));
					lg(".");
					lg("cpuDebug                "+MathUtil.bitsToString(fdlibm53Exp_normed_outs_pairs[i].r));
					lg("gpuDebug                "+MathUtil.bitsToString(openclDebug[i]));
					lg("diffDebug               "+MathUtil.bitsToString(openclDebug[i]^fdlibm53Exp_normed_outs_pairs[i].r));
					lg("");
					countErrors++;
					break;
				}
				*
				if(i%1000<4) lg("i="+i+" exp("+ins[i]+")");
			}catch(RuntimeException e){
				/*lg("");
				lg("");
				lg("testCpuAndGpuOfExponentsOfEOnDoubles "+i+" exp("+ins[i]+")");
				lg("fdlibm53Exp_normed_outs "+MathUtil.bitsToString(Double.doubleToRawLongBits(fdlibm53Exp_normed_outs[i]))+" raw");
				lg("fdlibm53Exp_normed_outs "+MathUtil.bitsToString(Double.doubleToLongBits(fdlibm53Exp_normed_outs[i]))+" normed");
				lg("fdlibm53Exp_raw_outs    "+MathUtil.bitsToString(Double.doubleToRawLongBits(fdlibm53Exp_raw_outs[i]))+" raw");
				lg("fdlibm53Exp_raw_outs    "+MathUtil.bitsToString(Double.doubleToLongBits(fdlibm53Exp_raw_outs[i]))+" normed");
				lg("javaOuts                "+MathUtil.bitsToString(Double.doubleToRawLongBits(javaOuts[i]))+" raw");
				lg("javaOuts                "+MathUtil.bitsToString(Double.doubleToLongBits(javaOuts[i]))+" normed");
				lg("javaOuts.lowbitas0      "+MathUtil.bitsToString(Double.doubleToRawLongBits(javaOutsLowBitAs0[i]))+" raw");
				lg("javaOuts.lowbitas0      "+MathUtil.bitsToString(Double.doubleToLongBits(javaOutsLowBitAs0[i]))+" normed");
				lg("openclOuts              "+MathUtil.bitsToString(Double.doubleToRawLongBits(openclOuts[i]))+" raw");
				lg("openclOuts              "+MathUtil.bitsToString(Double.doubleToLongBits(openclOuts[i]))+" normed");
				lg("");
				*
				lg("fdlibm53Exp_normed_outs "+MathUtil.bitsToString(Double.doubleToLongBits(fdlibm53Exp_normed_outs[i]))+" normed");
				lg("openclOuts              "+MathUtil.bitsToString(Double.doubleToLongBits(openclOuts[i]))+" normed");
				lg("");
				/*lg("maxDiff="+maxDiff+" countErrors="+countErrors+" fractionErr="+((double)countErrors/(i+1)));
				//throw e;
				e.printStackTrace(System.err);
				*
				countErrors++;
				*
			}*/
			//testEq("testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits (except lowest bit, by setLowBitTo0) for exp("+ins[i]+"), and fdlibm53Exp_outs["+i+"]="
			//	+fdlibm53Exp_outs[i], MathUtil.setLowBitTo0(javaOuts[i]), MathUtil.setLowBitTo0(openclOuts[i]));
		}
		//if(countErrors > 0) throw new RuntimeException("countErrors="+countErrors+" Dont just printStackTrace, do the throw");
		
		/* FIXME Fdlibm53.Exp says "according to an error analysis, the error is always less than 1 ulp (unit in the last place).".
		The difference between cpu and gpu appears to always be within 1 ulp.
		
		> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.018628745249), and fdlibm53Exp_outs[5888]=2.769394613551382
		> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0195410669489995), and fdlibm53Exp_outs[5889]=2.771922345230092
		> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.020453388649), and fdlibm53Exp_outs[5890]=2.7744523840655413
		> testCpuAndGpuOfExponentsOfEOnDoubles 5891 exp(1.0213657103489995)
		> fdlibm53Exp_outs   0100000000000110001101110100001111000101011100010001000111111111 raw
		> fdlibm53Exp_outs   0100000000000110001101110100001111000101011100010001000111111111 normed
		> javaOuts           0100000000000110001101110100001111000101011100010001000111111111 raw
		> javaOuts           0100000000000110001101110100001111000101011100010001000111111111 normed
		> javaOuts.lowbitas0 0100000000000110001101110100001111000101011100010001000111111110 raw
		> javaOuts.lowbitas0 0100000000000110001101110100001111000101011100010001000111111110 normed
		> openclOuts         0100000000000110001101110100001111000101011100010001001000000000 raw
		> openclOuts         0100000000000110001101110100001111000101011100010001001000000000 normed
		Exception in thread "main" java.lang.RuntimeException: TEST FAIL: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0213657103489995), and fdlibm53Exp_outs[5891]=2.776984732163555 cuz 2.7769847321635543 not .equals 2.7769847321635552
			at immutable.lazycl.spec.TestLazyCL.testEq(TestLazyCL.java:95)
			at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:225)
			at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:31)
			at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:17)
			at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
		 */
		lg("testCpuAndGpuOfExponentsOfEOnDoubles tests pass, of "+ins.length+" calls of exp(double)->double that match exactly between cpu and gpu, in both cases using Fdlibm53.");
	}
	
	/** TODO same bits as sigmoidOfFloatArrayOpenclCode, cuz need determinism for merkle hashing.
	Theres a calculation of exponent of e in (double)java.lang.FdLibm.Exp.compute(double),
	which says "should be able to forgo strictfp due to controlled over/underflow",
	but maybe cant use that one exactly cuz it uses Double.longBitsToDouble,
	and TODO does opencl have that op? opencl (todo which version) has as_int(float) and as_float(int) ops,
	but what about raw vs normed form? I need the normed form.
	If I'm already using compiler param "-cl-opt-disable" (see that string in Lwjgl.java) will it be the normed form?
	https://www.khronos.org/registry/OpenCL/specs/opencl-1.2.pdf says opencl1.2 has as_int(float).
	*
	public static float sigmoid(float x){
		throw new RuntimeException("TODO");
	}*/
	
	public static void testOpenclRecurrentNeuralnetNCyclesDeep(Lazycl lz, int nodes, int cyclesDeep){
		LazyBlob firstNodeStates = lz.wrap(float.class, nodes, (int i)->(((i*i*i)%19f)/19f)); //range 0 to 1
		LazyBlob nodeStates = firstNodeStates;
		LazyBlob weights = lz.wrap(float.class, nodes*nodes, (int i)->(((i*i+17+Math.pow(i,1.5))%23f)/23f * 6 - 3)); //range -3 to 3
		String sigmoidOfArrayCode = "opencl1.2:(global float* outs, global const float* ins){\n"+
			"	int id = get_global_id(0);\n"+
			"	outs[id] = 1.0f/(1.0f+(float)exp(-(double)ins[id]));\n"+
			"}";
		for(int cycle=0; cycle<cyclesDeep; cycle++){
			//matmul then sigmoid. its a [1*nodes] by [nodes*nodes] matmul in this simple test.
			LazyBlob weightedSums = lz.lazycl(
				"Code", matmulCode1dAs2d,
				"Bize", nodes*32L, //float is 32 bits. get float[1*nodes]
				"GlobalSize", nodes,
				//TODO also use LocalSize of new int[]{32,32} or 32, and GlobalSize of new int[]{something,32}
				"bSize", 1,
				"cSize", nodes,
				"dSize", nodes,
				"bc", nodeStates,
				"cd", weights
			);
			nodeStates = lz.lazycl(
				//TODO "Code", sigmoidOfFloatArrayOpenclCode,
				"Code", sigmoidOfArrayCode,
				"Bize", nodes*32L, //float is 32 bits
				"GlobalSize", nodes,
				"ins", weightedSums
			);
		}
		LazyBlob openclOut = nodeStates;
		
		float[] cpuNodeStates = firstNodeStates.arr(float[].class);
		float[] cpuWeights = weights.arr(float[].class);
		float[] cpuNextNodeStates = new float[cpuNodeStates.length];
		for(int cycle=0; cycle<cyclesDeep; cycle++){
			Arrays.fill(cpuNextNodeStates, 0f);
			for(int nodeTo=0; nodeTo<nodes; nodeTo++){
				float sum = 0;
				for(int nodeFrom=0; nodeFrom<nodes; nodeFrom++){
					sum += cpuNodeStates[nodeFrom]*cpuWeights[nodeFrom*nodes+nodeTo]; //FIXME is that backward?
				}
				//FIXME implement exp using arithmetic instead of the nonstandard ways it might differ between opencl.exp and java.lang.Math.exp
				cpuNextNodeStates[nodeTo] = 1f/(1f+(float)Math.exp(-sum));
			}
		}
		float[] cpuOut = cpuNextNodeStates;
		
		for(int node=0; node<nodes; node++){
			String n = "node"+node+"_of_"+nodes;
			float openclOutForNode = openclOut.f(node);
			float cpuOutForNode = cpuOut[node];
			lg(n+" openclOutForNode="+openclOutForNode+" cpuOutForNode="+cpuOutForNode);
			if(openclOutForNode != cpuOutForNode) throw new RuntimeException(
				n+" openclOutForNode="+openclOutForNode+" cpuOutForNode="+cpuOutForNode
				+" diff="+Math.abs(openclOutForNode-cpuOutForNode)
				+" If its very close, check for strictfp differences in different systems or the use of different algorithms to approximate exponents of e or things like that");
		}
		
		//"TODO compute it in cpu and verify same strictfp bits"
		
		lg("firstNodeStates.bize = "+firstNodeStates.bize());
		lg("nodeStates.bize = "+nodeStates.bize());
		
		throw new RuntimeException("TODO firstNodeStates.f(20)="+firstNodeStates.f(20)+" nodeStates.f(20)="+nodeStates.f(20));
	}
	
	public static void testOpenclConversionBetweenFloatAndItsRawBitsAndForDoubles(OpenCL cl, boolean includeDoubles){
		double[] insD = new double[]{Math.PI, Math.E};
		int siz = insD.length;
		float[] insF = new float[siz];
		for(int i=0; i<siz; i++) insF[i] = (float)insD[i];
		
		int[] correctOut_floatToIntBits = new int[insF.length];
		long[] correctOut_doubleToLongBits = new long[insF.length];
		for(int i=0; i<siz; i++) {
			correctOut_floatToIntBits[i] = Float.floatToIntBits(insF[i]);
			correctOut_doubleToLongBits[i] = Double.doubleToLongBits(insD[i]);
		}
		
		Object[] clOut_castFloatToInt = cl.callOpencl(
			"opencl1.2:(global int* out, global const float* in){\n"
			+"	int id = get_global_id(0);\n"
			+"	out[id] = (int)in[id];\n"
			+"}",
			new int[]{siz}, null, new int[siz], insF);
		int[] out_castFloatToInt = (int[])clOut_castFloatToInt[0];
		for(int i=0; i<siz; i++) {
			testEq("clOut_castFloatToInt_"+i, out_castFloatToInt[i], (int)insF[i]);
		}
		
		if(includeDoubles){
			Object[] clOut_castDoubleToLong = cl.callOpencl(
				"opencl1.2:(global long* out, global const double* in){\n"
				+"	int id = get_global_id(0);\n"
				+"	out[id] = (int)in[id];\n"
				+"}",
				new int[]{insD.length}, null, new long[insD.length], insD);
			long[] out_castDoubleToLong = (long[])clOut_castDoubleToLong[0];
			for(int i=0; i<siz; i++) {
				testEq("clOut_castDoubleToLong_"+i, out_castDoubleToLong[i], (long)insD[i]);
			}
		}
		
		Object[] clOut_floatToIntBits = cl.callOpencl(
			"opencl1.2:(global int* out, global const float* in){\n"
			+"	int id = get_global_id(0);\n"
			+"	out[id] = as_int(in[id]);\n"
			+"}",
			new int[]{siz}, null, new int[siz], insF);
		int[] out_floatToIntBits = (int[])clOut_floatToIntBits[0];
		for(int i=0; i<siz; i++) {
			testEq("clOut_floatToIntBits_"+i, out_floatToIntBits[i], correctOut_floatToIntBits[i]);
		}
		
		if(includeDoubles){
			Object[] clOut_doubleToLongBits = cl.callOpencl(
				"opencl1.2:(global long* out, global const double* in){\n"
				+"	int id = get_global_id(0);\n"
				+"	out[id] = as_long(in[id]);\n"
				+"}",
				new int[]{siz}, null, new long[siz], insD);
			long[] out_doubleToLongBits = (long[])clOut_doubleToLongBits[0];
			for(int i=0; i<siz; i++) {
				testEq("clOut_doubleToLongBits_"+i, out_doubleToLongBits[i], correctOut_doubleToLongBits[i]);
			}
		}
		
		Object[] clOut_intBitsToFloat = cl.callOpencl(
			"opencl1.2:(global float* out, global const int* in){\n"
			+"	int id = get_global_id(0);\n"
			+"	out[id] = as_float(in[id]);\n"
			+"}",
			new int[]{siz}, null, new float[siz], correctOut_floatToIntBits);
		float[] out_intBitsToFloat = (float[])clOut_intBitsToFloat[0];
		for(int i=0; i<siz; i++) {
			testEq("clOut_intBitsToFloat"+i, out_intBitsToFloat[i], insF[i]);
		}
		
		if(includeDoubles){
			Object[] clOut_longBitsToDouble = cl.callOpencl(
				"opencl1.2:(global double* out, global const long* in){\n"
				+"	int id = get_global_id(0);\n"
				+"	out[id] = as_double(in[id]);\n"
				+"}",
				new int[]{siz}, null, new double[siz], correctOut_doubleToLongBits);
			double[] out_longBitsToDouble = (double[])clOut_longBitsToDouble[0];
			for(int i=0; i<siz; i++) {
				testEq("clOut_longBitsToDouble"+i, out_longBitsToDouble[i], insD[i]);
			}
		}
		
		lg("testOpenclConversionBetweenFloatAndItsRawBitsAndForDoubles tests pass, includeDoubles="+includeDoubles);
	}
	
	public static void testDoublesInParams(Lazycl lz){
		lg("Starting testDoublesInParams");
		double[] insDoubleArray = new double[]{3, 4, 5};
		int doubles = insDoubleArray.length;
		LazyBlob ins = lz.wrapc(new double[]{3, 4, 5});
		//check with https://www.binaryconvert.com/result_double.html?decimal=051
		//says 3. is 0x4008000000000000
		testEq("testDoublesInParams ins.b(0) should be (byte)0x40", ins.b(0), (byte)0x40);
		testEq("testDoublesInParams ins.b(1) should be (byte)0x08", ins.b(1), (byte)0x08);
		testEq("testDoublesInParams ins.b(2) should be (byte)0x00", ins.b(2), (byte)0x00);
		testEq("testDoublesInParams ins.b(7) should be (byte)0x00", ins.b(7), (byte)0x00);
		testEq("testDoublesInParams ins.d(0) should be 3.0", ins.d(0), 3.);
		testEq("testDoublesInParams ins.d(1) should be 4.0", ins.d(1), 4.);
		testEq("testDoublesInParams ins.d(2) should be 5.0", ins.d(2), 5.);
		LazyBlob squares = lz.lazycl(
			"Code",
				"opencl1.2:(global double* outs, global const double* ins){\n"+
				"	int id = get_global_id(0);\n"+
				"	outs[id] = ins[id]*ins[id];\n"+
				"}",
			"Bize", doubles*64L,
			"GlobalSize", doubles,
			//TODO also use LocalSize of new int[]{32,32} or 32, and GlobalSize of new int[]{something,32}
			//"ins", new double[]{3, 4, 5}
			"ins", ins
		);
		testEq("testDoublesInParams squares3", squares.d(0), 9.);
		testEq("testDoublesInParams squares4", squares.d(1), 16.);
		testEq("testDoublesInParams squares5", squares.d(2), 25.);
	}
	
	public static void testDoublesInCode(Lazycl lz){
		lg("Starting testDoublesInCode");
		int size = 3;
		LazyBlob squares = lz.lazycl(
			"Code",
				"opencl1.2:(global float* outs, global const float* ins){\n"+
				"	int id = get_global_id(0);\n"+
				"	double x = (double)ins[id];\n"+
				"	x = x*x;\n"+
				"	outs[id] = (float)x;\n"+
				"}",
			"Bize", size*32L,
			"GlobalSize", size,
			//TODO also use LocalSize of new int[]{32,32} or 32, and GlobalSize of new int[]{something,32}
			"ins", new float[]{3, 4, 5}
		);
		testEq("testDoublesInCode squares3", squares.f(0), 9f);
		testEq("testDoublesInCode squares4", squares.f(1), 16f);
		testEq("testDoublesInCode squares5", squares.f(2), 25f);
	}
	
	public static void testDoubleLiterals(OpenCL cl){
		lg("Starting testDoubleLiterals");
		double[] in = new double[]{4.5};
		//final double huge = 1.0E300;
		final double twom1000 = 9.332636185032189E-302;
		final double o_threshold = 709.782712893384;
		final double u_threshold = -745.1332191019411;
		final double ln2HI_0 = 0.6931471803691238;
		final double ln2LO_0 = 1.9082149292705877E-10;
		final double invln2 = 1.4426950408889634;
		final double P1 = 0.16666666666666602;
		final double P2 = -0.0027777777777015593;
		final double P3 = 6.613756321437934E-5;
		final double P4 = -1.6533902205465252E-6;
		final double P5 = 4.1381367970572385E-8;
		final double ab = 0x1.62e42fefa39efp9;
		//final double correctOut = in[0]+huge+twom1000+o_threshold+u_threshold+ln2HI_0+ln2LO_0+invln2+P1+P2+P3+P4+P5;
		final double correctOut = in[0]+twom1000+o_threshold+u_threshold+ln2HI_0+ln2LO_0+invln2+P1+P2+P3+P4+P5+ab;
		Object[] clOut = cl.callOpencl(
			"opencl1.2:(global double* out, global const double* in){\n"
			+"	const int id = get_global_id(0);\n"
			//+"	const double huge = 1.0E300;\r\n"
			+"	const double twom1000 = 9.332636185032189E-302;\n"
			+"	const double o_threshold = 709.782712893384;\n"
			+"	const double u_threshold = -745.1332191019411;\n"
			+"	const double ln2HI_0 = 0.6931471803691238;\n"
			+"	const double ln2LO_0 = 1.9082149292705877E-10;\n"
			+"	const double invln2 = 1.4426950408889634;\n"
			+"	const double P1 = 0.16666666666666602;\n"
			+"	const double P2 = -0.0027777777777015593;\n"
			+"	const double P3 = 6.613756321437934E-5;\n"
			+"	const double P4 = -1.6533902205465252E-6;\n"
			+"	const double P5 = 4.1381367970572385E-8;\n"
			+"	const double ab = 0x1.62e42fefa39efp9;\n"
			//+"	out[id] = in[id]+huge+twom1000+o_threshold+u_threshold+ln2HI_0+ln2LO_0+invln2+P1+P2+P3+P4+P5;\n"
			+"	out[id] = in[id]+twom1000+o_threshold+u_threshold+ln2HI_0+ln2LO_0+invln2+P1+P2+P3+P4+P5+ab;\n"
			+"}",
			new int[]{1}, null, new double[1], in);
		double observedOut = ((double[])(clOut[0]))[0];
		testEq("testDoubleLiterals001", correctOut, observedOut);
	}
	
	public static void testOpencl_matmulFloat(OpenCL cl){
		lg("Starting testOpencl_matmulFloat. Testing with random arrays...");
		int bSize = 50, cSize = 30, dSize = 70;
		float[][] bc = new float[bSize][cSize];
		float[][] cd = new float[cSize][dSize];
		for(int c=0; c<cSize; c++){
			for(int b=0; b<bSize; b++){
				bc[b][c] = (float)Rand.strongRand.nextGaussian();
			}
			for(int d=0; d<dSize; d++){
				cd[c][d] = (float)Rand.strongRand.nextGaussian();
			}
		}
		float[][] bdFromCpu = matmulCpu(bc, cd);
		float[][] bdFromOpencl = matmul(cl, bc, cd);
		double sumOfSquares = 0;
		double sumOfSquaresOfCpu = 0, sumOfSquaresOfOpencl = 0;
		boolean isExact = true;
		for(int b=0; b<bSize; b++){
			for(int d=0; d<dSize; d++){
				float sub = bdFromCpu[b][d]-bdFromOpencl[b][d];
				if(sub != 0) isExact = false;
				sumOfSquares += sub*sub;
				//Cuz opencl got the right answer but stdDevOfErr=0.0
				//WARNING: An illegal reflective access operation has occurred
				//WARNING: Illegal reflective access by org.lwjgl.LWJGLUtil$3 (file:/C:/q29x/eclw/3/HumanAiNet_2019-2+_todoClibin/src/data/lib/lwjgl-debug.jar) to method java.lang.ClassLoader.findLibrary(java.lang.String)
				//WARNING: Please consider reporting this to the maintainers of org.lwjgl.LWJGLUtil$3
				//WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
				//WARNING: All illegal access operations will be denied in a future release
				//testOpencl matmul passed, stdDevOfErr=0.0
				sumOfSquaresOfCpu += bdFromCpu[b][d]*bdFromCpu[b][d];
				sumOfSquaresOfOpencl += bdFromOpencl[b][d]*bdFromOpencl[b][d];
			}
		}
		int samples = bSize*dSize;
		double stdDevOfErr = Math.sqrt(sumOfSquares/samples);
		String result = "stdDevOfErr="+stdDevOfErr+" sumOfSquaresOfCpu="+sumOfSquaresOfCpu+" sumOfSquaresOfOpencl="+sumOfSquaresOfOpencl;
		if(stdDevOfErr > .000001) throw new Error("matmul differs too much between cpu and opencl, "+result);
		lg("testOpencl_matmulFloat matmul passed !strictfp, "+result);
		if(!isExact) throw new Error("testOpencl_matmulFloat failed strictfp");
		lg("testOpencl_matmulFloat matmul passed strictfp");
	}
	
	public static void testOpencl_sum2Floats(OpenCL cl){
		float inA = (float)Math.PI, inB = (float)Math.E, correctOut = inA+inB;
		Object[] clOut = cl.callOpencl(
			"opencl1.2:(global float* out, global const float* in){ out[0] = in[0]+in[1]; }",
			new int[]{1}, null, new float[1], new float[]{inA, inB});
		float observedOut = ((float[])(clOut[0]))[0];
		testEq("testOpencl_sum2Floats", correctOut, observedOut);
	}
	
	public static void testOpencl_sum2Doubles(OpenCL cl){
		double inA = Math.PI, inB = Math.E, correctOut = inA+inB;
		Object[] clOut = cl.callOpencl(
			"opencl1.2:(global double* out, global const double* in){ out[get_global_id(0)] = in[0]+in[1]; }",
			new int[]{1}, null, new double[1], new double[]{inA, inB});
		double observedOut = ((double[])(clOut[0]))[0];
		testEq("testOpencl_sum2Doubles", correctOut, observedOut);
	}
	
	public static void testOpenclBoolInCode(OpenCL cl){
		float inA = (float)Math.PI, inB = (float)Math.E, correctOut = 22;
		Object[] clOut = cl.callOpencl(
			"opencl1.2:(global float* out, global const float* in){\n"
			+"	int id = get_global_id(0);\n"
			+"	const bool gt = in[0]>in[1];\n"
			+"	bool lt = in[0]<in[1];\n"
			+"	float ret = 10.0f;\n"
			+"	if(gt){\n"
			+"		ret += 5;\n"
			+"	}else if(lt){\n"
			+"		ret += 6;\n"
			+"	}"
			+"	if(gt|lt){\n"
			+"		ret += 7;\n"
			+"	}\n"
			+"	out[id] = ret;\n"
			+"}",
			new int[]{1}, null, new float[1], new float[]{inA, inB});
		float observedOut = ((float[])(clOut[0]))[0];
		testEq("testOpenclBoolInCode", correctOut, observedOut);
	}
	
	public static void testOpencl_matmulDouble(OpenCL cl){
		lg("Testing with random arrays...");
		int bSize = 50, cSize = 30, dSize = 70;
		double[][] bc = new double[bSize][cSize];
		double[][] cd = new double[cSize][dSize];
		for(int c=0; c<cSize; c++){
			for(int b=0; b<bSize; b++){
				bc[b][c] = Rand.strongRand.nextGaussian();
			}
			for(int d=0; d<dSize; d++){
				cd[c][d] = Rand.strongRand.nextGaussian();
			}
		}
		double[][] bdFromCpu = matmulCpu(bc, cd);
		double[][] bdFromOpencl = matmul(cl, bc, cd);
		double sumOfSquares = 0;
		double sumOfSquaresOfCpu = 0, sumOfSquaresOfOpencl = 0;
		long countErrors = 0;
		for(int b=0; b<bSize; b++){
			for(int d=0; d<dSize; d++){
				if(bdFromCpu[b][d] != bdFromOpencl[b][d]) {
					//in case theres roundoff in sumOfSquares or -. I've been finding 1 ulp size errors (plus/minus a lowest bit of double) 2021-3.
					countErrors++;
				}
				double sub = bdFromCpu[b][d]-bdFromOpencl[b][d];
				sumOfSquares += sub*sub;
				//Cuz opencl got the right answer but stdDevOfErr=0.0
				//WARNING: An illegal reflective access operation has occurred
				//WARNING: Illegal reflective access by org.lwjgl.LWJGLUtil$3 (file:/C:/q29x/eclw/3/HumanAiNet_2019-2+_todoClibin/src/data/lib/lwjgl-debug.jar) to method java.lang.ClassLoader.findLibrary(java.lang.String)
				//WARNING: Please consider reporting this to the maintainers of org.lwjgl.LWJGLUtil$3
				//WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
				//WARNING: All illegal access operations will be denied in a future release
				//testOpencl matmul passed, stdDevOfErr=0.0
				sumOfSquaresOfCpu += bdFromCpu[b][d]*bdFromCpu[b][d];
				sumOfSquaresOfOpencl += bdFromOpencl[b][d]*bdFromOpencl[b][d];
			}
		}
		int samples = bSize*dSize;
		double stdDevOfErr = Math.sqrt(sumOfSquares/samples);
		String result = "stdDevOfErr="+stdDevOfErr+" sumOfSquaresOfCpu="+sumOfSquaresOfCpu+" sumOfSquaresOfOpencl="+sumOfSquaresOfOpencl;
		//if(stdDevOfErr > .000001) throw new Error("matmul differs too much between cpu and opencl, "+result);
		testEq("testOpencl_matmulDouble must get same result for cpu and gpu", countErrors, 0L);
		lg("testOpencl_matmulDouble matmul passed, "+result);
	}
	
	public static float[][] matmulCpu(float[][] bc, float[][] cd){
		int B = bc.length;
		int C = bc[0].length;
		int D = cd[0].length;
		//FIXME verify sizes match and are rectangle arrays
		float[][] bd = new float[B][D];
		for(int b=0; b<B; b++){
			for(int d=0; d<D; d++){
				float sum = 0;
				for(int c=0; c<C; c++){
					sum += bc[b][c]*cd[c][d];
				}
				bd[b][d] = sum;
			}
		}
		return bd;
	}
	
	public static double[][] matmulCpu(double[][] bc, double[][] cd){
		int B = bc.length;
		int C = bc[0].length;
		int D = cd[0].length;
		//FIXME verify sizes match and are rectangle arrays
		double[][] bd = new double[B][D];
		for(int b=0; b<B; b++){
			for(int d=0; d<D; d++){
				double sum = 0;
				for(int c=0; c<C; c++){
					sum += bc[b][c]*cd[c][d];
				}
				bd[b][d] = sum;
			}
		}
		return bd;
	}
	
	public static void testOpencl(OpenCL cl, boolean includeDoubles){
		//testInt();
		testOpencl_sum2Floats(cl);
		testOpenclBoolInCode(cl);
		testOpencl_matmulFloat(cl);
		testOpencl_matmulFloat(cl);
		if(includeDoubles){
			testOpencl_sum2Doubles(cl);
			testOpencl_matmulDouble(cl);
			testOpencl_matmulDouble(cl);
		}
		testOpenclConversionBetweenFloatAndItsRawBitsAndForDoubles(cl,includeDoubles);
	}
	
	/** given float[b][c] and float[c][d] returns float[b][d] */
	public static synchronized float[][] matmul(OpenCL cl, float[][] bc, float[][] cd){
		int bSize = bc.length, cSize = bc[0].length, dSize = cd[0].length;
		if(cd.length != cSize) throw new Error("Sizes dont match");
		//FIXME verify sizes match and are rectangle arrays
		float[] bd1d = matmul(cl, bSize, cSize, dSize, LwjglOpenCL.array2dTo1d(bc), LwjglOpenCL.array2dTo1d(cd));
		return LwjglOpenCL.array1dTo2d(bd1d,bSize);
	}
	
	/** given double[b][c] and double[c][d] returns double[b][d] */
	public static synchronized double[][] matmul(OpenCL cl, double[][] bc, double[][] cd){
		int bSize = bc.length, cSize = bc[0].length, dSize = cd[0].length;
		if(cd.length != cSize) throw new Error("Sizes dont match");
		//FIXME verify sizes match and are rectangle arrays
		double[] bd1d = matmul(cl, bSize, cSize, dSize, LwjglOpenCL.array2dTo1d(bc), LwjglOpenCL.array2dTo1d(cd));
		return LwjglOpenCL.array1dTo2d(bd1d,bSize);
	}
	
	/** bc.length==bSize*cSize && cd.length==cSize*dSize */
	public static synchronized float[] matmul(OpenCL cl, int bSize, int cSize, int dSize, float[] bc, float[] cd){
		Object[] out = cl.callOpencl(
			
			//FIXME slower, try this until get the right answer then start using matmulCode1dAs2d instead and make that work
			matmulCode1dAs2d, new int[]{bSize*dSize}, null,
			
			//OLD...
			//FIXME This gets about 3.5 gflops on my 4x1.6GhzLaptop, while the other only about 2. Both give wrong answer,
			//this one gives 0 and other one gives it appears 1 of the input numbers, so I'm going back to the slower 1d one
			//while I fix that then come back to this for speed if I can
			//matmulCode2d, new int[]{bSize, dSize},

			new float[bSize*dSize], bSize, cSize, dSize, bc, cd);
		return (float[]) out[0];
	}
	
	/** bc.length==bSize*cSize && cd.length==cSize*dSize */
	public static synchronized double[] matmul(OpenCL cl, int bSize, int cSize, int dSize, double[] bc, double[] cd){
		Object[] out = cl.callOpencl(
			openclNdrangeCode_matmulDouble, new int[]{bSize*dSize}, null,
			new double[bSize*dSize], bSize, cSize, dSize, bc, cd);
		return (double[]) out[0];
	}
	
	/**
	https://www.reddit.com/r/gpgpu/comments/bklzru/my_float_code_works_but_double_code_throws_how/
	TODO
	https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/scalarDataTypes.html
	QUOTE
		Optional Double Precision and Half Floating Point
		OpenCL 1.0 adds support for double precision and half floating-point as optional extensions.
		The double data type must confirm to the IEEE-754 double precision storage format.
		
		An application that wants to use double will need to include the
			#pragma OPENCL EXTENSION cl_khr_fp64 : enable
			https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/cl_khr_fp64.html
		directive before any double precision data type is declared in the kernel code. This will extended the list of built-in vector and scalar data types to include the following:
		
		Type in OpenCL Language	Description	API type for application
		double	A double precision float.	cl_double
		double2	A 2-component double vector.	cl_double2
		double4	A 4-component double vector.	cl_double4
		double8	An 8-component double vector.	cl_double8
		double16	A 16-component double vector.	cl_double16
	UNQUOTE.
	*/
	public static final String openclNdrangeCode_matmulDouble =
		"opencl1.2:(global double* bdOut, int const bSize, int const cSize, int const dSize, global const double* bc, global const double* cd){\n"+
		"	int bd = get_global_id(0);\n"+
		"		const int b = bd/dSize;\n"+ //TODO optimize allow get_global_id(more dims)?//
		"		const int d = bd%dSize;\n"+ //TODO optimize allow get_global_id(more dims)?
		"		double sum = 0;\n"+
		"		for(int c=0; c<cSize; c++){\n"+
		"			sum += bc[b*cSize+c]*cd[c*dSize+d];\n"+ //TODO optimize allow get_global_id(more dims)?
		"		}\n"+
		"		bdOut[bd] = sum;\n"+
		"}";

}