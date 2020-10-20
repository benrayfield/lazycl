/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl;
import immutable.lazycl.spec.LazyBlob;
import immutable.lazycl.spec.Lazycl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class Util{
	
	public static final Lazycl lz = new Lazycl() {
		public LazyBlob wrap(Object o) {
			throw new RuntimeException("TODO");
		}
		public LazyBlob lazycl(Map<String, LazyBlob> params) {
			throw new RuntimeException("TODO");
		}
		public Set<LazyBlob> vm_evalWhichIf(LazyBlob observe){
			throw new RuntimeException("TODO");
		}
		public void vm_eval(Set<LazyBlob> evalThese){
			throw new RuntimeException("TODO");
		}
	}; 
	
	
	/*public static LazyBlob lazycl(Map<String,LazyBlob> params){
		return lz.lazycl(params);
	}
	
	public static LazyBlob ints(int... bits){
		return wrap(bits);
	}
	
	/** TODO object types include FloatBuffer, CLMem, long[], String, etc *
	public static LazyBlob wrap(Object o){
		throw new RuntimeException("TODO");
	}*/
	
	/** the map comes from LazyBlob.lazyCall(), which becomes null after its evaled.
	Example: param(someLazyBlob.lazyCall(),"IsTemp") where !someLazyBlob.lazyCall().containsKey("IsTemp"),
	and that returns the default for isTemp which is 0 (false), instead of 1 (true).
	*
	public static LazyBlob param(Map<String,LazyBlob> lazyCall, String name){
	}*/
	public static final Map<String,LazyBlob> defaultParams = lz.map(
		"IsTemp", lz.wrap(false),
		//-cl-opt-disable does not seem to make it slower but tells opencl to do strictfp etc.
		"OpenclCompileParams", lz.wrap("-cl-opt-disable"), //FIXME should wrapping a string be utf16 or utf8?
		"IsJavaStrictfp", lz.wrap(true),
		"IsJavascriptUseStrict", lz.wrap(true)
	);
	
	/*TODO instead of call of List<LazyBlob>, should it be of Map<String,lazyBlob>?
			Of course you can write a map as alternating key val key val.
		YES.
	*/
	
	/*public static LazyBlob call(LazyBlob... funcAndParams){
		List<LazyBlob> list = Collections.unmodifiableList(new ArrayList(Arrays.asList(funcAndParams))); //immutable
		TODO
	}
	
	public static LazyBlob call(Object... funcAndParams){
		LazyBlob[] a = new LazyBlob[funcAndParams.length];
		for(int i=0; i<funcAndParams.length; i++) a[i] = wrap(funcAndParams[i]);
		return call(a);
	}*/

}
