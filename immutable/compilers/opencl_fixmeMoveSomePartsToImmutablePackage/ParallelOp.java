package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;
import mutable.util.task.Task;

public class ParallelOp implements Task{
	
	/** Language colon code, and in that way its compatible with occamsfuncer
	since all its datastructs are type colon content such as "image/jpeg:...bytes of jpg...",
	though its most common types have a 1 byte name (a base64 digit).
	<br><br>
	Language might be "NonsandboxedOpenclNdrangeKernel" or "NonsandboxedJavaFunc" or "ForestOpLeaf"
	Any stateless code such as opencl ndrange kernel code.
	Its allowed to write only to 1 array, and it must be the first param.
	The array it writes must be the first param, and all other arrays will only be read
	and have been written only once earlier in this call of this forest.
	<br><br>
	Everything in occamsfuncer runs in a sandbox,
	which will eventually exist across millions or billions of computers still at gaming-low-lag.
	This class is not sandboxed but the plan is for occamsfuncer to compile its own kind of objects
	(which are sandboxed) into various optimized systems including the acyclicFlow music tools optimization
	and forest of opencl ops and some kinds of code compile to javassist andOr beanshell.
	The generated code will still implement the rules of the sandbox but this
	ForestOp layer wont try to verify that. This ForestOp layer should only run trusted code.
	Trusted code will be generated from [untrusted code in a trusted sandbox] that way.
	*/
	public final String nonsandboxedLangColonCode;
	
	public final ParallelSize parallelSize;
	
	/** In the case of opencl, 0 <= get_global_id(0) < parallelSize.
	If The DependOp is not an opencl ndrange kernel,
	such as maybe its java code to run, then parallelSize would maybe be 1,
	or maybe there would be some var in that code similar to get_global_id?
	*/ 
	public ParallelOp(String nonsandboxedLangColonCode, ParallelSize parallelSize){
		this.nonsandboxedLangColonCode = nonsandboxedLangColonCode;
		this.parallelSize = parallelSize;
	}
	
	public String lang(){
		return nonsandboxedLangColonCode.substring(0,colonIndex());
	}
	
	public String code(){
		return nonsandboxedLangColonCode.substring(colonIndex()+1);
	}
	
	public int colonIndex(){
		int i = nonsandboxedLangColonCode.indexOf(':');
		if(i == -1) throw new Error("nonsandboxedCode doesnt contain colon: "+nonsandboxedLangColonCode);
		return i;
	}

}
