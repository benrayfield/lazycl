/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/** Immutable. bitstring of size 0 to 2^63-1 bits.
Can wrap a variety of data sources such as FloatBuffer, CLMem, long[], String,
lazy or eager evaled.
To represent tensor, use 2 Blob together, 1 being the dimension sizes as longs,
where lowest dimension is the primitive size in bits.
To represent sparse tensor, interpret the bitstrings to have pointers in them somehow,
maybe similar to how in Tensorflow they use multiple dense tensors to represent sparse tensors.
Interpret the bitstrings however you like. They are all 1d here, as a lower level of optimization,
but even though blobs are 1d (like memory ranges) you can still use 1-3d opencl globalSize and localSize.
*/
public strictfp interface Blob{
	
	/** bitstring size */
	public long bize();
	
	/** bit at index 0 to bize()-1 */
	public default boolean z(long bitIndex){
		return (J(bitIndex)>>>63)!=0;
	}
	
	/** float at index 0 to bize()-1 */
	public default float F(long bitIndex){
		return Float.intBitsToFloat(I(bitIndex)); 
	}
	
	/** float at index 0 to bize()/32-1 */
	public default float f(int index){
		return Float.intBitsToFloat(i(index));
	}
	
	/** double at index 0 to bize()-1 */
	public default double D(long bitIndex){
		return Double.longBitsToDouble(J(bitIndex));
	}
	
	/** double at index 0 to bize()/64-1 */
	public default double d(int index){
		return Double.longBitsToDouble(j(index));
	}
	
	/** long at index 0 to bize()-1 */
	public long J(long bitIndex);
	
	/** long at index 0 to bize()/64-1 */
	public default long j(int index){
		return J(((long)index)<<6);
	}
	
	/** int at index 0 to bize()-1 */
	public default int I(long bitIndex){
		return (int)J(bitIndex+32);
	}
	
	/** int at index 0 to bize()/32-1 */
	public default int i(int index){
		return (int)I(((long)index)<<5);
	}
	
	/** char at index 0 to bize()-1 */
	public default char C(long bitIndex){
		return (char)S(bitIndex);
	}
	
	/** char at index 0 to bize()/16-1 */
	public default char c(int index){
		return (char)s(index);
	}
	
	/** short at index 0 to bize()-1 */
	public default short S(long bitIndex){
		return (short)J(bitIndex+48);
	}
	
	/** short at index 0 to bize()/16-1 */
	public default short s(int index){
		return (short)S(((long)index)<<4);
	}
	
	/** byte at index 0 to bize()-1 */
	public default byte B(long bitIndex){
		return (byte)J(bitIndex+56);
	}
	
	/** byte at index 0 to bize()/8-1 */
	public default byte b(int index){
		return (byte)B(((long)index)<<3);
	}
	
	public default InputStream IN(long bitFrom, long bitToExcl){
		return new InputStream(){
			long pos = bitFrom;
			public int read(){
				if(pos < bitToExcl){
					int ret;
					if(pos+8 <= bitToExcl){
						ret = B(pos)&0xff;
					}else{ //last byte in this InputStream, and not byte aligned. Pad with 0s.
						int keepHighBitsOfByte = (int)(bitToExcl-pos);
						ret = ((B(pos)&0xff)>>keepHighBitsOfByte)<<keepHighBitsOfByte;
					}
					pos += 8;
					return ret;
				}else{
					return -1;
				}
			}
		};
	}
	
	public default InputStream IN(){
		return IN(0L, bize());
	}
	
	/* Id like to put this in Lazycl interface but I dont want Blob to need a pointer to a Lazycl,
	so I'm moving the implementation of "public default <T> T arr(Class<T> type, long bitFrom, long bitToExcl)" into impl package. 
	public T newMutableMemToWriteThenWrapInImmutable(Class type, long bize){
		toArray
	}*/
	
	public <T> T arr(Class<T> type, long bitFrom, long bitToExcl);

	/** Example float[] f = blob.arr(float[].class) copies to a new float[] */
	public default <T> T arr(Class<T> type){
		return arr(type, 0L, bize());
	}
	
	public default Object arr(){
		return arr(arrayClassOf(prim()));
	}
	
	public static Class arrayClassOf(Class innerType){
		return Array.newInstance(innerType,0).getClass();
	}
	
	/** 2^primitiveSize. bit is 0. byte 3. short and char 4. int and float 5. long and double 6.
	float256 and int256 8.
	Regardless of piz, pize, and isFloat, can read raw bits as any type, but some are more efficient.
	*/
	public byte piz();
	
	/** primitiveSize. Regardless of piz, pize, and isFloat, can read raw bits as any type, but some are more efficient. */
	public default int pize(){
		return 1<<piz();
	}
	
	/** true if known to be stored as float or double etc.
	false if known to be stored as bit byte short char int long etc, or if unknown.
	Regardless of piz, pize, and isFloat, can read raw bits as any type, but some are more efficient.
	Even if its not isFloat() you can still use it as any type, so if unknown, then return false.
	Example: if(pize()==64 && flo()) then use d(int) or D(long).
	*/
	public boolean flo();
	
	/** derived from piz() and flo() */
	public default Class prim(){
		if(flo()){
			switch(piz()){
			case 5: return float.class;
			case 6: return double.class;
			}
		}else{
			switch(piz()){
			case 0: return boolean.class;
			case 3: return byte.class;
			case 4: return short.class;
			case 5: return int.class;
			case 6: return long.class;
			}
		}
		throw new RuntimeException("Has another primitive type been added?");
	}

}
