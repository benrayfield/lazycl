/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import java.io.InputStream;
import java.nio.ByteBuffer;

/** use this as any primitive type efficiently cuz java.nio.ByteBuffer
has functions like putFloat(int,float) and putLong(int,long) where int is byte index,
but this is immutable so use getFloat(int) etc,
and FIXME what about when it hangs off the end? Catch and do it the slower way then?
*/
public strictfp class ByteBufferBlob extends WrapBlob<ByteBuffer>{
	
	public ByteBufferBlob(ByteBuffer data){
		super(data.capacity()*8L, data);
	}

	public boolean z(long index){
		throw new RuntimeException("TODO");
	}

	public float F(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public float f(int index){
		return data.getFloat(index<<2);
	}

	public double D(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public double d(int index){
		return data.getDouble(index<<3);
	}

	public long J(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public long j(int index){
		return data.getLong(index<<3);
	}

	public int I(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public int i(int index){
		return data.getInt(index<<2);
	}

	public char C(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public char c(int index){
		return data.getChar(index<<1);
	}
	
	public short S(long index){
		throw new RuntimeException("TODO bit aligned read");
	}
	
	public short s(int index){
		return data.getShort(index<<1);
	}

	public byte B(long index){
		throw new RuntimeException("TODO bit aligned read");
	}

	public byte b(int index){
		return data.get(index);
	}

	public InputStream IN(long bitFrom, long bitToExcl){
		throw new RuntimeException("TODO bit aligned read");
	}

	/** 3 cuz byte is 1<<3 bits */
	public byte piz(){
		return 3;
	}

	/** is efficient for flo and non-flo cuz uses ByteBuffer */
	public boolean flo(){
		return false;
	}

}