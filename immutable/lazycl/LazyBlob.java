/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl;
import java.util.List;

/** lazyEvaled bitstring made of forest of List<bitstring> that are each a function call
similar to: "openclNdrangeKernel:...todo code here..." int[1]_parallelSizes openclParams...
or similar to: "java:..." params... (to define a lambda).
<br><br>
This is a separate software from occamsfuncer cuz this garbcol (garbage collection)
is much simpler and cuz this is not sandboxed.
Occamsfuncer will automatically compile some lambda calls to this
for its sandboxed [number crunching and acyclicFlow optimizations].
*/
public interface LazyBlob extends Blob{
	
	/** If true, then caller agrees to Throwable when try to read this blob's contents except
	that other blobs, internal to the lazyclVM, can read it, as an optimization to not have to store it,
	like if you unroll a loop of 100 cycles to 100*n lazyblob calls and set the first 99 of them to isTemp
	and only read the last one, the isTemp tells it that it can optimize that in a mutable array
	to calculate only the last n lazyblobs.
	*/
	public boolean isTemp();
	
	/** null if already evaled, else get the lazy function call that will automatically
	be run to get this blob's bits (unless it never returns).
	*/
	public List<LazyBlob> lazyCall();

}
