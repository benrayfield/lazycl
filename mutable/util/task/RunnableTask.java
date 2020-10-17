package mutable.util.task;

public class RunnableTask implements Task{
	
	public final Runnable runnable;
	
	public RunnableTask(Runnable r){
		this.runnable = r;
	}
}
