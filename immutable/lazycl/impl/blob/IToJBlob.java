/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.InputStream;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;

import immutable.lazycl.spec.LazyBlob;

/** a bitstring of size*64 bits. Each is IntToLongFunction.applyAsLong(int) */
public strictfp class IToJBlob extends WrapBlob<IntToLongFunction>{
	
	public IToJBlob(int size, IntToLongFunction func){
		super(size*64L, func);
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		throw new RuntimeException("TODO");
	}

	public double D(long index){
		throw new RuntimeException("TODO");
	}

	public double d(int index){
		return Double.longBitsToDouble(j(index));
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
		return 6;
	}

	public boolean flo(){
		return false;
	}

}
