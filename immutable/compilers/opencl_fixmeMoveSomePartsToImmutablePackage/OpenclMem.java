package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;
import org.lwjgl.opencl.CLMem;

public class OpenclMem extends Mem{
	
	public final CLMem mem;
	
	public OpenclMem(Class elType, int size, CLMem mem){
		super(elType, size);
		this.mem = mem;
	}
	
	//FIXME free or interact with OpenclUtil pooling of CLMem how?

}
