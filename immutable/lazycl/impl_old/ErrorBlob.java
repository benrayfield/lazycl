/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import java.util.Collections;
import java.util.Map;

import immutable.lazycl.spec.LazyBlob;
import immutable.lazycl.spec.Lazycl;

public class ErrorBlob extends AbstractLB{
	
	//public final String message;
	
	public ErrorBlob(LazyBlob errStr){
		super(Collections.singletonMap("errStr",errStr));
	}
	
	public ErrorBlob(Map<String,LazyBlob> params){
		super(params);
	}
	
	//TODO throw when nearly anything is called

}
