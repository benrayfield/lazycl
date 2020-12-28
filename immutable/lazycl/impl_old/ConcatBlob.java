/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import java.util.Map;
import java.util.function.Consumer;

import immutable.lazycl.spec.Blob;
import immutable.lazycl.spec.LazyBlob;

/** a blob that is the concat of 2 blobs. Its code string is "concat:"
and its other params have "prefix" and "suffix", or todo choose shorter names.
*/
public class ConcatBlob extends AbstractLB{
	
	public ConcatBlob(Map<String,LazyBlob> params){
		super(params);
	}

	
	//TODO

}