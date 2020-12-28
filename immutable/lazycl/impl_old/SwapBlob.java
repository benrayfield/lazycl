/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import java.io.InputStream;
import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

/** Immutable. wrapper class that swaps multiple kinds of LazyBlob depending where the data is,
like a separate LazyBlob for wrapping a CLMem and another for wrapping a FloatBuffer
which the CLMem is copied to or from,
but since they're both the same bits, we dont want the caller to have to
deal with the implementation details of if they're using cpu memory or gpu memory
or java code vs opencl code.
<br><br>
WARNING: this might make it too slow to loop over LazyBlob.f(int) to get float?
Will have to test that speed.
*/
public class SwapBlob /*implements LazyBlob*/{
	
	/*
	protected LazyBlob data;
	
	public long bize(){
		return data.bize();
	}

	public boolean z(long index){
		return data.z(index);
	}

	public float F(long index){
		return data.F(index);
	}

	public float f(int index){
		return data.f(index);
	}

	public double D(long index){
		return data.D(index);
	}

	public double d(int index){
		return data.d(index);
	}

	public long J(long index){
		return data.J(index);
	}

	public long j(int index){
		return data.j(index);
	}

	@Override
	public int I(long index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int i(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char C(long index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char c(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte B(long index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte b(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public InputStream IN(long bitFrom, long bitToExcl){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, LazyBlob> lazyCall() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

}
