package mutable.util;

import immutable.lazycl.impl.LazyclPrototype;
import immutable.lazycl.spec.Lazycl;
import immutable.util.Blob;

public class Options{
	private Options(){}
	
	public static Lazycl defaultLazycl(){
		Lazycl lz = LazyclPrototype.instance();
		System.out.println("defaultLazycl returning "+lz);
		return lz;
	}

}
