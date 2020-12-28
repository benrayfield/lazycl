/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

import immutable.lazycl.spec.Blob;
import immutable.lazycl.spec.LazyBlob;

public abstract class AbstractLB implements LazyBlob{
	
	protected Map<String,LazyBlob> params;
	
	public AbstractLB(Map<String,LazyBlob> params){
		this.params = params;
	}

	public long bize(){
		throw new RuntimeException("TODO");
	}

	public boolean z(long i){
		throw new RuntimeException("TODO");
	}

	public float F(long i){
		throw new RuntimeException("TODO");
	}

	public float f(int i){
		throw new RuntimeException("TODO");
	}

	public double D(long i){
		throw new RuntimeException("TODO");
	}

	public double d(int i){
		throw new RuntimeException("TODO");
	}

	public long J(long i){
		throw new RuntimeException("TODO");
	}

	public long j(int i){
		throw new RuntimeException("TODO");
	}

	public int I(long i){
		throw new RuntimeException("TODO");
	}

	public int i(int i){
		throw new RuntimeException("TODO");
	}

	public char C(long i){
		throw new RuntimeException("TODO");
	}

	public char c(int i){
		throw new RuntimeException("TODO");
	}

	public byte B(long i){
		throw new RuntimeException("TODO");
	}

	public byte b(int i){
		throw new RuntimeException("TODO");
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		throw new RuntimeException("TODO");
	}

	public byte piz(){
		throw new RuntimeException("TODO");
	}

	public boolean flo(){
		throw new RuntimeException("TODO");
	}

	public Map<String,LazyBlob> vm_lazyCall(){
		throw new RuntimeException("TODO");
	}
	
	public Consumer<Blob> vm_evalReturnsToHere(){
		throw new RuntimeException("TODO");
	}

}
