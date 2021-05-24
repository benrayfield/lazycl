package immutable.lazycl.spec;

import java.util.function.DoubleUnaryOperator;

import immutable.util.IEEE754;

public class LazyclStrictMath{
	
	public static final byte digitBitsInDoubleIncludingHigh1 = 1+IEEE754.sizeDFraction;
	public static final byte digitBitsInFloatIncludingHigh1 = 1+IEEE754.sizeFFraction;
	
	public static final double TwoPowNeg_digitBitsInDoubleIncludingHigh1 = pow(.5,digitBitsInDoubleIncludingHigh1);
	
	public static final float TwoPowNeg_digitBitsInFloatIncludingHigh1 = pow(.5f,digitBitsInFloatIncludingHigh1);

	/** e^x-1. This has far more precision where return value is near 0 than computing e^x first then subtracting 1. Returns -1..infinity, median 0.
	e^x =approxEquals= (1+(.5*x)^DigitBitsInDouble)^(2^DigitBitsInDouble)
	*/
	public static double cpuExpm1(double x){
		double m = TwoPowNeg_digitBitsInDoubleIncludingHigh1;
		m *= x; //If leave m as 1 here, returns (approx) e aka (1+.5^DigitBitsInDouble)^(2^DigitBitsInDouble)
		for(int i=0; i<digitBitsInDoubleIncludingHigh1; i++){ //exponent by squaring. deterministic roundoff if strictfp/"use strict".
			final double c = m*2;
			final double d = m*m;
			m = c+d;
		}
		return m;
	};
	
	/** e^x-1. This has far more precision where return value is near 0 than computing e^x first then subtracting 1. Returns -1..infinity, median 0.
	e^x =approxEquals= (1+(.5*x)^DigitBitsInDouble)^(2^DigitBitsInDouble)
	*/
	public static float cpuExpm1(float x){
		float m = TwoPowNeg_digitBitsInFloatIncludingHigh1;
		m *= x; //If leave m as 1 here, returns (approx) e aka (1+.5^DigitBitsInDouble)^(2^DigitBitsInDouble)
		for(int i=0; i<digitBitsInDoubleIncludingHigh1; i++){ //exponent by squaring. deterministic roundoff if strictfp/"use strict".
			final float c = m*2;
			final float d = m*m;
			m = c+d;
		}
		return m;
	}
	
	public static float cpuExp(float x){
		return 1+cpuExpm1(x);
	}
	
	public static double cpuExp(double x){
		return 1+cpuExpm1(x);
	}

	public static double[] cpuExps(Lazycl lz, double[] x){
		double[] ret = new double[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuExp(x[i]);
		return ret;
	}
	
	public static float cpuSigmoid(float x){
		return 1/(1+cpuExp(x));
	}
	
	public static float[] cpuSigmoids(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuSigmoid(x[i]);
		return ret;
	}
	
	/** (Sigmoid(x+epsilon)-Sigmoid(x))/epsilon */
	public static float cpuSigmoidDeriv(float x){
		//wolframalpha says: e^x/(e^x + 1)^2
		//e^x/(e^(2*x) + 2*e^x + 1)
		//1/(e^x + 2 + e^-x)
		//1/(e^x + e^-x + 2)
		final float expX = cpuExp(x);
		final float expNegX = 1/expX;
		final float sumExps = expX+expNegX;
		final float bottom = sumExps+2;
		return 1/bottom;
		//deriv of sigmoid: 1/(e^x + e^-x + 2)
		//deriv of tanh: 4/(e^(-2*x) + e^(2*x) + 2)
	};
	
	public static float[] cpuSigmoidDerivs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuSigmoidDeriv(x[i]);
		return ret;
	}
	
	public static float cpuTanh(float x){
		final float expX = cpuExp(x);
		final float expNegX = 1/expX;
		final float top = expX-expNegX;
		final float bottom = expX+expNegX;
		return top/bottom;
	}
	
	public static float[] cpuTanhs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuTanh(x[i]);
		return ret;
	}
	
	/** (Tanh(x+epsilon)-Tanh(x))/epsilon */
	public static float cpuTanhDeriv(float x){
		//wolframalpha says: 4/(e^-x + e^x)^2
		//4/(e^(-2*x) + e^(2*x) + 2)
		final float expTwoX = cpuExp(2*x);
		final float expNegTwoX = 1/expTwoX;
		final float sumExps = expTwoX + expNegTwoX;
		final float bottom = 2+sumExps;
		return 4/bottom;
	};
	
	public static float[] cpuTanhDerivs(float... x){
		float[] ret = new float[x.length];
		for(int i=0; i<x.length; i++) ret[i] = cpuTanhDeriv(x[i]);
		return ret;
	}

	public static float[] gpuSigmoids(Lazycl lz, float[] ins) {
		throw new RuntimeException("TODO");
	}

	public static float[] gpuSigmoidDerivs(Lazycl lz, float[] ins) {
		throw new RuntimeException("TODO");
	}

	public static float[] gpuTanhs(Lazycl lz, float[] ins) {
		throw new RuntimeException("TODO");
	}

	public static float[] gpuTanhDerivs(Lazycl lz, float[] ins) {
		throw new RuntimeException("TODO");
	}
	
	/** by squaring */
	public static double pow(double x, long y){
		if(y < 0) return pow(1/x, -y);
		double mul = x;
		double ret = 1;
		while(y-->0){
			if((y&1)!=0) ret *= mul;
			mul *= mul;
		}
		return ret;
	}
	
	/** by squaring */
	public static float pow(float x, long y){
		if(y < 0) return pow(1/x, -y);
		float mul = x;
		float ret = 1;
		while(y-->0){
			if((y&1)!=0) ret *= mul;
			mul *= mul;
		}
		return ret;
	}
	
	/** x^y for any number x and integer y. slow cuz not by squaring. *
	static double powSlow(double x, int y){
		double ret = 1;
		for(int i=y; i>0; i--) ret *= x;
		return ret;
	};
	
	/** x^y for any number x and integer y. slow cuz not by squaring. *
	static float powSlow(float x, int y){
		float ret = 1;
		for(int i=y; i>0; i--) ret *= x;
		return ret;
	};*/

	public static double Exp(double x){ return 1+cpuExpm1(x); };

	public static final double E = Exp(1);
	
	
}
