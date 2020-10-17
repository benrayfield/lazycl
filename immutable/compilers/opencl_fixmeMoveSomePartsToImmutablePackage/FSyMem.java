package immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import java.util.function.Consumer;
import java.util.function.IntFunction;

//import mutable.compilers.opencl.FMem;
import mutable.compilers.opencl.OpenclUtil;

public class FSyMem extends SyMem<FloatBuffer> implements Cloneable{
	
	//TODO redesign so the mem is not connected to the DependParam since thats done in OpenclUtil.callOpenclDependnet
	
	/** todo use Util.newFloatBuffer(int)? */
	static final IntFunction<FloatBuffer> intToFb = (int siz)->BufferUtils.createFloatBuffer(siz);
	
	public FSyMem(String comment, int size){
		this(new DependParam(comment, float.class, size));
	}
	
	public FSyMem(DependParam sy){
		this(sy, intToFb);
	}
	
	public FSyMem(DependParam sy, IntFunction<FloatBuffer> memFactory){
		super(sy, memFactory);
		if(sy.elType != float.class) throw new Error("Not a float DependParam");
	}
	
	/** read float at int index from the FloatBuffer,
	but this doesnt work if havent queued and executed an opencl action to sync
	from CLMem to FloatBuffer (see OpenclUtil for example,
	but TODO will have a function in Matrix andOr Graph for it.
	*/
	public final float get(int index){
		//FIXME mem() or mem? mem() might be too slow to call many times
		return mem().get(index);
	}
	
	/** write float at int index. See comment of get(int). */
	public final void put(int index, float f){
		//FIXME mem() or mem? mem() might be too slow to call many times
		mem().put(index,f);
	}
	
	/** write plusEqual f at index. See comment of get(int). */
	public final void putPlus(int index, float addMe){
		//FIXME mem() or mem? mem() might be too slow to call many times
		mem().put(index,mem().get(index)+addMe);
	}
	
	/** write multiplyEqual f at index. See comment of get(int). */
	public final void putMult(int index, float multMe){
		//FIXME mem() or mem? mem() might be too slow to call many times
		mem().put(index,mem().get(index)*multMe);
	}
	
	/** write divideEqual f at index. See comment of get(int).
	This is probably slightly more accurate than putMult(int, 1/multMe).
	*/
	public final void putDivide(int index, float divideMe){
		//FIXME mem() or mem? mem() might be too slow to call many times
		mem().put(index,mem().get(index)/divideMe);
	}
	
	public Object clone(){
		FSyMem m = new FSyMem((DependParam)sy.clone(), memFactory);
		if(mem != null){
			arraycopy(mem, 0, m.mem(), 0, sy.size);
		}
		return m;
	}
	
	public float[] toFloatArray(){
		float[] ret = mem().array().clone();
		if(ret.length != size) throw new Error(
			"FloatBuffer size "+ret.length+" differs from mem() size "+size);
		return ret;
	}
	
	/** Same params as System.arraycopy except for FloatBuffers.
	<br><br>
	TODO optimize by using FloatBuffer.put(FloatBuffer),
	and make sure to put their positions capacities etc
	back the way they were before the copy except the
	range thats been copied.
	*/
	public static void arraycopy(FloatBuffer from, int fromIndex, FloatBuffer to, int toIndex, int len){
		for(int i=0; i<len; i++){
			to.put(toIndex+i, from.get(fromIndex+i));
		}
	}
	
	/** this.writer().accept(Mem) copies that Mem into my Mem */
	public Consumer<Mem> writer(){
		return (Mem m)->{
			if(m instanceof FSyMem){
				copy(((FSyMem)m).mem,mem());
			}else{
				throw new RuntimeException("TODO");
			}
		};
	}
	
	public static void copy(FloatBuffer from, FloatBuffer to){
		from.position(0);
		to.position(0);
		to.put(from);
	}

}
