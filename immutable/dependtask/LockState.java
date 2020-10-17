package immutable.dependtask;

public enum LockState{

	noLock(false,false),
	
	readLock(false, true),
	
	writeLock(true,false),
	
	readWriteLock(true,true);
	
	public final boolean read, write;
	
	private LockState(boolean write, boolean read){
		this.write = write;
		this.read = read;
	}

}
