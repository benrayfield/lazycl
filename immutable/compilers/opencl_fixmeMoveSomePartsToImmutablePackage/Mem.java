package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;

/** the get* and put* funcs throw if this is a lazyEval mem (opencl),
work if this wraps a FloatBuffer etc (cpu).
*/
public abstract class Mem{
	
	/** type of the primtives (array or buffer elements) in the memory */
	public final Class elType;
	
	/** number of elType such as float[].length */
	public final int size;
	
	public Mem(Class elType, int size){
		this.elType = elType;
		this.size = size;
	}
	
	public int byteSize(){
		if(elType == float.class || elType == int.class){
			return size*4;
		}else if(elType == double.class || elType == long.class){
			return size*8;
		}else{
			throw new Error("TODO elType "+elType);
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
