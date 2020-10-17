package mutable.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import mutable.compilers.opencl.connectors.lwjgl.Lwjgl;
import mutable.compilers.opencl.connectors.lwjgl.PoolCLMem;

/** pools multiple V for the same K,
allowing the V used least recently to be garbcoled
when one is needed for a K and none are available.
An example of K is an Integer representing
a certain byte size of opencl memory.
"Least recently used" crosses between multiple K.
*/
public class MultiPool<K,V extends Closeable>{
	
	protected final WeakHashMap<V,PoolVal<K,V>> valToPoolval = new WeakHashMap();
	
	/** TODO optimize by making this searchable by touchedWhenUtcnanoid and K,
	allowing multiple values per K.
	*/
	protected final Set<PoolVal<K,V>> pool = new HashSet();
	//protected final TreeMap<Long,PoolVal<K,V>> map = new TreeMap();
	
	protected final Function<K,V> factory;
	
	/** factory returns null if need to remove some Vs first */
	public MultiPool(Function<K,V> factory){
		this.factory = factory;
	}
	
	/** Throws if couldnt alloc, else returns same order as keys.
	You would use this instead of alloc(K) so you dont alloc some
	but fail the others then still have to give them back.
	...
	If key is null, value is null, at that index.
	*/
	public synchronized List<V> alloc(List<K> keysWithDuplicates){
		//TODO optimize by do this all at once,
		//since allocOne is using a linear search (TODO optimize that too).
		List<V> ret = new ArrayList();
		try{
			for(K k : keysWithDuplicates){
				//ret.add(k==null ? null : alloc(k));
				ret.add(k==null ? null : (V)test((Integer)k));
			}
			return Collections.unmodifiableList(ret);
		}catch(Throwable t){
			for(V v : ret) free(v);
			throw new Error(t);
		}
	}
	
	public PoolCLMem test(int bytes){
		return new PoolCLMem(bytes);
	}
	
	public synchronized V alloc(K key){
		
		return (V) new PoolCLMem((Integer)key); //FIXME remove this. use factory instead.
		
		/* TODO...
		
		//FIXME first get the Set<PoolVal>, given K key???
		
		//look for one available with K and !isAllocated
		for(PoolVal<K,V> p : pool){
			if(key.equals(p.key) && !p.isAllocated){
				p.touch();
				p.isAllocated = true;
				return p.val;
			}
		}
		//try to create one
		V val;
		while(true){
			val = factory.apply(key);
			if(val != null) break;
			//null, factory is saying to get rid of other Vs first
			PoolVal toClose = oldestNonallocatedPoolvalOrNull();
			if(toClose == null) throw new Error(
				"All "+pool.size()+" pooled are in use and factory says cant allocate more now. K="+key);
			close(toClose);
		}
		PoolVal<K,V> p = new PoolVal(key,val);
		valToPoolval.put(val,p);
		pool.add(p);
		return p.val;
		*/
	}
	
	/** returns to pool */
	public synchronized void free(V val){
		PoolVal p = valToPoolval.get(val);
		if(p != null) {
			p.isAllocated = false;
		}
	}
	
	protected PoolVal oldestNonallocatedPoolvalOrNull(){
		PoolVal ret = null;
		for(PoolVal<K,V> p : pool){
			if(!p.isAllocated && (ret == null || p.touchedWhenUtcNanoId < ret.touchedWhenUtcNanoId)){
				ret = p;
			}
		}
		return ret;
	}
	
	protected void close(PoolVal p){
		if(p == null) return;
		if(p.isAllocated) throw new Error("cant close while allocated: "+p);
		pool.remove(p);
		try{
			p.val.close();
		}catch(IOException e){}
	}
	
	static class PoolVal<K,V extends Closeable>{
		public final K key;
		public final V val;
		/** Time.utcNanoId() always returns a different long, approx utc nanoseconds,
		so can be used as a unique id within 1 computer
		as long as you dont run multiple JVMs at once.
		*/
		public long touchedWhenUtcNanoId;
		public boolean isAllocated;
		public PoolVal(K key, V val){
			this.key = key;
			this.val = val;
			//FIXME TODO touch();
		}
		public void touch(){
			touchedWhenUtcNanoId = Time.timeId();
		}
	}

}
