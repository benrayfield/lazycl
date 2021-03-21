package assemblylikevms.experimentalVM35462;

public class ExperimentalVM35462{
	
	//TODO use Unsafe class to read and write all primitive types in byte[] for speed?
	
	/** mem[ptrFirstIP..(ptrFirstIP+threads)] is instructionPointer.
	TODO threads later? For now just do 1 thread, which may be a GPU thread but
	leaving the between threads part to caller to set this up
	(or a translation of it to opencl or javassist etc).
	*/
	public static final int IP = 0;
	
	/** stack 1 of 3 stacks used for copy and reorder similar to https://en.wikipedia.org/wiki/Tower_of_Hanoi
	OLD: mem[ptrFirstSP..(ptrFirstSP+threads)] is stackPointer. Not all ops use stack.
	*/
	public static final int SPa = 1;
	
	/** stack 2 of 3 stacks used for copy and reorder similar to https://en.wikipedia.org/wiki/Tower_of_Hanoi */
	public static final int SPb = 2;
	
	/** stack 3 of 3 stacks used for copy and reorder similar to https://en.wikipedia.org/wiki/Tower_of_Hanoi */
	public static final int SPc = 3;
	
	public static final byte threeFlipAllBits = 0;
	public static final byte stackFlipAllBits = 1;
	
	public static final byte stackNeg = 2;
	public static final byte threeNeg = 3;
	
	public static final byte stackIadd = 4;
	public static final byte threeIadd = 5;
	
	public static final byte stackImul = 6;
	/** pop to get jump address */
	public static final byte popJump = 8;
	
	/** pop and add that to ip to get jump address */
	public static final byte popJumpRel = 10;
	
	public static final byte stackFmulnorm = 20;
	/** pop addr. push val at that addr */
	public static final byte stackReadMem = 22;
	/** pop addr. pop val. write that val to that addr. */
	public static final byte stackWriteMem = 24;
	
	public static final byte stackPopIgnore = 26;
	
	public static final byte stackBPopIgnore = 28;
	
	public static final byte stackCPopIgnore = 30;
	
	/** pop from stackA and push to stackB */
	public static final byte moveAB = 40;
	
	public static final byte moveBA = 42;
	
	public static final byte moveBC = 44;
	
	public static final byte moveCB = 46;
	
	public static final byte moveCA = 48;
	
	public static final byte moveAC = 50;
	
	/** aka copyAA */
	public static final byte dup = 52;
	
	/** aka copyBB */
	public static final byte dupB = 54;
	
	/** aka copyCC */
	public static final byte dupC = 56;
	
	/** peek stackA and push to stackB */
	public static final byte copyAB = 58;
	
	public static final byte copyBA = 60;
	
	public static final byte copyBC = 62;
	
	public static final byte copyCB = 64;
	
	public static final byte copyCA = 60;
	
	public static final byte copyAC = 62;
	
	/** pop from stackB and push to stackC */
	public static final byte move = 40;
	
	public static void pushStackB(int[] mem, int val){
		final int mask = mem.length-1;
		mem[SPb] = (mem[SPb]+1)&mask;
		mem[mem[SPb]] = val;
	}
	
	public static int popStackB(int[] mem, int val){
		final int mask = mem.length-1;
		int ret = mem[mem[SPb]];
		mem[SPb] = (mem[SPb]-1)&mask;
		return ret;
	}
	
	public static void pushStackC(int[] mem, int val){
		final int mask = mem.length-1;
		mem[SPc] = (mem[SPc]+1)&mask;
		mem[mem[SPc]] = val;
	}
	
	public static int popStackC(int[] mem, int val){
		final int mask = mem.length-1;
		int ret = mem[mem[SPc]];
		mem[SPc] = (mem[SPc]-1)&mask;
		return ret;
	}
	
	public static void push(int[] mem, int val){
		final int mask = mem.length-1;
		mem[SPa] = (mem[SPa]+1)&mask;
		mem[mem[SPa]] = val;
	}
	
	public static int pop(int[] mem){
		final int mask = mem.length-1;
		int ret = mem[mem[SPa]];
		mem[SPa] = (mem[SPa]-1)&mask;
		return ret;
	}
	
	public static float popf(int[] mem){
		return Float.intBitsToFloat(pop(mem));
	}
	
	public static void pushfraw(int[] mem, float f){
		push(mem, Float.floatToRawIntBits(f));
	}
	
	public static void pushfnorm(int[] mem, float f){
		push(mem, Float.floatToIntBits(f));
	}
	
	public static long popj(int[] mem){
		return (long)pop(mem)|(((long)pop(mem))<<32); //lo is on top of stack.
	}
	
	public static double popd(int[] mem){
		return Double.longBitsToDouble(popj(mem));
	}
	
	public static int op(int[] mem){
		return mem[mem[IP]];
	}
	
	public static int safePtr(int[] mem, int ptr){
		final int mask = mem.length-1;
		return ptr&mask;
	}
	
	public static int safeRead(int[] mem, int ptr){
		final int mask = mem.length-1;
		return mem[ptr&mask];
	}
	
	public static void safeWrite(int[] mem, int ptr, int val){
		final int mask = mem.length-1;
		mem[ptr&mask] = val;
	}
	
	/** verify mem is powOf2 size and that size is at least 256.
	TODO verify does not write where it shouldnt, etc.
	*/
	public static void fastTest(int[] mem) throws RuntimeException{
		final int mask = mem.length-1;
		if((mem.length&mask) != 0 || mem.length < 256)
			throw new RuntimeException("mem is not powOf2 size or is smaller than 256: "+mem.length);
	}
	
	public static void nextState(int[] mem){
		fastTest(mem); //TODO remove this line for speed
		int ip = safePtr(mem,mem[IP]);
		int nextIP = ip+1;
		int opcode = mem[ip]; //all possible opcodes are valid
		if(opcode >= 0){
			//any nonnegative value is an op to push itself onto stack.
			//To push a negative value x, do this for ~x then stackFlipAllBits.
			push(mem,opcode);
		}else{
			byte op = (byte)(opcode>>24); //TODO long opcode instead of byte so have more address space?
			switch(op){
			case stackFlipAllBits:
				push(mem,~pop(mem));
			break;case threeFlipAllBits:
				//TODO get 3 bytes from opcode, read 2 of them, write the third.
			break; case stackIadd:
				push(mem,pop(mem)+pop(mem));
			break; case stackImul:
				push(mem,pop(mem)*pop(mem));
			break; case popJump:
				nextIP = pop(mem);
			break; case popJumpRel:
				nextIP = ip+pop(mem);
			break; case stackFmulnorm:
				pushfnorm(mem,popf(mem)*popf(mem));
			break; case stackReadMem:
				push(mem,safeRead(mem,pop(mem)));
			break; case stackWriteMem:
				safeWrite(mem,pop(mem),pop(mem));
			
			}
		}
		mem[IP] = safePtr(mem,nextIP);
	}

}
