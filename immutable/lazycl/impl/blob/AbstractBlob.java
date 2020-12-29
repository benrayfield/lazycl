package immutable.lazycl.impl.blob;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import immutable.lazycl.spec.Blob;

public abstract class AbstractBlob implements Blob{
	
	public <T> T arr(Class<T> type, long bitFrom, long bitToExcl){
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

}
