/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl;
import static immutable.lazycl.Util.*;

public class TestLazyCL{
	
	/** an optimization useful for music tools or simple loop bodies */
	public static void testAcylicFlow(){
		String acyclicFlow = "java:...TODO copy that static func that takes int[] and double[] params from the acyclicflow dir in earlier project, which already works...";
		int[] opcodes = TODO;
		double[] in = TODO;
		LazyBlob outBlob = call(acyclicFlow, opcodes, in); TODO it also needs to know inSize, tempSize, outSize, andOr totalSize? 
		double[] outArray = TODO get from that LazyBlob;
		TODO
	}
	
	public static void testOpenclMatmul(){
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
	}
	
	public static void testOpenclRecurrentNeuralnet10CyclesDeep(){
		TODO
	}
	
	public static void main(String[] args){
		testAcylicFlow();
		testOpenclMatmul();
		testOpenclRecurrentNeuralnet10CyclesDeep();
	}

}
