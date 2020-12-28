package immutable.util;
import static mutable.util.X.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mutable.util.Time;

public class HashUtil{
	
	public static byte[] sha3_256(byte[] in) {
		try{
			return MessageDigest.getInstance("SHA3-256").digest(in);
		}catch (NoSuchAlgorithmException e){ throw X(e); }
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("SHA3-256");
		md.digest(new byte[1]);
		byte[] in = Text.stringToBytes("abcd asldjrfq3lw4krjasedfk3w4sdfasdfasd fasldkjtalk4rj");
		double start = Time.now();
		int repeat = 100000;
		for(int i=0; i<repeat; i++) {
			//if(i%10000==0) Time.sleepNoThrow(.001);
			md.reset();
			in[0] = (byte)(i>>>24);
			in[1] = (byte)(i>>>16);
			in[2] = (byte)(i>>>8);
			in[3] = (byte)i;
			byte[] out = md.digest(in);
		}
		double end = Time.now();
		double duration = end-start;
		double hashesPerSecond = repeat/duration;
		System.out.println("hashesPerSecond="+hashesPerSecond+" duration="+duration+" hashes="+repeat);
	}

}
