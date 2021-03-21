package immutable.lazycl.spec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import immutable.util.MathUtil;
import immutable.util.Pair;
import immutable.util.Text;
import mutable.util.Files;

public strictfp class LazyclStrictMath{
	
	/** same bits as gpu computing exp */
	public static double cpuExp(double x){
		return StrictMath.exp(x);
		//return MathUtil.setLowBitTo0(StrictMath.exp(x));
	}
	
	/** Same as setLowBitTo0(java.lang.StrictMath.exp(x)), computed in opencl,
	though this is wasteful to only do 1 double at a time, its mostly for testing.
	*/
	public static double exp(Lazycl lz, double x){
		return exps(lz, new double[]{x})[0];
	}
	
	/** Same as multiple calls of java.lang.StrictMath.exp(x), computed in opencl in 1 parallel call. */
	public static double[] exps(Lazycl lz, double[] x){
		//TODO optimize by using wrapb (backing double[]) instead of wrapc (copies double[])?
		//Only if caller wont modify the double[] before lazyeval of the returned LazyBlob.
	
		//TODO after callOpenclDependnet works for doubles. As of 2021-2-23 only callOpencl (1 kernel at a time) does.
		boolean useNewCodeForDoubles = false;
		if(useNewCodeForDoubles){
			return exps(lz, lz.wrapc(x)).arr(double[].class);
		}else{
			return (double[]) lz.opencl().callOpencl(
				//readStringFromRelFileCached("/data/lib/Fdlibm53ExpExceptSetLowBitOfReturnedDoubleTo0.langColonCode"),
					readStringFromRelFileCached("/data/lib/fdlibm53/Fdlibm53Exp.langColonCode"),
				new int[]{x.length},
				null,
				
				//copy output size from this. in callOpencl(Object[]), the param Object[] and returned Object[]
				//are same length, containing all opencl params and returns
				new double[x.length],
				
				x
			)[0];
		}
	}
	
	/** the longs are various things I'm testing as I change the Fdlibm53Exp_withExtraOutputForDebug.langColonCode
	file and Fdlibm53Exp.java together to track down why they're not getting the exact same answer (differs by at most 1 ulp).
	*/
	public static Pair<double[],long[]> exp_withExtraOutputForDebug(Lazycl lz, double[] x){
		Object[] out = lz.opencl().callOpencl(
			readStringFromRelFileCached("/data/lib/Fdlibm53Exp_withExtraOutputForDebug.langColonCode"),
			new int[]{x.length},
			null,
			
			//copy output size from this. in callOpencl(Object[]), the param Object[] and returned Object[]
			//are same length, containing all opencl params and returns
			new double[x.length],
			new long[x.length],
			x
		);
		return new Pair((double[])out[0], (long[])out[1]);
	}
	
	/** Same as multiple calls of java.lang.StrictMath.exp(x), computed in opencl in 1 parallel call.
	For each in double[], same as java.lang.StrictMath.exp(double), returns double[] same size */
	public static LazyBlob exps(Lazycl lz, LazyBlob doubles){
		return lz.lazycl(
			"Code", readStringFromRelFileCached("/data/lib/Fdlibm53Exp.langColonCode"),
			"Bize", doubles.bize(),
			"GlobalSize", doubles.bize()/64,
			"in", doubles
		);
	}
	
	private static final Map<String,String> readStringFromRelFile = Collections.synchronizedMap(new HashMap());
	
	/** from inside self as jar or working dir */
	public static String readStringFromRelFileCached(String relFile) {
		String ret = readStringFromRelFile.get(relFile);
		if(ret == null){
			ret = Text.bytesToStr(Files.readFileOrInternalRel(relFile));
			readStringFromRelFile.put(relFile, ret);
		}
		return ret;
	}

}
