/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntToDoubleFunction;

import immutable.lazycl.spec.Blob;
import immutable.lazycl.spec.LazyBlob;

/** a bitstring of size*32 bits. Each is (float)IntToDoubleFunction.applyAsDouble(int) */
public class LazyBlobIToDCastF implements LazyBlob{
	
	public final IntToDoubleFunction func;
	
	public final long bize;
	
	public LazyBlobIToDCastF(int size, IntToDoubleFunction func){
		this.func = func;
		bize = (long)size*pize();
	}

	public long bize(){
		return bize;
	}
	
	public boolean isTemp(){
		return false;
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		return (float)func.applyAsDouble(index);
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
		return 5;
	}

	public boolean flo(){
		return true;
	}

	public Map<String, LazyBlob> vm_lazyCall(){
		throw new RuntimeException("TODO");
	}
	
	public Consumer<Blob> vm_evalReturnsToHere(){
		return null;
	}

}
