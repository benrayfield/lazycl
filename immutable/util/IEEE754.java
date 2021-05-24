package immutable.util;

import java.util.Random;

import immutable.lazycl.spec.Lazycl;
import mutable.util.Rand;
import static mutable.util.Lg.*;

/** The purpose of this is not to replace java code or opencl code
(TODO its the exact same calculation with bit precision but slower) but is to derive
it so will know how to derive float/double math in wikibinator106 (a universal lambda
which has no int or float math built in, but has bitstrings of powOf2 sizes 1..2^120 built in,
so before can use float/double math there must be a lambda for each such math op,
that can run in interpreted mode very slowly and can be optimized with an Evaler.java instance
to call java strictfp float/double multiply andOr opencl1.2 whichever of the same ops it can optimize).
Java does these correctly when strictfp, and it appears (TODO test more) so does opencl1.2
(in gpus that have double ability which is optional in opencl1.2, but strict ability,
depending on a string param in compiler command (in java code in lazycl)
is required by opencl1.2 if they have it).

https://en.wikipedia.org/wiki/Single-precision_floating-point_format
says (about values other than nan, infinities, etc):
value = (-1)^sign * 2^(exponent-127) * (1+sum<i=1..23>(bit<23-i>*2^-i))

java.lang.Float says[

	A constant holding the positive infinity of type
	{@code float}. It is equal to the value returned by
	{@code Float.intBitsToFloat(0x7f800000)}.
	public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

	A constant holding the negative infinity of type
	{@code float}. It is equal to the value returned by
	{@code Float.intBitsToFloat(0xff800000)}.
	public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

	A constant holding a Not-a-Number (NaN) value of type
	{@code float}.  It is equivalent to the value returned by
	{@code Float.intBitsToFloat(0x7fc00000)}.
	public static final float NaN = 0.0f / 0.0f;

	A constant holding the largest positive finite value of type
	{@code float}, (2-2<sup>-23</sup>)&middot;2<sup>127</sup>.
	It is equal to the hexadecimal floating-point literal
	{@code 0x1.fffffeP+127f} and also equal to
	{@code Float.intBitsToFloat(0x7f7fffff)}.
	public static final float MAX_VALUE = 0x1.fffffeP+127f; // 3.4028235e+38f

	A constant holding the smallest positive normal value of type
	{@code float}, 2<sup>-126</sup>.  It is equal to the
	hexadecimal floating-point literal {@code 0x1.0p-126f} and also
	equal to {@code Float.intBitsToFloat(0x00800000)}.
	@since 1.6
	
	public static final float MIN_NORMAL = 0x1.0p-126f; // 1.17549435E-38f

	A constant holding the smallest positive nonzero value of type
	{@code float}, 2<sup>-149</sup>. It is equal to the
	hexadecimal floating-point literal {@code 0x0.000002P-126f}
	and also equal to {@code Float.intBitsToFloat(0x1)}.
	
	public static final float MIN_VALUE = 0x0.000002P-126f; // 1.4e-45f

	/**
	Maximum exponent a finite {@code float} variable may have.  It
	is equal to the value returned by {@code
	Math.getExponent(Float.MAX_VALUE)}.
	
	@since 1.6
	public static final int MAX_EXPONENT = 127;

	Minimum exponent a normalized {@code float} variable may have.
	It is equal to the value returned by {@code
	Math.getExponent(Float.MIN_NORMAL)}.
	@since 1.6
	public static final int MIN_EXPONENT = -126;
]

Possibly relevant links:

https://stackoverflow.com/questions/55551307/how-to-correctly-implement-multiply-for-floating-point-numbers-software-fp
https://en.wikipedia.org/wiki/Rounding#Tie-breaking
https://en.wikipedia.org/wiki/Floating-point_arithmetic
https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.4
https://github.com/bchavez14/Floating_Point_Add_Multiply/blob/master/src/fp.java

https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-6.html#jvms-6.5.fmul
"In the remaining cases, where neither an infinity nor NaN is involved, the product is computed
and rounded to the nearest representable value using IEEE 754 round to nearest mode. If the magnitude
is too large to represent as a float, we say the operation overflows; the result is then an infinity
of appropriate sign. If the magnitude is too small to represent as a float, we say the operation
underflows; the result is then a zero of appropriate sign.
The Java Virtual Machine requires support of gradual underflow as defined by IEEE 754.
Despite the fact that overflow, underflow, or loss of precision may occur, execution of an
fmul instruction never throws a run-time exception."

https://en.wikipedia.org/wiki/Arithmetic_underflow

https://docs.oracle.com/cd/E19957-01/816-2464/ncg_math.html
*/
public strictfp class IEEE754{
	
	//This is broken as of 2021-4-24, hadnt finished coding it.
	
	/** number of bits*/
	public static final byte sizeFExponent = 8;
	public static final byte sizeFFraction = 23;
	public static final byte sizeDExponent = 11;
	public static final byte sizeDFraction = 52;
	
	public static final int maskFSign =	      0b10000000000000000000000000000000;
	public static final int maskFExponent =   0b01111111100000000000000000000000;
	public static final int fExponentOfOne =  0b001111111;
	public static final int maskFFraction =   0b00000000011111111111111111111111;
	public static final long maskDSign =      0b1000000000000000000000000000000000000000000000000000000000000000L;
	public static final long maskDExponent =  0b0111111111110000000000000000000000000000000000000000000000000000L;
	public static final long dExponentOfOne_fixmeVerify = 0b0011111111110000000000000000000000000000000000000000000000000000L;
	public static final long maskDDigits =    0b0000000000001111111111111111111111111111111111111111111111111111L;
	
	public static final int maxFExponent = (1<<sizeFExponent)-1;
	
	public static boolean logDetail = false;
	
	
	
	public static float asFloat(int bits){
		return Float.intBitsToFloat(bits);
	}
	
	/** faster, whatever bits are in the float. often not normed. often the float was nondeterministicly generated. */
	public static int asIntRaw(float bits){
		return Float.floatToRawIntBits(bits);
	}
	
	/** this java kind of norm allows subnormals and has only 1 possible value for nan posInfin negInfin and 0 */
	public static int asInt(float bits){
		return Float.floatToIntBits(bits);
	}
	
	/** uses fmul(int,int) with the raw floats *
	public static float fmulRaw(float x, float y){
		return as
	}*/
	
	public static int fmul(int x, int y){
		return fmul(x, y, 0x7fc00000, 0x7f800000, true, true);
	}
	
	/** //ret = retSign|(((1<<sizeFFraction)^retFraction)>>>(1-retExponent));
	//java >>> op doesnt work when rotating ints by more than 31?
	//Integer.toBinaryString((-1)>>>32) returns "11111111111111111111111111111111"
	//Integer.toBinaryString((-1)>>>60) returns "1111".
	//Integer.toBinaryString((-1)>>>27) returns "11111".
	ret = retSign|downShiftUnsigned(((1<<sizeFFraction)^retFraction),(1-retExponent));
	*/
	public static int downShiftUnsigned(int shiftMe, int shiftAmount){
		return shiftAmount<31 ? shiftMe>>>shiftAmount : 0;
	}
	
	public static long downShiftUnsigned(long shiftMe, int shiftAmount){
		return shiftAmount<63 ? shiftMe>>>shiftAmount : 0;
	}
	
	/** multiply https://en.wikipedia.org/wiki/Single-precision_floating-point_format
	<br><br>
	FIXME handle subnormals. Are they allowed?
	FIXME use the same constants for normed NaN, positiveInfinity, and negativeInfinity java uses.
	FIXME negative zero.
	FIXME rounding.
	<br><br>
	0 00000000000 00000000000000000000000000000000000000000000000000002 ≙ 0000 0000 0000 000016 ≙ +0
	1 00000`000000 00000000000000000000000000000000000000000000000000002 ≙ 8000 0000 0000 000016 ≙ −0
	0 11111111111 00000000000000000000000000000000000000000000000000002 ≙ 7FF0 0000 0000 000016 ≙ +∞ (positive infinity)
	1 11111111111 00000000000000000000000000000000000000000000000000002 ≙ FFF0 0000 0000 000016 ≙ −∞ (negative infinity)
	0 11111111111 00000000000000000000000000000000000000000000000000012 ≙ 7FF0 0000 0000 000116 ≙ NaN (sNaN on most processors, such as x86 and ARM)
	0 11111111111 10000000000000000000000000000000000000000000000000012 ≙ 7FF8 0000 0000 000116 ≙ NaN (qNaN on most processors, such as x86 and ARM)
	0 11111111111 11111111111111111111111111111111111111111111111111112 ≙ 7FFF FFFF FFFF FFFF16 ≙ NaN (an alternative encoding of NaN)
	
	https://en.wikipedia.org/wiki/Single-precision_floating-point_format
	says (about values other than nan, infinities, etc):
	value = (-1)^sign * 2^(exponent-127) * (1+sum<i=1..23>(bit<23-i>*2^-i))
	
	This doesnt handle systems where theres multiple normed nan values (quiet and signaling kinds) depending whats multiplied,
	cuz you have to choose one as param here. 0f/0f==NaN. (-0f)/0f==NaN. 0f.(-0f)==NaN. (-0f)/(-0f)==NaN. If x or y is nan, returns nan.
	FIXME what about infinity-infinity etc?
	*/
	public static int fmul(int x, int y, int nan, int positiveInfinity, boolean allowSubnormalsOtherThanPositiveZero, boolean allowNegativeZero){
 		if(logDetail) lg("");
		if(logDetail) lg("");
		if(logDetail) lg("");
		if(logDetail) lg("fmul("+s(x)+","+s(y)+")");
		//lg("fmul("+s(x)+","+s(y)+") aka fmul("+asFloat(x)+","+asFloat(y)+")");
		if(logDetail) lg("x = "+s(x));
		if(logDetail) lg("y = "+s(y));
		int xExponent = (x&maskFExponent)>>>sizeFFraction; //FIXME is this off by 1 etc?
		if(logDetail) lg("xExponent = "+s(xExponent)+" "+(xExponent-fExponentOfOne)+"+fExponentOfOne");
		int yExponent = (y&maskFExponent)>>>sizeFFraction; //FIXME is this off by 1 etc?
		if(logDetail) lg("yExponent = "+s(yExponent)+" "+(yExponent-fExponentOfOne)+"+fExponentOfOne");
		
		int retSign = (x^y)&maskFSign; //FIXME negative zero, nans, infinities, subnormals
		if(logDetail) lg("retSign = "+s(retSign));
		
		int xFraction = x&maskFFraction;
		if(logDetail) lg("xFraction = "+s(xFraction));
		int yFraction = y&maskFFraction;
		if(logDetail) lg("yFraction = "+s(yFraction));
		
		boolean xIsNan = xExponent == maxFExponent && xFraction != 0;
		boolean yIsNan = yExponent == maxFExponent && yFraction != 0;
		if(xIsNan || yIsNan) return nan;
		
		 //exponent 0 is subnormal
		int xOneThenFraction = xExponent==0 ? (xFraction<<1) : (1<<sizeFFraction)|xFraction;
		if(logDetail) lg("xOneThenFraction = "+s(xOneThenFraction));
		int yOneThenFraction = yExponent==0 ? (yFraction<<1) : (1<<sizeFFraction)|yFraction;
		if(logDetail) lg("yOneThenFraction = "+s(yOneThenFraction));
		//FIXME this uses extra digits of precision. must round differently during it?
		//Whats the rounding rule? Round sizeFFraction times?
		long mulFraction = (long)xOneThenFraction*yOneThenFraction;
		//long mulFractionOriginal = mulFraction; //FIXME remove this debug line
		if(logDetail) lg("mulFraction = "+s(mulFraction));
		int mulLeadingZeros = Long.numberOfLeadingZeros(mulFraction);
		if(logDetail) lg("mulLeadingZeros = "+s(mulLeadingZeros)+" "+mulLeadingZeros);
		int targetMulLeadingZeros = 17; //FIXME derive it instead of a constant her
		
		//int correctLeadingZerosInInt = 32-(1+sizeFFraction); //as if its a 1 just above the fraction digits
		//FIXME I know its supposed to be 17, and this gives 17, but why is it +1 instead of -1 since it
		//takes away 2 sign bits and puts back 1 sign bit. That means if port this float code to doubles, it wont work???
		//int correctLeadingZerosInLong = correctLeadingZerosInInt*2+1;
		//lg("correctLeadingZerosInInt = "+s(correctLeadingZerosInInt));
		//lg("correctLeadingZerosInLong = "+s(correctLeadingZerosInLong));
		
		//FIXME addToExponent is either 0 or 1, cuz its multiplying 2 numbers in range 1 (inclusive) to 2 (exclusive),
		//unless either number is 0
		int addToExponent = targetMulLeadingZeros-mulLeadingZeros;
		if(logDetail) lg("addToExponent = "+s(addToExponent)+" "+addToExponent);
		
		//int digitsToDropFromLong = mulLeadingZeros-targetMulLeadingZeros+32;
		int digitsToDropFromLong = sizeFFraction+addToExponent;
		if(logDetail) lg("digitsToDropFromLong = "+s(digitsToDropFromLong)+" "+digitsToDropFromLong);
		
		
		//long addForRounding = (1L<<digitsToDropFromLong)-1; //round to higher magnitude
		long addForRounding = (1L<<(digitsToDropFromLong-1));
		if(logDetail) lg("addForRounding = "+s(addForRounding));
		//long addForRounding = 0;
		//int roundedOneThenFraction = (int)((mulFraction+addForRounding)>>>digitsToDropFromLong);
		int roundedOneThenFraction = (int)downShiftUnsigned((mulFraction+addForRounding),digitsToDropFromLong);
		
		/** addForRounding needs to happen after subnormal shifting *
		//FIXME dont duplicate these vars, one for if subnormal, one for not subnormal.
		long addForRounding_ifSubnormal = addForRounding<<1;
		if(logDetail) lg("addForRounding_ifSubnormal = "+s(addForRounding_ifSubnormal));
		int roundedOneThenFraction_ifSubnormal =
			(int)downShiftUnsigned((mulFraction+addForRounding_ifSubnormal),digitsToDropFromLong);
		if(logDetail) lg("roundedOneThenFraction_ifSubnormal = "+s(roundedOneThenFraction_ifSubnormal));
		int roundedFraction_ifSubnormal = roundedOneThenFraction_ifSubnormal^(1<<sizeFFraction);
		if(logDetail) lg("roundedFraction_ifSubnormal = "+s(roundedFraction_ifSubnormal));
		*/
		
		
		//FIXME what if addForRounding changes addToExponent to addToExponent+1 cuz it was 1.11111...111?
		//Or is that impossible cuz of 1.11111...111 * 1.11111...111 would have a 0 in its digits somewhere?
		if(logDetail) lg("roundedOneThenFraction = "+s(roundedOneThenFraction));
		//int addToExponent = digitsToDrop-sizeFFraction; //FIXME?
		
		if(logDetail) lg("addToExponent = "+s(addToExponent)+" "+addToExponent);
		//drop the 1 above the fraction. &maskFFraction would also work, but this explains it better.
		int roundedFraction = roundedOneThenFraction^(1<<sizeFFraction);
		if(logDetail) lg("roundedFraction = "+s(roundedFraction));
		int retFraction = roundedFraction;
		if(logDetail) lg("retFraction = "+s(retFraction));
		int retExponent = xExponent+yExponent-fExponentOfOne+addToExponent;
		if(logDetail){
			lg("retExponent = "+s(retExponent)+" "+(retExponent-fExponentOfOne)+"+fExponentOfOne");
			lg("correctExpo = "+s(StrictMath.getExponent(asFloat(x)*asFloat(y))+127));
		}
		
		//FIXME can it change from subnormal to not subnormal during adding addForRounding?
		//boolean isSubnormal = false; //FIXME. (also, positiveZero is a subnormal).
		//boolean isSubnormal = retExponent==0; //also includes +0 and -0
		boolean isSubnormal = retExponent<=0; //also includes +0 and -0
		
		
		if(logDetail) lg("maxExponent = "+s(maxFExponent));
		if(retExponent > maxFExponent) retExponent = maxFExponent;
		if(logDetail) lg("retExponent = "+s(retExponent)+" "+(retExponent-fExponentOfOne)+"+fExponentOfOne");
		//if(retExponent < 0){
			//TODO simplify this by computing exponent and shifting the fraction earlier? This way its done twice.
		//}
		boolean isInfiniteOrNaN = retExponent == maxFExponent;
		boolean isNaN = false; //FIXME
		boolean isInfinite = isInfiniteOrNaN & !isNaN;
		//FIXME use allowNegativeZero var
		boolean isZero = ((isSubnormal&!allowSubnormalsOtherThanPositiveZero) | ((retExponent|retFraction)==0));
		
		/*if(retExponent <= 0){ //subnormal. Increase exponent until its 0, and downshift onePlusFraction that much
			one
		}*/
		
		//TODO compute this without using long. use only int, so its faster in gpu.
		
		/*if((retExponent&0xff) != retExponent){
			throw new RuntimeException("retExponent has extra bits: "+s(retExponent));
		}*/
		if((retFraction&maskFFraction) != retFraction){
			throw new RuntimeException("retFraction has extra bits: "+s(retFraction));
		}
		
		/*if(xExponent+yExponent-fExponentOfOne-sizeFFraction+1 < 0){
			//TODO dont hardcode constants
			return allowNegativeZero ? 0 : retSign;
		}*/
		
		int ret;
		if(isZero) ret = 0;
		else if(isSubnormal){
			if(logDetail){
				lg("SUBNORMAL ADJUSTING... retExponent = "+s(retExponent)+" "+(retExponent-fExponentOfOne)+"+fExponentOfOne");
				lg("SUBNORMAL ADJUSTING... correctExpo = "+s(StrictMath.getExponent(asFloat(x)*asFloat(y))+127));
			}
			//ret = retSign|(((1<<sizeFFraction)^retFraction)>>>(1-retExponent));
			//java >>> op doesnt work when rotating ints by more than 31?
			//Integer.toBinaryString((-1)>>>32) returns "11111111111111111111111111111111"
			//Integer.toBinaryString((-1)>>>60) returns "1111".
			//Integer.toBinaryString((-1)>>>27) returns "11111".
			
			//FIXME dont duplicate these vars, one for if subnormal, one for not subnormal.
			lg("SUBNORMAL ADJUSTING... retFractWAS = "+s(retFraction));
			//retFraction = roundedFraction_ifSubnormal; //
			lg("SUBNORMAL ADJUSTING... retFraction = "+s(retFraction));
			
			ret = retSign|downShiftUnsigned(((1<<sizeFFraction)^retFraction),(1-retExponent));
			
			//retFraction >>= (1-retExponent);
			retExponent = 0;
			if(logDetail){
				lg("SUBNORMAL ADJUSTING... ret         = "+s(ret));
				lg("SUBNORMAL ADJUSTING... retExponent = "+s(retExponent)+" "+(retExponent-fExponentOfOne)+"+fExponentOfOne");
				lg("SUBNORMAL ADJUSTING... correctExpo = "+s(StrictMath.getExponent(asFloat(x)*asFloat(y))+127));
			}
		}else if(isNaN) ret = nan;
		else if(isInfinite) ret = retSign^positiveInfinity;
		else ret = retSign|(retExponent<<sizeFFraction)|retFraction;
		
		if((retExponent&0xff) != retExponent){
			throw new RuntimeException("retExponent has extra bits: "+s(retExponent));
		}
		if((retFraction&maskFFraction) != retFraction){
			throw new RuntimeException("retFraction has extra bits: "+s(retFraction));
		}
	
		int correctRet = asInt(asFloat(x)*asFloat(y)); //FIXME move this into logDetail block
		if(true || logDetail){ //FIXME remove true ||
			lg("isSubnormal = "+isSubnormal);
			lg("isZero = "+isZero);
			lg("isNaN = "+isNaN);
			lg("isInfinite = "+isInfinite);
			lg("ret        = "+s(ret));
			//lg("mulFracOrigLowBits = "+s(mulFractionOriginal).substring(14));
			lg("mulFractionLowBits = "+s(mulFraction).substring(digitsToDropFromLong-10));
			lg("correctRet = "+s(correctRet)+" "+(correctRet-ret));
		}
		
		//display this aligned way (of course this shows a bug)...
		//> ret        = 1_00111101_10100111101111011000001<-2.2432623E-20>
		//> correctRet = 1_00111101_10100111101111011000010<-2.2432624E-20>
		//> mulFractionLowBits = 00110100111101111011000001111001001011000101110010
		
		return ret;
	}
	
	/** multiply https://en.wikipedia.org/wiki/Double-precision_floating-point_format/ */
	public static long dmul(long x, long y){
		throw new RuntimeException("TODO");
	}
	
	/** 32 "1" and "0", with sign, with 3 sections of sign, exponent, and digits. */
	public static String s(int bits){
		String s = "0000000000000000000000000000000"+Integer.toUnsignedString(bits,2);
		//s = s.substring(s.length()-32)+"<"+asFloat(bits)+">";
		s = s.substring(s.length()-32)+"<"+Float.toHexString(asFloat(bits))+">";
		return s.substring(0,1)+"_"+s.substring(1,1+sizeFExponent)+"_"+s.substring(1+sizeFExponent);
	}
	
	/** 64 "1" and "0" */
	public static String s(long bits){
		String s = "000000000000000000000000000000000000000000000000000000000000000"+Long.toUnsignedString(bits,2);
		return s.substring(s.length()-64);
	}
	
	/*public static void testFmul_allPossibleFloatsMulBothDirectionsBy(float x){
		int i = 0;
		do{
			float f = asFloat(i);
			TODO
			i++;
		}while(i != 0);
	}*/
	
	public static float normFloat(float f){
		return Float.intBitsToFloat(Float.floatToIntBits(f));
	}
	
	public static void testFmul(boolean logEach, boolean logAtEnd, Random rand, long numTests){
		for(long x=0; x<numTests; x++){
			lg("x="+x);
			//int mask = 0b10111111111111111111111111111111; //drop high 1 bits of exponent
			int mask = ~0; //no change
			int i = rand.nextInt()&mask;
			int j = rand.nextInt()&mask;
			if(logEach){
				lg("");
				lg("fmul:");
				lg(s(i));
				lg(s(j));

			}
			float javafmuledF = normFloat(asFloat(i)*asFloat(j));
			/*if(javafmuledF != javafmuledF || Float.isInfinite(javafmuledF) || Math.abs(javafmuledF) < Float.MIN_NORMAL){
				lg("FIXME skipping test cuz javafmuledF="+javafmuledF+" and i dont have infinities, nans, zeros, and subnormals working yet");
				continue;
			}*
			if(Math.abs(javafmuledF) < Float.MIN_NORMAL){
				lg("FIXME skipping test cuz javafmuledF="+javafmuledF+" cuz i dont have subnormals working yet");
				continue;
			}*/
			int javafmuled = asIntRaw(javafmuledF);
			int fmuled = fmul(i,j);
			if(logEach || javafmuled != fmuled){
				lg(s(javafmuled)+" javafmul");
				lg(s(fmuled)+" fmul");
				lg(s(javafmuled^fmuled)+" diff .. "+(javafmuled-fmuled));
				
				if(!logDetail){
					for(int r=0; r<2; r++){
						lg("\n\n\nreplaying it with logDetail=true");
						logDetail = true;
						fmul(i,j);
						logDetail = false;
						lg("\n\n");
					}
				}
			}
			//if(false && javafmuled != fmuled){ //FIXME remove false &&
			if(javafmuled != fmuled){ //FIXME remove false &&
				throw new RuntimeException(
					"javafmuled and fmuled differ.\n"
					+s(javafmuled)+"=javafmuled\n"
					+s(fmuled)+"=fmuled\n"
					+s(javafmuled^fmuled)+"=diff .. "+Math.abs(javafmuled-fmuled)+"\n"
					+s(i)+"=i\n"
					+s(j)+"=j\n"
					+"after x="+x+" tests in same loop.");
			}else{
				//lg("Test pass x="+x);
			}
		}
		if(logAtEnd) lg("testFmul numTests="+numTests+" pass, rand="+rand);
		//FIXME throw if not match exact bits
	}
	
	static int javaFmul(int x, int y){
		return asInt(asFloat(x)*asFloat(y));
	}
	
	static void logJavafmul(int x, int y){
		lg("");
		lg(s(x)+" x");
		lg(s(y)+" y");
		lg(s(javaFmul(x,y))+" javaFmul");
	}
	
	static void logJavaAndGpuFmul(int x, int y){
		lg("");
		int j = javaFmul(x,y);
		int g = gpuFmul(x,y);
		lg("");
		lg(s(x)+" x");
		lg(s(y)+" y");
		if(j != g){
			lg(s(j)+" javaFmul");
			lg(s(g)+" gpuFmul");
			lg(s(j^g)+" diff");
		}else{
			lg(s(j)+" javaAndGpuFmul");
		}
	}
	
	public static Blob lazycl(Object... map){
		return mutable.util.Options.defaultLazycl().lazycl(map);
	}
	
	/** FIXME move this code out of wikibinator as it shouldnt directly depend on lazycl. that should be a plugin.
	This is a very slow way to use GPU, so dont call this in a loop. If you want that, redesign to use var size array.
	*/
	public static int gpuFmul(int x, int y){
		return lazycl(
			"Code", "opencl1.2:(global int* out, const int x, const int y){ out[get_global_id(0)] = as_int(as_float(x)*as_float(y)); }",
			"GlobalSize", 1,
			"Bize", 32L,
			"x", x,
			"y", y
		).i(0);
	}
	
	
	public static void main(String[] args){
		
		int one = asInt(1f);
		int half = asInt(.5f);
		int two = asInt(2f);
		int three = asInt(3f);
		int four = asInt(4f);
		int five = asInt(5f);
		int eight = asInt(8f);
		//logJavafmul((1<<23)|1, (1<<23)|1);
		//logJavafmul((1<<22), (1<<22));
		//logJavafmul(one, (1<<22));
		//logJavafmul(two, (1<<22));
		//logJavafmul(eight, (1<<22));
		//logJavafmul(five, (1<<22));
		//logJavafmul(five, 1);
		//logJavafmul(asInt(9f), 7);
		//logJavaAndGpuFmul(asInt(9f), 7);
		//logJavaAndGpuFmul(asInt(4f+1f/4), 4);
		//logJavaAndGpuFmul(4, asInt(4f+1f/4));
		//logJavaAndGpuFmul(asInt(4f+1f/4), 16*3);
		//logJavaAndGpuFmul(asInt(4f+1f/4), 8*3);
		//logJavaAndGpuFmul(asInt(4f+1f/4), 4*3);
		//logJavaAndGpuFmul(asInt(4f+1f/4), 2*3);
		//logJavaAndGpuFmul(asInt(4f+1f/4), 3);
		
		
		logJavafmul(asInt(4f+1f/4), 4*3);
		logJavafmul(asInt(4f+1f/4), 2*3);
		
		/*
		logDetail = true;
		
		int one = 0b00111111100000000000000000000000;
		int oo = fmul(one,one);
		lg("one = "+s(one));
		lg("fmul("+s(one)+" aka "+asFloat(one)+",same)="+s(one)+" aka "+asFloat(one));
		
		lg("");
		lg("");
		lg("-----a");
		lg("");
		lg("");
		
		int x = 0b00111111100000000000000000000001;
		float xAsFloat = asFloat(x);
		float xAsFloatSquared = xAsFloat*xAsFloat;
		int xAsFloatSquaredAsInt = asInt(xAsFloatSquared);
		lg("x = "+s(x));
		lg("xAsFloat = "+xAsFloat);
		lg("xAsFloatSquared = "+xAsFloatSquared);
		lg("xAsFloatSquaredAsInt = "+s(xAsFloatSquaredAsInt));
		int ff = fmul(x,x);
		lg("fmul("+s(x)+" aka "+asFloat(x)+",same)="+s(ff)+" aka "+asFloat(ff));
		
		lg("");
		lg("");
		lg("-----b");
		lg("");
		lg("");
		
		logDetail = false;
		
		int i=0b00111111100000000000000000000000;
		for(int j=0; j<100; j++){
			lg(s(i)+" "+asFloat(i));
			i++;
		}*/
		
		testFmul(true,true,Rand.strongRand, 100);
		
		testFmul(false,true,Rand.strongRand, 10000);
		
		testFmul(false,true,Rand.strongRand, 1000000);
		
	}

}


/** 2021-4-23 getting closer...

> fmul:
> 00000011100101011100101011000000<8.803982E-37>
> 01101011101110101011000101101101<4.5139614E26>
> 00101111110110100111101001000001<3.9740836E-10> javafmul
> 00101111110110100111101001000000<3.9740833E-10> fmul
> 00000000000000000000000000000001<1.4E-45> diff
Exception in thread "main" java.lang.RuntimeException: javafmuled and fmuled differ.
00101111110110100111101001000001<3.9740836E-10>=javafmuled
00101111110110100111101001000000<3.9740833E-10>=fmuled
00000000000000000000000000000001<1.4E-45>=diff
00000011100101011100101011000000<8.803982E-37>=i
01101011101110101011000101101101<4.5139614E26>=j
after x=2 tests in same loop.
	at immutable.wikibinator106.impls.marklar106.IEEE754.testFmul(IEEE754.java:268)
	at immutable.wikibinator106.impls.marklar106.IEEE754.main(IEEE754.java:318)
	
	
> fmul:
> 10010001110011111010001010101011<-3.2759093E-28>
> 01010010011101011111111011100100<2.64135836E11>
> 10100100110001111000010101101010<-8.6528505E-17> javafmul
> 10100011100011110000101011010011<-1.5508675E-17> fmul
> 00000111010010001000111110111001<1.5088564E-34> diff
Exception in thread "main" java.lang.RuntimeException: javafmuled and fmuled differ.
10100100110001111000010101101010<-8.6528505E-17>=javafmuled
10100011100011110000101011010011<-1.5508675E-17>=fmuled
00000111010010001000111110111001<1.5088564E-34>=diff
10010001110011111010001010101011<-3.2759093E-28>=i
01010010011101011111111011100100<2.64135836E11>=j
after x=3 tests in same loop.
	at immutable.wikibinator106.impls.marklar106.IEEE754.testFmul(IEEE754.java:268)
	at immutable.wikibinator106.impls.marklar106.IEEE754.main(IEEE754.java:318)


> 
> fmul:
> 00100111001001111011100100111110<2.3276326E-15>
> 01001110100000101000100000010001<1.09497766E9>
> 00110110001010110000101001101110<2.5487057E-6> javafmul
> 00110110001010110000101001101110<2.5487057E-6> fmul
> 00000000000000000000000000000000<0.0> diff
> 
> fmul:
> 11100111010010110001001011111000<-9.589903E23>
> 11110001101110000010101010100001<-1.8238969E30>
> FIXME skipping test cuz javafmuledF=Infinity and i dont have infinities, nans, zeros, and subnormals working yet



> fmul(1_11001100_01111010110011111101011<-2.2361105E23>,1_01011000_00001100111011001111011<-1.9108315E-12>)
> xExponent = 0_00000000_00000000000000011001100<2.86E-43>
> yExponent = 0_00000000_00000000000000001011000<1.23E-43>
> xFraction = 0_00000000_01111010110011111101011<5.639244E-39>
> yFraction = 0_00000000_00001100111011001111011<5.93516E-40>
> xOneThenFraction = 0_00000001_01111010110011111101011<1.7394188E-38>
> yOneThenFraction = 0_00000001_00001100111011001111011<1.2348459E-38>
> mulFraction = 0000000000000000011000110111110000001101111010010011111111101001
> mulLeadingZeros = 0_00000000_00000000000000000010001<2.4E-44>
> digitsToDropFromLong = 0_00000000_00000000000000000010111<3.2E-44>
> roundedOneThenFraction = 0_00000001_10001101111100000011011<1.8272432E-38>
> addToExponent = 0_00000000_00000000000000000000000<0.0>
> roundedFraction = 0_00000000_10001101111100000011011<6.517488E-39>
> retFraction = 0_00000000_10001101111100000011011<6.517488E-39>
> retExponent = 0_00000000_00000000000000010100101<2.31E-43>
> correctExpo = 0_00000000_00000000000000010100101<2.31E-43>
> maxExponent = 0_00000000_00000000000000011111111<3.57E-43>
> retExponent = 0_00000000_00000000000000010100101<2.31E-43>
> retSign = 0_00000000_00000000000000000000000<0.0>
> isZero = false
> isNaN = false
> isInfinite = false
> ret        = 0_10100101_10001101111100000011011<4.27283022E11>
> mulFractionLowBits = 00011000110111110000001101111010010011111111101001
> correctRet = 0_10100101_10001101111100000011100<4.27283055E11> 1
> 

0_00101110_00110100101001000111001<4.9863863E-25>=javafmuled
0_00101110_00110100101001000111000<4.986386E-25>=fmuled
000000000010011010010100100011100010000101100001111101001

1_01000111_01111000101001100111000<-2.0418245E-17>=javafmuled
1_01000111_01111000101001100110111<-2.0418244E-17>=fmuled
000000000010111100010100110011011111000101011000110101110

> ret        = 1_10110010_01110110001010001110110<-3.29114486E15>
> correctRet = 1_10110010_01110110001010001110101<-3.29114459E15>
> mulFractionLowBits = 00101110110001010001110101010100001000101100000001






> fmul(1_00101100_11001011000101000011100<-1.8542052E-25>,0_10111011_11001000110010001110110<2.05717609E18>)
> xExponent = 0_00000000_00000000000000000101100<6.2E-44>
> yExponent = 0_00000000_00000000000000010111011<2.62E-43>
> xFraction = 0_00000000_11001011000101000011100<9.32493E-39>
> yFraction = 0_00000000_11001000110010001110110<9.219588E-39>
> xOneThenFraction = 0_00000001_11001011000101000011100<2.1079873E-38>
> yOneThenFraction = 0_00000001_11001000110010001110110<2.0974532E-38>
> mulFraction = 0000000000000000110011001100100100010100101100001001100011101000
> mulLeadingZeros = 0_00000000_00000000000000000010000<2.24E-44>
> digitsToDropFromLong = 0_00000000_00000000000000000011000<3.4E-44>
> roundedOneThenFraction = 0_00000001_10011001100100100010101<1.8806576E-38>
> addToExponent = 0_00000000_00000000000000000000001<1.4E-45>
> roundedFraction = 0_00000000_10011001100100100010101<7.051632E-39>
> retFraction = 0_00000000_10011001100100100010101<7.051632E-39>
> retExponent = 0_00000000_00000000000000001101001<1.47E-43>
> correctExpo = 0_00000000_00000000000000001101001<1.47E-43>
> maxExponent = 0_00000000_00000000000000011111111<3.57E-43>
> retExponent = 0_00000000_00000000000000001101001<1.47E-43>
> retSign = 1_00000000_00000000000000000000000<-0.0>
> isZero = false
> isNaN = false
> isInfinite = false
> ret        = 1_01101001_10011001100100100010101<-3.8144267E-7>
> mulFractionLowBits = 00110011001100100100010100101100001001100011101000
> correctRet = 1_01101001_10011001100100100010101<-3.8144267E-7> 0
> 
> 
> fmul:
> 0_00111110_01100110010100110111101<3.793925E-20>
> 0_10110100_01000111000011001011100<1.15070377E16>
> isZero = false
> isNaN = false
> isInfinite = false
> ret        = 0_01110011_11001001110001100111000<4.365684E-4>
> mulFractionLowBits = 00011100100111000110011011100100101110110111101100
> correctRet = 0_01110011_11001001110001100110111<4.3656837E-4> -1




> ret        = 0_11010010_01111011001110000010011<1.4326501E25>
> mulFractionLowBits = 0010111101100111000001001001111001110100111101110
> correctRet = 0_11010010_01111011001110000010010<1.43265E25> -1

*/

