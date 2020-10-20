package immutable.lazycl.impl;

import immutable.lazycl.spec.LazyBlob;

/** a blob whose eval is done by javassist, beanshell, jdk, or openjdk etc if code starts with "java8:"
or with some implementation of javascript if code starts with "javascript:", etc.
The advantage of this over opencl is it can do many millions or low billionss of sequential steps
but far fewer calculations in total. Used for things like music tools or string indexOf
or if theres lots of different things to do or lots of branching in memory.
*/
public class CpuBlob /*implements LazyBlob*/{
	
	//TODO

}