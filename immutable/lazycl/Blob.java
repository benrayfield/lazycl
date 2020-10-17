/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl;
import java.io.InputStream;

/** bitstring of size 0 to 2^63-1 bits. Can wrap a variety of data sources such as FloatBuffer, CLMem, long[], String,
lazy or eager evaled.
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

}
