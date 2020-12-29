package immutable.opencl;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import mutable.dependtask.DependOp;
import mutable.dependtask.DependParam;
import mutable.dependtask.mem.Mem;

/** The OpenCL interface is more general than the Lazycl interface.
Lazycl is normally implemented by calling that OpenCL interface
which may call any implementation of opencl such as LWJGL
(or in theory AMD's C++ opencl code or whatever).
Both are stateless, but they differ in their encapsulated stateful atomic behaviors.
Both use a forest of ndrange kernel calls, some of which can (TODO) be done in parallel
(n kernels done at the same time, which each do things in parallel within themself),
where each is already a parallel call of globalSize and localSize GPU threads (such as get_global_id(2)).
Lazycl has exactly 1 output blob per ndrange kernel call, and all other params are read only. 
The OpenCL interface uses LockPar to readLock, writeLock, andOr readWriteLock each mutable blob,
and defines a forest of ndrange kernel calls (using DependnetBuilder) which
enforces write-one-or-readwrite-one-or-read-many locking per mutable blob
(actually a Mem since Blob interface is immutable),
which happens during a call of callOpencl or callOpenclDependnet
which is immutable and stateless as viewed from the outside but uses mutable optimizations
during that atomic encapsulated call.
Lazycl can compute anything that OpenCL interface can compute but there are some optimizations
that you might want to implement in the OpenCL interface instead,
such as an ndrange kernel that writes both an int array and a float array
using shared input arrays instead of having to compute that in 2 parallel ndrange kernel calls.
*/
public interface OpenCL{
	
	/** Example: "1.2" if it supports only whats up to https://en.wikipedia.org/wiki/OpenCL#OpenCL_1.2 */
	public String version();
	
	/** /** FIXME upgrade the pool to garbcol. As of 2020-4-12 it only allocates and shares them
	but will run out of memory if a variety of sizes keep being requested
	since the earlier requested sizes wont be garbcoled.
	<br><br> 
	The Mems input and output are self contained such as ArrayMem,
	but (TODO) I'm undecided exactly which subtype(s) of Mem.
	<br><br>
	This is the recommended way to call opencl (TODO not working as of 2020-2-1).
	You call this for example 50 times per second with multiple opencl kernels each.
	This does not leave any state in opencl
	except the pooling of CLMems and CompiledKernel etc
	but caller doesnt have to know about that other than
	...
	(TODO instead of DependParam holding memory, pool it by size and type only,
	using PoolCLMem).
	...
	to allow DependnetParam to be garbcoled asap since
	those resources may be held in pool until DependnetParam is garbcoled
	(FIXME verify memory is freed then, in BiMem).
	The CLMem pooling is needed cuz allocating CLMem is laggy
	and would prevent running a neuralnet
	between adjacent video frames of a game (such as .01 second).
	<br><br>
	Example Object values in the maps:
	int[], float[], long[], double[], Integer, Float, Long, Double.
	<br><br>
	---------------
	<br><br>
	similar to callOpenclForest except this is more optimizable
	as it can use the same CLMem multiple times and simultaneously read and write it.
	callOpenclForest maybe should call this.
	*/
	public SortedMap<DependParam,Mem> callOpenclDependnet(
		Map<DependParam,Mem> ins, Set<DependOp> tasks, Set<DependParam> outs);
	
	/** Does 1 opencl ndrange kernel call. This is the older function, that has int[] globalSize
	but not int[] localSize (UPDATE: I'm putting that param in, but as of 2020-12 it has to
	always be null which means opencl chooses localSize automatically),
	and the newer more general function (thats TODO lower lag for a
	forest of multiple calls before returning from GPU to CPU) is callOpenclDependnet.
	You should use callOpenclDependnet for near everything instead of this,
	except this is still useful for fast experimental calculations as its params are simpler.
	<br><br>
	TODO create a default (java keyword) implementation of this which calls callOpenclDependnet
	instead of the original code here which was refactored to create callOpenclDependnet.
	That code was first in HumanAINetNeural then was refactored to create LazyCL (2 projects on github).
	<br><br>
	This is the simplest way to call opencl but can only call it about 100 times
	per second cuz of the lag from java to lwjgl opencl to java.
	For lower lag do for example 30 opencl kernels per call in
	callOpenclDependnet for total 1500 sequential kernel calls per second.
	callOpenclDependnet is the recommended way (TODO not working as of 2020-2-1).
	<br><br>
	OpenclUtil doesnt modify any of the inputs or outputs, even if marked as mutable like "global double* bdOut".
	Takes an opencl kernel code string and caches its compiled form,
	and takes an nd-range int[1] (only 1d works so far, but wraps 2d array in 1d and back automatically)
	and Object[] params which may be array, int, etc,
	and returns an Object[] of the same size as Object[] params, reusing those not modified,
	and replacing those modified by opencl. It knows the difference by basic parsing of parts of the kernel string.
	I will upgrade it to do doubles instead of just floats, for use in recurrentjava,
	but Ive read that opencl is not reliable of support for doubles but is reliable of support for floats,
	*/
	public Object[] callOpencl(String kernelCode, int[] globalSize, int[] localSizeOrNull, Object... params);

}
