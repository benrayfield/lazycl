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
Interpret the bitstrings however you like. They are all 1d here, as a lower level of optimization.
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
	
	public default <T> T arr(Class<T> type, long bitFrom, long bitToExcl){
		//int mask = pize()-1;
		//if((bitFrom&mask) != 0 || (bitToExcl&mask) != 0) throw new RuntimeException("TODO not aligned on blocks of "+pize()+" bits. Should still work (dont forget to ieee754 norm if isFloat) but todo write the code.");
		long biz = bitToExcl-bitFrom;
		if(biz > (long)Integer.MAX_VALUE*pize()) throw new RuntimeException("too big");
		if(type == float[].class){
			int sizeInUnitsOfPrims = (int)(biz>>5);
			if(sizeInUnitsOfPrims<<5 != biz) throw new RuntimeException("TODO not aligned on blocks of float size");
			float[] retF = new float[sizeInUnitsOfPrims];
			for(int i=0; i<retF.length; i++) retF[i] = f(i);
			return (T)retF;
		}else if(type == FloatBuffer.class){
			int sizeInUnitsOfPrims = (int)(biz>>5);
			if(sizeInUnitsOfPrims<<5 != biz) throw new RuntimeException("TODO not aligned on blocks of float size");
			FloatBuffer ret = BufferUtils.createFloatBuffer(sizeInUnitsOfPrims); //FIXME move this out of spec and into an impl
			for(int i=0; i<sizeInUnitsOfPrims; i++) ret.put(i, f(i));
			return (T)ret;
		}if(type == int[].class){
			int sizeInUnitsOfPrims = (int)(biz>>5);
			if(sizeInUnitsOfPrims<<5 != biz) throw new RuntimeException("TODO not aligned on blocks of int size");
			int[] retI = new int[sizeInUnitsOfPrims];
			for(int i=0; i<retI.length; i++) retI[i] = i(i);
			return (T)retI;
		}else if(type == byte[].class){
			int sizeInUnitsOfPrims = (int)(biz>>3);
			if(sizeInUnitsOfPrims<<3 != biz) throw new RuntimeException("TODO not aligned on blocks of byte size");
			byte[] retB = new byte[sizeInUnitsOfPrims];
			for(int i=0; i<retB.length; i++) retB[i] = b(i);
			return (T)retB;
		}else{
			throw new RuntimeException("TODO "+type);
		}
	}

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
