package immutable.util;

public class EscapeUtil{
	
	/** backup prog limits filename (not path) to 220, so includes extensions
	such as ".jsonperline" (todo change that to .jsonl?).
	*/
	public static final int maxEscapedNameLen = 200; //TODO chars or bytes? Check at least Windows and Linux limits.
	
	/** All these are replaced urlEscaping (but on these chars), such as "_" becomes "%5f".
	Also, capitals must be preceded by "_" since names are caseSensitive
	but must also work in fileSystems that check filename equality caseInsensitive (Windows).
	*/
	public static final String escapedChars = "%\\/:;\r\n?*\"<>|._";
	
	/** 2017-9-15 Some of benrayfields existing data is too long when names are escaped. Will fix them later. */
	public static boolean isValidName(String name){
		//capital letters must be prefixed by _ so caseInsensitive file systems can store case sensitive listweb names
		return name.length()!=0 && (name.length() <= maxEscapedNameLen/2
			|| escapeNameNoThrow(name).length() <= maxEscapedNameLen);
	}
	
	public static String escapeName(String name){
		String escaped = escapeNameNoThrow(name);
		//TODO does any relevant filesystem measure in bytes instead of chars? What max len per path part?
		if(maxEscapedNameLen < escaped.length()){
			throw new RuntimeException("EscapedName too long: escaped="+escaped+" escapedLen="+escaped.length()+" name="+name);
		}
		return escaped;
	}
	
	/** escapeName is the normal way. This is used when choosing how to rename when its too long. */
	public static String escapeNameNoThrow(String name){
		if(!name.equals(name.trim())){
			throw new RuntimeException("Name has leading or trailing whitespace["+name+"]");
		}
		//TODO optimize
		for(int i=0; i<escapedChars.length(); i++){
			name = name.replace(escapedChars.substring(i,i+1),escapeFor(escapedChars.charAt(i)));
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<name.length(); i++){
			char c = name.charAt(i);
			if(Character.isUpperCase(c)) sb.append('_');
			sb.append(c);
		}
		String escaped = sb.toString();
		return escaped;
	}
	
	static String escapeFor(char c){
		String s = Integer.toHexString(c);
		if(s.length() > 2) throw new RuntimeException("TODO urlescape multibyte char: "+c);
		if(s.length() == 1) s = "0"+s;
		return "%"+s;
	}

}
