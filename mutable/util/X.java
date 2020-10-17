/** Ben F Rayfield offers this software opensource MIT license */
package mutable.util;

/** short name for a RuntimeException*/
public class X extends RuntimeException{
	
	public static X X(String message){
		return new X(message);
	}
	
	public static X X(Throwable rethrow){
		return new X(rethrow);
	}
	
	public static X X(String message, Throwable rethrow){
		return new X(message, rethrow);
	}
	
	public X(){}

	public X(String message){
		super(message);
	}

	public X(String message, Throwable cause){
		super(message, cause);
	}
	
	public X(Throwable cause){
		super(cause);
	}
	
}
