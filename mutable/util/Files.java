/** Ben F Rayfield offers this software opensource MIT license */
package mutable.util;
import static mutable.util.Lg.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import immutable.util.EscapeUtil;
import immutable.util.Text;

public class Files{
	public static final Map<String,String> readStringFromRelFile = Collections.synchronizedMap(new HashMap());

	private Files(){}
	
	/** path relative to where this program started, like "/data/listweb/s/start.json"
	or "/immutable/occamsfuncer/Id.java" or "/immutable/occamsfuncer/Id.class" (FIXME some of those might be internal aka in jar not files,
	and I do want ability to move them between jar and file and store in any combo I want, but not to write them when in jar.
	*/
	public static File r(String path){
		return new File(Files.dirWhereThisProgramStarted, path); //FIXME remove / if path starts with /?
	}

	/** null if not exist */
	public static byte[] read(File file){
		if(!file.exists()) return null;
		try{
			if(file.isDirectory()) return new byte[0];
			InputStream fileIn = null;
			try{
				fileIn = new FileInputStream(file);
				long byteSize = file.length();
				if(byteSize > Integer.MAX_VALUE) throw new Error("File content too big ("+byteSize+" bytes) for byte array: "+file);
				byte fileBytes[] = new byte[(int)byteSize];
				int bytesRead = fileIn.read(fileBytes);
				if(bytesRead != fileBytes.length) throw new IOException(
					"Tried to write "+fileBytes.length+" bytes but did write "+bytesRead+" bytes.");
				return fileBytes;
			}finally{
				if(fileIn!=null) fileIn.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	//TODO always File then byte[], use same order of params everywhere
	
	public static void write(byte data[], File file){
		write(data, file, false);
	}
	
	public static void append(byte data[], File file){
		write(data, file, true);
	}
	
	public static void write(File f, byte[] val){
		f.getParentFile().mkdirs();
		OutputStream out = null;
		try{
			out = new FileOutputStream(f);
			out.write(val);
		}catch(IOException e){
			throw new Error(e);
		}finally{
			if(out != null) try{ out.close(); }catch(IOException e){ throw new Error(e); }
		}
	}
	
	/** make sure to close it before calling other write/append funcs */
	public static OutputStream appending(File file){
		try{
			if(!file.exists()) file.createNewFile();
			return new FileOutputStream(file,true);
		}catch(IOException e){ throw new Error(e); }
	}
	
	public static void delete(File f){
		if(!f.delete()){
			throw new Error("Couldnt delete "+f);
		}
	}
	
	public static byte[] serializableToBytes(Serializable s){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			new ObjectOutputStream(out).writeObject(s);
		}catch (IOException e){ throw new Error(e); }
		return out.toByteArray();
	}
	
	public static Serializable bytesToSerializable(byte[] b){
		try{
			return (Serializable) new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
		}catch (ClassNotFoundException | IOException e){ throw new Error(e); }
	}
	
	/** Creates dirs under file if not exist already, unless names of those dirs already exist as files */
	protected static void write(byte data[], File file, boolean append){
		lg("Saving "+file);
		try{
			file.getParentFile().mkdirs();
			OutputStream fileOut = null;
			try{
				if(!file.exists()){
					File parent = file.getParentFile();
					if(parent!=null) parent.mkdirs();
					file.createNewFile();
				}
				fileOut = new FileOutputStream(file, append);
				fileOut.write(data, 0, data.length);
				fileOut.flush();
			}finally{
				if(fileOut!=null) fileOut.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static final File dirWhereThisProgramStarted, dataDir, codeAndClasspathDir, varDir;
	
	public static final File libDir;
	static{
		dirWhereThisProgramStarted = new File(System.getProperty("user.dir")).getAbsoluteFile();
		dataDir = new File(dirWhereThisProgramStarted,"data");
		codeAndClasspathDir = new File(dataDir,"code");
		libDir = new File(dataDir,"lib");
		varDir = new File(dataDir,"var");
		//File test = new File(acycDir,"readme.txt");
		File test = dataDir;
		if(!test.exists()) throw new RuntimeException("(TODO rewrite these instructions, too long) My data dir not found, as its expected to contain certain files. Did not find: "+test+" (often thats cuz its in src/data but src should be the working dir). If this program is in an IDE such as Eclipse or Netbeans, set the working dir to the source dir aka classpath. In Eclipse (after checking the 'allow output folders for source folders') in 'project build path', set 'default output dir' to forExample the src dir which contains package/name.java and package/name.class and the data dir. In any 'run configuration' there is also a 'working dir' in the options. This would all happen automaticly if doubleclick a jar file containing most of that (including data dir and classes and source, and the rest is generated in a new data dir or reuse found data dir parallel to where you doubleclicked the jar.");
	}
	
	public static boolean bytesEqual(byte x[], byte y[]){
		if(x.length != y.length) return false;
		for(int i=0; i<x.length; i++){
			if(x[i] != y[i]) return false;
		}
		return true;
	}
	
	public static byte[] readFileOrInternalRel(String relPath){
		//TODO merge duplicate code
		String r = relPath.startsWith("\\")||relPath.startsWith("/") ? relPath.substring(1) : relPath;
		File f = new File(r);
		if(f.exists()){ //avoid cache problems with using file url
			System.out.println("Reading file "+f);
			InputStream in = null;
			try{
				in = new FileInputStream(f);
				if(Integer.MAX_VALUE < f.length()) throw new RuntimeException("File too big: "+f);
				byte b[] = new byte[(int)f.length()];
				in.read(b);
				return b;
			}catch(IOException e){
				throw new RuntimeException(e);
			}finally{
				if(in != null) try{ in.close(); }catch(IOException e){}
			}
		}else{
			//System.out.println("Reading Class.getResourceAsStream "+relPath);
			InputStream in = null;
			try{
				in = Files.class.getResourceAsStream(relPath);
				return ByteStreams.bytes(in, 1<<28, 5, true);
			}finally{
				if(in != null) try{ in.close(); }catch(IOException e){}
			}
		}
	}
	
	public static byte[] readFully(InputStream in){
		return ByteStreams.bytes(in, 1<<28, 20, true);
	}
	
	/** ignores if null */
	public static void close(Closeable c){
		if(c != null){
			try{
				c.close();
			}catch(IOException e){ throw new Error(e); }
		}
	}
	
	/** ignores if null */
	public static void closeNoThrow(Closeable c){
		if(c != null){
			try{
				c.close();
			}catch(IOException e){ lgErr(e); }
		}
	}
	
	/** WARNING: this is not for longterm storage, since the datastruct can change across different java versions,
	and instead I plan to create ufnode which is a programming language and merkle datastruct for longterm storage and gaming low lag.
	*/
	public static byte[] serialize(Serializable s){
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(s);
			return out.toByteArray();
		}catch (IOException e){
			throw new Error(e);
		}
		
	}
	
	/** WARNING: this is not for longterm storage, since the datastruct can change across different java versions,
	and instead I plan to create ufnode which is a programming language and merkle datastruct for longterm storage and gaming low lag.
	*/
	public static Serializable deserialize(byte[] serialized){
		if(serialized == null) return null;
		try{
			return (Serializable) new ObjectInputStream(new ByteArrayInputStream(serialized)).readObject();
		}catch(ClassNotFoundException | IOException e){
			throw new Error(e);
		}
	}
	
	/** returns file for saving and loading a named object, such as "rbm5" */
	public static File serializeFile(String name){
		return new File(new File(Files.dataDir,"tempSerializable"),name+".ser");
	}
	
	/** Example: shift+number on keyboard saves rbm by Serializable (not for longterm storage) to a file */
	public static void saveBySerialize(Serializable s, String name){
		write(serialize(s), serializeFile(name));
	}
	
	public static Serializable loadBySerialize(String name){
		return deserialize(read(serializeFile(name)));
	}
	
	public static String changeBackToForwardSlashesAndAddSlashAtEndIfFolder(String fileOrFolder){
		fileOrFolder = fileOrFolder.replace('\\','/');
		File file = new File(fileOrFolder);
		if(file.isDirectory() && !fileOrFolder.endsWith("/")) fileOrFolder += "/";
		return fileOrFolder;
	}
	
	public static boolean isFolderPathName(String folderPathName){
		File f = new File(folderPathName);
		return f.exists() && f.isDirectory();
	}
	
	/** includes folder. Returns empty list if folder is not a folder. */
	public static List<File> allFoldersRecursive(File folder){
		if(!folder.isDirectory()) return new ArrayList();
		List<File> folders = new ArrayList();
		folders.add(folder);
		for(File f : folder.listFiles()) folders.addAll( allFoldersRecursive(f) );
		return folders;
	}
	
	public static File fileOfVar(String varName){
		if(!varName.equals(varName.trim())) throw new RuntimeException("Is not trimmed: "+varName);
		if(varName.length()==0) throw new RuntimeException("Empty varName");
		String escaped = EscapeUtil.escapeName(varName);
		/*String first = escaped.startsWith("_") ? FIXME what if it starts with __?
		
		FIXME if it starts with _x for any char x, then thats 1 dir, like in listweb.
		If it starts with _x_y for any chars x and y thats 2 dirs.
		so are xy. Otherwise _T which is the escape of T (cuz of case insensitive file systems) creates "/_/T/_The brown fox".
		but should create "/_T/h/_The brown fox".
		*/
		
		return escaped.length()<2
			? new File(new File(varDir,"oneChar"),escaped)
			: new File(new File(new File(varDir,escaped.substring(0,1)),escaped.substring(1,2)),escaped);
	}

	/** from inside self as jar or working dir */
	public static String readStringFromRelFileCached(String relFile) {
		String ret = Files.readStringFromRelFile.get(relFile);
		if(ret == null){
			ret = Text.bytesToStr(readFileOrInternalRel(relFile));
			Files.readStringFromRelFile.put(relFile, ret);
		}
		return ret;
	}

}
