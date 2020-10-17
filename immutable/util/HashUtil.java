package immutable.util;
import static mutable.util.X.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil{
	
	public static byte[] sha3_256(byte[] in) {
		try{
			return MessageDigest.getInstance("SHA3-256").digest(in);
		}catch (NoSuchAlgorithmException e){ throw X(e); }
	}

}
