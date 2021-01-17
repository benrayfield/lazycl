package mutable.util;
import static mutable.util.Lg.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** java reflection funcs */
public class JReflect{
	
	/** Example: call("java.lang.System.exit", 0) */
	public static Object call(String func, Object... params){
		try{
			int lastDot = func.lastIndexOf('.');
			if(lastDot == -1) throw new RuntimeException("No dot in "+func);
			Class cl = Class.forName(func.substring(0,lastDot));
			String funcName = func.substring(lastDot+1);
			for(Method m : cl.getMethods()){
				if(m.getName().equals(funcName)){
					//FIXME check if theres multiple Method that could work,
					//and use the one with the most specific types that match,
					//but for now just use the first one found.
					
					boolean isStatic = Modifier.isStatic(m.getModifiers());
					Class[] paramTypes = m.getParameterTypes();
					int offset = isStatic ? 0 : 1;
					if(params.length == (offset+paramTypes.length)){
						boolean typesMatch = true;
						if(!isStatic && !m.getDeclaringClass().isInstance(params[0])){
							//instance is null for static Method
							typesMatch = false;
						}
						for(int i=0; i<paramTypes.length; i++){
							if(params[offset+i] != null && !norm(paramTypes[i]).isInstance(params[offset+i])){
								//null can be cast to anything
								typesMatch = false;
								break;
							}
						}
						if(typesMatch){ //do the call
							if(isStatic){
								lg("JReflect.call "+m+" .. "+Arrays.asList(params));
								return m.invoke(null, params);
							}else{
								Object[] allParamsExceptFirst = new Object[params.length-1];
								System.arraycopy(params, 1, allParamsExceptFirst, 0, allParamsExceptFirst.length);
								lg("JReflect.call "+m+" .. "+params[0]+" .. "+Arrays.asList(allParamsExceptFirst));
								return m.invoke(params[0], allParamsExceptFirst);
							}
						}
					}
				}
			}
		}catch(Exception e){ throw new RuntimeException(e); }
		throw new RuntimeException("Didnt find func "+func
			+" compatible with params (first is instance if isStatic, else first is first param) "+Arrays.asList(params));
	}
	
	/** autoboxes if its a primitive type, but not if its an array type of primitives */
	public static Class norm(Class c){
		if(!c.isPrimitive()) return c;
		if(c == boolean.class) return Boolean.class;
		if(c == byte.class) return Byte.class;
		if(c == short.class) return Short.class;
		if(c == char.class) return Character.class;
		if(c == int.class) return Integer.class;
		if(c == long.class) return Long.class;
		if(c == float.class) return Float.class;
		if(c == double.class) return Double.class;
		if(c == void.class) return Void.class;
		throw new Error("Unknown primitive type (did java add int256 or float128 etc?): "+c);
	}

}
