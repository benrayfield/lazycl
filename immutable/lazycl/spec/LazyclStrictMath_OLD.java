package immutable.lazycl.spec;
import static mutable.util.Lg.lg;

import immutable.util.IEEE754;
import immutable.util.MathUtil;
import immutable.util.Pair;
import mutable.util.Files;

/** some of these funcs match their opencl forms bit for bit,
like gpuSigmoids(...) matches /data/code/lib/fdlibm53/Fdlibm53SigmoidFloatButUsesDoubles.langColonCode
*/
public strictfp class LazyclStrictMath_OLD{
	
	/** same bits as gpu computing exp */
	public static double cpuExp(double x){
		return normSubnormals(MathUtil.setLowBitTo0(StrictMath.exp(x)));
		//return StrictMath.exp(x);
		//return MathUtil.setLowBitTo0(StrictMath.exp(x));
	}
	
	public static double cpuSigmoid(double x){
		return 1/(1+cpuExp(-x));
	}
	
	public static double cpuSigmoidDerivative(double x){
		final double sigmoid = cpuSigmoid(x);
		final double oneMinusSigmoid = 1-sigmoid; //force order of ops for determinism
		return sigmoid*oneMinusSigmoid;
	}
	
	public static float cpuSigmoidDerivative(float x){
		return (float)cpuSigmoidDerivative((double)x);
	}
	
	public static float cpuSigmoid(float x){
		return (float)cpuSigmoid((double)x);
	}
	
	public static float[] cpuSigmoids(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuSigmoid(x[i]);
		return ret;
	}
	
	public static float[] cpuSigmoidDerivs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuSigmoidDerivative(x[i]);
		return ret;
	}
	
	public static float[] cpuTanhs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuTanh(x[i]);
		return ret;
	}
	
	public static float[] cpuTanhDerivs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuTanhDerivative(x[i]);
		return ret;
	}
	
	public static float[] gpuSigmoids(Lazycl lz, float... x){
		return (float[]) lz.opencl().callOpencl(
			Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53SigmoidFloatButUsesDoubles.langColonCode"),
			new int[]{x.length}, //globalSize
			null, //localSize
			new float[x.length], //ignores this other than to get its size
			x
		)[0]; //return replacement of x
	}
	
	public static float[] gpuSigmoidDerivs(Lazycl lz, float... x){
		return (float[]) lz.opencl().callOpencl(
			Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53DerivativeOfSigmoidFloatButUsesDoubles.langColonCode"),
			new int[]{x.length}, //globalSize
			null, //localSize
			new float[x.length], //ignores this other than to get its size
			x
		)[0]; //return replacement of x
	}
	
	public static float[] gpuTanhs(Lazycl lz, float... x){
		return (float[]) lz.opencl().callOpencl(
			Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53TanhFloatButUsesDoubles.langColonCode"),
			new int[]{x.length}, //globalSize
			null, //localSize
			new float[x.length], //ignores this other than to get its size
			x
		)[0]; //return replacement of x
	}
	
	public static float[] gpuTanhDerivs(Lazycl lz, float... x){
		return (float[]) lz.opencl().callOpencl(
			Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53DerivativeOfTanhFloatButUsesDoubles.langColonCode"),
			new int[]{x.length}, //globalSize
			null, //localSize
			new float[x.length], //ignores this other than to get its size
			x
		)[0]; //return replacement of x
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
				//readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53ExpExceptSetLowBitOfReturnedDoubleTo0.langColonCode"),
					Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53Exp.langColonCode"),
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
			Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53Exp_withExtraOutputForDebug.langColonCode"),
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
	
	/** change subnormals to 0. Does not norm infinites or nans. */
	public static double normSubnormals(double d){
		return (-Double.MIN_NORMAL < d && d < Double.MIN_NORMAL) ? 0. : d;
	}
	
	/** Same as multiple calls of java.lang.StrictMath.exp(x), computed in opencl in 1 parallel call.
	For each in double[], same as java.lang.StrictMath.exp(double), returns double[] same size */
	public static LazyBlob exps(Lazycl lz, LazyBlob doubles){
		return lz.lazycl(
			"Code", Files.readStringFromRelFileCached("/data/code/lib/fdlibm53/Fdlibm53Exp.langColonCode"),
			"Bize", doubles.bize(),
			"GlobalSize", doubles.bize()/64,
			"in", doubles
		);
	}
	
	/** TODO this loses some precision near x=0 by using sigmoid as a middle step
	but the math is exact other than float roundoff.
	TODO test this.
	*/
	public static double cpuTanh(double x){
		return cpuSigmoid(2*x)*2-1;
		/*double ePowX = cpuExp(x);
		double ePowNegX = 1./ePowX;
		return (ePowX - ePowNegX)/(ePowX + ePowNegX);
		/*=
		FIXME match the *langColonCode which does it a different way than by sigmoid.
		final double twoTimesSigmoidOf2X = cpuSigmoid(x*2)*2; //force order of ops for determinism
		return twoTimesSigmoidOf2X-1;
		*/
	}
	
	public static float cpuTanh(float x){
		return (float)cpuTanh((double)x);
	}
	
	/** TODO test this. */
	public static double cpuTanhDerivative(double x){
		double sigmoid = cpuSigmoid(x);
		double derivOfSigmoid = (sigmoid * (1.-sigmoid));
		double derivOfTanh = 4*derivOfSigmoid; //FIXME?
		return derivOfTanh;
		/*double ePowX = cpuExp(x);
		double ePowNegX = 1./ePowX;
		return 4./(ePowX + ePowNegX);
		//return cpuSigmoidDerivative(x*.5)*.5;
		*/
	}
	
	public static float cpuTanhDerivative(float x){
		return (float)cpuTanhDerivative((double)x);
	}
	
	public static final class SimpleAndALittleLessPreciseButStillDeterministic{
		
		//x^y for any number x and integer y
		public static double MultiplyByItselfNTimes(double x, int y){
			double ret = 1;
			for(int i=y; i>0; i--) ret *= x;
			return ret;
		};
		
		public static final byte digitBitsInDoubleIncludingHigh1 = 1+IEEE754.sizeDFraction;
		
		public static final double TwoPowNeg_digitBitsInDoubleIncludingHigh1 = MultiplyByItselfNTimes(.5,digitBitsInDoubleIncludingHigh1);
	
		/** e^x-1. This has far more precision where return value is near 0 than computing e^x first then subtracting 1. Returns -1..infinity, median 0.
		e^x =approxEquals= (1+(.5*x)^DigitBitsInDouble)^(2^DigitBitsInDouble)
		*/
		public static double expm1(double x){
			double m = TwoPowNeg_digitBitsInDoubleIncludingHigh1;
			m *= x; //If leave m as 1 here, returns (approx) e aka (1+.5^DigitBitsInDouble)^(2^DigitBitsInDouble)
			for(int i=0; i<digitBitsInDoubleIncludingHigh1; i++){ //exponent by squaring. deterministic roundoff if strictfp/"use strict".
				final double c = m*2;
				final double d = m*m;
				m = c+d;
			}
			return m;
		};
	
		public static double Exp(double x){ return 1+expm1(x); };
	
		public static final double E = Exp(1);
	
	}
	
	public static final class ExperimentFloatLog{
		/*
		https://stackoverflow.com/questions/9799041/efficient-implementation-of-natural-logarithm-ln-and-exponentiation
		float ln(float y) {
		    int log2;
		    float divisor, x, result;

		    log2 = msb((int)y); // See: https://stackoverflow.com/a/4970859/6630230
		    divisor = (float)(1 << log2);
		    x = y / divisor;    // normalized value between [1.0, 2.0]

		    result = -1.7417939 + (2.8212026 + (-1.4699568 + (0.44717955 - 0.056570851 * x) * x) * x) * x;
		    result += ((float)log2) * 0.69314718; // ln(2) = 0.69314718

		    return result;
		}

		Although if you plan to use it only in the [1.0, 2.0] interval, then the function is like:

		float ln(float x) {
		    return -1.7417939 + (2.8212026 + (-1.4699568 + (0.44717955 - 0.056570851 * x) * x) * x) * x;
		}
		*/
	}
	
	public static void main(String[] args){
		
		//wolframalpha says e =
		//2.718281828459045235360287471352662497757247093699959574966967627724076630353547594571382...
		
		//java.lang.Math.E says: The {@code double} value that is closer than any other to
	    //<i>e</i>, the base of the natural logarithms.
	    //public static final double E = 2.7182818284590452354;
		
		double derivedE = LazyclStrictMath_OLD.SimpleAndALittleLessPreciseButStillDeterministic.E;
		//This derived E is closer than any other double value to the exact value of E
		//and exactly equals the constant java.lang.Math.E and browser javascript's Math.E.
		lg("derivedE="+derivedE+" Math.E="+Math.E+" diff="+Math.abs(derivedE-Math.E));
		//derivedE=2.718281828459045 Math.E=2.718281828459045 diff=0.0
		//Same thing it said in firefox and chrome in win10 with this code.
		//Does not disagree with the actual value of e on any digit except maybe the last should be rounded up.
		/*
		"use strict"; //TODO which ways of using doubles have deterministic roundoff same in javascript, java, and opencl?

		//2 exponent 53 is biggest integer that double can densely represent.
		//It has 1 sign bit, 11 exponent bits, a high 1 bit not stored, and 52 fraction digit bits.
		//The exponent bits are 0 for subnormals (very small numbers and 0), 11 1s for infinities and nans, else is a normal exponent.
		const DigitBitsInDouble = 53;
		const digitBitsInDoubleIncludingHigh1 = 1+DigitBitsInDouble;
		
		//x^y for any number x and integer y
		const MultiplyByItselfNTimes = (x,y)=>{
			let ret = 1;
			for(let i=y; i>0; i--) ret *= x;
			return ret;
		};
		
		const TwoPowNeg_digitBitsInDoubleIncludingHigh1 = MultiplyByItselfNTimes(.5,digitBitsInDoubleIncludingHigh1);
		
		//e^x-1. This has far more precision where return value is near 0 than computing e^x first then subtracting 1. Returns -1..infinity, median 0.
		//e^x =approxEquals= (1+(.5*x)^digitBitsInDoubleIncludingHigh1)^(2^digitBitsInDoubleIncludingHigh1)
		const Expm1 = function(x){
			let m = TwoPowNeg_digitBitsInDoubleIncludingHigh1;
			m *= x; //If leave m as 1 here, returns (approx) e aka (1+.5^digitBitsInDoubleIncludingHigh1)^(2^digitBitsInDoubleIncludingHigh1)
			for(let i=0; i<digitBitsInDoubleIncludingHigh1; i++){ //exponent by squaring. deterministic roundoff if strictfp/"use strict".
				const c = m*2;
				const d = m*m;
				m = c+d;
			}
			return m;
		};
		
		//e^x
		const Exp = (x)=>(1+Expm1(x));
		
		const E = Exp(1);
		*/
	}

}
