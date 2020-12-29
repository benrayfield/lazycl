package mutable.dependtask;

import java.util.Arrays;

import immutable.util.MathUtil;

/** Immutable. This is more general than but compatible with opencl. It could do a parallel calculation in javassist compiled code.
The 1-3 dimensions of opencl ndrange kernel, which you use with [get_local_id(int) and get_group_id(int)] or get_global_id(int).
See TestOpenclLocalMem.java for an example of using get_local_id(int) and get_group_id(int) for faster matrix multiply
which I copied and slightly modified from the MIT licensed code at https://cnugteren.github.io/tutorial/pages/page5.html
*/
public class ForkSize{
	
	private final int[] globalSize;
	
	private final int[] localSize;
	
	public int dims(){ return globalSize.length; }
	
	public int globalSize(int dim){ return globalSize[dim]; }
	
	/** 0 if unknown */
	public int localSize(int dim){ return localSize!=null ? localSize[dim] : 0; }
	
	/** If localSize is null in clEnqueueNDRangeKernel then opencl chooses it */
	public boolean knowsLocalSize() { return localSize != null; }
	
	/** TODO verify this theory that its ceil(globalSize(dim)/localSize(dim))*
	public int groupSize(int dim){ return MathUtil.ceilOfDivide(globalSize(dim), localSize(dim)); }
	*/
	
	/*
	FIXME if you localSize is null in clEnqueueNDRangeKernel then opencl chooses it, so thats not the same as it being all 1s.
	public ForkSize(int... globalSize){
		this(globalSize,intArrayOfAllOnes(globalSize.length));
	}*/
	
	
	/** clEnqueueNDRangeKernel chooses localSize */
	public ForkSize(int... globalSize){
		this(globalSize, null);
	}
	
	/** 1 <= globalSize.length==localSize <= 3.
	<br><br>
	globalSize[0]*globalSize[1]*globalSize[2] is number of total parallel things to do.
	<br><br>
	This is a theory (todo verify):
		get_num_groups*get_local_size <= get_global_size < (get_num_groups+1)*get_local_size
	<br><br>
	localSize[0]*localSize[1]*localSize[2] (etc) is what happens in 1 small part of a GPU
	(such as 32 nvidia cores is a "warp" that all have to do the same opcode at the same time. Also works on AMD cards.).
	<br><br>
	https://cnugteren.github.io/tutorial/pages/page5.html (TS is for example 32 for 32x32x32 matmul optimization)
	const int row = get_local_id(0); // Local row ID (max: TS)
	const int col = get_local_id(1); // Local col ID (max: TS/WPT == RTS)
	const int globalRow = TS*get_group_id(0) + row; // Row ID of C (0..M)
	const int globalCol = TS*get_group_id(1) + col; // Col ID of C (0..N)
	<br><br>
	get_work_dim
	get_global_id
	get_group_id
	get_local_id
	<br><br>
	While these are how opencl does it, other GPU frameworks have some or all of this in common.
	LazyCL should be more general than OpenCL but is starting with just OpenCL.
	clEnqueueNDRangeKernel params include work_dim=1..3 and global_work_size[work_dim] and local_work_size[work_dim].
	*/
	public ForkSize(int[] globalSize, int[] localSize){
		this.globalSize = globalSize.clone();
		this.localSize = localSize!=null ? localSize.clone() : null;
		
		/* While these are how opencl does it, other GPU frameworks have some or all of this in common.
		LazyCL should be more general than OpenCL but is starting with just OpenCL.
		...
		https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/workItemFunctions.html (I reordered the lines of text...)
		
		get_work_dim 	Number of dimensions in use
		
		get_global_size Number of global work items
		get_local_size 	Number of local work items
		get_num_groups 	Number of work groups
		
		get_global_id 	Global work item ID value
		get_group_id 	Work group ID
		get_local_id 	Local work item ID
		
		This is a theory (todo verify):
			get_num_groups*get_local_size <= get_global_size < (get_num_groups+1)*get_local_size
		
		
		
	
		https://cnugteren.github.io/tutorial/pages/page5.html (TS is for example 32 for 32x32x32 matmul optimization)
		// Thread identifiers
	    const int row = get_local_id(0); // Local row ID (max: TS)
		const int col = get_local_id(1); // Local col ID (max: TS/WPT == RTS)
		const int globalRow = TS*get_group_id(0) + row; // Row ID of C (0..M)
		const int globalCol = TS*get_group_id(1) + col; // Col ID of C (0..N)
		
		
		https://www.khronos.org/registry/OpenCL/sdk/1.0/docs/man/xhtml/clEnqueueNDRangeKernel.html says...
		
		clEnqueueNDRangeKernel
		Enqueues a command to execute a kernel on a device.
		
		cl_int clEnqueueNDRangeKernel (	cl_command_queue command_queue,
		 	cl_kernel kernel,
		 	cl_uint work_dim,
		 	const size_t *global_work_offset,
		 	const size_t *global_work_size,
		 	const size_t *local_work_size,
		 	cl_uint num_events_in_wait_list,
		 	const cl_event *event_wait_list,
		 	cl_event *event)
		Parameters
		command_queue
		A valid command-queue. The kernel will be queued for execution on the device associated with command_queue.
		
		kernel
		A valid kernel object. The OpenCL context associated with kernel and command_queue must be the same.
		
		work_dim
		The number of dimensions used to specify the global work-items and work-items in the work-group. work_dim must be greater than zero and less than or equal to three.
		
		global_work_offset
		Must currently be a NULL value. In a future revision of OpenCL, global_work_offset can be used to specify an array of work_dim unsigned values that describe the offset used to calculate the global ID of a work-item instead of having the global IDs always start at offset (0, 0,... 0).
		
		global_work_size
		Points to an array of work_dim unsigned values that describe the number of global work-items in work_dim dimensions that will execute the kernel function. The total number of global work-items is computed as global_work_size[0] *...* global_work_size[work_dim - 1].
		
		The values specified in global_work_size cannot exceed the range given by the sizeof(size_t) for the device on which the kernel execution will be enqueued. The sizeof(size_t) for a device can be determined using CL_DEVICE_ADDRESS_BITS in the table of OpenCL Device Queries for clGetDeviceInfo. If, for example, CL_DEVICE_ADDRESS_BITS = 32, i.e. the device uses a 32-bit address space, size_t is a 32-bit unsigned integer and global_work_size values must be in the range 1 .. 2^32 - 1. Values outside this range return a CL_OUT_OF_RESOURCES error.
		
		local_work_size
		Points to an array of work_dim unsigned values that describe the number of work-items that make up a work-group (also referred to as the size of the work-group) that will execute the kernel specified by kernel. The total number of work-items in a work-group is computed as local_work_size[0] *... * local_work_size[work_dim - 1]. The total number of work-items in the work-group must be less than or equal to the CL_DEVICE_MAX_WORK_GROUP_SIZE value specified in table of OpenCL Device Queries for clGetDeviceInfo and the number of work-items specified in local_work_size[0],... local_work_size[work_dim - 1] must be less than or equal to the corresponding values specified by CL_DEVICE_MAX_WORK_ITEM_SIZES[0],.... CL_DEVICE_MAX_WORK_ITEM_SIZES[work_dim - 1]. The explicitly specified local_work_size will be used to determine how to break the global work-items specified by global_work_size into appropriate work-group instances. If local_work_size is specified, the values specified in global_work_size[0],... global_work_size[work_dim - 1] must be evenly divisable by the corresponding values specified in local_work_size[0],... local_work_size[work_dim - 1].
		
		The work-group size to be used for kernel can also be specified in the program source using the __attribute__((reqd_work_group_size(X, Y, Z)))qualifier. In this case the size of work group specified by local_work_size must match the value specified by the reqd_work_group_size __attribute__ qualifier.
		
		local_work_size can also be a NULL value in which case the OpenCL implementation will determine how to be break the global work-items into appropriate work-group instances.
		
		See note for more information.
		
		event_wait_list and num_events_in_wait_list
		Specify events that need to complete before this particular command can be executed. If event_wait_list is NULL, then this particular command does not wait on any event to complete. If event_wait_list is NULL, num_events_in_wait_list must be 0. If event_wait_list is not NULL, the list of events pointed to by event_wait_list must be valid and num_events_in_wait_list must be greater than 0. The events specified in event_wait_list act as synchronization points. The context associated with events in event_wait_list and command_queue must be the same.
		
		event
		Returns an event object that identifies this particular kernel execution instance. Event objects are unique and can be used to identify a particular kernel execution instance later on. If event is NULL, no event will be created for this kernel execution instance and therefore it will not be possible for the application to query or queue a wait for this particular kernel execution instance.
		
		Notes
		Work-group instances are executed in parallel across multiple compute units or concurrently on the same compute unit.
		
		Each work-item is uniquely identified by a global identifier. The global ID, which can be read inside the kernel, is computed using the value given by global_work_size and global_work_offset. In OpenCL 1.0, the starting global ID is always (0, 0, ... 0). In addition, a work-item is also identified within a work-group by a unique local ID. The local ID, which can also be read by the kernel, is computed using the value given by local_work_size. The starting local ID is always (0, 0, ... 0).
		
		Errors
		Returns CL_SUCCESS if the kernel execution was successfully queued. Otherwise, it returns one of the following errors:
		
		CL_INVALID_PROGRAM_EXECUTABLE if there is no successfully built program executable available for device associated with command_queue.
		CL_INVALID_COMMAND_QUEUE if command_queue is not a valid command-queue.
		CL_INVALID_KERNEL if kernel is not a valid kernel object.
		CL_INVALID_CONTEXT if context associated with command_queue and kernel is not the same or if the context associated with command_queue and events in event_wait_list are not the same.
		CL_INVALID_KERNEL_ARGS if the kernel argument values have not been specified.
		CL_INVALID_WORK_DIMENSION if work_dim is not a valid value (i.e. a value between 1 and 3).
		CL_INVALID_WORK_GROUP_SIZE if local_work_size is specified and number of work-items specified by global_work_size is not evenly divisable by size of work-group given by local_work_size or does not match the work-group size specified for kernel using the __attribute__((reqd_work_group_size(X, Y, Z))) qualifier in program source.
		CL_INVALID_WORK_GROUP_SIZE if local_work_size is specified and the total number of work-items in the work-group computed as local_work_size[0] *... local_work_size[work_dim - 1] is greater than the value specified by CL_DEVICE_MAX_WORK_GROUP_SIZE in the table of OpenCL Device Queries for clGetDeviceInfo.
		CL_INVALID_WORK_GROUP_SIZE if local_work_size is NULL and the __attribute__((reqd_work_group_size(X, Y, Z))) qualifier is used to declare the work-group size for kernel in the program source.
		CL_INVALID_WORK_ITEM_SIZE if the number of work-items specified in any of local_work_size[0], ... local_work_size[work_dim - 1] is greater than the corresponding values specified by CL_DEVICE_MAX_WORK_ITEM_SIZES[0], .... CL_DEVICE_MAX_WORK_ITEM_SIZES[work_dim - 1].
		CL_INVALID_GLOBAL_OFFSET if global_work_offset is not NULL.
		CL_OUT_OF_RESOURCES if there is a failure to queue the execution instance of kernel on the command-queue because of insufficient resources needed to execute the kernel. For example, the explicitly specified local_work_size causes a failure to execute the kernel because of insufficient resources such as registers or local memory. Another example would be the number of read-only image args used in kernel exceed the CL_DEVICE_MAX_READ_IMAGE_ARGS value for device or the number of write-only image args used in kernel exceed the CL_DEVICE_MAX_WRITE_IMAGE_ARGS value for device or the number of samplers used in kernel exceed CL_DEVICE_MAX_SAMPLERS for device.
		CL_MEM_OBJECT_ALLOCATION_FAILURE if there is a failure to allocate memory for data store associated with image or buffer objects specified as arguments to kernel.
		CL_INVALID_EVENT_WAIT_LIST if event_wait_list is NULL and num_events_in_wait_list > 0, or event_wait_list is not NULL and num_events_in_wait_list is 0, or if event objects in event_wait_list are not valid events.
		CL_OUT_OF_HOST_MEMORY if there is a failure to allocate resources required by the OpenCL implementation on the host.
		Specification
		 OpenCL Specification
		
		Also see
		clCreateCommandQueue, clGetDeviceInfo, clEnqueueNativeKernel, clEnqueueTask, Work-Item Functions
		

		
		
		https://www.khronos.org/registry/OpenCL//sdk/2.2/docs/man/html/get_num_groups.html says...
		
		workItemFunctions(3) Manual Page
		Name
		
		workItemFunctions - Work-Item Functions
		Description
		
		The following table describes the list of built-in work-item functions that can be used to query the number of dimensions, the global and local work size specified to clEnqueueNDRangeKernel, and the global and local identifier of each work-item when this kernel is being executed on a device.
		Table 1. Work-Item Functions Table
		
		Function
			
		
		Description
		
		uint get_work_dim()
			
		
		Returns the number of dimensions in use. This is the value given to the work_dim argument specified in clEnqueueNDRangeKernel.
		
		size_t get_global_size(uint dimindx)
			
		
		Returns the number of global work-items specified for dimension identified by dimindx. This value is given by the global_work_size argument to clEnqueueNDRangeKernel.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_global_size() returns 1.
		
		size_t get_global_id(uint dimindx)
			
		
		Returns the unique global work-item ID value for dimension identified by dimindx. The global work-item ID specifies the work-item ID based on the number of global work-items specified to execute the kernel.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_global_id() returns 0.
		
		size_t get_local_size(uint dimindx)
			
		
		Returns the number of local work-items specified in dimension identified by dimindx. This value is at most the value given by the local_work_size argument to clEnqueueNDRangeKernel if local_work_size is not NULL; otherwise the OpenCL implementation chooses an appropriate local_work_size value which is returned by this function. If the kernel is executed with a non-uniform work-group size28, calls to this built-in from some work-groups may return different values than calls to this built-in from other work-groups.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_local_size() returns 1.
		
		size_t get_enqueued_local_size( uint dimindx)
			
		
		Returns the same value as that returned by get_local_size(dimindx) if the kernel is executed with a uniform work-group size.
		
		If the kernel is executed with a non-uniform work-group size, returns the number of local work-items in each of the work-groups that make up the uniform region of the global range in the dimension identified by dimindx. If the local_work_size argument to clEnqueueNDRangeKernel is not NULL, this value will match the value specified in local_work_size[dimindx]. If local_work_size is NULL, this value will match the local size that the implementation determined would be most efficient at implementing the uniform region of the global range.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_enqueued_local_size() returns 1.
		
		size_t get_local_id(uint dimindx)
			
		
		Returns the unique local work-item ID, i.e. a work-item within a specific work-group for dimension identified by dimindx.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_local_id() returns 0.
		
		size_t get_num_groups(uint dimindx)
			
		
		Returns the number of work-groups that will execute a kernel for dimension identified by dimindx.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values of dimindx, get_num_groups() returns 1.
		
		size_t get_group_id(uint dimindx)
			
		
		get_group_id returns the work-group ID which is a number from 0 .. get_num_groups(dimindx) - 1.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values, get_group_id() returns 0.
		
		size_t get_global_offset(uint dimindx)
			
		
		get_global_offset returns the offset values specified in global_work_offset argument to clEnqueueNDRangeKernel.
		
		Valid values of dimindx are 0 to get_work_dim() - 1. For other values, get_global_offset() returns 0.
		
		size_t get_global_linear_id()
			
		
		Returns the work-items 1-dimensional global ID.
		
		For 1D work-groups, it is computed as get_global_id(0) - get_global_offset(0).
		
		For 2D work-groups, it is computed as (get_global_id(1) - get_global_offset(1)) * get_global_size(0) + (get_global_id(0) - get_global_offset(0)).
		
		For 3D work-groups, it is computed as get_global_id(2) - get_global_offset(2 * get_global_size(1) * get_global_size(0)) + get_global_id(1) - get_global_offset(1 * get_global_size (0)) + (get_global_id(0) - get_global_offset(0)).
		
		size_t get_local_linear_id()
			
		
		Returns the work-items 1-dimensional local ID.
		
		For 1D work-groups, it is the same value as
		
		get_local_id(0).
		
		For 2D work-groups, it is computed as
		
		get_local_id(1) * get_local_size(0) + get_local_id(0).
		
		For 3D work-groups, it is computed as
		
		(get_local_id(2) * get_local_size(1) * get_local_size(0)) + (get_local_id(1) * get_local_size(0)) + get_local_id(0).
		*/
	}
	
	/*public int globalSize(){
		int i = 1;
		for(int size : globalSize) i *= size;
		return i;
	}
	
	public int localSize(){
		int i = 1;
		for(int size : localSize) i *= size;
		return i;
	}
	
	/** nonbacking */
	public int[] globalToIntArray(){
		return globalSize.clone();
	}
	
	/** nonbacking, or null */
	public int[] localToIntArrayOrNull(){
		return localSize!=null ? localSize.clone() : null;
	}
	
	/* Dont do this. Let opencl choose if localSize isnt specified (redesigned 2020-12-28 to use null instead of all 1s).
	static int[] intArrayOfAllOnes(int dims){
		int[] ret = new int[dims];
		Arrays.fill(ret, 1);
		return ret;
	}*/

}
