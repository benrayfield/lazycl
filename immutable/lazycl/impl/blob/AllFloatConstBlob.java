/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.function.IntToDoubleFunction;
import immutable.lazycl.spec.LazyBlob;

/** repeat 0f */
public class AllFloatConstBlob extends AbstractBlob{
	
	public final int floats;
	
	public final boolean isTemp;
	
	public final float floatVal;
	
	public final int intVal;
	
	public AllFloatConstBlob(int floats, float val, boolean isTemp){
		this.floatVal = val;
		this.intVal = Float.floatToIntBits(floatVal);
		this.floats = floats;
		this.isTemp = isTemp;
	}
	
	public long bize() {
		return 32L*floats;
	}
	
	public boolean isTemp(){
		return isTemp;
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO");
	}

	public float f(int index){
		return floatVal;
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
		return intVal;
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
	
	public strictfp long fsize(){
		return floats;
	}

	public strictfp int fsizeIntElseThrow(){
		return floats;
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		throw new RuntimeException("TODO");
	}

	public byte piz(){
		return 5;
	}

	public boolean flo(){
		return true;
	}

}
