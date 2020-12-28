/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;
import immutable.lazycl.impl.dependnet.LazyblobDependEdge;
import immutable.lazycl.spec.*;
import immutable.util.MathUtil;
import immutable.util.Text;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;

/** the first implementation of Lazycl.
Its planned to support opencl, java, and javascript, in windows and linux at least.
*/
public class LazyclPrototype_old implements Lazycl{
	
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
	
	private static Lazycl instance;
	public static Lazycl instance(){
		if(instance == null){
			synchronized(LazyclPrototype_old.class){
				if(instance == null){ //doubleCheckedLocking
					instance = new LazyclPrototype_old();
				}
			}
		}
		return instance;
	}
	
	public LazyBlob wrapc(Object o){
		return wrap(true,o);
	}
	
	public LazyBlob wrapb(Object o){
		return wrap(false,o);
	}
	
	protected LazyBlob wrap(boolean forceCopy, Object o){
		if(o instanceof LazyBlob) return (LazyBlob)o; //Dont copy even if forceCopy cuz LazyBlob supposed to be immutable
		if(o instanceof String){
			return wrap(forceCopy, Text.stringToBytes((String)o)); //utf8
		}else if(o instanceof byte[]){
			return new ByteArrayLB((byte[])o);
		}else if(o instanceof Integer){
			//TODO optimize by creating a differerent wrapper class for each primitive array type, and this would be int[1]
			//but for now just put everything in wrapper of byte[]
			return new ByteArrayLB(MathUtil.intToBytes((Integer)o));
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
	
	public LazyBlob wrap(Class primitiveArrayType, int size, IntToDoubleFunction wrapMe){
		if(primitiveArrayType == float[].class){
			return new LazyBlobIToDCastF(size, wrapMe);
		}else if(primitiveArrayType == double[].class){
			return new LazyBlobIToD(size, wrapMe);
		}
		throw new RuntimeException("TODO");
	}
	
	public LazyBlob wrap(IntToLongFunction wrapMe, int size, Class<?> primitiveArrayType){
		if(primitiveArrayType == long[].class){
			return new LazyBlobIToJ(size, wrapMe);
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
	
	public LazyBlob lazycl(Map<String,LazyBlob> params){
		String lang = lang(params);
		Function<Map<String,LazyBlob>,LazyBlob> factory = factories().get(lang);
		if(factory == null) throw new RuntimeException(
			"This "+Lazycl.class.getName()+" doesnt support lang["+lang+"] but others might");
		return factory.apply(params);
		
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
		);
		return defaultParams.get(name);
	}
	
	/** an error message */
	public LazyBlob err(String errStr){
		return new ErrorBlob(wrapc(errStr));
	}
	
	protected Map<String,Function<Map<String,LazyBlob>,LazyBlob>> factories;
	
	/** cached. immutable except possibly the inner workings of the factories.
	for each lang (such as "opencl1.2" or "download"), a factory that wraps a Map<String,LazyBlob> in a LazyBlob.
	*/
	protected Map<String,Function<Map<String,LazyBlob>,LazyBlob>> factories(){
		if(factories == null){
			synchronized(this){
				if(factories == null){ //doubleCheckedLocking
					Map<String,Function<Map<String,LazyBlob>,LazyBlob>> map = new HashMap();
					/*map.put("opencl1.2", (Map<String,LazyBlob> params)->{
						return new CLBlob(params); dont do that. CLMem is only used in OpenclUtil.callOpenclDependnet
					});*/
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
	}
	
	/** cached. immutable. */
	public Set<String> langs(){
		return factories().keySet();
	}

	public LazyBlob wrap(Class<?> primitiveType, int size, IntToLongFunction wrapMe){
		throw new RuntimeException("TODO");
	}

	public Set<LazyBlob> vm_lazys(){
		throw new RuntimeException("TODO");
	}

	public Set<LazyblobDependEdge> vm_dependnet(){
		throw new RuntimeException("TODO");
	}

	public Set<LazyblobDependEdge> vm_dependnet(Set<LazyBlob> observes){
		throw new RuntimeException("TODO");
	}

	public void vm_eval(Set<LazyblobDependEdge> evalThese){
		throw new RuntimeException("TODO");
	}

	public void vm_lazyBlobsEvalCallsThis(LazyBlob lb){
		throw new RuntimeException("TODO");
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
	
	/*public static LazyBlob lazycl(Map<String,LazyBlob> params){
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
