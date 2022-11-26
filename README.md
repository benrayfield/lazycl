# lazycl
a Lazy Compute Language/Library. Makes it easy to use opencl, to do in 0.01 second what takes CPU 10 seconds. Gaming-low-lag stateless/immutable lazyEvaled form of opencl_1.2 ndrange kernels, internally using lwjgl2's opencl api for java. Each LazyBlob is a List of LazyBlob and replaces that List with the bitstring when lazyEval finishes. This is a refactoring of the working OpenclUtil code in humanAiNetNeural.

How to run the tests (requires GPU but 1 line of code could change that, asking opencl for a GPU vs CPU instance)..
C:\x>C:\x\openjdk-11.0.2_windows-x64_bin\bin\java -cp lwjgl.jar;lazycl_1.0_java8_srcAndBin(1).jar -Djava.library.path=. immutable.lazycl.impl.TestLazyclPrototype

All tests pass as of 2021-3-25, including computing a recurrent neuralnet 10 time cycles all at once, and it computes exactly the same bits in cpu vs gpu, but todo more tests. Also, vm_evalOneTheSlowWayNotInGroups is only doing 1 opencl ndrange kernel at a time, but for low lag that needs an upgrade to queue multiple ndrange kernels and do them all in GPU before copying any of it back to CPU. 
```
> node96_of_100 gpuOut[96]=2.5188996E-8 cpuOut[96]=2.5188996E-8
> node97_of_100 gpuOut[97]=0.999992 cpuOut[97]=0.999992
> node98_of_100 gpuOut[98]=0.99998206 cpuOut[98]=0.99998206
> node99_of_100 gpuOut[99]=0.64454347 cpuOut[99]=0.64454347
> firstNodeStates.bize = 3200
> nodeStates.bize = 3200
> testOpenclRecurrentNeuralnetNCyclesDeep(100,10) test pass.
> Lacycl tests pass.
> TestLazyclPrototype tests pass
```

You only need these 3 interfaces to use LazyCL:

https://github.com/benrayfield/lazycl/blob/main/immutable/lazycl/spec/Lazycl.java (make forest of opencl ndrange kernel calls from here, statelessly and without dealing with buffers, pooling, compiling, caching, etc)

https://github.com/benrayfield/lazycl/blob/main/immutable/lazycl/spec/LazyBlob.java (immutable lazy-evaled bitstring, is a Blob).
Doubles are not working in callOpenclDependnet (for lower lag, the new code that uses LazyBlob) yet, only in callOpencl (which does 1 ndrange kernel at a time). You can use floats and ints for now.

https://github.com/benrayfield/lazycl/blob/main/immutable/util/Blob.java (immutable bitstring)


This is an important interface for its internal workings, if you want to port Lazycl to some other implementation of opencl such as AMD's C++ opencl code or to call opencl in a GPU cloud, for example:

https://github.com/benrayfield/lazycl/blob/main/immutable/opencl/OpenCL.java


Here's a port of Fdlibm53's exp function (which I need for sigmoid in neuralnets) to opencl, which matches https://docs.oracle.com/javase/8/docs/api/java/lang/StrictMath.html#exp-double- on nearly all testcases so far (will make it 100% match soon):
https://github.com/benrayfield/lazycl/blob/main/data/lib/fdlibm53/Fdlibm53Exp.langColonCode


The lag you can expect from this system is to do multiple opencl calls within a single video frame of a game except the first time each is called has a compiling delay around 0.1 second, and the speed you can expect is, for example, to matmul 2 float 1000 1000 arrays together in 1/60 second, and 6 times that much work done per time if its bottlenecked by compute instead of movement of bits between GPU cores and the GPU memory outside them and its a big enough calculation, on a Nvidia Geforce RTX 2080 SUPER GPU which is supposedly a 9 teraflop card (UPDATE: I've seen it do 1.1 teraflops) but it appears to be IO bottlenecked and (I havent done much testing on this part yet) go faster for things that dont read as much from global memory as matmul must do (or maybe its one of the memory levels between and I should be using per GPU instead of global memory?), or maybe dividing it into more of smaller calls to do in parallel might speed it up. Opencl optimizations can be explored within the first param of call func which is a code string, and the global and local number of threads.

It uses opencl version 1.2 cuz thats whats most standardized. For example, it works on both AMD and Nvidia cards.
List of opencl compatible devices: https://www.khronos.org/conformance/adopters/conformant-products/opencl

(UPDATE: lazyEvaling the code string is interfering with knowing which multiple nodes to eval together in GPU before returning to CPU)((( You may also lazyEval the code string if you're willing to pay compiling delay, or if its not the first call of whatever code string it evals to then you dont pay compiling delay but you do pay the delay between cpu and gpu which otherwise would have done multiple opencl ndrange kernel calls in gpu before returning multiple blobs to cpu (excluding blobs marked as temp which are not copied). This means you may also lazy eval which language its using. For example, to be in "superposition" of using java or opencl for a certain node in the forest of lazy calls until its evaled to "java8:..." or "opencl1.2:..." or "javascript:..." or maybe even "cuda:..." someday. )))

The main classes are immutable.lazycl.spec.LazyBlob and immutable.lazycl.impl.LazyclPrototype (was Util)

Caches compiler output so you can use the code string of the function as the function itself. Pools CLMem objects to avoid lag of reallocating them.

Currently comes with the lwjgl dll for 64 bit windows but todo will also be supported on linux. lwjgl works on linux windows and mac, it says,
and on other OS (if someone writes the code) could use other opencl implementations. Opencl works on a variety of OS.

Will also support lazyeval of java lambdas that return blobs (such as FloatBuffer or long[]), and the syntax is expandable to any number of languages prefixed by "languageName:".





https://github.com/benrayfield/lazycl/blob/main/immutable/lazycl/spec/TestLazyCL.java

UPDATE: log says: 30000 testFloatSigmoidMatchesBetweenCpuAndGpuExactly tests pass, including gpu says sigmoid(3.1415927)=0.95857614,
and its computing the sigmoid as doubles then casting to float. It passed all 500k double exp tests, but I gave up on matching java.lang.StrictMath.exp and just match the slightly modified port of it.
Will have it computing neuralnets deterministicly soon.

UPDATE: Nearly have cpu and gpu computing exact same bits, other than denormal zeros it appears. First 62736 of 500000 testCpuAndGpuOfExponentsOfEOnDoubles tests passed:
```
> return [[D@60f82f98, [D@35f983a6]
> inBits                        1100000010000111000100111101111001001101101001010100011101011001 -738.483546534767 get exp of this
>  . . 
> cpuOutBits                    0000000000000000000000000000000000000000000000000000000110000010 1.907E-321 an exp output
> cpu2OutBits                   0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
> gpuOutBits                    0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
>  . . 
> diffFirst2                    0000000000000000000000000000000000000000000000000000000110000010
> diffSecond2                   0000000000000000000000000000000000000000000000000000000000000000
> as doubles do first 2 ==:     false
> as doubles do secopnd 2 ==:   true
> as doubles do 1st and 3rd ==: false
>  . . NORMS...
> cpuOutBits                    0000000000000000000000000000000000000000000000000000000110000010 1.907E-321 an exp output
> cpu2OutBits                   0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
> gpuOutBits                    0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
>  . . 
> diffFirst2                    0000000000000000000000000000000000000000000000000000000110000010
> diffSecond2                   0000000000000000000000000000000000000000000000000000000000000000
> as doubles do first 2 ==:     false
> as doubles do secopnd 2 ==:   true
> as doubles do 1st and 3rd ==: false
> 
> FIXME: 2021-3-21 since changing the c= line changed the output of Fdlibm53, todo compute it using BigDecimal (or compute it using boolean[] which I can do very slowly) etc and find which is more precise in math, which is probably StrictMath, but verify that and figure out what order of ops its using and write them in that order. Make all 3 match.
> First 62736 of 500000 testCpuAndGpuOfExponentsOfEOnDoubles tests passed.
Exception in thread "main" java.lang.RuntimeException: Test fail at i=62736
	at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:410)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:42)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:18)
	at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
```

UPDATE: Nearly have cpu and gpu computing exact same bits of exponents of e (for sigmoid in neuralnets) aka exp
It passed 282 exp tests (including infinity, nan, sqrt(2), etc), getting the exact same 64 bits, then differed by the lowest bit.
I tried setting all the lowest bits to 0, then it passed 5891 exp tests, but got 1000000000 instead of 0111111111
which is still just plus 1 at the lowest bit. Fdlibm53 (which java StrictMath uses) says
"according to an error analysis, the error is always less than 1 ulp (unit in the last place)."
CPU and GPU appear to differ by at most 1 ULP, or may differ by more that I just havent seen yet.
I need 100% determinism so it works in merkle forest, so I will keep trying.
```
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.007680884849), and fdlibm53Exp_outs[5876]=2.7392410277263233
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0085932065489995), and fdlibm53Exp_outs[5877]=2.741741237081655
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.009505528249), and fdlibm53Exp_outs[5878]=2.7442437284730556
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0104178499489995), and fdlibm53Exp_outs[5879]=2.7467485039834214
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.011330171649), and fdlibm53Exp_outs[5880]=2.7492555656975597
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0122424933489995), and fdlibm53Exp_outs[5881]=2.7517649157021706
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.013154815049), and fdlibm53Exp_outs[5882]=2.754276556085868
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0140671367489995), and fdlibm53Exp_outs[5883]=2.756790488939164
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.014979458449), and fdlibm53Exp_outs[5884]=2.7593067163544864
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0158917801489995), and fdlibm53Exp_outs[5885]=2.7618252404261656
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.016804101849), and fdlibm53Exp_outs[5886]=2.764346063250451
> Test pass: testCpuAndGpuOfExponentsOfEOnDoubles CPU and GPU get exact same bits for exp(1.0177164235489995), and fdlibm53Exp_outs[5887]=2.7668691869254967
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
	at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:256)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:31)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:17)
	at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
```

This is how you use Lazycl (notice the "test pass" before the next test fails):

OLD (see floats and doubles passing tests farther below, the few pages of output at the end of readme)...
https://github.com/benrayfield/lazycl/blob/main/immutable/lazycl/spec/TestLazyCL.java
```
2020-12-28 In TestLazyCL.java the first matrix multiply test passed, but its not well optimized yet (cuz still refactoring optimized code from humanAiNetNeural).
> out FloatBuffer[997]=4.8162797E15
> out FloatBuffer[998]=4.8257458E15
> out FloatBuffer[999]=4.8340603E15
Exception in thread "main" > Test pass: matmul bc cd, testB=7 testD=15
java.lang.RuntimeException: TODO
	at immutable.lazycl.spec.TestLazyCL.testOpenclRecurrentNeuralnet10CyclesDeep(TestLazyCL.java:133)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:12)
	at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
```





TODO...
String matmulCode1dAs2d = //todo generate kernel void hashNameBasedOnKernelCodeString

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
			
		int a = 20;
		
		int b = 30;
		
		int c = 50;
		
		LazyBlob ab = floats(a*b, (int i)->i*i*i-7*i*i+3); //size a*b
		
		LazyBlob bc = floats(b*c, (int i)->i^(i*i-23)); //size b*c
		
		LazyBlob ac = call(matmulCode1dAs2d, new int[]{b}, a, b, c, ab, bc); //size a*c. multiplied
		
		TODO param for output blob size, such as if each get_global_id(...) writes multiple indexs, like if you were doing a bunch of sha3_256 or ed25519 calculations in parallel.
		
		TODO 2 int[] for the global size and local size such as for the 32x32x32 matmul cache optimization (which doubled matmul speed on nvidia card, but still seems IO bottlenecked between cores and global memory).
		
		TODO...

		
		
		
Older code (wont be part of this API). I'm trying to figure out why this supposedly 9 teraflop card is only doing 60 billion multiplies and 60 billion adds (and loop counters etc) per second even with caching 2048 floats 2 of 32x32 to multiply together in each core. So the following code doesnt read from global memory at all...

/** matmul is probably bottlenecked by IO moving between globalMem and gpuCores
	as it seems to be many times slower than the theoretical max flops of the gpu,
	so I'm testing this which wont have any global memory at all except to write 1 number
	per millions or more of calculations that dont read from global memory
	and instead are derived from get_global_id(0).
	*/

public static void testGpuComputeFlopsWithoutMuchGlobalMem(){

		int threads =  1000000;
		
		int loopSize = 1000000;
		
		int opsPerLoopBody = 1;
		
		double totalOps = (double)threads*loopSize*opsPerLoopBody;
		
		String code =
		
			"kernel void testGpuComputeFlopsWithoutMuchGlobalMem(int const a, global float* theOut){\n"+
			
			"	int id = get_global_id(0);\n"+
			
			"	float sum = 0;\n"+
			
			"	for(int x=id; x<a+id; x++){\n"+
			
			"		sum += x;\n"+
			
			"	}\n"+
			
			"	theOut[id] = sum;\n"+
			
			"}";
			
		Object[] outs = OpenclUtil.callOpencl( //compile and run it once before timing it
		
			code,
			
			new int[]{100},
			
			120,
			
			new float[100] //ignored
			
		);
		
		double timeStart = Time.now();
		
		outs = OpenclUtil.callOpencl(
		
			code,
			
			new int[]{threads},
			
			loopSize,
			
			new float[threads] //ignored
			
		);
		
		float[] out = (float[]) outs[1];
		
		double timeEnd = Time.now();
		
		double duration = timeEnd-timeStart;
		
		double flops = totalOps/duration;
		
		double gflops = flops*1e-9;
		
		lg("outs[0] = "+out[0]);
		
		lg("outs[5] = "+out[5]);
		
		lg("gflops="+gflops+" seconds="+duration+" ops="+totalOps);
		
	}
	
	
	
	
> globalWorkSize put 0 1000000

> clEnqueueNDRangeKernel queue CLKernel pointer (0x29A7F92B630) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null

> clEnqueueReadBuffer queue CLMem pointer (0x29A7F67CBD0) CL_TRUE 0 java.nio.DirectFloatBufferU[pos=0 lim=1000000 cap=1000000] null, null

> clFinish queue

> copy param 0 from (somekindof)Buffer to array

> copy param 1 from (somekindof)Buffer to array

> return [1000000, [F@722c41f4]

> outs[0] = 4.9994036E11

> outs[5] = 4.99945439E11

> gflops=355.16929906737136 seconds=2.815558671951294 ops=1.0E12


Its supposedly a 9585 gflop card (as https://en.wikipedia.org/wiki/List_of_Nvidia_graphics_processing_units says) but is only getting 355 * flops_per_loop_body, or 60 * flops_per_matmul_loop_body which is probably more bottlenecked by IO. Or maybe its using flops to copy from one core to adjacent core and so on?

Strange... I changed the += x to += x * x * x * x * x and it only did slightly less loops per second (289 billion) but 5 times more "work" (or 4 times if it did z = x * x and z * z * x) per loop body as its a plus and 4 multiplies, which is 1.1 to 1.4 teraflops.

Whatever the reason those expected and observed speeds dont match, it is enough computing power to do what I need for now.





Float and double tests pass...
```
> FIXME gargcol Lwjgl.errorBuff in finalize()?WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.lwjgl.LWJGLUtil$3 (file:/Z:/q/q35x/w/e6/lazycl_todo3dMandelbrot_frozenDevelopInNonsandboxedtreeexperiment/src/data/lib/lwjgl.jar) to method java.lang.ClassLoader.findLibrary(java.lang.String)
WARNING: Please consider reporting this to the maintainers of org.lwjgl.LWJGLUtil$3
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
> 
Lwjgl.java device (CLDevice pointer (0x18809A6E660)) capabilities: OpenCL 1.2 - Extensions: cl_khr_byte_addressable_store cl_khr_fp64 cl_khr_gl_sharing cl_khr_global_int32_base_atomics cl_khr_global_int32_extended_atomics cl_khr_int64_base_atomics cl_khr_int64_extended_atomics cl_khr_local_int32_base_atomics cl_khr_local_int32_extended_atomics cl_nv_compiler_options cl_nv_device_attribute_query cl_nv_pragma_unroll 

> clCreateBuffer context CL_MEM_READ_ONLY 4 erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[0]=java.nio.DirectFloatBufferU[pos=0 lim=1 cap=1] isWrite=true p=[F@5383967b
> FloatBuffer put float[] then rewind
> clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR java.nio.DirectFloatBufferU[pos=0 lim=2 cap=2] erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[1]=java.nio.DirectFloatBufferU[pos=0 lim=2 cap=2] isWrite=false p=[F@2ac273d3
> PointerBuffer globalWorkSize
> globalWorkSize put 0 1
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BC9B4D0) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer queue CLMem pointer (0x1880AA1D110) CL_TRUE 0 java.nio.DirectFloatBufferU[pos=0 lim=1 cap=1] null, null
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> return [[F@71423665, [F@20398b7c]
> Test pass: testOpencl_sum2Floats
> Testing with random arrays...
> clCreateBuffer context CL_MEM_READ_ONLY 14000 erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[0]=java.nio.DirectFloatBufferU[pos=0 lim=3500 cap=3500] isWrite=true p=[F@3b0143d3
> buffers[1]=null isWrite=false p=50
> buffers[2]=null isWrite=false p=30
> buffers[3]=null isWrite=false p=70
> FloatBuffer put float[] then rewind
> clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR java.nio.DirectFloatBufferU[pos=0 lim=1500 cap=1500] erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[4]=java.nio.DirectFloatBufferU[pos=0 lim=1500 cap=1500] isWrite=false p=[F@5a8e6209
> FloatBuffer put float[] then rewind
> clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR java.nio.DirectFloatBufferU[pos=0 lim=2100 cap=2100] erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[5]=java.nio.DirectFloatBufferU[pos=0 lim=2100 cap=2100] isWrite=false p=[F@4b4523f8
> PointerBuffer globalWorkSize
> globalWorkSize put 0 3500
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BD75210) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer queue CLMem pointer (0x1880AA1D320) CL_TRUE 0 java.nio.DirectFloatBufferU[pos=0 lim=3500 cap=3500] null, null
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> copy param 2 from (somekindof)Buffer to array
> copy param 3 from (somekindof)Buffer to array
> copy param 4 from (somekindof)Buffer to array
> copy param 5 from (somekindof)Buffer to array
> return [[F@731a74c, 50, 30, 70, [F@369f73a2, [F@1f28c152]
> testOpencl_matmulFloat matmul passed !strictfp, stdDevOfErr=0.0 sumOfSquaresOfCpu=95964.89942961474 sumOfSquaresOfOpencl=95964.89942961474
> testOpencl_matmulFloat matmul passed strictfp
> Testing with random arrays...
> clCreateBuffer context CL_MEM_READ_ONLY 14000 erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[0]=java.nio.DirectFloatBufferU[pos=0 lim=3500 cap=3500] isWrite=true p=[F@7791a895
> buffers[1]=null isWrite=false p=50
> buffers[2]=null isWrite=false p=30
> buffers[3]=null isWrite=false p=70
> FloatBuffer put float[] then rewind
> clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR java.nio.DirectFloatBufferU[pos=0 lim=1500 cap=1500] erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[4]=java.nio.DirectFloatBufferU[pos=0 lim=1500 cap=1500] isWrite=false p=[F@3a5ed7a6
> FloatBuffer put float[] then rewind
> clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR java.nio.DirectFloatBufferU[pos=0 lim=2100 cap=2100] erbuf=java.nio.DirectIntBufferU[pos=0 lim=1 cap=1]
> checkCLError
> buffers[5]=java.nio.DirectFloatBufferU[pos=0 lim=2100 cap=2100] isWrite=false p=[F@6325a3ee
> PointerBuffer globalWorkSize
> globalWorkSize put 0 3500
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BD75210) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer queue CLMem pointer (0x1880AA1E190) CL_TRUE 0 java.nio.DirectFloatBufferU[pos=0 lim=3500 cap=3500] null, null
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> copy param 2 from (somekindof)Buffer to array
> copy param 3 from (somekindof)Buffer to array
> copy param 4 from (somekindof)Buffer to array
> copy param 5 from (somekindof)Buffer to array
> return [[F@1d16f93d, 50, 30, 70, [F@67b92f0a, [F@2b9627bc]
> testOpencl_matmulFloat matmul passed !strictfp, stdDevOfErr=0.0 sumOfSquaresOfCpu=106322.57624335376 sumOfSquaresOfOpencl=106322.57624335376
> testOpencl_matmulFloat matmul passed strictfp
> not logging details of doubles
> buffers[0]=java.nio.DirectDoubleBufferU[pos=0 lim=1 cap=1] isWrite=true p=[D@65e2dbf3
> not logging details of doubles
> buffers[1]=java.nio.DirectDoubleBufferU[pos=0 lim=2 cap=2] isWrite=false p=[D@4f970963
> PointerBuffer globalWorkSize
> globalWorkSize put 0 1
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BD746D0) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer doubles
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> return [[D@61f8bee4, [D@7b49cea0]
> Test pass: testOpencl_sum2Doubles
> Testing with random arrays...
> not logging details of doubles
> buffers[0]=java.nio.DirectDoubleBufferU[pos=0 lim=3500 cap=3500] isWrite=true p=[D@887af79
> buffers[1]=null isWrite=false p=50
> buffers[2]=null isWrite=false p=30
> buffers[3]=null isWrite=false p=70
> not logging details of doubles
> buffers[4]=java.nio.DirectDoubleBufferU[pos=0 lim=1500 cap=1500] isWrite=false p=[D@7fac631b
> not logging details of doubles
> buffers[5]=java.nio.DirectDoubleBufferU[pos=0 lim=2100 cap=2100] isWrite=false p=[D@5b87ed94
> PointerBuffer globalWorkSize
> globalWorkSize put 0 3500
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BD74E50) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer doubles
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> copy param 2 from (somekindof)Buffer to array
> copy param 3 from (somekindof)Buffer to array
> copy param 4 from (somekindof)Buffer to array
> copy param 5 from (somekindof)Buffer to array
> return [[D@6e0e048a, 50, 30, 70, [D@5bc79255, [D@47ef968d]
> testOpencl_matmulDouble matmul passed, stdDevOfErr=0.0 sumOfSquaresOfCpu=110839.07105534943 sumOfSquaresOfOpencl=110839.07105534943
> Testing with random arrays...
> not logging details of doubles
> buffers[0]=java.nio.DirectDoubleBufferU[pos=0 lim=3500 cap=3500] isWrite=true p=[D@23e028a9
> buffers[1]=null isWrite=false p=50
> buffers[2]=null isWrite=false p=30
> buffers[3]=null isWrite=false p=70
> not logging details of doubles
> buffers[4]=java.nio.DirectDoubleBufferU[pos=0 lim=1500 cap=1500] isWrite=false p=[D@3dd4520b
> not logging details of doubles
> buffers[5]=java.nio.DirectDoubleBufferU[pos=0 lim=2100 cap=2100] isWrite=false p=[D@5ae63ade
> PointerBuffer globalWorkSize
> globalWorkSize put 0 3500
> clEnqueueNDRangeKernel queue CLKernel pointer (0x1881BD74E50) 1 null org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1] null null null
> clEnqueueReadBuffer doubles
> clFinish queue
> copy param 0 from (somekindof)Buffer to array
> copy param 1 from (somekindof)Buffer to array
> copy param 2 from (somekindof)Buffer to array
> copy param 3 from (somekindof)Buffer to array
> copy param 4 from (somekindof)Buffer to array
> copy param 5 from (somekindof)Buffer to array
> return [[D@610694f1, 50, 30, 70, [D@43814d18, [D@5c5a1b69]
> testOpencl_matmulDouble matmul passed, stdDevOfErr=0.0 sumOfSquaresOfCpu=100686.95643520005 sumOfSquaresOfOpencl=100686.95643520005
> TODOS: [TODO upgrade callOpenclDependnet to use multiple CLQueue to run multiple kernels in parallel when dependnet allows and when theres enough gpu threads. As of 2020-5-4 it looks at Set<DependOp> and chooses a sequence, only doing 1 kernel at a time, and a kernel can have many gpu threads, but sometimes a kernel has less gpu threads than the hardware supports or could be optimized by doing multiple kernels in parallel for some other reason.] ENDTODOS.
> dependParamsList [dp_bdOut_1614007427714005700_float_siz1000, dp_literal_50, dp_literal_20, dp_cd_1614007427715202000_float_siz1500, dp_bc_1614007427714845600_float_siz600, dp_literal_30]
> dependParamSizes [1000, null, null, 1500, 600, null]
> dependParamsSizesInBytes [4000, null, null, 6000, 2400, null]
> dpToPoolclmem.put dp_bdOut_1614007427714005700_float_siz1000 [PoolCLMem id=1614007427716681100 bytes=4000 clmem=CLMem pointer (0x1880AA1DB60)]
> dpToPoolclmem.put dp_cd_1614007427715202000_float_siz1500 [PoolCLMem id=1614007427716691800 bytes=6000 clmem=CLMem pointer (0x1880AA1DF80)]
> dpToPoolclmem.put dp_bc_1614007427714845600_float_siz600 [PoolCLMem id=1614007427716696000 bytes=2400 clmem=CLMem pointer (0x1880AA1E190)]
> ins.size 5
> In dp: dp_literal_20
> In mem: dp_literal_20
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_20
> In dp: dp_literal_30
> In mem: dp_literal_30
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_30
> In dp: dp_literal_50
> In mem: dp_literal_50
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_50
> In dp: dp_bc_1614007427714845600_float_siz600
> In mem: mutable.dependtask.SyMem@42d8062c
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=600 cap=600] CLMem=CLMem pointer (0x1880AA1E190)
> In dp: dp_cd_1614007427715202000_float_siz1500
> In mem: mutable.dependtask.SyMem@6043cd28
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=1500 cap=1500] CLMem=CLMem pointer (0x1880AA1DF80)
> taskSequence.size 1
> globalWorkSize.put 0 1000
> set kernel param 0 to CLMem CLMem pointer (0x1880AA1DB60) dp=dp_bdOut_1614007427714005700_float_siz1000
> set kernel param 1 to Number 20
> set kernel param 2 to Number 30
> set kernel param 3 to Number 50
> set kernel param 4 to CLMem CLMem pointer (0x1880AA1E190) dp=dp_bc_1614007427714845600_float_siz600
> set kernel param 5 to CLMem CLMem pointer (0x1880AA1DF80) dp=dp_cd_1614007427715202000_float_siz1500
> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd){
	int bd = get_global_id(0);
	const int b = bd/dSize;
	const int d = bd%dSize;
	float sum = 0;
	for(int c=0; c<cSize; c++){
		sum += bc[b*cSize+c]*cd[c*dSize+d];
	}
	bdOut[bd] = sum;
} forkSize=mutable.dependtask.ForkSize@cb51256 numDepends=0 param=[mutable.dependtask.LockPar@9f127368, mutable.dependtask.LockPar@70fc86c9, mutable.dependtask.LockPar@6a8335b7, mutable.dependtask.LockPar@85b1be6f, mutable.dependtask.LockPar@bc244394, mutable.dependtask.LockPar@cd0fa2ae]]
> outs.size 1
> clEnqueueReadBuffer CLMem=CLMem pointer (0x1880AA1DB60) Buffer=java.nio.DirectByteBuffer[pos=0 lim=4000 cap=4000], using JReflect.call...
> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1880A72D2D0), CLMem pointer (0x1880AA1DB60), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=4000 cap=4000], null, null]
> clFinish
> Returning to pool: [PoolCLMem id=1614007427716681100 bytes=4000 clmem=CLMem pointer (0x1880AA1DB60)]
> Returning to pool: [PoolCLMem id=1614007427716691800 bytes=6000 clmem=CLMem pointer (0x1880AA1DF80)]
> Returning to pool: [PoolCLMem id=1614007427716696000 bytes=2400 clmem=CLMem pointer (0x1880AA1E190)]
> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
> Test pass: matmul bc cd, testB=7 testD=15
> dependParamsList [dp_ins_1614007427727710000_float_siz3, dp_outs_1614007427727700600_float_siz3]
> dependParamSizes [3, 3]
> dependParamsSizesInBytes [12, 12]
> dpToPoolclmem.put dp_ins_1614007427727710000_float_siz3 [PoolCLMem id=1614007427727921600 bytes=12 clmem=CLMem pointer (0x1881CF39210)]
> dpToPoolclmem.put dp_outs_1614007427727700600_float_siz3 [PoolCLMem id=1614007427727932700 bytes=12 clmem=CLMem pointer (0x1881CF37B60)]
> ins.size 1
> In dp: dp_ins_1614007427727710000_float_siz3
> In mem: mutable.dependtask.SyMem@47db50c5
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=3 cap=3] CLMem=CLMem pointer (0x1881CF39210)
> taskSequence.size 1
> globalWorkSize.put 0 3
> set kernel param 0 to CLMem CLMem pointer (0x1881CF37B60) dp=dp_outs_1614007427727700600_float_siz3
> set kernel param 1 to CLMem CLMem pointer (0x1881CF39210) dp=dp_ins_1614007427727710000_float_siz3
> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global float* outs, global const float* ins){
	int id = get_global_id(0);
	double x = (double)ins[id];
	x = x*x;
	outs[id] = (float)x;
} forkSize=mutable.dependtask.ForkSize@5c072e3f numDepends=0 param=[mutable.dependtask.LockPar@c144cd00, mutable.dependtask.LockPar@6c9c270f]]
> outs.size 1
> clEnqueueReadBuffer CLMem=CLMem pointer (0x1881CF37B60) Buffer=java.nio.DirectByteBuffer[pos=0 lim=12 cap=12], using JReflect.call...
> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1880A72D2D0), CLMem pointer (0x1881CF37B60), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=12 cap=12], null, null]
> clFinish
> Returning to pool: [PoolCLMem id=1614007427727921600 bytes=12 clmem=CLMem pointer (0x1881CF39210)]
> Returning to pool: [PoolCLMem id=1614007427727932700 bytes=12 clmem=CLMem pointer (0x1881CF37B60)]
> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
> Test pass: testDoublesInCode squares3
> Test pass: testDoublesInCode squares4
> Test pass: testDoublesInCode squares5
> Test pass: testDoublesInParams ins.b(0) should be (byte)0x40
> Test pass: testDoublesInParams ins.b(1) should be (byte)0x08
> Test pass: testDoublesInParams ins.b(2) should be (byte)0x00
> Test pass: testDoublesInParams ins.b(7) should be (byte)0x00
> Test pass: testDoublesInParams ins.d(0) should be 3.0
> Test pass: testDoublesInParams ins.d(1) should be 4.0
> Test pass: testDoublesInParams ins.d(2) should be 5.0
> dependParamsList [dp_ins_1614007427730242300_double_siz3, dp_outs_1614007427730235000_double_siz3]
> dependParamSizes [3, 3]
> dependParamsSizesInBytes [24, 24]
> dpToPoolclmem.put dp_ins_1614007427730242300_double_siz3 [PoolCLMem id=1614007427730526900 bytes=24 clmem=CLMem pointer (0x1881CF39A50)]
> dpToPoolclmem.put dp_outs_1614007427730235000_double_siz3 [PoolCLMem id=1614007427730538100 bytes=24 clmem=CLMem pointer (0x1881CF37F80)]
> ins.size 1
> In dp: dp_ins_1614007427730242300_double_siz3
> In mem: mutable.dependtask.SyMem@306279ee
> clEnqueueWriteBuffer DoubleBuffer=java.nio.DirectDoubleBufferU[pos=0 lim=3 cap=3] CLMem=CLMem pointer (0x1881CF39A50)
> taskSequence.size 1
> globalWorkSize.put 0 3
> set kernel param 0 to CLMem CLMem pointer (0x1881CF37F80) dp=dp_outs_1614007427730235000_double_siz3
> set kernel param 1 to CLMem CLMem pointer (0x1881CF39A50) dp=dp_ins_1614007427730242300_double_siz3
> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global double* outs, global const double* ins){
	int id = get_global_id(0);
	outs[id] = ins[id]*ins[id];
} forkSize=mutable.dependtask.ForkSize@545997b1 numDepends=0 param=[mutable.dependtask.LockPar@d1a8cf87, mutable.dependtask.LockPar@a416076e]]
> outs.size 1
> clEnqueueReadBuffer CLMem=CLMem pointer (0x1881CF37F80) Buffer=java.nio.DirectByteBuffer[pos=0 lim=24 cap=24], using JReflect.call...
> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1880A72D2D0), CLMem pointer (0x1881CF37F80), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=24 cap=24], null, null]
> clFinish
> Returning to pool: [PoolCLMem id=1614007427730526900 bytes=24 clmem=CLMem pointer (0x1881CF39A50)]
> Returning to pool: [PoolCLMem id=1614007427730538100 bytes=24 clmem=CLMem pointer (0x1881CF37F80)]
> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
> Test pass: testDoublesInParams squares3
> Test pass: testDoublesInParams squares4
> Test pass: testDoublesInParams squares5
> dependParamsList [dp_cd_1614007427734248300_float_siz25, dp_literal_5, dp_literal_1, dp_literal_5, dp_bdOut_1614007427734206200_float_siz5, dp_bc_1614007427734224000_float_siz5]
> dependParamSizes [25, null, null, null, 5, 5]
> dependParamsSizesInBytes [100, null, null, null, 20, 20]
> dpToPoolclmem.put dp_cd_1614007427734248300_float_siz25 [PoolCLMem id=1614007427734430500 bytes=100 clmem=CLMem pointer (0x1881CF37D70)]
> dpToPoolclmem.put dp_bdOut_1614007427734206200_float_siz5 [PoolCLMem id=1614007427734480200 bytes=20 clmem=CLMem pointer (0x1881CF39000)]
> dpToPoolclmem.put dp_bc_1614007427734224000_float_siz5 [PoolCLMem id=1614007427734488500 bytes=20 clmem=CLMem pointer (0x1881CF383A0)]
> ins.size 5
> In dp: dp_literal_1
> In mem: dp_literal_1
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_1
> In dp: dp_literal_5
> In mem: dp_literal_5
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_5
> In dp: dp_literal_5
> In mem: dp_literal_5
> Its a DependParam so is byValue so no enqueueCopyBufferToCLMem: dp_literal_5
> In dp: dp_bc_1614007427734224000_float_siz5
> In mem: mutable.dependtask.SyMem@5579bb86
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=5 cap=5] CLMem=CLMem pointer (0x1881CF383A0)
> In dp: dp_cd_1614007427734248300_float_siz25
> In mem: mutable.dependtask.SyMem@5204062d
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=25 cap=25] CLMem=CLMem pointer (0x1881CF37D70)
> taskSequence.size 1
> globalWorkSize.put 0 5
> set kernel param 0 to CLMem CLMem pointer (0x1881CF39000) dp=dp_bdOut_1614007427734206200_float_siz5
> set kernel param 1 to Number 1
> set kernel param 2 to Number 5
> set kernel param 3 to Number 5
> set kernel param 4 to CLMem CLMem pointer (0x1881CF383A0) dp=dp_bc_1614007427734224000_float_siz5
> set kernel param 5 to CLMem CLMem pointer (0x1881CF37D70) dp=dp_cd_1614007427734248300_float_siz25
> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd){
	int bd = get_global_id(0);
	const int b = bd/dSize;
	const int d = bd%dSize;
	float sum = 0;
	for(int c=0; c<cSize; c++){
		sum += bc[b*cSize+c]*cd[c*dSize+d];
	}
	bdOut[bd] = sum;
} forkSize=mutable.dependtask.ForkSize@4fcd19b3 numDepends=0 param=[mutable.dependtask.LockPar@8b898a0c, mutable.dependtask.LockPar@8b067922, mutable.dependtask.LockPar@8d978462, mutable.dependtask.LockPar@6bc95690, mutable.dependtask.LockPar@83d00748, mutable.dependtask.LockPar@b8141afe]]
> outs.size 1
> clEnqueueReadBuffer CLMem=CLMem pointer (0x1881CF39000) Buffer=java.nio.DirectByteBuffer[pos=0 lim=20 cap=20], using JReflect.call...
> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1880A72D2D0), CLMem pointer (0x1881CF39000), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=20 cap=20], null, null]
> clFinish
> Returning to pool: [PoolCLMem id=1614007427734430500 bytes=100 clmem=CLMem pointer (0x1881CF37D70)]
> Returning to pool: [PoolCLMem id=1614007427734480200 bytes=20 clmem=CLMem pointer (0x1881CF39000)]
> Returning to pool: [PoolCLMem id=1614007427734488500 bytes=20 clmem=CLMem pointer (0x1881CF383A0)]
> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
> dependParamsList [dp_ins_1614007427735895300_float_siz5, dp_outs_1614007427735880000_float_siz5]
> dependParamSizes [5, 5]
> dependParamsSizesInBytes [20, 20]
> dpToPoolclmem.put dp_ins_1614007427735895300_float_siz5 [PoolCLMem id=1614007427736035500 bytes=20 clmem=CLMem pointer (0x1881CF39420)]
> dpToPoolclmem.put dp_outs_1614007427735880000_float_siz5 [PoolCLMem id=1614007427736049900 bytes=20 clmem=CLMem pointer (0x1881CF39630)]
> ins.size 1
> In dp: dp_ins_1614007427735895300_float_siz5
> In mem: mutable.dependtask.SyMem@5fdba6f9
> clEnqueueWriteBuffer FloatBuffer=java.nio.DirectFloatBufferU[pos=0 lim=5 cap=5] CLMem=CLMem pointer (0x1881CF39420)
> taskSequence.size 1
> globalWorkSize.put 0 5
> set kernel param 0 to CLMem CLMem pointer (0x1881CF39630) dp=dp_outs_1614007427735880000_float_siz5
> set kernel param 1 to CLMem CLMem pointer (0x1881CF39420) dp=dp_ins_1614007427735895300_float_siz5
> clEnqueueNDRangeKernel for [DependOp nonsandboxedLangColonCode=opencl1.2:(global float* outs, global const float* ins){
	int id = get_global_id(0);
	outs[id] = 1.0f/(1.0f+(float)exp(-(double)ins[id]));
} forkSize=mutable.dependtask.ForkSize@10d59286 numDepends=0 param=[mutable.dependtask.LockPar@64cdd6a7, mutable.dependtask.LockPar@731d02c4]]
> outs.size 1
> clEnqueueReadBuffer CLMem=CLMem pointer (0x1881CF39630) Buffer=java.nio.DirectByteBuffer[pos=0 lim=20 cap=20], using JReflect.call...
> JReflect.call public static int org.lwjgl.opencl.CL10.clEnqueueReadBuffer(org.lwjgl.opencl.CLCommandQueue,org.lwjgl.opencl.CLMem,int,long,java.nio.ByteBuffer,org.lwjgl.PointerBuffer,org.lwjgl.PointerBuffer) .. [CLCommandQueue pointer (0x1880A72D2D0), CLMem pointer (0x1881CF39630), 1, 0, java.nio.DirectByteBuffer[pos=0 lim=20 cap=20], null, null]
> clFinish
> Returning to pool: [PoolCLMem id=1614007427736035500 bytes=20 clmem=CLMem pointer (0x1881CF39420)]
> Returning to pool: [PoolCLMem id=1614007427736049900 bytes=20 clmem=CLMem pointer (0x1881CF39630)]
> FIXME does this need special code to free its mem?: org.lwjgl.PointerBuffer[pos=0 lim=1 cap=1]
> node0_of_5 openclOutForNode=0.08617278 cpuOutForNode=0.08617278
> node1_of_5 openclOutForNode=0.46299192 cpuOutForNode=0.46299192
> node2_of_5 openclOutForNode=0.48675504 cpuOutForNode=0.48675504
> node3_of_5 openclOutForNode=0.14655071 cpuOutForNode=0.14655071
> node4_of_5 openclOutForNode=0.44410244 cpuOutForNode=0.4441024
Exception in thread "main" java.lang.RuntimeException: node4_of_5 openclOutForNode=0.44410244 cpuOutForNode=0.4441024 diff=2.9802322E-8 If its very close, check for strictfp differences in different systems or the use of different algorithms to approximate exponents of e or things like that
	at immutable.lazycl.spec.TestLazyCL.testOpenclRecurrentNeuralnetNCyclesDeep(TestLazyCL.java:260)
	at immutable.lazycl.spec.TestLazyCL.runTests(TestLazyCL.java:59)
	at immutable.lazycl.impl.TestLazyclPrototype.main(TestLazyclPrototype.java:8)
```


