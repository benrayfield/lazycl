/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.InputStream;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;

import immutable.lazycl.spec.LazyBlob;

/** a bitstring of size*32L bits. Each is (float)IntToLongFunction.applyAsLong(int) */
public strictfp class IToJCastFBlob extends WrapBlob<IntToLongFunction>{
	
	public IToJCastFBlob(int size, IntToLongFunction func){
		super(size*32L, func);
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		return (float)data.applyAsLong(index);
	}

	public double D(long index){
		throw new RuntimeException("TODO");
	}

	public double d(int index){
		throw new RuntimeException("TODO");
	}

	public long J(long index){
		throw new RuntimeException("TODO");
	}

	public long j(int index){
		return data.applyAsLong(index);
	}

	public int I(long index){
		throw new RuntimeException("TODO");
	}

	public int i(int index){
		//TODO merge duplicate code with other classes
		long j = j(index>>1);
		return (index&1)==0 ? (int)(j>>>32) : (int)j;
	}

	public char C(long index){
		throw new RuntimeException("TODO");
	}

	public char c(int index){
		throw new RuntimeException("TODO");
	}

	public byte B(long index){
		throw new RuntimeException("TODO");
	}

	public byte b(int index){
		throw new RuntimeException("TODO");
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		throw new RuntimeException("TODO");
	}

	public byte piz(){
		return 5;
	}

	public boolean flo(){
		return true;
	}

}
