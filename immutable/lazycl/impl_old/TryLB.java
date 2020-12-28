/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

/** when evaled, try to eval 1 LazyBlob, and if that fails, try to eval another LazyBlob,
and whichever succeeds, this one acts like (TODO create func to get it for optimization to reduce try tree depth).
Can create tree (such as linkedlist) of trys.
Example: try of 2 "download:..." of the same content but different locations. 
*/
public class TryLB extends AbstractLB{
	
	/*FIXME should this be just a general container of Map<String,LazyBlob>
		whose Code starts with "try:" and has params "a" and "b"?
		Or should it be its own class here?
	*/
	
	/*protected LazyBlob a, b;
	
	public TryLB(LazyBlob a, LazyBlob b){
		this.a = a;
		this.b = b;
	}*/
	
	/** params include Code, A, B */
	public TryLB(Map<String,LazyBlob> params){
		super(params);
	}

}