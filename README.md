# lazycl
a Lazy Compute Language/Library. (in progress) Makes it easy to use opencl, to do in 0.01 second what takes CPU 10 seconds. Gaming-low-lag stateless/immutable lazyEvaled form of opencl_1.2 ndrange kernels, internally using lwjgl2's opencl api for java. Each LazyBlob is a List of LazyBlob and replaces that List with the bitstring when lazyEval finishes. This is a refactoring of the working OpenclUtil code in humanAiNetNeural.

The lag you can expect from this system is to do multiple opencl calls within a single video frame of a game except the first time each is called has a compiling delay around 0.1 second, and the speed you can expect is, for example, to matmul 2 float[1000][1000] together in 1/60 second, and 6 times that much work done per time if its bottlenecked by compute instead of movement of bits between GPU cores and the GPU memory outside them and its a big enough calculation, on a Nvidia Geforce RTX 2080 SUPER GPU which is supposedly a 9 teraflop card (UPDATE: I've seen it do 1.1 teraflops) but it appears to be IO bottlenecked and (I havent done much testing on this part yet) go faster for things that dont read as much from global memory as matmul must do (or maybe its one of the memory levels between and I should be using per GPU instead of global memory?), or maybe dividing it into more of smaller calls to do in parallel might speed it up. Opencl optimizations can be explored within the first param of call func which is a code string, and the global and local number of threads.

It uses opencl version 1.2 cuz thats whats most standardized. For example, it works on both AMD and Nvidia cards.
List of opencl compatible devices: https://www.khronos.org/conformance/adopters/conformant-products/opencl If any of these devices which are said to support opencl 1.2 or higher, and which support java (TODO choose minimum version, 8? 11? 14?) do not work in the OpenclUtil.callOpencl or OpenclUtil.callOpenclDependnet functions, please report a bug in the issues link above, but as of 2020-11-27 the OpenclUtil code appears to work but the LazyBlob ways of automatically calling it do not work yet. Lazycl could be ported to other languages, so does not have to depend on java, using the same syntax of lazycl(map)->LazyBlob where such map is of string to LazyBlob recursively in a forest of lazy calls.

(UPDATE: lazyEvaling the code string is interfering with knowing which multiple nodes to eval together in GPU before returning to CPU)((( You may also lazyEval the code string if you're willing to pay compiling delay, or if its not the first call of whatever code string it evals to then you dont pay compiling delay but you do pay the delay between cpu and gpu which otherwise would have done multiple opencl ndrange kernel calls in gpu before returning multiple blobs to cpu (excluding blobs marked as temp which are not copied). This means you may also lazy eval which language its using. For example, to be in "superposition" of using java or opencl for a certain node in the forest of lazy calls until its evaled to "java8:..." or "opencl1.2:..." or "javascript:..." or maybe even "cuda:..." someday. )))

The main classes are immutable.lazycl.spec.LazyBlob and immutable.lazycl.impl.LazyclPrototype (was Util)

Caches compiler output so you can use the code string of the function as the function itself. Pools CLMem objects to avoid lag of reallocating them.

Currently comes with the lwjgl dll for 64 bit windows but todo will also be supported on linux. lwjgl works on linux windows and mac, it says,
and on other OS (if someone writes the code) could use other opencl implementations. Opencl works on a variety of OS.

Will also support lazyeval of java lambdas that return blobs (such as FloatBuffer or long[]), and the syntax is expandable to any number of languages prefixed by "languageName:".

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

