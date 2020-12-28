/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Immutable. lazyEvaled bitstring made of forest of List<bitstring> that are each a function call
similar to: "openclNdrangeKernel:...todo code here..." int[1]_forkSizes openclParams...
or similar to: "java:..." params... (to define a lambda).
<br><br>
This is a separate software from occamsfuncer cuz this garbcol (garbage collection)
is much simpler and cuz this is not sandboxed.
Occamsfuncer will automatically compile some lambda calls to this
for its sandboxed [number crunching and acyclicFlow optimizations].
*/
public strictfp interface LazyBlob extends Blob{
	
	//TODO separate the spec from the implementation so multiple implementations of opencl can be used without changing the core interfaces.
	//Thats probably not necessary for lwjgl on windows vs linux but there are other opencls such as the AMD sample code in C++.
	
	/** If true, then caller agrees to Throwable when try to read this blob's contents except
	that other blobs, internal to the lazyclVM, can read it, as an optimization to not have to store it,
	like if you unroll a loop of 100 cycles to 100*n lazyblob calls and set the first 99 of them to isTemp
	and only read the last one, the isTemp tells it that it can optimize that in a mutable array
	to calculate only the last n lazyblobs.
	*/
	public default boolean isTemp(){
		/*Map<String,LazyBlob> lazyCall = lazyCall();
		if(lazyCall != null) return lazyCall.con
		*/
		throw new RuntimeException("If lazyCall() != null then it either contains key \"isTemp\" mapping to a single bit, or does not contain that key and takes the default value of isTemp (TODO that should be 0, but is there a more standard way to define default params like a static func in Util)");
	}
	
	/** null if already evaled, else get the lazy function call that will automatically
	be run to get this blob's bits (unless it never returns).
	*
	public List<LazyBlob> lazyCall();
	*/
	public Map<String,LazyBlob> vm_lazyCall();
	
	public default boolean vm_isEvaled(){
		return vm_lazyCall() == null;
	}
	
	/** Lazycl gets it once and remembers in a WeakHashMap<LazyBlob,Consumer<Blob>>, then its gone, so others cant modify,
	or if a Blob is given in constructor then this is always null as if that constructor used this the first time.
	*/
	public Consumer<Blob> vm_evalReturnsToHere();
	
	/** if !vm_isEvaled() && vm_dependenciesAreEvaled() then can eval */
	public default boolean vm_dependenciesAreEvaled(){
		for(LazyBlob dependency : vm_lazyCall().values()){
			if(!dependency.vm_isEvaled()) return false;
		}
		return true;
	}

}
