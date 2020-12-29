/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.spec;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;

import immutable.opencl.OpenCL;
import immutable.util.Text;

/** A cross-platform simple low lag lazyEvaled number crunching system (TODO).
NOT sandboxed, but could be sandboxed by formalVerification on what possible
statements to eval, which is planned in occamsfuncer, and it has other uses
as a normal nonsandboxed number crunching system such as gru and lstm
neuralnets and display of 3d mandelbrot fractal in realtime, etc.
Blobs are immutable and 1d, similar to memory ranges,
but you can use 1-3d globalSize and localSize opencl ndrange kernels.
*/
public strictfp interface Lazycl{
	
	/** get the implementation of OpenCL which this Lazycl generates ndrange kernel calls for.
	As explained in the comments of the OpenCL interface its more general than Lazycl
	in what it can optimize but Lazycl can do all the same things,
	mostly that Lazycl only has 1 output array per ndrange kernel call
	and encapsulates the read and write locking of combos of mems, while OpenCL gives access to that.
	<br><br>
	If you want to port Lazycl to use a different implementation of opencl,
	such as to use the AMD C++ opencl code instead of LWJGL, you just create another OpenCL instance
	and for example use that in the same immutable.lazycl.impl.LazyclPrototype constructor,
	or potentially you could even wrap non-opencl GPU libraries in that OpenCL interface
	such as in theory a CUDA backed implementation of OpenCL ndrange kernels
	or backed by GPU.js thru a localhost server to reach the browser where the GPU calculations happen.
	*/
	public OpenCL opencl();

	/**
	 * lazy call of blobs each as a named param, that returns a blob. Normally
	 * returns instantly since it doesnt do the work until observed inside LazyBlob.
	 * <br>
	 * <br>
	 * Common keys include: Code IsTemp OpenclCompileParams <param name in lambda in
	 * code> To avoid naming conflict, lazycl params start with capital letter, and
	 * params in Code string should start with lowercase letter, so if more lazycl
	 * params are added later, they dont break your existing opencl or java code
	 * strings. <br>
	 * <br>
	 * OLD... TODO put isTemp param, and maybe other options such as strictfp and
	 * opencl compiler options, but certainly need at least isTemp, as 1 of the
	 * params in call(...). Put it in "isTemp" key of Map<String,LazyBlob>. TODO get
	 * the strictfp etc related options from humanAiNetNeural code, if its not
	 * already there in the OpenclUtil copied. Allow caller to choose from those
	 * options but default it to strict.
	 */
	public LazyBlob lazycl(Map<String, LazyBlob> params);

	/**
	 * wrap a copy of it, so caller can optionally later modify the param without
	 * modifying the returned LazyBlob. throws if type not supported.
	 */
	public LazyBlob wrapc(Object o);

	/**
	 * wrap either by copying or directly as a Backing array. Its caller's
	 * responsibility not to modify the array after wrapb. throws if type not
	 * supported.
	 */
	public LazyBlob wrapb(Object o);

	/**
	 * Example: lz.wrap(float.class, b*c, (int
	 * i)->i*i*i-7*i*i+3).arr(float.class)[x] -> x*x*x-7*x*x+3, where 0 <= x <=
	 * b*c-1.
	 */
	public LazyBlob wrap(Class<?> primitiveType, int size, IntToDoubleFunction wrapMe);

	// TODO use BlobType instead of "Class<?> primitiveArrayType, int size"?
	// TODO wrap(Class<?> primitiveArrayType, int size, Object wrapMe)?

	public LazyBlob wrap(Class<?> primitiveType, int size, IntToLongFunction wrapMe);

	public default Map<String, LazyBlob> map(Object... alternateKeyValKeyVal){
		NavigableMap<String, LazyBlob> ret = new TreeMap();
		if ((alternateKeyValKeyVal.length & 1) == 1)
			throw new RuntimeException("odd size. must be key val key val...");
		for (int i = 0; i < alternateKeyValKeyVal.length; i += 2) {
			Object key = alternateKeyValKeyVal[i];
			LazyBlob val = wrapc(alternateKeyValKeyVal[i + 1]); // become LazyBlob if its not already
			if (!(key instanceof String))
				throw new RuntimeException("i=" + i + " is not a " + String.class.getName() + ": " + key);
			// if(!(val instanceof LazyBlob)) throw new RuntimeException("i="+i+" is not a
			// "+LazyBlob.class.getName()+": "+val);
			ret.put((String) key, (LazyBlob) val);
		}
		return Collections.unmodifiableNavigableMap(ret);
	}

	/**
	 * same as lazycl(Map<String,LazyBlob> params) but written as alternating key
	 * val key val
	 */
	public default LazyBlob lazycl(Object... alternateKeyValKeyVal){
		Map<String, LazyBlob> m = map(alternateKeyValKeyVal);
		LazyBlob Bize = m.get("Bize");
		if (Bize != null && Bize.bize() != 64)
			throw new RuntimeException("Bize (if it exists) must be a long (64 bits) but is " + Bize.bize() + " bits");
		return lazycl(m);
	}

	/**
	 * For convenience can use when theres only a Code param. Example:
	 * LazyBlob bytes = lazycl("download:https://upload.wikimedia.org/wikipedia/commons/a/a3/Ice_water_vapor.jpg");
	 */
	public default LazyBlob lazycl(String Code){
		return lazycl(map("Code", Code));
	}

	/**
	 * set of LazyBlobs that arent evaled yet and were created by this Lazycl. You
	 * can use any subset of these as param of vm_dependnet(Set<LazyBlob> observes).
	 * You can also include those which are already evaled in that param but they
	 * will be ignored.
	 *
	 * public Set<LazyBlob> vm_lazys();
	 */

	/**
	 * automatically called by LazyBlob.f(int) or LazyBlob.IN().read(...) etc when
	 * try/catch(NullPointerException). <br>
	 * <br>
	 * If observe inside the param LazyBlob, then which LazyBlobs will be
	 * automatically evaled? Clearly those which it depends on must be evaled except
	 * those already evaled, but others which depend on them but dont have to be
	 * evaled yet may be evaled anyways to avoid recalculating shared dependencies,
	 * especially if those dependencies isTemp which forExample could mean its still
	 * in a CLMem or could mean it was in a CLMem that was returned to a pool. <br>
	 * <br>
	 * The simplest strategy is to wait for anything to wait for the first observe
	 * (excluding things already in cpu reachable memory like float[] or in some
	 * cases FloatBuffer) then eval everything and garbcol all the isTemp pieces of
	 * gpu memory (return CLMems to pool). Thats the first thing I'll build
	 * 2020-10-20+. <br>
	 * <br>
	 * A problem with this is it couled wait too long and not have enough gpu memory
	 * to do all those calculations, so in that case the better thing to do would be
	 * to copy some of the isTemps and !isTemps to cpu memory (or even to harddrive
	 * if thats not enough) between multiple calls of
	 * OpenclUtil.callOpenclDependnet. <br>
	 * <br>
	 * A more advanced strategy would be to leave some things in CLMems and only
	 * queue them to be copied to cpu memory (like FloatBuffer) as needed, so could
	 * do multiple opencl calls between multiple CLMem without giving those CLMems
	 * back to the pool. <br>
	 * <br>
	 * In any case, it should still be immutable as viewed from LazyBlobs which
	 * encapsulate those uses of mutable memory. That will be enforced by OpenclUtil
	 * having parsing functions to check which memories are readable and writable,
	 * around the "const" keyword, except that doesnt prevent the use of OpenclUtil
	 * directly to access those CLMems, but it wont happen just by use of LazyBlobs.
	 *
	 * public Set<LazyBlob> vm_evalWhichIf(LazyBlob observe);
	 *
	 * public Set<DependEdge> vm_dependnet();
	 */

	/**
	 * whatever is reachable from any of those and is not already evaled * public
	 * Set<DependEdge> vm_dependnet(Set<LazyBlob> observes);
	 */

	/*
	 * FIXME redesign to only eval deepest group first, since not all LazyBlobs can
	 * be evaled in a group together, and evaling the deepest group may change which
	 * set of lazyblobs (a group) is to be evaled next. Prototype this using a combo
	 * of java and opencl blobs that cant all be evaled at once. Put all parent and
	 * child which are the same language (such as lang="opencl1.2" or
	 * lang="java8WithJava4Syntax" or lang="download" in the same group, and only
	 * run 1 group at a time.
	 */

	/**
	 * true if vm_eval(ifEvalThese) is not known to fail, but may still fail for
	 * unexpected reasons like costing too much gpu memory or the Code string in
	 * some LazyBlob doesnt compile or contains an infinite loop etc.
	 *
	 * public boolean vm_isValidSetToEval(Set<LazyBlob> ifEvalThese);
	 */

	/**
	 * UPDATE: doesnt have to be the same set as returned by vm_dependnet but cant
	 * be just any subset of it. The allowed subsets are those were if LazyBlob x is
	 * in the set and x depends on y then y is either already evaled or in the set.
	 * <br>
	 * <br>
	 * Automatically called by LazyBlob.f(int) or LazyBlob.IN().read(...) etc when
	 * try/catch(NullPointerException). Not every Set<LazyBlob> is valid since it
	 * must include all dependencies of all in the Set.
	 *
	 * public void vm_eval(Set<LazyBlob> evalThese);
	 *
	 * public void vm_eval(Set<DependEdge> evalThese);
	 */

	/**
	 * If that LazyBlob isnt already evaled, causes eval of it and any
	 * not-evaled-yet dependencies and depending on the implementation of Lazycl may
	 * also eval things which share those dependencies such as all the LazyBlobs
	 * created so far that arent evaled yet. TODO May eval them in groups (those
	 * connected by LazyblobDependEdge.together) if its too much to fit into GPU
	 * memory at once.
	 */
	public void vm_lazyBlobsEvalCallsThis(LazyBlob lb);

	public LazyBlob defaultParam(String name);

	/**
	 * supports which languages, such as "opencl1.2", "java8", "javascript", "download", where
	 * the Map<String,LazyBlob> has "Code" -> "opencl1.2:...code..." for example.
	 */
	public Set<String> langs();

}