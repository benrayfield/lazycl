/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.function.Supplier;

public strictfp class IntArrayBlob extends WrapBlob<int[]>{
	
	public IntArrayBlob(int... data){
		super(data.length*32L, data);
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		throw new RuntimeException("TODO");
	}

	public double D(long index){
		throw new RuntimeException("TODO");
	}

	public double d(int index){
		throw new RuntimeException("TODO");
	}

	public long J(long index){
		throw new RuntimeException("TODO");
	}

	public long j(int index){
		throw new RuntimeException("TODO");
	}

	public int I(long index){
		throw new RuntimeException("TODO");
	}

	public int i(int index){
		throw new RuntimeException("TODO");
	}

	public char C(long index){
		throw new RuntimeException("TODO");
	}

	public char c(int index){
		throw new RuntimeException("TODO");
	}

	public byte B(long index){
		throw new RuntimeException("TODO");
	}

	public byte b(int index){
		throw new RuntimeException("TODO");
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		throw new RuntimeException("TODO");
	}

	public byte piz(){
		return 5;
	}

	public boolean flo(){
		return false;
	}

}
