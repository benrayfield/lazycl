/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import immutable.lazycl.spec.Blob;

public strictfp class OneBitBlob extends AbstractBlob{
	
	public final boolean bit;

	public OneBitBlob(boolean bit){
		this.bit = bit;
	}
	
	public byte piz(){
		return 0;
	}
	
	public int pize(){
		return 1;
	}
	
	public long bize(){
		return 1;
	}
	
	/** long at index 0 to bize()-1 */
	public long J(long bitIndex){
		if(!bit || bitIndex <= -64 || 1 <= bitIndex) return 0L;
		return Long.MIN_VALUE>>>bitIndex;
	}

	public boolean flo(){
		return false;
	}

}
