# lazycl
(in progress) Makes it easy to use opencl. Gaming-low-lag stateless/immutable lazyEvaled form of opencl ndrange kernels, internally using lwjgl's opencl api for java. Each LazyBlob is a List of LazyBlob and replaces that List with the bitstring when lazyEval finishes. This is a refactoring of the working OpenclUtil code in humanAiNetNeural. 

The main classes are immutable.lazycl.LazyBlob and immutable.lazycl.Util

Caches compiler output so you can use the code string of the function as the function itself.

Currently comes with the lwjgl dll for 64 bit windows but todo will also be supported on linux. lwjgl works on a variety of OS, and could use other opencl implementations too.

Will also support lazyeval of java lambdas that return blobs (such as FloatBuffer or long[]).

TODO...
String matmulCode1dAs2d = //todo generate kernel void hashNameBasedOnKernelCodeString

			"opencl:(global float* bdOut, int const bSize, int const cSize, int const dSize, global const float* bc, global const float* cd){\n"+
			
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
		
		TODO
