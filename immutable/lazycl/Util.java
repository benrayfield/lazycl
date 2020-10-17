/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Util{
	
	/** TODO object types include FloatBuffer, CLMem, long[], String, etc */
	public static LazyBlob wrap(Object o){
		TODO
	}
	
	public static LazyBlob call(LazyBlob... funcAndParams){
		List<LazyBlob> list = Collections.unmodifiableList(new ArrayList(Arrays.asList(funcAndParams))); //immutable
		TODO
	}
	
	public static LazyBlob call(Object... funcAndParams){
		LazyBlob[] a = new LazyBlob[funcAndParams.length];
		for(int i=0; i<funcAndParams.length; i++) a[i] = wrap(funcAndParams[i]);
		return call(a);
	}

}
