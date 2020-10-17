package mutable.util.task;

import java.util.Collection;
import java.util.List;

/** a task that may be a Runnable or may be lazyEvaled in dependnet in opencl, etc */
public interface Task{
	
	public static void doTasksInCpu(Collection<Task> tasks){
		for(Task t : tasks){
			((RunnableTask)t).runnable.run();
		}
	}
	
	/** may do them in a different order as long as dependnet is satisfied *
	public static void doTasksInOpencl(Collection<Task> tasks){
		throw new Error("TODO");
	}*/
	
}
