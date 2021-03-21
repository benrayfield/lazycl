package assemblylikevms.experimentalVM35462;

/** 4 registers: 1 instruction pointer (IP) and 3 stack pointers (SPa SPb SPc).
Its ok for these to overlap, but may be confusing if they do.
All possible opcodes and states of the long[] are valid.
*/
public class ExperimentalVM35463Longs{
	
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
	
	public static final int MIN_SIZE = 4;
	
	
	
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
	
	public static final byte pushIP = 64;
	
	public static final byte pushSPa = 66;
	
	public static final byte pushSPb = 68;
	
	public static final byte pushSPc = 70;
	
	public static final byte incj = 72;
	
	public static final byte decj = 74;
	
	public static void pushStackB(long[] mem, long val){
		final int mask = mem.length-1;
		mem[SPb] = (mem[SPb]+1)&mask;
		mem[(int)mem[SPb]] = val;
	}
	
	public static long popStackB(long[] mem){
		final int mask = mem.length-1;
		long ret = mem[(int)mem[SPb]];
		mem[SPb] = (mem[SPb]-1)&mask;
		return ret;
	}
	
	public static void pushStackC(long[] mem, long val){
		final int mask = mem.length-1;
		mem[SPc] = (mem[SPc]+1)&mask;
		mem[(int)mem[SPc]] = val;
	}
	
	public static long popStackC(long[] mem){
		final int mask = mem.length-1;
		long ret = mem[(int)mem[SPc]];
		mem[SPc] = (mem[SPc]-1)&mask;
		return ret;
	}
	
	public static void push(long[] mem, long val){
		final int mask = mem.length-1;
		mem[SPa] = (mem[SPa]+1)&mask;
		mem[(int)mem[SPa]] = val;
	}
	
	public static long pop(long[] mem){
		final int mask = mem.length-1;
		long ret = mem[(int)mem[SPa]];
		mem[SPa] = (mem[SPa]-1)&mask;
		return ret;
	}
	
	public static int popi(long[] mem){
		return (int)pop(mem);
	}
	
	public static void pushi(long[] mem, int val){
		push(mem,(long)val);
	}
	
	public static float popf(long[] mem){
		return Float.intBitsToFloat((int)pop(mem));
	}
	
	public static void pushfraw(long[] mem, float f){
		push(mem, Float.floatToRawIntBits(f));
	}
	
	public static void pushfnorm(long[] mem, float f){
		push(mem, Float.floatToIntBits(f));
	}
	
	public static double popd(long[] mem){
		return Double.longBitsToDouble(pop(mem));
	}
	
	public static long op(long[] mem){
		return mem[(int)mem[IP]];
	}
	
	public static int safePtr(long[] mem, int ptr){
		final int mask = mem.length-1;
		return ptr&mask;
	}
	
	public static int safePtr(long[] mem, long ptr){
		final int mask = mem.length-1;
		return ((int)ptr)&mask;
	}
	
	public static long safeRead(long[] mem, int ptr){
		final int mask = mem.length-1;
		return mem[ptr&mask];
	}
	
	public static void safeWrite(long[] mem, int ptr, long val){
		final int mask = mem.length-1;
		mem[ptr&mask] = val;
	}
	
	/** verify mem is powOf2 size and is at least MIN_SIZE.
	TODO verify does not write where it shouldnt, etc.
	*
	public static void fastTest(long[] mem) throws RuntimeException{
		final int mask = mem.length-1;
		if((mem.length&mask) != 0 ||)
			throw new RuntimeException("mem is not powOf2 size: "+mem.length);
	}*/
	
	public static void nextState(long[] mem){
		//fastTest(mem); //TODO remove this line for speed
		int ip = safePtr(mem,mem[IP]);
		int nextIP = ip+1;
		long opcode = mem[ip]; //all possible opcodes are valid
		if(opcode >= 0){
			//any nonnegative value is an op to push itself onto stack.
			//To push a negative value x, do this for ~x then stackFlipAllBits.
			push(mem,opcode);
		}else{
			byte op = (byte)opcode;
			switch(op){
			case stackFlipAllBits:
				push(mem,~pop(mem));
			break;case threeFlipAllBits:
				//TODO get 3 bytes from opcode, read 2 of them, write the third.
			break; case stackIadd:
				push(mem,popi(mem)+popi(mem));
			break; case stackImul:
				push(mem,pop(mem)*pop(mem));
			break; case popJump:
				nextIP = popi(mem);
			break; case popJumpRel:
				nextIP = ip+popi(mem);
			break; case stackFmulnorm:
				pushfnorm(mem,popf(mem)*popf(mem));
			break; case stackReadMem:
				push(mem,safeRead(mem,popi(mem)));
			break; case stackWriteMem:
				safeWrite(mem,popi(mem),pop(mem));
			break; case moveAB:
				pushStackB(mem, pop(mem));
			break; case moveBA:
				push(mem, popStackB(mem));
			break; case moveBC:
				pushStackC(mem, popStackB(mem));
			break; case moveCB:
				pushStackB(mem, popStackC(mem));
			break; case moveCA:
				push(mem, popStackC(mem));
			break; case moveAC:
				pushStackC(mem, pop(mem));
			break; case pushIP:
				push(mem,ip);
			break; case pushSPa:
				push(mem,safePtr(mem,mem[SPa]));
			break; case pushSPb:
				push(mem,safePtr(mem,mem[SPb]));
			break; case pushSPc:
				push(mem,safePtr(mem,mem[SPc]));
			break; case incj:
				push(mem,pop(mem)+1);
			break; case decj:
				push(mem,pop(mem)-1);
			}
		}
		mem[IP] = safePtr(mem,nextIP);
	}

}
