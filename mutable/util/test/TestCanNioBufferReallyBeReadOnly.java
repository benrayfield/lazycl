package mutable.util.test;
import static mutable.util.Lg.*;
import java.nio.ByteBuffer;

import immutable.util.Text;
import mutable.util.Rand;
import mutable.util.Time;

/** similar to Collections.unmodifiable* cant be written thru the object it returns (at least not without reflection)
but can be written thru the param it wraps.
If you receive a FloatBuffer (which is a ByteBuffer) thats isReadOnly, can you know for sure that
its float contents (ignoring position and limit which are always mutable) wont change?
*/
public class TestCanNioBufferReallyBeReadOnly{
	public static void main(String[] args){
		byte[] a = Text.stringToBytes("hello");
		ByteBuffer firstBuf = ByteBuffer.wrap(a);
		ByteBuffer secondBuf = firstBuf.asReadOnlyBuffer();
		lg("(char)firstBuf.get(0) = "+(char)firstBuf.get(0));
		lg("(char)secondBuf.get(0) = "+(char)secondBuf.get(0));
		lg("Replacing first byte in firstBuf with 'j'");
		firstBuf.put(0,(byte)'j');
		lg("(char)firstBuf.get(0) = "+(char)firstBuf.get(0));
		lg("(char)secondBuf.get(0) = "+(char)secondBuf.get(0));
		lg("Replacing first byte in secondBuf with 'y'");
		lg("If (char)secondBuf.get(0) was ever 2 different values, then its not really readonly.");
		secondBuf.put(0,(byte)'y');
		lg("(char)firstBuf.get(0) = "+(char)firstBuf.get(0));
		lg("(char)secondBuf.get(0) = "+(char)secondBuf.get(0));
	}
}

/*
> (char)firstBuf.get(0) = h
> (char)secondBuf.get(0) = h
> Replacing first byte in firstBuf with 'j'
> (char)firstBuf.get(0) = j
> (char)secondBuf.get(0) = j
> Replacing first byte in secondBuf with 'y'
> If (char)secondBuf.get(0) was ever 2 different values, then its not really readonly.
Exception in thread "main" java.nio.ReadOnlyBufferException
	at java.nio.HeapByteBufferR.put(HeapByteBufferR.java:185)
	at mutable.util.test.TestCanNioBufferReallyBeReadOnly.main(TestCanNioBufferReallyBeReadOnly.java:27)
*/