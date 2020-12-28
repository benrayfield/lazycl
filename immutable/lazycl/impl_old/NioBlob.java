/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import immutable.lazycl.spec.Blob;
import immutable.lazycl.spec.LazyBlob;
import immutable.lazycl.spec.Lazycl;

/** Immutable. Example: wrapper of FloatBuffer or IntBuffer or ByteBuffer or DoubleBuffer etc.
*/
public class NioBlob<T extends Buffer> implements LazyBlob{
	
	protected FloatBuffer data;
	
	protected Map<String,LazyBlob> lazyCall;
	
	/** becomes null after get the FloatBuffer, for garbcol */
	protected Supplier<FloatBuffer> eval;
	
	public final long bize;
	
	public NioBlob(Supplier<FloatBuffer> eval, long bize, Map<String,LazyBlob> lazyCall){
		this.eval = eval;
		this.bize = bize;
		this.lazyCall = lazyCall;
	}
	
	protected void eval(){
		//lz.vm_eval(lz.vm_evalWhichIf(this)); FIXME how does this put the FloatBuffer here?
		data = eval.get();
		eval = null;
		long observedBize = data.capacity()*pize();
		if(observedBize != bize) throw new RuntimeException("Wrong bize, expected "+bize+" but got "+observedBize);
	}
	
	public Map<String, LazyBlob> vm_lazyCall(){
		return lazyCall;
	}

	public long bize(){
		return bize;
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		try{
			return data.get(index);
		}catch(NullPointerException e){
			eval();
			return data.get(index);
		}
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
		throw new RuntimeException("TODO");
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
	
	public Consumer<Blob> vm_evalReturnsToHere(){
		return null;
	}

}
