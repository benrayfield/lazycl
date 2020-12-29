/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import immutable.lazycl.spec.Blob;

/** a blob thats wrapping an object, such as a FloatBuffer or float[] or LongBuffer or IntToLongFunction */
public strictfp abstract class WrapBlob<T> extends AbstractBlob{
	
	public final long bize;
	
	protected final T data;
	
	public WrapBlob(long bize, T data){
		this.bize = bize;
		this.data = data;
	}
	
	public long bize(){
		return bize;
	}

}
