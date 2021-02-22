package mutable.dependtask.mem;

/** the get* and put* funcs throw if this is a lazyEval mem (opencl),
work if this wraps a FloatBuffer etc (cpu).
*/
public abstract class Mem{
	
	/** type of the primtives (array or buffer elements) in the memory.
	<br><br>
	FIXME??? observed this being java.lang.Integer.class when its a DependParam wrapping an int,
	but if thats its elType then that implies its an array of integer.
	Or should it being an autoboxing type (such as Integer, or Double) imply its such a primitive?
	Or should there be a boolean in Mem to say its an array vs a single primitive?
	*/
	public final Class elType;
	
	/** number of elType such as float[].length */
	public final int size;
	
	public Mem(Class elType, int size){
		this.elType = elType;
		this.size = size;
	}
	
	public int byteSize(){
		return bizeof(elType)/8*size; //divide by 8 first to avoid exceeding int range
	}
	
	public long bize(){
		return (long)bizeof(elType)*size;
	}
	
	/** bitstring size of primitive, such as 64 for double and 32 for int */
	public static int bizeof(Class c){
		if(c == double.class || c == long.class || c == Double.class || c == Long.class){
			return 64;
		}else if(c == float.class || c == int.class || c == Float.class || c == Integer.class){
			return 32;
		}else if(c == short.class || c == char.class || c == Short.class || c == Character.class){
			return 16;
		}else if(c == byte.class || c == Byte.class){
			return 8;
		}else if(c == boolean.class || c == Boolean.class){
			return 1;
		}else{
			throw new Error("Unknown supposedly-primitive type: "+c);
		}
	}
	
	//public abstract boolean lazy();
	
	//TODO rename get to getF etc
	
	public float get(int index){
		throw new UnsupportedOperationException();
	}
	
	public void put(int index, float f){
		throw new UnsupportedOperationException();
	}
	
	public void put(float[] a){
		throw new UnsupportedOperationException();
	}
	
	public void putPlus(int index, float addMe){
		throw new UnsupportedOperationException();
	}
	
	public void putMult(int index, float multMe){
		throw new UnsupportedOperationException();
	}
	
	public void putDivide(int index, float divideMe){
		throw new UnsupportedOperationException();
	}
	
	/** nonbacking */
	public float[] toFloatArray(){
		float[] f = new float[size];
		for(int i=0; i<f.length; i++) f[i] = get(i);
		return f;
	}

}
