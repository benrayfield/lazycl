/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import immutable.lazycl.spec.LazyBlob;
import immutable.lazycl.spec.Lazycl;
import immutable.util.Blob;

/** at all times, exactly 1 of the inner Blob or Map<String,Blob> is null.
OLD: 1 of Blob data or Supplier<Blob> eval is null at a time.
Blob becomes nonnull when f(int) or d(int) or other funcs that read this as a bitstring.
*/
public strictfp class SimpleLazyBlob implements LazyBlob{
	
	/*How is Blob put into SimpleLazyBlob during eval?
	
	If theres a Supplier_or_Function to eval, should it be Supplier<Blob> or Function<LazyBlob,Blob>?
			
	Should the Map<String,LazyBlob> be stored in Lazycl or in LazyBlob?
			
	Should isTemp and DependEdge.together be related?
	*/
	
	/** null until eval the Map<String,Blob> or is it Map<String,LazyBlob> after which the map becomes null
	so the blobs this blob was derived from can be garbcoled.
	*/
	protected Blob data;
	
	protected Lazycl lz;
	
	/** the command to eval (in Lazycl.lazycl(Map)) to get a Blob */
	protected Map<String,LazyBlob> params;
	//FIXME should it be Blob or LazyBlob in the params? Dont want to have to write casts lots of places,
	//	but also dont want to have to wrap Blob in a LazyBlob to be returned places expecting a LazyBlob.
	
	protected Consumer<Blob> evalReturnsToHere;
	
	//protected Supplier<Blob> eval;
	
	//FIXME how to choose isTemp? In Map<String,LazyBlob> theres "isTemp" key that maps to 1 or 0 (a bit).
	//But what about the public SimpleLazyBlob(Blob data) constructor? Would temp ever be used with that? Probably not needed.
	//Temp would probably always be only used in Map<String,LazyBlob> params and never set the Blob
	//since its created and garbcoled in gpu memory for example or in java memory for a loop
	//that reads and writes the same array and returns its final state for example.
	
	/** already evaled (unless param is a LazyBlob thats not evaled yet) */
	public SimpleLazyBlob(Blob data){
		this.data = data;
		this.lz = null;
		this.params = null;
		this.evalReturnsToHere = null;
	}
	
	public Consumer<Blob> vm_evalReturnsToHere(){
		Consumer<Blob> ret = evalReturnsToHere;
		evalReturnsToHere = null;
		return ret;
	}
	
	/*public SimpleLazyBlob(Supplier<Blob> eval){
		this.eval = eval;
	}*/
	
	/** param is command to eval in Lazycl.lazycl(Map) to get a Blob */
	public SimpleLazyBlob(Lazycl lz, Map<String,LazyBlob> params){
		this.data = null;
		this.lz = lz;
		//immutable. FIXME what if already was immutable. dont keep wrapping deeper.
		this.params = Collections.unmodifiableNavigableMap(new TreeMap(params));
		final SimpleLazyBlob thiz = this;
		this.evalReturnsToHere = (Blob evalReturned)->{
			if(evalReturned == null) throw new NullPointerException();
			thiz.data = evalReturned;
			thiz.lz = null;
			thiz.params = null;
		};
	}
	
	protected void eval(){
		lz.vm_lazyBlobsEvalCallsThis(this); //causes the first value of evalReturnsToHere to receive the returned Blob, unless it throws
		
		/*FIXME how does Blob get set? Do I want the Supplier<Blob> back?
			or Function<LazyBlob,Blob>?
				
		
				
		
		data = lz.
		*/
		
		/*Supplier<Blob> e = eval;
		eval = null;
		data = eval.get();
		*/
	}

	public long bize(){
		try{
			return data.bize();
		}catch(NullPointerException e){
			eval();
			return data.bize();
		}
	}

	public byte piz(){
		try{
			return data.piz();
		}catch(NullPointerException e){
			eval();
			return data.piz();
		}
	}

	public boolean flo(){
		try{
			return data.flo();
		}catch(NullPointerException e){
			eval();
			return data.flo();
		}
	}
	
	public boolean z(long bitIndex){
		try{
			return data.z(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.z(bitIndex);
		}
	}
	
	public float F(long bitIndex){
		try{
			return data.F(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.F(bitIndex);
		}
	}
	
	public float f(int index){
		try{
			return data.f(index);
		}catch(NullPointerException e){
			eval();
			return data.f(index);
		}
	}
	
	public double D(long bitIndex){
		try{
			return data.D(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.D(bitIndex);
		}
	}
	

	public double d(int index){
		try{
			return data.d(index);
		}catch(NullPointerException e){
			eval();
			return data.d(index);
		}
	}
	
	public long J(long bitIndex){
		try{
			return data.J(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.J(bitIndex);
		}
	}
	
	public long j(int index){
		try{
			return data.j(index);
		}catch(NullPointerException e){
			eval();
			return data.j(index);
		}
	}
	
	public int I(long bitIndex){
		try{
			return data.I(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.I(bitIndex);
		}
	}
	
	public int i(int index){
		try{
			return data.i(index);
		}catch(NullPointerException e){
			eval();
			return data.i(index);
		}
	}
	
	public char C(long bitIndex){
		try{
			return data.C(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.C(bitIndex);
		}
	}
	
	public char c(int index){
		try{
			return data.c(index);
		}catch(NullPointerException e){
			eval();
			return data.c(index);
		}
	}
	
	public short S(long bitIndex){
		try{
			return data.S(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.S(bitIndex);
		}
	}
	
	public short s(int index){
		try{
			return data.s(index);
		}catch(NullPointerException e){
			eval();
			return data.s(index);
		}
	}
	
	public byte B(long bitIndex){
		try{
			return data.B(bitIndex);
		}catch(NullPointerException e){
			eval();
			return data.B(bitIndex);
		}
	}
	
	public byte b(int index){
		try{
			return data.b(index);
		}catch(NullPointerException e){
			eval();
			return data.b(index);
		}
	}
	
	public InputStream IN(long bitFrom, long bitToExcl){
		try{
			return data.IN(bitFrom,bitToExcl);
		}catch(NullPointerException e){
			eval();
			return data.IN(bitFrom,bitToExcl);
		}
	}
	
	public InputStream IN(){
		try{
			return data.IN();
		}catch(NullPointerException e){
			eval();
			return data.IN();
		}
	}
	
	public <T> T arr(Class<T> type, long bitFrom, long bitToExcl){
		try{
			return data.arr(type,bitFrom,bitToExcl);
		}catch(NullPointerException e){
			eval();
			return data.arr(type,bitFrom,bitToExcl);
		}
	}

	public <T> T arr(Class<T> type){
		try{
			return data.arr(type);
		}catch(NullPointerException e){
			eval();
			return data.arr(type);
		}
	}
	
	public Object arr(){
		try{
			return data.arr();
		}catch(NullPointerException e){
			eval();
			return data.arr();
		}
	}

	public Map<String,LazyBlob> vm_lazyCall(){
		return params;
	}

}
