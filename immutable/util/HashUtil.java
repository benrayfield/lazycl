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
		for(int r=0; r<5; r++){
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
	
	public static byte[] sha256(byte[] b){
		try{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(b);
			return md.digest();
		}catch(NoSuchAlgorithmException e){ throw new Error(e); }
	}
	
	/** last 32 bits of sha256 of utf8 of the string,
	for creating "magic numbers" that work between people who dont try to create collisions
	in low thousands of unique things, such as the CoreType and Op enums
	can use this to help forks of opensource occamsfuncer VM code merge code together
	without relying on a central authority of Enum.ordinal() of a specific enum.
	Use these things by their string name instead of Enum.ordinal(),
	and only optimize to use Enum at runtime but use this in datastructs to merkle hash.
	*/
	public static int sha256HashStringToInt(String s){
		byte[] b = sha256(Text.stringToBytes(s));
		return ((b[28]&0xff)<<24)|((b[29]&0xff)<<16)|((b[30]&0xff)<<8)|(b[31]&0xff);
	}
	
	public static byte[] hash(String hashAlgorithm, byte[] in) {
		try{
			return MessageDigest.getInstance(hashAlgorithm).digest(in);
		}catch (NoSuchAlgorithmException e){ throw new RuntimeException(e); }
	}

}
