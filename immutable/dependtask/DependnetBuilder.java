package immutable.dependtask;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.DependParam;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.LockPar;
import immutable.compilers.opencl_fixmeMoveSomePartsToImmutablePackage.ParallelSize;

public class DependnetBuilder{ //FIXME move to mutable package
	
	/** DependOp[] which each have no DependOp childs/depends, in a sequence.
	Returns a dependnet which can vary that sequence any way that results in the exact same calculation.
	*/
	public static SortedSet<DependOp> listToDependnet(List<DependOp> sequence){
		DependnetBuilder b = new DependnetBuilder();
		for(DependOp op : sequence){
			if(!op.depends.isEmpty()) throw new Error("has depends: "+op);
			b.add(op.nonsandboxedLangColonCode, op.parallelSize, op.params.toArray(new LockPar[0]));
		}
		return b.dependnet();
	}
	
	/** all keys it doesnt contain are mapped to LockState.noLock *
	protected final WeakHashMap<DependParam,LockState> paramLocks = new WeakHashMap();
	/** Map of DependParam to set of DependOp which are each locking (max 1 writeandoptionallyread or any number of readers).
	ReadLock(dp) -> set of DependOp that read -> WriteAndOptionallyReadLock(dp) -> 1 DependOp that writes and optionally reads -> ReadLock(dp)...
	Repeats those 4 things so each Mem (such as CLMem or FloatBuffer) can be read and written multiple times during a run of a dependnet.
	*
	protected final WeakHashMap<DependParam,Set<DependOp>> paramLockers = new WeakHashMap();
	*/
	protected final WeakHashMap<DependParam,ParamInfo> paramInfos = new WeakHashMap();

	protected final Set<DependOp> dependnet = new HashSet();
	
	protected SortedSet<DependOp> cacheLastDependnetReturned = null;
	
	protected ParamInfo paramInfo(DependParam d){
		ParamInfo ret = paramInfos.get(d);
		if(ret == null) paramInfos.put(d, ret = new ParamInfo(d));
		return ret;
	}
	
	/*
	protected LockState lockState(DependParam d){
		LockState ret = paramLocks.get(d);
		return ret==null ? LockState.noLock : ret;
	}
	
	protected void setLockState(DependParam d, LockState ls){
		if(ls == LockState.noLock) paramLocks.remove(d);
		paramLocks.put(d,ls);
	}
	
	/** backing Set *
	protected Set<DependOp> lockers(DependParam d){
		Set<DependOp> ret = paramLockers.get(d);
		if(ret == null) paramLockers.put(d, ret = new HashSet());
		return ret;
	}*/
	
	/** a plan to lock a DependParam after certain other DependOps (my childs) happen.
	It doesnt mean those DependOps are locking the DependParam into that state.
	Examples...
	ReadLock(dp) -> set of DependOp that read -> WriteAndOptionallyReadLock(dp) -> 1 DependOp that writes and optionally reads -> ReadLock(dp)...
	Repeats those 4 things so each Mem (such as CLMem or FloatBuffer) can be read and written multiple times during a run of a dependnet.
	*
	protected static DependOp lockAfter(Set<DependOp> depends, LockPar next){
		return new DependOp(depends, "lockAfter:", 0, next);
	}*/
	
	protected static class ParamInfo{
		//public final DependParam dp;
		public final Set<DependOp> readLockers = new HashSet();
		/** includes readWriteLock and writeLock */
		public DependOp writeLocker;
		/** writeLocker is copied to here when first readLocker is added, so other readers remember to depend on the same thing instead of eachother. */
		public DependOp readersDependOn;
		//public LockState ls = LockState.noLock;
		public LockPar lp;
		public ParamInfo(DependParam dp){
			lp = new LockPar(LockState.noLock, dp);
		}
		/** put a DependOp, whose params include this, either into multiple readers (remove writer) or 1 writer (remove all readers) */
		public void put(DependOp locker){
			int i = locker.indexOf(lp.dp);
			if(i == -1) throw new Error("DependOp "+locker+" doesnt touch DependParam "+lp.dp);
			LockPar lp = locker.params.get(i);
			if(lp.ls == LockState.noLock) throw new Error("Param is wasting space. Param with no lock: "+lp.dp);
			if(lp.ls == LockState.readLock){
				if(writeLocker != null) readersDependOn = writeLocker;
				writeLocker = null;
				readLockers.add(locker);
			}else{ //write
				readersDependOn = null;
				readLockers.clear();
				writeLocker = locker;
			}
		}
	}
	
	public void add(String nonsandboxedLangColonCode, ParallelSize parallelSize, LockPar... params){
		cacheLastDependnetReturned = null;
		
		/*choose what class Read and Writeandoptionallyread will be. LockPar?
		DependOp has DependOps as childs. Maybe it should have Tasks as childs?
		It will have 1 or more childs. It will have a LockState for what must happen in future and its childs will be what DependOps
		must finish before that lock can start.
		
		use lockAfter(Set,new LockPar(LockState.readLock,dp)) or for readWriteLock or writeLock.
		*/
		
		/*
		//First check if have to change anything in lockers(dp) for each dp in my params,
		//since whatever is in lockers(dp) (or FIXME the readlock below it)
		for(int p=0; p<params.length; p++){
			DependParam dp = params[p].dp;
			ParamInfo i = paramInfo(dp);
			LockState prevLock = i.ls;
			LockState nextLock = params[p].ls;
			if(prevLock == LockState.readLock && nextLock.write && i.readLockers.size()>1){
				//CANCELLED: put a writeLock or writeAndOptionallyReadLock after that set of 2+ readLockers so multiple can depend on it WAIT...
				//wait... there can only be 1 writeLock or writeAndOptionallyReadLock so its ok for it to depend on those multiple readers
				//since it wont do an outerJoin between 2 sets of lockers.
				
				//The problem I still need to solve is how to stop a simultaneous read and write of the same param.
				//Or maybe this design solves it. TODO verify it.
				
			}
		}*/
		
		Set<DependOp> depends = new HashSet();
		for(int p=0; p<params.length; p++){
			DependParam dp = params[p].dp;
			ParamInfo i = paramInfo(dp);
			LockState prevLock = i.lp.ls;
			LockState nextLock = params[p].ls;
			if(nextLock == LockState.noLock) throw new Error("Param is wasting space. Param with no lock: "+params[p]);
			if(prevLock == LockState.noLock){
				//no depends to add
			}else if(prevLock == LockState.readLock){
				if(nextLock == LockState.readLock){
					//adding another reader
					if(i.readersDependOn != null) depends.add(i.readersDependOn);
				}else{ //nextLock writes
					//adding a writer which depends on all readers of it 
					depends.addAll(i.readLockers);
				}
			}else{ //prevLock writes
				//adding a writer which depends on the previous writer
				depends.add(i.writeLocker);
			}
			
			
			/*switch(prevLock){
			case noLock: //nextLock can be anything except noLock
				//add first DependOp using the var
				TODO
			break;
			case readLock:
				if(nextLock == LockState.readLock){
					//Add another reader
					TODO
					
				}else{ //readWriteLock or writeLock
					//1 writer waits on 1 or more readers, then puts itself in the paramInfo (removing all readers) for that param
					//so if more readers or another writer comes after that they have to wait on the write to finish.
					//So lockAfter(...) isnt needed.
					TODO
				}
			break;
			case readWriteLock: case writeLock:
				if(nextLock == LockState.readLock){
					//first readlock waits on a writer. After that, more readlocks can accumulate on this param.
					TODO
				}else{ //readWriteLock or writeLock
					//Another writer waits on this writer.
					TODO
				}
			}*/
			if(i.lp.equals(params[p])){
				params[p] = i.lp; //dedup LockPar sometimes
			}
		}
		DependOp d = new DependOp(depends, nonsandboxedLangColonCode, parallelSize, params);
		for(LockPar lp : params) paramInfo(lp.dp).put(d);
		dependnet.add(d);
		
		
		
		/*DependOp d = new DependOp(depends, nonsandboxedLangColonCode, parallelSize, params);
		
		Then in some cases create DependOps from the new Read of DependParam which depends on that DependOp (with the nonsandboxedLangColonCode),
		and update paramLocks map and update a map of DependParam to DependOp which is the last DependOp to read or write it.
		
		What about the Read and Writeandoptionallyread kind of DependOps? should those be DependOps? They dont need all the fields.
		Maybe they should be Task instead (super..class of DependOp)?
		
		see comment in OpenclGraph
		*/
	}
	
	/** get an immutable Set of the dependnet. From that you can find all the DependParams and which are inputs. Any can be outputs.
	Efficiently returns the same immutable Set until the next add(...).
	*/
	public SortedSet<DependOp> dependnet(){
		if(cacheLastDependnetReturned == null) cacheLastDependnetReturned = Collections.unmodifiableSortedSet(new TreeSet(dependnet));
		return cacheLastDependnetReturned;
	}
	
	/* Will get basics of OpenclGraph working first, and test those, comparing it to the cpu form of Graph.
	FIXME write testcases of DependnetBuilder, multiple add(...) then dependnet(),
	and dont call it on opencl yet, just verify it generates the correct dependnet.
	Other than order a Set returns things, its the exact same DependNet.
	So TODO create a way to compare 2 dependnets with hashCode and equals, and use that in the tests.
	*/
	
}
