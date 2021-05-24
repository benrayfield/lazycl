/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import immutable.lazycl.spec.LazyBlob;

public class AllZerosBlob extends AbstractBlob{
	
	public final long bize;
	
	public final byte piz;
	
	public final boolean isFlo;
	
	public final boolean isTemp;
	
	public AllZerosBlob(long bize, byte piz, boolean isFlo, boolean isTemp){
		this.bize = bize;
		this.piz = piz;
		this.isFlo = isFlo;
		this.isTemp = isTemp;
	}
	
	public long bize() {
		return bize;
	}
	
	public boolean isTemp(){
		return isTemp;
	}

	public boolean z(long index){
		return false;
	}

	public float F(long index){
		return 0;
	}

	public float f(int index){
		return 0;
	}

	public double D(long index){
		return 0;
	}

	public double d(int index){
		return 0;
	}

	public long J(long index){
		return 0;
	}

	public long j(int index){
		return 0;
	}

	public int I(long index){
		return 0;
	}

	public int i(int index){
		return 0;
	}

	public char C(long index){
		return 0;
	}

	public char c(int index){
		return 0;
	}

	public byte B(long index){
		return 0;
	}

	public byte b(int index){
		return 0;
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		if(bitToExcl != bitFrom) throw new RuntimeException("Nonzero range size");
		return new ByteArrayInputStream(new byte[0]);
	}

	public byte piz(){
		return piz;
	}

	public boolean flo(){
		return isFlo;
	}

}
