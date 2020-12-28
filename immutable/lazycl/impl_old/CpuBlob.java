/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

/** a blob whose eval is done by javassist, beanshell, jdk, or openjdk etc if code starts with
or "java8WithJava4Syntax:" or with some implementation of javascript if code starts with "javascript:", etc.
The advantage of this over opencl is it can do many millions or low billionss of sequential steps
but far fewer calculations in total. Used for things like music tools or string indexOf
or if theres lots of different things to do or lots of branching in memory.
*/
public class CpuBlob extends AbstractLB{
	
	public CpuBlob(Map<String,LazyBlob> params){
		super(params);
	}
	
	//TODO

}