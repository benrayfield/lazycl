package immutable.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetUtil{
	
	/** immutable set */
	public static <T> Set<T> set(T... content){
		return Collections.unmodifiableSet(new HashSet(Arrays.asList(content)));
	}

}
