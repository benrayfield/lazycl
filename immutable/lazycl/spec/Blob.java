/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import java.io.InputStream;

/** Immutable. bitstring of size 0 to 2^63-1 bits. Can wrap a variety of data sources such as FloatBuffer, CLMem, long[], String,
lazy or eager evaled.
To represent tensor, use 2 Blob together, 1 being the dimension sizes as longs,
where lowest dimension is the primitive size in bits.
To represent sparse tensor, interpret the bitstrings to have pointers in them somehow,
maybe similar to how in Tensorflow they use multiple dense tensors to represent sparse tensors.
Interpret the bitstrings however you like. They are all 1d here, as a lower level of optimization.
*/
public interface Blob{
	
	/** bitstring size */
	public long bize();
	
	/** bit at index 0 to bize()-1 */
	public boolean z(long index);
	
	/** float at index 0 to bize()-1 */
	public float F(long index);
	
	/** float at index 0 to bize()/32-1 */
	public float f(int index);
	
	/** double at index 0 to bize()-1 */
	public double D(long index);
	
	/** double at index 0 to bize()/64-1 */
	public double d(int index);
	
	/** long at index 0 to bize()-1 */
	public long J(long index);
	
	/** long at index 0 to bize()/64-1 */
	public long j(int index);
	
	/** int at index 0 to bize()-1 */
	public int I(long index);
	
	/** int at index 0 to bize()/32-1 */
	public int i(int index);
	
	/** char at index 0 to bize()-1 */
	public char C(long index);
	
	/** char at index 0 to bize()/16-1 */
	public char c(int index);
	
	/** short at index 0 to bize()-1 */
	public default short S(long index){ return (short)C(index); }
	
	/** short at index 0 to bize()/16-1 */
	public default short s(int index){ return (short)c(index); }
	
	/** byte at index 0 to bize()-1 */
	public byte B(long index);
	
	/** byte at index 0 to bize()/8-1 */
	public byte b(int index);
	
	public InputStream IN(long bitFrom, long bitToExcl);
	
	public default InputStream IN(){ return IN(0L, bize()); }
	
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

}
