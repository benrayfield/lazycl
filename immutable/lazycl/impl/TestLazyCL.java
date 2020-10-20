/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl;
import immutable.lazycl.spec.LazyBlob;
import immutable.lazycl.spec.Lazycl;

public class TestLazyCL{
	
	/*TODO choose javassist andOr beanshell, cuz beanshell claims to have a compiled mode. Also, can it put debug breakpoints?
	Also, do i want the code that finds jdk or openjdk etc and uses it for compiling with debug info so can breakpoint in runtime generated code?
	*/
	
	/** an optimization useful for music tools or simple loop bodies */
	public static void testAcylicFlow(Lazycl lz){
		String acyclicFlow = "java:...TODO copy that static func that takes int[] and double[] params from the acyclicflow dir in earlier project, which already works...";
		int[] opcodes = null; //FIXME
		double[] in = null; //FIXME
		LazyBlob outBlob = lz.lazycl(
			//is default "IsTemp", false,
			"Code", acyclicFlow,
			"opcodes", opcodes,
			"in", in
		); //TODO it also needs to know inSize, tempSize, outSize, andOr totalSize? 
		double[] outArray = null; //FIXME get from outblob.d(int);
		throw new RuntimeException("TODO");
	}
	
	public static void testOpenclMatmul(Lazycl lz){
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
		int b = 20;
		int c = 30;
		int d = 50;
		LazyBlob bc = floats(b*c, (int i)->i*i*i-7*i*i+3); //size a*b
		LazyBlob cd = floats(c*d, (int i)->i^(i*i-23)); //size c*d
		LazyBlob bd = lz.lazycl(
			//is default "IsTemp", false,
			"Code", matmulCode1dAs2d,
			"Get_global_id", ints(c),
			//todo will get default in this? unsure about that. "TODO local id, see how i did that 32x32x32 matmul cache optimization, and give url i copied it from before slightly modifying it", ints(1) or leave this as a default? but how many id dims (max 3 in cl),
			"bSize", b,
			"cSize", c,
			"dSize", d,
			"bc", bc,
			"cd", cd
		); //size b*d. multiplied (or waits until you observe the floats in bd)
		throw new RuntimeException("TODO");
	}
	
	public static void testOpenclRecurrentNeuralnet10CyclesDeep(Lazycl lz){
		throw new RuntimeException("TODO");
	}
	
	public static void main(String[] args){
		Lazycl lz = Util.lz;
		testAcylicFlow(lz);
		testOpenclMatmul(lz);
		testOpenclRecurrentNeuralnet10CyclesDeep(lz);
	}

}
