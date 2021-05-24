/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl.blob;
import immutable.util.Blob;

public strictfp class EmptyBlob extends AbstractBlob{
	
	public static final EmptyBlob instance = new EmptyBlob();
	
	public byte piz(){
		return 0;
	}
	
	public int pize(){
		return 1;
	}
	
	public long bize(){
		return 0;
	}
	
	/** long at index 0 to bize()-1 */
	public long J(long bitIndex){
		return 0;
	}

	public boolean flo(){
		return false;
	}

}
