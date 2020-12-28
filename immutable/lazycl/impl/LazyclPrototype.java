/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl;
import static mutable.util.Lg.*;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.DependParam;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.FSyMem;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.LockPar;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.Mem;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.ForkSize;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.SyMem;
import immutable.dependtask.DependOp;
import immutable.dependtask.LockState;
import immutable.lazycl.impl.blob.ByteArrayBlob;
import immutable.lazycl.impl.blob.FloatBufferBlob;
import immutable.lazycl.impl.blob.IToDBlob;
import immutable.lazycl.impl.blob.IToDCastFBlob;
import immutable.lazycl.impl.blob.IToJBlob;
import immutable.lazycl.impl.blob.IToJCastFBlob;
import immutable.lazycl.impl.blob.OneBitBlob;
import immutable.lazycl.impl.dependnet.LazyblobDependEdge;
import immutable.lazycl.spec.*;
import immutable.util.MathUtil;
import immutable.util.Text;
import mutable.compilers.opencl.OpenclUtil;
import mutable.downloader.Download;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.Supplier;

/** the first implementation of Lazycl.
Its planned to support opencl, java, and javascript, in windows and linux at least.
*/
public strictfp class LazyclPrototype implements Lazycl{
	
	/*TODO have fewer LazyBlob types.
		arrayBlob
		CpuBlob does download, javascript, java8WithJava4Syntax, etc
		maybe also gpuTempBlob
		gpuBlob
			has a Buffer. doesnt have a CLMem cuz thats done in OpenclUtil.callOpenclDependnet
		FunctionBlob
			wraps things like IntToDoubleFunction and IntToLongFunction.
		TryBlob
		ConcatBlob
	..
	TODO the wrapping, or lazy getting one later, of Buffer, needs redesign.
	..
	TODO first create Blob types for all the things it can wrap, including FloatBuffer, LongBuffer, float[],
		and maybe IntToDoubleFunction etc but maybe those should go in a shared superinterface.
		Then use SwapBlob or something like it to swap those blobs for whatever you need.
	
	
	
	Do I want a class with both Buffer (such as FloatBuffer or LongBuffer) and CLMem in it? Or SwapBlob?
			
	If SwapBlob is used,
	Should it contain a Map<String,LazyBlob> and a Blob (not LazyBlob) that exactly 1 of is null at a time?
	Is 2 blobs deep too slow for looping over each index and forexample getting f(int)?
			
	I might want just 1 class type for Lazycl.java to deal with on the outside,
		so that Map<String,LazyBlob> are easy to replace, but maybe ok to just put a vm_ func in
		the LazyBlob interface to replace that map. n
	*/ 
	
	/** remember LazyBlob.vm_lazyBlobsEvalCallsThis so eval can return there.
	Second and later calls of vm_lazyBlobsEvalCallsThis return null to enforce immutable after Lazycl first returns the LazyBlob.
	A LazyBlob is removed when return Blob to it using vm_returnBlobToLazyBlob.
	*/
	protected WeakHashMap<LazyBlob,Consumer<Blob>> evalReturnsTo = new WeakHashMap();
	
	private static Lazycl instance;
	public static Lazycl instance(){
		if(instance == null){
			synchronized(LazyclPrototype.class){
				if(instance == null){ //doubleCheckedLocking
					instance = new LazyclPrototype();
				}
			}
		}
		return instance;
	}
	
	protected void vm_returnBlobToLazyBlob(Blob ret, LazyBlob to){
		Consumer<Blob> c = evalReturnsTo.get(to);
		if(c == null) throw new RuntimeException(
			"Cant return to "+to+" This is probably cuz already returned to there once or it was created somewhere other than in this Lazycl");
		c.accept(ret);
		evalReturnsTo.remove(to);
	}
	
	public LazyBlob wrapc(Object o){
		return wrap(true,o);
	}
	
	public LazyBlob wrapb(Object o){
		return wrap(false,o);
	}
	
	protected Blob wrapbInBlob(Object o){
		if(o instanceof byte[]) return new ByteArrayBlob((byte[])o);
		throw new RuntimeException("TODO "+o.getClass().getName());
	}
	
	/** a 1 bit */
	protected final Blob trueAsBlob = new OneBitBlob(true);
	
	/** a 0 bit */
	protected final Blob falseAsBlob = new OneBitBlob(false);
	
	protected final LazyBlob trueAsLazyBlob = lb(trueAsBlob);
	
	protected final LazyBlob falseAsLazyBlob = lb(falseAsBlob);
	
	protected LazyBlob wrap(boolean forceCopy, Object o){
		if(o instanceof LazyBlob) return (LazyBlob)o; //Dont copy even if forceCopy cuz LazyBlob supposed to be immutable
		if(o instanceof Boolean){
			return (Boolean)o ? trueAsLazyBlob : falseAsLazyBlob;
			
		}else if(o instanceof String){
			return wrap(forceCopy, Text.stringToBytes((String)o)); //utf8
		}else if(o instanceof byte[]){
			if(forceCopy) o = ((byte[])o).clone();
			return new SimpleLazyBlob(new ByteArrayBlob((byte[])o));
		}else if(o instanceof Integer){
			//TODO optimize by creating a differerent wrapper class for each primitive array type, and this would be int[1]
			//but for now just put everything in wrapper of byte[]
			return new SimpleLazyBlob(new ByteArrayBlob(MathUtil.intToBytes((Integer)o)));
		}else if(o instanceof Long){
			return new SimpleLazyBlob(new ByteArrayBlob(MathUtil.longToBytes((Long)o)));
		}else if(o instanceof Buffer){
			if(o instanceof FloatBuffer){
				if(forceCopy){
					//o = ((?)o).clone();
					throw new RuntimeException("TODO copy");
				}
				return lb(new FloatBufferBlob((FloatBuffer)o));
			}
			//TODO IntBuffer etc
		}else if(o instanceof SyMem){
			return wrap(forceCopy, ((SyMem)o).mem()); //Example: wrap FloatBuffer or IntBuffer
		}
		throw new RuntimeException("TODO type "+o.getClass().getName());
		/*TODO should String be wrapped as utf8 byte array? or char/short array?
		
		TODO should float[] be wrapped as float[] or FloatBuffer? Should it be copied to prove immutable or used as it is?
			Should there be 2 funcs, one that copies and one that trusts caller not to modify it?
		
		throw new RuntimeException("TODO wrap "+o);
		

		TODO should String be wrapped as utf8 byte array? or char/short array?
		
		TODO should float[] be wrapped as float[] or FloatBuffer? Should it be copied to prove immutable or used as it is?
			Should there be 2 funcs, one that copies and one that trusts caller not to modify it?
		
		throw new RuntimeException("TODO wrap "+o);
		*/
	}
	
	protected LazyBlob lb(Blob b){
		if(b instanceof LazyBlob) return (LazyBlob) b;
		return new SimpleLazyBlob(b);
	}
	
	public LazyBlob wrap(Class primitiveType, int size, IntToDoubleFunction wrapMe){
		if(primitiveType == float.class){
			return lb(new IToDCastFBlob(size, wrapMe));
		}else if(primitiveType == double.class){
			return lb(new IToDBlob(size, wrapMe));
		}
		throw new RuntimeException("TODO");
	}
	
	public LazyBlob wrap(Class<?> primitiveType, int size, IntToLongFunction wrapMe){
		if(primitiveType == long.class){
			return lb(new IToJBlob(size, wrapMe));
		}else if(primitiveType == float.class){
			return lb(new IToJCastFBlob(size, wrapMe));
		}
		throw new RuntimeException("TODO");
	}
	

	/** whats before the first colon in Code */
	public String lang(Map<String,LazyBlob> params){
		/*FIXME might need to trigger lazyeval of Code, despite in readme (update that?) it says
		code can be lazyevaled, ONLY cuz need to know which things to eval together
		(such as multiple opencl ndrange kernels before returning to cpu).
		Could I put a bit in that says needs to be grouped, and that bit is evaled right away?
		*/
		String code = str(param(params,"Code"));
		int i = code.indexOf(':');
		if(i == -1) throw new RuntimeException("No colon in code: "+code);
		return code.substring(0,i).trim();
	}

	/** value of param "Code" is lang(x)+":"+afterLangColon(x) */
	public String afterLangColon(Map<String,LazyBlob> params){
		String code = str(param(params,"Code"));
		int i = code.indexOf(':');
		if(i == -1) throw new RuntimeException("No colon in code: "+code);
		return code.substring(i+1).trim();
	}
	
	public LazyBlob lazycl(Map<String,LazyBlob> params){
		LazyBlob lb = new SimpleLazyBlob(this,params);
		//prevent others from getting that Consumer<Blob>. Only this Lazycl can return to it.
		evalReturnsTo.put(lb, lb.vm_evalReturnsToHere());
		return lb;
		
		//Supplier<Blob> eval = TODO;
		
		/*String lang = lang(params);
		Function<Map<String,LazyBlob>,LazyBlob> factory = factories().get(lang);
		if(factory == null) throw new RuntimeException(
			"This "+Lazycl.class.getName()+" doesnt support lang["+lang+"] but others might");
		return factory.apply(params);
		*/
		
		/*
		if(!langs().contains(lang)) throw new RuntimeException(
			"This "+Lazycl.class.getName()+" doesnt support lang["+lang+"] but others might");
		
		/*FIXME check if all the nonevaled (!LazyBlob.isEvaled()) parts are opencl.
		If not, then must do multiple calls, such as evaling java, then evaling javascript, then evaling 20 opencl blobs.
		For now, just check if all the nonevaled parts are opencl.
		*/
		/*are all nonevaled parts opencl?
		FIXME redesign vm_evalWhichIf for multiple groups,
			considering that doing the deepest group can remove things from other groups by evaling those
			so th ey dont need to be evaled again so are removed from those groups to eval together.
			Need a way to detect those borders.
			Probably only gpu code (such as lang="opencl1.2") needs to be done in a group (for lower lag).
			Other code, such as lang="download" or lang="java8WithJava4Syntax" can be done 1 at a time
			or in parallel but without needing to done together.
			TODO find that beanshell that claims to contain a java compiler, and what version of java syntax does it use?
				Can I get rid of java8WithJava4Syntax and have java 8 syntax?
				Beanshell seems to require files for compiling, though I havent looked into it much,
					compared to javassist which can do it purely in memory which is better.
			
		else throw new RuntimeException("TODO do multiple groups of evals, one group at a time");
		*/
		
		//throw new RuntimeException("TODO params="+params);
	}
	
	/*&public Set<LazyBlob> vm_evalWhichIf(LazyBlob observe){
		throw new RuntimeException("TODO");
	}
	
	public boolean vm_isValidSetToEval(Set<LazyBlob> evalThese){
		throw new RuntimeException("TODO");
	}
	
	public void vm_eval(Set<LazyBlob> evalThese){
		if(!vm_isValidSetToEval(evalThese)) throw new RuntimeException("Invalid set to eval: "+evalThese);
		throw new RuntimeException("TODO");
	}*/
	
	public LazyBlob param(Map<String,LazyBlob> params, String name){
		LazyBlob b = params.get(name);
		return b!=null ? b : defaultParam(name);
	}
	
	/** utf8 */
	public static String str(LazyBlob b){
		return Text.bytesToStr(b.arr(byte[].class));
	}
	
	/** the map comes from LazyBlob.lazyCall(), which becomes null after its evaled.
	Example: param(someLazyBlob.lazyCall(),"IsTemp") where !someLazyBlob.lazyCall().containsKey("IsTemp"),
	and that returns the default for isTemp which is 0 (false), instead of 1 (true).
	*
	public static LazyBlob param(Map<String,LazyBlob> lazyCall, String name){
	}*/
	Map<String,LazyBlob> defaultParams;
	public LazyBlob defaultParam(String name){
		if(defaultParams == null) defaultParams = map(
			"IsTemp", wrapc(false),
			//-cl-opt-disable does not seem to make it slower but tells opencl to do strictfp etc.
			"OpenclCompileParams", wrapc("-cl-opt-disable"), //FIXME should wrapping a string be utf16 or utf8?
			"IsJavaStrictfp", wrapc(true),
			"IsJavascriptUseStrict", wrapc(true)
			//default LocalSize is null so opencl chooses it
		);
		return defaultParams.get(name);
	}
	
	/** an error message *
	public LazyBlob err(String errStr){
		return new ErrorBlob(wrapc(errStr));
	}*/
	
	/*
	protected Map<String,Function<Map<String,LazyBlob>,LazyBlob>> factories;
	
	/** cached. immutable except possibly the inner workings of the factories.
	for each lang (such as "opencl1.2" or "download"), a factory that wraps a Map<String,LazyBlob> in a LazyBlob.
	*
	protected Map<String,Function<Map<String,LazyBlob>,LazyBlob>> factories(){
		if(factories == null){
			synchronized(this){
				if(factories == null){ //doubleCheckedLocking
					Map<String,Function<Map<String,LazyBlob>,LazyBlob>> map = new HashMap();
					//map.put("opencl1.2", (Map<String,LazyBlob> params)->{
					//	return new CLBlob(params); dont do that. CLMem is only used in OpenclUtil.callOpenclDependnet
					//});
					Function<Map<String,LazyBlob>,LazyBlob> cpuFac = (Map<String,LazyBlob> params)->{
						return new CpuBlob(params);
					};
					map.put("java8WithJava4Syntax", cpuFac);
					map.put("javascript", cpuFac);
					map.put("try", (Map<String,LazyBlob> params)->{
						return new TryLB(params);
					});
					map.put("concat", (Map<String,LazyBlob> params)->{
						return new ConcatBlob(params);
					});
					map.put("download", (Map<String,LazyBlob> params)->{
						return new DownloadBlob(params);
					});
					factories = Collections.unmodifiableMap(map);
				}
			}
		}
		return factories;
	}*/
	
	protected Set<String> langs = Text.whitespaceDelimitedTokenSet(
		"opencl1.2 java8WithJava4Syntax javascript try concat download"); //TODO
	
	/** cached. immutable. */
	public Set<String> langs(){
		return langs;
		//return factories().keySet();
	}

	/*public Set<LazyBlob> vm_lazys(){
		throw new RuntimeException("TODO");
	}

	public Set<DependEdge> vm_dependnet(){
		throw new RuntimeException("TODO");
	}

	public Set<DependEdge> vm_dependnet(Set<LazyBlob> observes){
		throw new RuntimeException("TODO");
	}*/

	protected void vm_evalInPotentiallyMultipleGroups(Set<LazyblobDependEdge> evalThese){
		throw new RuntimeException("TODO");
	}
	
	/** called by LazyBlob.eval(). When a LazyBlob lb is created by Lazycl lc,
	lc remembers (Consumer<Blob>) lb.vm_evalReturnsToHere() so this func vm_lazyBlobsEvalCallsThis(lb)
	can return Blob to it and other Blobs to others that are evaled during it.
	 */
	public void vm_lazyBlobsEvalCallsThis(LazyBlob lb){
		if(!lb.vm_isEvaled()){
			while(!evalReturnsTo.isEmpty()){ //while theres any LazyBlob not evaled yet
				Set<LazyBlob> lbs = new HashSet(evalReturnsTo.keySet());
				
				//FIXME. for now doing it a slow way. just eval 1 LazyBlob at a time after linear search of whichever can be evaled next.
				for(LazyBlob x : lbs){
					if(!x.vm_isEvaled() && x.vm_dependenciesAreEvaled()){
						vm_evalOneTheSlowWayNotInGroups(x);
					}
				}
				
				//TODO eval in groups for lower lag. Going back and forth between CPU and GPU is expensive,
				//so for example if 50 opencl ndrange kernels start with Code of "opencl1.2:" then can
				//run all of them in lwjgl opencl before returning the blobs to CPU which !isTemp.
				//The isTemp blobs, in that case, exist only as CLMem objects, never as FloatBuffer etc.
				//Most blobs will be isTemp, so that extremely cuts down on data copied between CPU and GPU.
				//PoolCLMem extremely reduces lag by reusing/pooling CLMems.
			}
		}
	}
	
	protected void vm_evalOneTheSlowWayNotInGroups(LazyBlob x){
		if(x.vm_isEvaled()) return;
		if(!x.vm_dependenciesAreEvaled()) throw new RuntimeException("Dependencies not evaled of "+x);
		
		Map<String,LazyBlob> params = x.vm_lazyCall(); //know this is nonnull cuz !vm_isEvaled. FIXME thread?
		String lang = lang(params);
		Blob outBlob;
		if("opencl1.2".equals(lang)){
			String langColonCode = str(param(params,"Code"));
			String openclCode = afterLangColon(params);
			//openclCode starts with ( ends with }
			//openclCode's first param is output Blob (readOnly after returned here). All others are readOnly.
			//Params that start with a capital letter (like "Code") are lazycl params.
			//Params that start with lowercase (like "code" or "xyz") are params of the function defined in Code string
			//and are either in Map<String,LazyBlob> params or defaultParam(...) is used.
			
			
			
			List<String> ndrangeParamNames = OpenclUtil.getParamNames(openclCode);
			//String outBlobParamName = ndrangeParamNames.get(0);
			List<String> ndrangeParamTypes = OpenclUtil.getParamTypes(openclCode); //Example types: float* float int* int double* double
			boolean[] ndrangeWritesParams = OpenclUtil.openclWritesParams(openclCode);
			//FIXME also check if reads it cuz dont want it to read the output blob cuz then its not stateless
			//cuz is reading from something lazycl didnt (this time) put there (garbage in memory from whatever used it last?).
			
			//the params other than the first one (output blob). TODO optimize: many ndrange calls at once.
			SortedMap<DependParam,Mem> ins = new TreeMap();
			
			//the task of that 1 openclCode. TODO optimize: many ndrange calls at once.
			//Set<DependOp> tasks = new TreeSet();
			Set<DependOp> tasks = new HashSet();
			
			//the first ndrange param is output blob, in this case. TODO optimize: many ndrange at once, but only the !isTemp
			Set<DependParam> outs = new TreeSet(); 
			
			//1 for each ndrange param. The first one is output blob so make that one up except its type and size.
			//The rest are LazyBlobs from Map<String,LazyBlob> params.
			//A DependParam is either a wrapper of a primitive or a symbol that externally is mapped to memory to be read andOr written.
			List<DependParam> dependParams = new ArrayList();
			List<LockPar> lockDependParams = new ArrayList();
			for(int i=0; i<ndrangeParamNames.size(); i++){
				boolean isOutputBlob = i==0;
				if(ndrangeWritesParams[i] != isOutputBlob) throw new RuntimeException(
					"ndrangeWritesParams["+i+"] != isOutputBlob. Must write first param, and read all others. in "+params);
				
				//The type the ndrange function wants is not necessarily same as Blob.prim().
				//For safety, use ndrangeParamTypes instead, since Blob is a bitstring and can be viewed as any of those types raw bits.
				
				String paramName = ndrangeParamNames.get(i);
				if(ndrangeWritesParams[i]){
					String outType = ndrangeParamTypes.get(i);
					Class outputEltype; //example: float.class [for FloatBuffer or float[]]
					long bize = params.get("Bize").j(0);
					int outputSizeInUnitsOfEltype;
					if("float*".equals(outType)){
						outputEltype = float.class;
						outputSizeInUnitsOfEltype = (int)(bize/32); //FIXME check for exceeds int range
					}else if("double*".equals(outType)){
						outputEltype = double.class;
						outputSizeInUnitsOfEltype = (int)(bize/64); //FIXME check for exceeds int range
					}else if("int*".equals(outType)){
						outputEltype = int.class;
						outputSizeInUnitsOfEltype = (int)(bize/32); //FIXME check for exceeds int range
					}else if("long*".equals(outType)){
						outputEltype = long.class;
						outputSizeInUnitsOfEltype = (int)(bize/64); //FIXME check for exceeds int range
					}else{
						throw new RuntimeException("TODO type "+outType);
					}
					DependParam dp = new DependParam(paramName, outputEltype, outputSizeInUnitsOfEltype);
					dependParams.add(dp);
					outs.add(dp);
					lockDependParams.add(new LockPar(LockState.writeLock, dp));
				}else{ //read
					LazyBlob paramVal = param(params,paramName);
					if(paramVal == null) throw new RuntimeException("No param named "+paramName);
					Class inputEltype;
					long bize = paramVal.bize(); //FIXME in SimpleLazyBlob bize() shouldnt trigger lazyEval, but it appears 2020-11-25 it does
					int inputSizeInUnitsOfEltype;
					Number paramValAsNumber = null; //either this or array
					String inType = ndrangeParamTypes.get(i);
					if("float*".equals(inType)){
						inputEltype = float.class;
						inputSizeInUnitsOfEltype = (int)(bize/32); //FIXME check for exceeds int range
					}else if("int*".equals(inType)){
						inputEltype = int.class;
						inputSizeInUnitsOfEltype = (int)(bize/32); //FIXME check for exceeds int range
					}else if("int".equals(inType)){
						inputEltype = int.class;
						inputSizeInUnitsOfEltype = 32;
						paramValAsNumber = paramVal.i(0);
					}else{
						throw new RuntimeException("TODO type "+inType);
					}
					
					//TODO if paramVal is a single primitive (like a float or long)
					//then use that constructor of DependParam so that constant instead of a CLMem is used in ndrange call
					
					DependParam dp = paramValAsNumber!=null
						? new DependParam(paramName, paramValAsNumber) //literal
						: new DependParam(paramName, inputEltype, inputSizeInUnitsOfEltype);
					dependParams.add(dp);
					Mem val = paramValAsNumber!=null
						? dp //literal Number, small enough to fit in param of opencl ndrange kernel byValue (instead of byReference)
						: wrapBlobInSyMem(dp, paramVal);
					
					if(val instanceof SyMem){ //test. todo remove this
						FloatBuffer test = ((FloatBuffer)((SyMem)val).mem());
						for(int j=0; j<test.capacity(); j++){
							lg("in FloatBuffer["+j+"]="+test.get(j));
						}
					}
					
					ins.put(dp, val);
					lockDependParams.add(new LockPar(LockState.readLock, dp));
				}
			}
			
			tasks.add(new DependOp(langColonCode, forkSize(params), lockDependParams.toArray(new LockPar[0])));
			
			//callOpenclDependnet can do many opencl ndrange kernels before returning to CPU for lower lag,
			//but for now just doing 1. TODO optimize
			SortedMap<DependParam,Mem> openclReturned = OpenclUtil.callOpenclDependnet(ins, tasks, outs);
			Mem outMem = openclReturned.get(dependParams.get(0));
			
			//todo remove this[
			FloatBuffer test = ((FloatBuffer)((SyMem)outMem).mem());
			for(int i=0; i<test.capacity(); i++){
				lg("out FloatBuffer["+i+"]="+test.get(i));
			}
			//]
			
			outBlob = wrapb(outMem);
		}else if("java8WithJava4Syntax".equals(lang)){
			//use javassist andOr beanshell
			//outBlob = TODO;
			throw new RuntimeException("TODO syntax of a static java func that returns Blob, excluding function name, starting with '(' and ending with '}'");
		}else if("javascript".equals(lang)){
			//outBlob = TODO;
			throw new RuntimeException("TODO similar to java8WithJava4Syntax except maybe using Uint8Array etc, if thats available outside of browser");
		}else if("try".equals(lang)){
			//outBlob = TODO; Wait, does that require multiple blobs be evaled?
			throw new RuntimeException("TODO");
		}else if("concat".equals(lang)){
			//outBlob = TODO; Wait, should this just be a view of 2 blobs that offsets the index then recurses?
			throw new RuntimeException("TODO return a Blob thats a wrapper of multiple (just 2 at a time?) blobs");
		}else if("download".equals(lang)){
			//WARNING: this one seems not reliably immutable since a http(s) get can give different bytestrings
			//at different times or depending what address is downloading etc.
			
			//synchronous. Code like download:https://upload.wikimedia.org/wikipedia/commons/a/a3/Ice_water_vapor.jpg
			//or download:file:///c:/path/to/the%20file.txt
			String url = afterLangColon(params);
			outBlob = wrapbInBlob(Download.downloadWithDefaultOptions(url));
		}else{
			throw new RuntimeException("Unknown lang: "+lang+" in "+x);
		}
		vm_returnBlobToLazyBlob(outBlob, x); //x is no longer lazy. can use x.f(int) etc, which may be what triggered this eval.
	}
	
	/** as of 2020-12-28 only applies for opencl* Code, and maybe later some other kinds of Code will have a ForkSize */
	public ForkSize forkSize(Map<String,LazyBlob> params){
		int[] globalSize = param(params, "GlobalSize").arr(int[].class);
		LazyBlob localSizeBlob = param(params, "LocalSize"); //null if not in map, to let opencl choose
		int[] localSize = localSizeBlob!=null ? localSizeBlob.arr(int[].class) : null;
		return new ForkSize(globalSize, localSize);
	}
	
	protected Mem wrapBlobInSyMem(DependParam sy, Blob b){
		return new SyMem<FloatBuffer>(sy, b.arr(FloatBuffer.class));
	}
	
	
	
	/*Set<String> langs;
	public Set<String> langs(){
		//TODO java8, but currently I only know how to implement java lambdas in beanshell
		//by using the implements keyword instead of lambda syntax directly.
		if(langs == null) langs = Collections.unmodifiableSet(new TreeSet(Arrays.asList(
			"opencl1.2", "java8WithJava4Syntax", "javascript",
			"download", "concat", "try")));
		return langs;
	}*/
	
	/*public static LazyBlob lazyclMap<String,LazyBlob> params){
		return lz.lazycl(params);
	}
	
	public static LazyBlob ints(int... bits){
		return wrap(bits);
	}
	
	/** TODO object types include FloatBuffer, CLMem, long[], String, etc *
	public static LazyBlob wrap(Object o){
		throw new RuntimeException("TODO");
	}*/
	
	/*TODO instead of call of List<LazyBlob>, should it be of Map<String,lazyBlob>?
			Of course you can write a map as alternating key val key val.
		YES.
	*/
	
	/*public static LazyBlob call(LazyBlob... funcAndParams){
		List<LazyBlob> list = Collections.unmodifiableList(new ArrayList(Arrays.asList(funcAndParams))); //immutable
		TODO
	}
	
	public static LazyBlob call(Object... funcAndParams){
		LazyBlob[] a = new LazyBlob[funcAndParams.length];
		for(int i=0; i<funcAndParams.length; i++) a[i] = wrap(funcAndParams[i]);
		return call(a);
	}*/

}
