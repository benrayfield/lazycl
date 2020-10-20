package immutable.lazycl.impl;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

/** Immutable. Example: wrapper of FloatBuffer or IntBuffer or ByteBuffer or DoubleBuffer etc.
*/
public class NioBlob<T extends Buffer> /*implements LazyBlob*/{
	
	/*
	TODO even if you try to read a byte from a FloatBuffer
	or read a float from an IntBuffer etc,
	view them all as bitstrings and use the to/from int/long bits funcs in Float and Double static funcs,
	and when it aligns to the normal type used, its more efficient,
	and always norm to IEEE754 form.
	
	protected T data;
	
	protected long bize = ((long)data.capacity())<<3; FIXME is capacity right? position? range in use's size?
	
	public long bize(){
		return bize;
	}

	public boolean z(long index){
		TODO
	}

	public float F(long index){
		TODO
	}

	public float f(int index){
		TODO
	}

	public double D(long index){
		TODO
	}

	public double d(int index){
		TODO
	}

	public long J(long index){
		TODO
	}

	public long j(int index){
		TODO
	}

	@Override
	public int I(long index) {
		TODO
	}

	@Override
	public int i(int index) {
		TODO
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
