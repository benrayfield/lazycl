/** Ben F Rayfield offers this benrayfield.* software opensource MIT license */
package mutable.downloader;
import java.io.ByteArrayInputStream;

/** same as ByteArrayInputStream except ByteArrayInputStream.count can be set by a function,
which causes more and more bytes to be available (or less?), without replacing the byte[] array.
*/
public class GrowingByteArrayInputStream extends ByteArrayInputStream{
	
	public GrowingByteArrayInputStream(byte[] buf, int offset, int length){
		super(buf,offset,length);
	}
	
	public void setCount(int c){
		count = c;
	}
	
	public int count(){
		return count;
	}
	
	/** Changes max capacity for count(), how many bytes can be stored. Replaces byte array. */
	public void setCapacity(int newSize){
		if(newSize < count) throw new IndexOutOfBoundsException("Cant shrink to "+newSize+" because "+count+" bytes filled");
		byte newArray[] = new byte[newSize];
		System.arraycopy(buf, 0, newArray, 0, count);
		buf = newArray;
	}
	
	public int capacity(){
		return buf.length;
	}
	
	/** TODO would be better design similar to PipedOutputStream and PipedInputStream
	since whoever receives this InputStream cant change its contents, only receive them,
	but as long as nobody is trying to break it from inside the program, it will work.
	*/
	public byte[] array(){
		return buf;
	}
	
	public void appendByte(byte b){
		if(count == buf.length){
			setCapacity(buf.length*2);
		}
		buf[count++] = b;
	}
	
	/** replace the byte array with one the size of whats been read, copying that. Then returns array(). */
	public byte[] trim(){
		byte[] b = new byte[count];
		System.arraycopy(buf, 0, b, 0, count);
		buf = b;
		return array();
	}

}