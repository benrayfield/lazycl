/** Ben F Rayfield offers this software opensource MIT license */
package mutable.compilers.opencl.connectors.lwjgl;
import static mutable.util.Lg.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLDeviceCapabilities;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

//import mutable.compilers.opencl.Mem_OLD;
import mutable.compilers.opencl.OpenclUtil;
import mutable.util.JReflect;

/** Some parts modified from http://wiki.lwjgl.org/wiki/OpenCL_in_LWJGL.html#The_Full_Code
Wrapper of the OpenCL parts of LWJGL.
*/
public class Lwjgl{
	
	//TODO Should there be multiple CLCommandQueue? Could I get lower lag for threaded calls to opencl that way
	//such as if jsoundcard and the main thread both wanted to use opencl?
	//Just do 1 queue 1 java-synchronized for now. Look for more optimizations later.

	private static Lwjgl instance;
	
	private final CLContext context;
	private final CLPlatform platform;
	private final List<CLDevice> devices;
	private final CLCommandQueue queue;
	static{
		lgErr("FIXME gargcol Lwjgl.errorBuff in finalize()?");
	}
	final IntBuffer errorBuff = BufferUtils.createIntBuffer(1); //FIXME garbcol this in finalize()
	
	public CLContext context(){
		return context;
	}
	
	public CLCommandQueue queue(){
		return queue;
	}
	
	private static final Map<String,CompiledKernel> codeToCompiled = new WeakHashMap();
	
	public synchronized final CompiledKernel compiledOrFromCache(String kernelCode){
		//System.out.println("kernelCode="+kernelCode);
		CompiledKernel k = codeToCompiled.get(kernelCode);
		if(k == null){
			CLProgram prog = CL10.clCreateProgramWithSource(context, kernelCode, null);
			String compilerParams = "-cl-opt-disable"; //FIXME remove this?
			//String compilerParams = ""; //FIXME
			int error = CL10.clBuildProgram(prog, devices.get(0), compilerParams, null);
			//FIXME choose which of https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/clBuildProgram.html to use
			//	for determinism but without telling it to be slower than it has to
			//	for that determinism.
			Util.checkCLError(error);
			String kernName = OpenclUtil.findKernelName(kernelCode);
			CLKernel kern = CL10.clCreateKernel(prog, kernName, null);
			k = new CompiledKernel(kernelCode, prog, kern, kernName, error);
			codeToCompiled.put(kernelCode, k);
		}
		return k;
	}
	
	
	
	/** Lazyeval so (TODO verify) program doesnt depend on Lwjgl unless this is called.
	Calls lwjgl destructor on java finalize of this object.
	TODO also caching of opencl objects should be done in here.
	*/
	public static synchronized Lwjgl instance(){
		if(instance == null){
			try{
				instance = new Lwjgl();
			}catch(LWJGLException e){ throw new Error(e); }
		}
		return instance;
	}
	
	private Lwjgl() throws LWJGLException{
		try{
			Class.forName("org.lwjgl.opencl.CLObject");
		}catch(ClassNotFoundException e){
			throw new Error(e);
		}
		IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
		// Create OpenCL
		CL.create();
		// Get the first available platform
		platform = CLPlatform.getPlatforms().get(0); 
		// Run our program on the GPU
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
		//devices = platform.getDevices(CL10.CL_DEVICE_TYPE_ALL);
		//devices = platform.getDevices(CL10.CL_DEVICE_TYPE_CPU);
		for(CLDevice device : devices){
			lg("\r\nLwjgl.java device ("+device+") capabilities: "+new CLDeviceCapabilities(device)+"\r\n");
		}
		// Create an OpenCL context, this is where we could create an OpenCL-OpenGL compatible context
		context = CLContext.create(platform, devices, errorBuf);
		// Create a command queue
		queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
		// Check for any errors
		Util.checkCLError(errorBuf.get(0));
	}
	
	/** TODO I want a wrapper of OpenCL that starts with Object[] containing float[] and float[][],
	and then a sequence of calls to do that read some and write (copies of) others in that Object[],
	except it does all that in GPU and only at end copies it to new Object[] in java and returns it lowlag.
	https://forums.khronos.org/showthread.php/5810-calling-the-same-kernel-object-multiple-times
	says clEnqueueNDRangeKernel takes whatever params CLKernel has at the time so can queue
	same kernel multiple times with diff params.
	*/
	
	/*Considering CL10.CL_MEM_READ_ONLY : (CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR)
	implies that lwjgl doesnt need to copy between nio and CLMem every kernel and can queue many such ops,
	I want a redesigned wrapper that can forExample run 220 cycles of RNN per .01 second
	with a .01 second block of streaming microphone and other inputs
	like I talked about in https://www.reddit.com/r/gpgpu/comments/7u3sm9/can_opencl_run_22k_kernel_calls_per_second_each/
	where I would define RNN node states as readable and writable,
	and the RNN weights as only readable, and the block of .01 seconds of inputs as only readable.
	Even if (in the unlikely case) LWJGL doesnt allow that (which its way of organizing the objects implies it does),
	I still want my API to be able to support it efficiently in other implementations of OpenCL.
	It will probably work in LWJGL too.
	Can the same CLKernel be in the same queue multiple times during a single opencl call?
	If so, must it have the same CLMems and other params?
	https://forums.khronos.org/showthread.php/5810-calling-the-same-kernel-object-multiple-times
	*/
	
	/** TODO returns same size Object[] as params,
	with those that were written first copied (not modify params) and the others reused.
	<br><br>
	TODO Params can be Integer or float[] or float[][]. Maybe I'll add support for double arrays and more dims later.
	Contents of paramsRead concat contents of paramsWrite must be same order as in kernelCode.
	OLD: params must be Integer or float[] same order as in kernelCode.
	OLD: General enough for all possible single kernel of floats if you wrap multiple dimensions in float[]
	and use % and / to get the indexs. TODO caches compiled opencl objects for that String by WeakHashMap<String,...>.
	*/
	public synchronized Object[] callOpencl(String kernelCode, int[] ndRange, Object... params){
		boolean logDebug = true;
		if(ndRange.length > 3) throw new Error("ndRange.length=="+ndRange.length+" > 3");
		CompiledKernel k = compiledOrFromCache(kernelCode);
		//FIXME only allows each param to be readonly or writeonly but not both,
		//and while its probably inefficient to do both in the same param, its said to be allowed.
		boolean[] openclWritesParam = OpenclUtil.openclWritesParams(kernelCode); //TODO optimize by moving this into CompiledKernel
		if(openclWritesParam.length != params.length) throw new Error(
			"params.length="+params.length+" but openclWritesParam.length="+openclWritesParam.length);
		//Object clParam[] = new Object[params.length];
		CLMem[] clmems = new CLMem[params.length]; //null where param is Integer, nonnull where float[] or float[][] etc
		
		/*FloatBuffer[] floatBuffers = new FloatBuffer[params.length]; //null if Integer. reads wrap. writes are nio direct.
		//TODO adding support for doubles 2019-5. Ive read opencl may have unreliable double support on some computers.
		//TODO should I generalize to Buffer instead of FloatBuffer, DoubleBuffer,
		//and later I'll also want IntBuffer for graphics and IntBuffer or LongBuffer for ed25519 hashing?
		DoubleBuffer[] doubleBuffers = new DoubleBuffer[params.length];
		*/
		Buffer[] buffers = new Buffer[params.length]; //null if Integer. reads wrap. writes are nio direct.
		try{
			for(int i=0; i<params.length; i++){
				Object p = params[i];
				if(p instanceof Number){
					if(p instanceof Integer) k.kernel.setArg(i, (int)p);
					else if(p instanceof Float) k.kernel.setArg(i, (float)p);
					else if(p instanceof Double) k.kernel.setArg(i, (double)p);
					else throw new Error("TODO type "+p.getClass().getName());
				}else if(p instanceof float[] || p instanceof float[][]){
					
					//http://wiki.lwjgl.org/wiki/OpenCL_in_LWJGL.html "allocating memory"
					
					int size1d = p instanceof float[] ? ((float[])p).length : ((float[][])p).length*((float[][])p)[0].length;
					//floatBuffers[i] = FloatBuffer.wrap(fa);
					//floatBuffers[i] = ByteBuffer.allocateDirect(fa.length*4).asFloatBuffer();
					buffers[i] = BufferUtils.createFloatBuffer(size1d);
					//FIXME need nio direct floatbuffer for all of them? Or which?
					//System.out.println("openclWritesParam["+i+"]="+openclWritesParam[i]+" fb="+floatBuffers[i]+" paramI="+params[i]);
					if(openclWritesParam[i]){
						//System.out.println("IN param"+i+" NOT FILLING BUFFER SINCE OPENCL ONLY WRITES IT");
						if(logDebug) lg("clCreateBuffer context CL_MEM_READ_ONLY "+(size1d*4)+" erbuf="+errorBuff);
						clmems[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, size1d*4, errorBuff);
					}else{
						float[] fa = p instanceof float[] ? (float[])p : OpenclUtil.array2dTo1d((float[][])p);
						if(logDebug) lg("FloatBuffer put float[] then rewind");
						((FloatBuffer)buffers[i]).put(fa);
						((FloatBuffer)buffers[i]).rewind();
						//for(int j=0; j<fa.length; j++){
						//	//System.out.println("IN param"+i+" buf"+j+" = "+floatBuffers[i].get(j));
						//}
						if(logDebug) lg("clCreateBuffer context CL10.CL_MEM_WRITE_ONLY|CL10.CL_MEM_COPY_HOST_PTR "+buffers[i]+" erbuf="+errorBuff);
						clmems[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, (FloatBuffer)buffers[i], errorBuff);
					}
					if(logDebug) lg("checkCLError");
					Util.checkCLError(errorBuff.get(0)); //FIXME If theres an error erase it at end of call so can reuse errorBuff
					k.kernel.setArg(i, clmems[i]);
				}else if(p instanceof double[] || p instanceof double[][]){
					if(logDebug) lg("not logging details of doubles");
					int size1d = p instanceof double[] ? ((double[])p).length : ((double[][])p).length*((double[][])p)[0].length;
					buffers[i] = BufferUtils.createFloatBuffer(size1d);
					if(openclWritesParam[i]){
						clmems[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, size1d*4, errorBuff);
					}else{
						double[] fa = p instanceof double[] ? (double[])p : OpenclUtil.array2dTo1d((double[][])p);
						((DoubleBuffer)buffers[i]).put(fa);
						((DoubleBuffer)buffers[i]).rewind();
						clmems[i] = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, (DoubleBuffer)buffers[i], errorBuff);
					}
					Util.checkCLError(errorBuff.get(0)); //FIXME If theres an error erase it at end of call so can reuse errorBuff
					k.kernel.setArg(i, clmems[i]);
				}else{
					throw new Error("TODO upgrade OpenclUtil for type "+p.getClass().getName());
				}
			}
			if(logDebug) lg("PointerBuffer globalWorkSize");
			PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(ndRange.length);
			//FIXME free globalWorkSize
			for(int n=0; n<ndRange.length; n++){
				if(logDebug) lg("globalWorkSize put "+n+" "+ndRange[n]);
				globalWorkSize.put(n, ndRange[n]);
			}
			if(logDebug) lg("clEnqueueNDRangeKernel queue "+k.kernel+" "+ndRange.length+" null "+globalWorkSize+" null null null");
			CL10.clEnqueueNDRangeKernel(queue, k.kernel, ndRange.length, null, globalWorkSize, null, null, null);
			for(int i=0; i<params.length; i++){
				if(openclWritesParam[i]){
					if(buffers[i] instanceof FloatBuffer){
						if(logDebug) lg("clEnqueueReadBuffer queue "+clmems[i]+" CL_TRUE 0 "+buffers[i]+" null, null");
						CL10.clEnqueueReadBuffer(queue, clmems[i], CL10.CL_TRUE, 0, (FloatBuffer)buffers[i], null, null);
					}else if(buffers[i] instanceof DoubleBuffer){
						if(logDebug) lg("clEnqueueReadBuffer doubles");
						CL10.clEnqueueReadBuffer(queue, clmems[i], CL10.CL_TRUE, 0, (DoubleBuffer)buffers[i], null, null);
					}else{
						throw new Error("TODO upgrade OpenclUtil for type: "+buffers[i]);
					}
					
				}
			}
			if(logDebug) lg("clFinish queue");
			CL10.clFinish(queue);
			Object[] ret = new Object[params.length];
			for(int i=0; i<params.length; i++){
				if(logDebug) lg("copy param "+i+" from (somekindof)Buffer to array");
				Object p = params[i];
				//FIXME consider read or write here. reuse if not modified
				if(p instanceof Number){ //int, float (maybe others later)
					ret[i] = p;
				}else if(p instanceof float[]){
					float[] fa = new float[((float[])params[i]).length];
					FloatBuffer b = (FloatBuffer)buffers[i];
					b.rewind();
					for(int j=0; j<fa.length; j++){
						fa[j] = b.get(j);
					}
					ret[i] = fa;
				}else if(p instanceof double[]){
					double[] da = new double[((double[])params[i]).length];
					DoubleBuffer b = (DoubleBuffer)buffers[i];
					b.rewind();
					for(int j=0; j<da.length; j++){
						da[j] = b.get(j);
					}
					ret[i] = da;
				}else if(p instanceof float[][]){
					int outerDim = ((float[][])p).length;
					int innerDim = ((float[][])p)[0].length;
					float[][] faa = new float[outerDim][innerDim];
					FloatBuffer b = (FloatBuffer)buffers[i];
					b.rewind();
					int g = 0;
					for(int o=0; o<outerDim; o++){
						for(int j=0; j<innerDim; j++){
							faa[o][j] = b.get(g++);
						}
					}
					ret[i] = faa;
				}else if(p instanceof double[][]){
					int outerDim = ((double[][])p).length;
					int innerDim = ((double[][])p)[0].length;
					double[][] daa = new double[outerDim][innerDim];
					DoubleBuffer b = (DoubleBuffer)buffers[i];
					b.rewind();
					int g = 0;
					for(int o=0; o<outerDim; o++){
						for(int j=0; j<innerDim; j++){
							daa[o][j] = b.get(g++);
						}
					}
					ret[i] = daa;
				}else{
					throw new Error("TODO type "+p.getClass().getName());
				}
			}
			if(logDebug) lg("return "+Arrays.asList(ret));
			return ret;
		}finally{
			for(int i=0; i<params.length; i++){
				//FIXME? I read somewhere that nio is garbcoled by java even though not counted in the normal java mem,
				//so nothing to deallocate here?
				if(clmems[i] != null) CL10.clReleaseMemObject(clmems[i]);
			}
			//FIXME free globalWorkSize
			//FIXME free all these CLMems
			//FIXME free the FloatBuffers (or only the nio direct ones?)
			//FIXME should some or all o the FloatBuffers be nio direct? Which of them should be?
		}
	}
	
	public CLMem clmemWrapsJavaMem(FloatBuffer b){
		return CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_USE_HOST_PTR, b, errorBuff);	
	}
	
	/** caller must free the CLMem */
	public CLMem copy(float[] a){
		FloatBuffer b = BufferUtils.createFloatBuffer(a.length);
		b.put(a);
		b.rewind();
		//return CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, b, errorBuff);
		return CL10.clCreateBuffer(context, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR, b, errorBuff);
	}
	
	/** FIXME SECURITY: is there a problem in sharing errorBuff? What if it puts too many errors in it? Will it overflow? */
	public CLMem newClmemReadableAndWritable(int byteSize){
		return CL10.clCreateBuffer(context, CL10.CL_MEM_READ_WRITE, byteSize, errorBuff);
	}
	
	public CLMem newClmemReadonly(int byteSize){
		return CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, byteSize, errorBuff);
	}
	
	public void enqueueCopyClmemToFloatbuffer(CLMem mem, FloatBuffer buf){
		lg("clEnqueueReadBuffer CLMem="+mem+" FloatBuffer="+buf);
		CL10.clEnqueueReadBuffer(
			queue,
			mem,
			CL10.CL_TRUE, //blocking_read
			0L, //offset
			buf,
			null, //event_wait_list. https://www.khronos.org/registry/OpenCL/sdk/1.1/docs/man/xhtml/clEnqueueReadBuffer.html says can be null to not wait on anything.
			null //https://www.khronos.org/registry/OpenCL/sdk/1.1/docs/man/xhtml/clEnqueueReadBuffer.html says this can be null if dont need to ask if the event is finished later
		);
	}
	
	public void enqueueCopyFloatbufferToCLMem(FloatBuffer buf, CLMem mem){
		lg("clEnqueueWriteBuffer FloatBuffer="+buf+" CLMem="+mem);
		CL10.clEnqueueWriteBuffer(
			queue,
			mem, 
			CL10.CL_TRUE, //blocking_write
			0L, //offset
			buf, 
			null, //event_wait_list
			null //event
		);
	}
	
	/** TODO
	Examples: FloatBuffer, IntBuffer
	*
	public void enqueueCopyBufferToCLMem(Buffer buf, CLMem mem){
		lg("clEnqueueWriteBuffer Buffer="+buf+" CLMem="+mem);
		JReflect.call(CL10.class.getName()+".clEnqueueWriteBuffer",
			queue,
			mem, 
			CL10.CL_TRUE, //blocking_write
			0L, //offset
			buf, //a few different funcs depending on this type 
			null, //event_wait_list
			null //event
		);
	}*/
	
	/*public CLMem copyThenUseAsImmutable(float[][] in){
		return copyThenUseAsImmutable(FloatBuffer.wrap(OpenclUtil.array2dTo1d(in)));
	}
	
	public CLMem copyThenUseAsImmutable(float[] in){
		return copyThenUseAsImmutable(FloatBuffer.wrap(in));
	}
	
	/** FIXME How to enforce that this java.nio memory and GPU memory will be freed *
	public CLMem copyThenUseAsImmutable(FloatBuffer in){
		CLMem mem = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, in, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		return mem;
	}*/
	
	
	/** TODO separate this from the CLQueue and other opencl objects in this class, and make this static *
	public synchronized void clEnqueueNDRangeKernel(String kernelCode, int[] ndRange, Mem... params){
		CL10.clEnqueueNDRangeKernel(queue, k.kernel, ndRange.length, null, globalWorkSize, null, null, null);
		TODO
	}*/
	
	public static PointerBuffer pointerBufferOf(int... content){
		PointerBuffer p = BufferUtils.createPointerBuffer(content.length);
		for(int i=0; i<content.length; i++) p.put(i, content[i]);
		return p;
	}
	

}