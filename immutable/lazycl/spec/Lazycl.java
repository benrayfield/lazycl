package immutable.lazycl.spec;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public interface Lazycl{
	
	/** lazy call of blobs each as a named param, that returns a blob.
	Normally returns instantly since it doesnt do the work until observed inside LazyBlob.
	<br><br>
	Common keys include: Code IsTemp OpenclCompileParams <param name in lambda in code>
	To avoid naming conflict, lazycl params start with capital letter, and params in Code string
	should start with lowercase letter, so if more lazycl params are added later,
	they dont break your existing opencl or java code strings.
	<br><br>
	OLD...
	TODO put isTemp param, and maybe other options such as strictfp and opencl compiler options,
		but certainly need at least isTemp, as 1 of the params in call(...).
		Put it in "isTemp" key of Map<String,LazyBlob>.
	TODO get the strictfp etc related options from humanAiNetNeural code, if its not already there in the OpenclUtil copied.
	Allow caller to choose from those options but default it to strict.
	*/
	public LazyBlob lazycl(Map<String,LazyBlob> params);
	
	/** wrap. throws if type not supported.
	TODO object types include FloatBuffer, CLMem, long[], String, etc.
	*/
	public LazyBlob wrap(Object o);
	
	public default Map<String,LazyBlob> map(Object... alternateKeyValKeyVal){
		NavigableMap<String,LazyBlob> ret = new TreeMap();
		if((alternateKeyValKeyVal.length&1)==1) throw new RuntimeException("odd size. must be key val key val...");
		for(int i=0; i<alternateKeyValKeyVal.length; i++){
			Object key = alternateKeyValKeyVal[i];
			Object val = alternateKeyValKeyVal[i+1];
			if(!(key instanceof String)) throw new RuntimeException("i="+i+" is not a "+String.class.getName()+": "+key);
			if(!(val instanceof LazyBlob)) throw new RuntimeException("i="+i+" is not a "+LazyBlob.class.getName()+": "+val);
			ret.put((String)key, (LazyBlob)val);
		}
		return Collections.unmodifiableNavigableMap(ret);
	}
	
	/** same as lazycl(Map<String,LazyBlob> params) but written as alternating key val key val */
	public default LazyBlob lazycl(Object... alternateKeyValKeyVal){
		return lazycl(map(alternateKeyValKeyVal));
	}
	
	/** automatically called by LazyBlob.f(int) or LazyBlob.IN().read(...) etc
	when try/catch(NullPointerException).
	<br><br>
	If observe inside the param LazyBlob, then which LazyBlobs will be automatically evaled?
	Clearly those which it depends on must be evaled except those already evaled,
	but others which depend on them but dont have to be evaled yet
	may be evaled anyways to avoid recalculating shared dependencies,
	especially if those dependencies isTemp which forExample could mean
	its still in a CLMem or could mean it was in a CLMem that was returned to a pool.
	<br><br>
	The simplest strategy is to wait for anything to wait for the first observe
	(excluding things already in cpu reachable memory like float[] or in some cases FloatBuffer)
	then eval everything and garbcol all the isTemp pieces of gpu memory (return CLMems to pool).
	Thats the first thing I'll build 2020-10-20+.
	<br><br>
	A problem with this is it couled wait too long and not have enough gpu memory to
	do all those calculations, so in that case the better thing to do would be
	to copy some of the isTemps and !isTemps to cpu memory
	(or even to harddrive if thats not enough) between multiple calls
	of OpenclUtil.callOpenclDependnet.
	<br><br>
	A more advanced strategy would be to leave some things in CLMems
	and only queue them to be copied to cpu memory (like FloatBuffer) as needed,
	so could do multiple opencl calls between multiple CLMem without giving those CLMems
	back to the pool.
	<br><br>
	In any case, it should still be immutable as viewed from LazyBlobs
	which encapsulate those uses of mutable memory.
	That will be enforced by OpenclUtil having parsing functions to check which
	memories are readable and writable, around the "const" keyword,
	except that doesnt prevent the use of OpenclUtil directly to access those CLMems,
	but it wont happen just by use of LazyBlobs.
	*/
	public Set<LazyBlob> vm_evalWhichIf(LazyBlob observe);
	
	/** automatically called by LazyBlob.f(int) or LazyBlob.IN().read(...) etc
	when try/catch(NullPointerException).
	Not every Set<LazyBlob> is valid since it must include all dependencies of all in the Set.
	*/
	public void vm_eval(Set<LazyBlob> evalThese);

}