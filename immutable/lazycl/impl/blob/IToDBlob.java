/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.InputStream;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import immutable.lazycl.spec.Blob;
import immutable.lazycl.spec.LazyBlob;

/** a bitstring of size*64 bits. Each is IntToDoubleFunction.applyAsDouble(int) */
public strictfp class IToDBlob extends WrapBlob<IntToDoubleFunction>{
	
	public IToDBlob(int size, IntToDoubleFunction func){
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
		return data.applyAsDouble(index);
	}

	public long J(long index){
		throw new RuntimeException("TODO");
	}

	public long j(int index){
		throw new RuntimeException("TODO");
	}

	public int I(long index){
		throw new RuntimeException("TODO");
	}

	public int i(int index){
		return Float.floatToIntBits(index); //normed, not raw
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
		return true;
	}

}
