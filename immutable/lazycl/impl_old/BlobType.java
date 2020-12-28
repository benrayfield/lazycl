/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import immutable.lazycl.spec.Blob;

/** a blob of all 0s of a certain Class type and bize */
public class BlobType implements Blob{
	
	public final Class prim;
	
	public final long bize;
	
	public BlobType(Class prim, long bize){
		this.prim = prim;
		this.bize = bize;
	}

	public long bize(){
		return bize;
	}

	public long J(long index){
		return 0L;
	}

	public byte piz(){
		throw new RuntimeException("TODO get it from Class prim");	
	}

	public boolean flo(){
		return prim==float.class || prim==double.class;
	}

}
