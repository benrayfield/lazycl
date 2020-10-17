package mutable.compilers.opencl.connectors.lwjgl;
import static mutable.util.Lg.*;
import java.io.Closeable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLMem;

import mutable.util.Time;

/** garbcols the CLMem when this is garbcoled */
public class PoolCLMem implements Closeable{
	
	public final CLMem mem;
	
	public int bytes;
	
	public final long id = Time.timeId();
	
	public PoolCLMem(int bytes){
		//this(bytes,Lwjgl.instance().newClmemReadonly(bytes)); //FIXME
		//lg("FIXME this is only for temporary tests of why callOpencl works but not callOpenclDependnet. callOpencl uses CLMems that are each either readable or writable but not both");
		this(bytes,Lwjgl.instance().newClmemReadableAndWritable(bytes));
	}
	
	public PoolCLMem(int bytes, CLMem mem){
		this.mem = mem;
		this.bytes = bytes;
	}
	
	protected void finalize(){
		close();
	}

	public void close(){
		CL10.clReleaseMemObject(mem);
	}
	
	public String toString(){
		return "[PoolCLMem id="+id+" bytes="+bytes+" clmem="+mem+"]";
	}

}
