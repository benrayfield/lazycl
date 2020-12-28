/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

public class WrapperLB<T> extends AbstractLB{
	
	protected final T data;
	
	public WrapperLB(T data){
		super(null); //already evaled
		this.data = data;
	}
	
	/** already evaled, so false */
	public boolean isTemp(){
		return false;
	}

}
