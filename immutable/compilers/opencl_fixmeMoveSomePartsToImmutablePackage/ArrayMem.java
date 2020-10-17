package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

import java.lang.reflect.Array;

public class ArrayMem<ElType> extends Mem{
	
	public final ElType[] mem;

	public ArrayMem(Class elType, int size){
		super(elType, size);
		mem = (ElType[]) Array.newInstance(elType,size);
	}

}
