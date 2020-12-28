/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl;
import immutable.lazycl.spec.TestLazyCL;
import static mutable.util.Lg.*;

public strictfp class TestLazyclPrototype{
	public static void main(String[] args){
		TestLazyCL.runTests(LazyclPrototype.instance());
		lg("TestLazyclPrototype tests pass");
	}
}
