package data.lib.fdlibm53; //TODO what package? careful about classpath exception.

import immutable.util.Pair;

/** 2021-3-4 dropping support for java.lang.StrictMath.exp(double) cuz could not make it match exactly
but does appear to always match within 1 ulp (lowest digit, with carrying),
so the plan is to use this cpu port (Fdlibm53Exp.java) and gpu port (Fdlibm53Exp.langColonCode) which seem to always match exactly,
either that or some faster less precise way of doing exp(float)->float cuz what I really want is determinism, not more precision.
<br><br>
and2021-2-23+ Ben F Rayfield is copying openjdk11 java.lang.FdLibm.exp(double),
which is [GNU GPL2 + classpath exception] licensed, as in this data/lib dir,
to here to reproduce it in a single function before porting it to opencl1.2.
TODO create Fdlibm53Exp.cl in data/lib and load it from opencl as an ndrange kernel
that is a function of double[] to double[] of same size, doing exp(double)->double on each,
and match it to what java.lang.Math.exp(double) does bit for bit,
so there will be 2 cpu implementations and 1 gpu implementation, which all match bit for bit.
*/
public strictfp class Fdlibm53Exp{
	
	/** ??? deterministic, matches between cpu and gpu, and matches normSubnormals(setLowBitTo0(java.lang.StrictMath.exp(double))). TODO verify. */
	public static double exp(double x){
		return expUsingRawDoubleLongTransform(x);
		//return setLowBitTo0(expUsingRawDoubleLongTransform(x));
		//return expUsingNormedDoubleLongTransform(x);
	}
	
	/** This is an experiment to get cpu and gpu to compute exp the exact same way, sacrificing a bit of precision */
	protected static double setLowBitTo0(double d){
		return Double.longBitsToDouble(Double.doubleToLongBits(d)&(-2L));
	}
	
	/** the long should match the long in Fdlibm53Exp_withExtraOutputForDebug.langColonCode */
	protected static Pair<Double,Long> expUsingNormedDoubleLongTransform_withExtraOutputForDebug(double x){
	
		long debug = 0L;
		
		debug += 3L;
		
		final double one	 = 1.0;
		//final double[] half = {0.5, -0.5,};
		final double half_0 = 0.5;
		//final double half_1 = -0.5;
		
		
		/** stdout, with the *p way of writing them such as "final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02".
		huge=1.0E300
		twom1000=9.332636185032189E-302
		o_threshold=709.782712893384
		u_threshold=-745.1332191019411
		ln2HI_0=0.6931471803691238
		ln2LO_0=1.9082149292705877E-10
		invln2=1.4426950408889634
		P1=0.16666666666666602
		P2=-0.0027777777777015593
		P3=6.613756321437934E-5
		P4=-1.6533902205465252E-6
		P5=4.1381367970572385E-8
		*/
		
		final double huge = 1.0E300;
		final double twom1000 = 9.332636185032189E-302;
		final double o_threshold = 709.782712893384;
		final double u_threshold = -745.1332191019411;
		final double ln2HI_0 = 0.6931471803691238;
		final double ln2LO_0 = 1.9082149292705877E-10;
		final double invln2 = 1.4426950408889634;
		final double P1 = 0.16666666666666602;
		final double P2 = -0.0027777777777015593;
		final double P3 = 6.613756321437934E-5;
		final double P4 = -1.6533902205465252E-6;
		final double P5 = 4.1381367970572385E-8;
		
		
		
		/*final double huge	= 1.0e+300;
		System.out.println("huge="+huge);
		
		final double twom1000=	 0x1.0p-1000;			 //  9.33263618503218878990e-302 = 2^-1000
		System.out.println("twom1000="+twom1000);
		
		final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02
		System.out.println("o_threshold="+o_threshold);
		final double u_threshold= -0x1.74910d52d3051p9;	 // -7.45133219101941108420e+02;
		System.out.println("u_threshold="+u_threshold);
		//final double[] ln2HI   ={  0x1.62e42feep-1,		 //  6.93147180369123816490e-01
		//										 -0x1.62e42feep-1};		// -6.93147180369123816490e-01
		final double ln2HI_0 = 0x1.62e42feep-1;		 //  6.93147180369123816490e-01
		System.out.println("ln2HI_0="+ln2HI_0);
		//final double ln2HI_1 = -0x1.62e42feep-1;
		//final double[] ln2LO   ={  0x1.a39ef35793c76p-33,   //  1.90821492927058770002e-10
		//										 -0x1.a39ef35793c76p-33};  // -1.90821492927058770002e-10
		final double ln2LO_0 = 0x1.a39ef35793c76p-33;   //  1.90821492927058770002e-10
		System.out.println("ln2LO_0="+ln2LO_0);
		//final double ln2LO_1 = -0x1.a39ef35793c76p-33;
		
		final double invln2 =	  0x1.71547652b82fep0;	 //  1.44269504088896338700e+00
		System.out.println("invln2="+invln2);

		final double P1   =  0x1.555555555553ep-3;  //  1.66666666666666019037e-01
		System.out.println("P1="+P1);
		final double P2   = -0x1.6c16c16bebd93p-9;  // -2.77777777770155933842e-03
		System.out.println("P2="+P2);
		final double P3   =  0x1.1566aaf25de2cp-14; //  6.61375632143793436117e-05
		System.out.println("P3="+P3);
		final double P4   = -0x1.bbd41c5d26bf1p-20; // -1.65339022054652515390e-06
		System.out.println("P4="+P4);
		final double P5   =  0x1.6376972bea4d0p-25; //  4.13813679705723846039e-08
		System.out.println("P5="+P5);
		*/
		
		//double y;
		double y = 0;
		double hi = 0.0;
		double lo = 0.0;
		double c = 0;
		double t = 0;
		int k = 0;
		int xsb = 0;
		/*unsigned*/ int hx = 0;

		//hx  = __HI(x);  /* high word of x */
		hx = (int)(as_normed_long(x)>>32);
		xsb = (hx >> 31) & 1;			   /* sign bit of x */
		hx &= 0x7fffffff;			   /* high word of |x| */
		
		debug = debug*30 + hx;
		
		double ret = 0;
		boolean returned = false;

		/* filter out non-finite argument */
		if (hx >= 0x40862E42) {				  /* if |x| >= 709.78... */
			if (hx >= 0x7ff00000) {
				int loX = (int)as_normed_long(x);
				debug = debug*30 + loX;
				if(((hx & 0xfffff) | loX) != 0){
					ret = x + x;				/* NaN */
				}else{
					ret = (xsb == 0) ? x : 0.0;	/* exp(+-inf) = {inf, 0} */
				}
				debug = debug*30 + as_normed_long(ret);
			}else if (x > o_threshold){
				ret = huge * huge; /* overflow */
				debug = debug*30 + as_normed_long(ret);
			}else if (x < u_threshold){ // unsigned compare needed here?
				ret = twom1000 * twom1000; /* underflow */
				debug = debug*30 + as_normed_long(ret);
			}
			returned = true;
		}
		
		debug = debug*30 + as_long(ret);
		debug = debug*30 + hx;
		debug = debug*30 + xsb;

		if(!returned){
			/* argument reduction */
			if (hx > 0x3fd62e42) {		   /* if  |x| > 0.5 ln2 */
				if(hx < 0x3FF0A2B2) {	   /* and |x| < 1.5 ln2 */
					//hi = x - ln2HI[xsb];
					hi = x - (xsb==0 ? ln2HI_0 : -ln2HI_0);
					lo=(xsb==0 ? ln2LO_0 : -ln2LO_0);
					k = 1 - xsb - xsb;
				} else {
					//k  = (int)(invln2 * x + half[xsb]);
					k  = (int)(invln2 * x + (xsb==0 ? half_0 : -half_0));
					t  = k;
					//hi = x - t*ln2HI[0];	/* t*ln2HI is exact here */
					hi = x - t*ln2HI_0;	/* t*ln2HI is exact here */
					lo = t*ln2LO_0;
				}
				x  = hi - lo;
			} else if (hx < 0x3e300000)  {	 /* when |x|<2**-28 */
				if (huge + x > one){
					ret = one + x; /* trigger inexact */
					returned = true;
				}
			} else {
				k = 0;
			}
			
			debug = debug*30 + as_long(ret);
			debug = debug*30 + hx;
			debug = debug*30 + xsb;
			debug = debug*30 + as_long(hi);
			debug = debug*30 + as_long(lo);
			debug = debug*30 + k;
			debug = debug*30 + as_long(t);
			debug = debug*30 + as_long(x);
	
			/* x is now in primary range */
			t  = x * x;
			
			debug = debug*30 + as_long(t);
			
			//deterministic roundoff, same in cpu and gpu,
			//instead of "c  = x - t*(P1 + t*(P2 + t*(P3 + t*(P4 + t*P5))))" differing between cpu and gpu.
			c = t*P5;
			c = P4+c;
			c = t*c;
			c = P3+c;
			c = t*c;
			c = P2+c;
			c = t*c;
			c = P1+c;
			c = t*c;
			c = x-c;
			
			debug = debug*30 + as_long(c);
			
			if (k == 0){
				ret = one - ((x*c)/(c - 2.0) - x);
				returned = true;
			}else{
				y = one - ((lo - (x*c)/(2.0 - c)) - hi);
			}
			
			//debug = debug*30 + as_long(ret);
			//debug = debug*30 + as_long(y);
	
			if(!returned){
				int hiY = (int)(as_normed_long(y)>>32);
				if(k >= -1021) {
					//y = __HI(y, __HI(y) + (k << 20)); /* add k to y's exponent */
					//y = __HI(y, hiY + (k << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_normed_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_normed_long(y)&0xffffffffL)|(((long)(hiY + (k << 20))))<<32); /* add k to y's exponent */
					ret = y;
				} else {
					//y = __HI(y, hiY + ((k + 1000) << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_normed_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_normed_long(y)&0xffffffffL)|(((long)((k + 1000) << 20)))<<32); /* add k to y's exponent */
					ret = y * twom1000;
				}
			}
		}
		
		debug = debug*30 + as_normed_long(ret);
		
		//return ret;
		return new Pair(ret,debug);
	}
	
	protected static double expUsingNormedDoubleLongTransform(double x){
	
		
		final double one	 = 1.0;
		//final double[] half = {0.5, -0.5,};
		final double half_0 = 0.5;
		//final double half_1 = -0.5;
		
		
		/** stdout, with the *p way of writing them such as "final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02".
		huge=1.0E300
		twom1000=9.332636185032189E-302
		o_threshold=709.782712893384
		u_threshold=-745.1332191019411
		ln2HI_0=0.6931471803691238
		ln2LO_0=1.9082149292705877E-10
		invln2=1.4426950408889634
		P1=0.16666666666666602
		P2=-0.0027777777777015593
		P3=6.613756321437934E-5
		P4=-1.6533902205465252E-6
		P5=4.1381367970572385E-8
		*/
		
		final double huge = 1.0E300;
		final double twom1000 = 9.332636185032189E-302;
		final double o_threshold = 709.782712893384;
		final double u_threshold = -745.1332191019411;
		final double ln2HI_0 = 0.6931471803691238;
		final double ln2LO_0 = 1.9082149292705877E-10;
		final double invln2 = 1.4426950408889634;
		final double P1 = 0.16666666666666602;
		final double P2 = -0.0027777777777015593;
		final double P3 = 6.613756321437934E-5;
		final double P4 = -1.6533902205465252E-6;
		final double P5 = 4.1381367970572385E-8;
		
		
		
		/*final double huge	= 1.0e+300;
		System.out.println("huge="+huge);
		
		final double twom1000=	 0x1.0p-1000;			 //  9.33263618503218878990e-302 = 2^-1000
		System.out.println("twom1000="+twom1000);
		
		final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02
		System.out.println("o_threshold="+o_threshold);
		final double u_threshold= -0x1.74910d52d3051p9;	 // -7.45133219101941108420e+02;
		System.out.println("u_threshold="+u_threshold);
		//final double[] ln2HI   ={  0x1.62e42feep-1,		 //  6.93147180369123816490e-01
		//										 -0x1.62e42feep-1};		// -6.93147180369123816490e-01
		final double ln2HI_0 = 0x1.62e42feep-1;		 //  6.93147180369123816490e-01
		System.out.println("ln2HI_0="+ln2HI_0);
		//final double ln2HI_1 = -0x1.62e42feep-1;
		//final double[] ln2LO   ={  0x1.a39ef35793c76p-33,   //  1.90821492927058770002e-10
		//										 -0x1.a39ef35793c76p-33};  // -1.90821492927058770002e-10
		final double ln2LO_0 = 0x1.a39ef35793c76p-33;   //  1.90821492927058770002e-10
		System.out.println("ln2LO_0="+ln2LO_0);
		//final double ln2LO_1 = -0x1.a39ef35793c76p-33;
		
		final double invln2 =	  0x1.71547652b82fep0;	 //  1.44269504088896338700e+00
		System.out.println("invln2="+invln2);

		final double P1   =  0x1.555555555553ep-3;  //  1.66666666666666019037e-01
		System.out.println("P1="+P1);
		final double P2   = -0x1.6c16c16bebd93p-9;  // -2.77777777770155933842e-03
		System.out.println("P2="+P2);
		final double P3   =  0x1.1566aaf25de2cp-14; //  6.61375632143793436117e-05
		System.out.println("P3="+P3);
		final double P4   = -0x1.bbd41c5d26bf1p-20; // -1.65339022054652515390e-06
		System.out.println("P4="+P4);
		final double P5   =  0x1.6376972bea4d0p-25; //  4.13813679705723846039e-08
		System.out.println("P5="+P5);
		*/
		
		//double y;
		double y = 0;
		double hi = 0.0;
		double lo = 0.0;
		double c;
		double t;
		int k = 0;
		int xsb;
		/*unsigned*/ int hx;

		//hx  = __HI(x);  /* high word of x */
		hx = (int)(as_normed_long(x)>>32);
		xsb = (hx >> 31) & 1;			   /* sign bit of x */
		hx &= 0x7fffffff;			   /* high word of |x| */
		
		double ret = 0;
		boolean returned = false;

		/* filter out non-finite argument */
		if (hx >= 0x40862E42) {				  /* if |x| >= 709.78... */
			if (hx >= 0x7ff00000) {
				int loX = (int)as_normed_long(x);
				if (((hx & 0xfffff) | loX) != 0)
					ret = x + x;				/* NaN */
				else
					ret = (xsb == 0) ? x : 0.0;	/* exp(+-inf) = {inf, 0} */
			}else if (x > o_threshold){
				ret = huge * huge; /* overflow */
			}else if (x < u_threshold){ // unsigned compare needed here?
				ret = twom1000 * twom1000; /* underflow */
			}
			returned = true;
		}

		if(!returned){
			/* argument reduction */
			if (hx > 0x3fd62e42) {		   /* if  |x| > 0.5 ln2 */
				if(hx < 0x3FF0A2B2) {	   /* and |x| < 1.5 ln2 */
					//hi = x - ln2HI[xsb];
					hi = x - (xsb==0 ? ln2HI_0 : -ln2HI_0);
					lo=(xsb==0 ? ln2LO_0 : -ln2LO_0);
					k = 1 - xsb - xsb;
				} else {
					//k  = (int)(invln2 * x + half[xsb]);
					k  = (int)(invln2 * x + (xsb==0 ? half_0 : -half_0));
					t  = k;
					//hi = x - t*ln2HI[0];	/* t*ln2HI is exact here */
					hi = x - t*ln2HI_0;	/* t*ln2HI is exact here */
					lo = t*ln2LO_0;
				}
				x  = hi - lo;
			} else if (hx < 0x3e300000)  {	 /* when |x|<2**-28 */
				if (huge + x > one){
					ret = one + x; /* trigger inexact */
					returned = true;
				}
			} else {
				k = 0;
			}
	
			/* x is now in primary range */
			t  = x * x;
			
			//deterministic roundoff, same in cpu and gpu,
			//instead of "c  = x - t*(P1 + t*(P2 + t*(P3 + t*(P4 + t*P5))))" differing between cpu and gpu.
			c = t*P5;
			c = P4+c;
			c = t*c;
			c = P3+c;
			c = t*c;
			c = P2+c;
			c = t*c;
			c = P1+c;
			c = t*c;
			c = x-c;
			
			if (k == 0){
				ret = one - ((x*c)/(c - 2.0) - x);
				returned = true;
			}else{
				y = one - ((lo - (x*c)/(2.0 - c)) - hi);
			}
	
			if(!returned){
				int hiY = (int)(as_normed_long(y)>>32);
				if(k >= -1021) {
					//y = __HI(y, __HI(y) + (k << 20)); /* add k to y's exponent */
					//y = __HI(y, hiY + (k << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_normed_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_normed_long(y)&0xffffffffL)|(((long)(hiY + (k << 20))))<<32); /* add k to y's exponent */
					ret = y;
				} else {
					//y = __HI(y, hiY + ((k + 1000) << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_normed_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_normed_long(y)&0xffffffffL)|(((long)((k + 1000) << 20)))<<32); /* add k to y's exponent */
					ret = y * twom1000;
				}
			}
		}
		
		return ret;
		
	}
	
	protected static double expUsingRawDoubleLongTransform(double x){
	
		
		final double one	 = 1.0;
		//final double[] half = {0.5, -0.5,};
		final double half_0 = 0.5;
		//final double half_1 = -0.5;
		
		
		/** stdout, with the *p way of writing them such as "final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02".
		huge=1.0E300
		twom1000=9.332636185032189E-302
		o_threshold=709.782712893384
		u_threshold=-745.1332191019411
		ln2HI_0=0.6931471803691238
		ln2LO_0=1.9082149292705877E-10
		invln2=1.4426950408889634
		P1=0.16666666666666602
		P2=-0.0027777777777015593
		P3=6.613756321437934E-5
		P4=-1.6533902205465252E-6
		P5=4.1381367970572385E-8
		*/
		
		final double huge = 1.0E300;
		final double twom1000 = 9.332636185032189E-302;
		final double o_threshold = 709.782712893384;
		final double u_threshold = -745.1332191019411;
		final double ln2HI_0 = 0.6931471803691238;
		final double ln2LO_0 = 1.9082149292705877E-10;
		final double invln2 = 1.4426950408889634;
		final double P1 = 0.16666666666666602;
		final double P2 = -0.0027777777777015593;
		final double P3 = 6.613756321437934E-5;
		final double P4 = -1.6533902205465252E-6;
		final double P5 = 4.1381367970572385E-8;
		
		
		
		/*final double huge	= 1.0e+300;
		System.out.println("huge="+huge);
		
		final double twom1000=	 0x1.0p-1000;			 //  9.33263618503218878990e-302 = 2^-1000
		System.out.println("twom1000="+twom1000);
		
		final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02
		System.out.println("o_threshold="+o_threshold);
		final double u_threshold= -0x1.74910d52d3051p9;	 // -7.45133219101941108420e+02;
		System.out.println("u_threshold="+u_threshold);
		//final double[] ln2HI   ={  0x1.62e42feep-1,		 //  6.93147180369123816490e-01
		//										 -0x1.62e42feep-1};		// -6.93147180369123816490e-01
		final double ln2HI_0 = 0x1.62e42feep-1;		 //  6.93147180369123816490e-01
		System.out.println("ln2HI_0="+ln2HI_0);
		//final double ln2HI_1 = -0x1.62e42feep-1;
		//final double[] ln2LO   ={  0x1.a39ef35793c76p-33,   //  1.90821492927058770002e-10
		//										 -0x1.a39ef35793c76p-33};  // -1.90821492927058770002e-10
		final double ln2LO_0 = 0x1.a39ef35793c76p-33;   //  1.90821492927058770002e-10
		System.out.println("ln2LO_0="+ln2LO_0);
		//final double ln2LO_1 = -0x1.a39ef35793c76p-33;
		
		final double invln2 =	  0x1.71547652b82fep0;	 //  1.44269504088896338700e+00
		System.out.println("invln2="+invln2);

		final double P1   =  0x1.555555555553ep-3;  //  1.66666666666666019037e-01
		System.out.println("P1="+P1);
		final double P2   = -0x1.6c16c16bebd93p-9;  // -2.77777777770155933842e-03
		System.out.println("P2="+P2);
		final double P3   =  0x1.1566aaf25de2cp-14; //  6.61375632143793436117e-05
		System.out.println("P3="+P3);
		final double P4   = -0x1.bbd41c5d26bf1p-20; // -1.65339022054652515390e-06
		System.out.println("P4="+P4);
		final double P5   =  0x1.6376972bea4d0p-25; //  4.13813679705723846039e-08
		System.out.println("P5="+P5);
		*/
		
		//double y;
		double y = 0;
		double hi = 0.0;
		double lo = 0.0;
		double c;
		double t;
		int k = 0;
		int xsb;
		/*unsigned*/ int hx;

		//hx  = __HI(x);  /* high word of x */
		hx = (int)(as_raw_long(x)>>32);
		xsb = (hx >> 31) & 1;			   /* sign bit of x */
		hx &= 0x7fffffff;			   /* high word of |x| */
		
		double ret = 0;
		boolean returned = false;

		/* filter out non-finite argument */
		if (hx >= 0x40862E42) {				  /* if |x| >= 709.78... */
			if (hx >= 0x7ff00000) {
				int loX = (int)as_raw_long(x);
				if (((hx & 0xfffff) | loX) != 0)
					ret = x + x;				/* NaN */
				else
					ret = (xsb == 0) ? x : 0.0;	/* exp(+-inf) = {inf, 0} */
			}else if (x > o_threshold){
				ret = huge * huge; /* overflow */
			}else if (x < u_threshold){ // unsigned compare needed here?
				ret = twom1000 * twom1000; /* underflow */
			}
			returned = true;
		}

		if(!returned){
			/* argument reduction */
			if (hx > 0x3fd62e42) {		   /* if  |x| > 0.5 ln2 */
				if(hx < 0x3FF0A2B2) {	   /* and |x| < 1.5 ln2 */
					//hi = x - ln2HI[xsb];
					hi = x - (xsb==0 ? ln2HI_0 : -ln2HI_0);
					lo=(xsb==0 ? ln2LO_0 : -ln2LO_0);
					k = 1 - xsb - xsb;
				} else {
					//k  = (int)(invln2 * x + half[xsb]);
					k  = (int)(invln2 * x + (xsb==0 ? half_0 : -half_0));
					t  = k;
					//hi = x - t*ln2HI[0];	/* t*ln2HI is exact here */
					hi = x - t*ln2HI_0;	/* t*ln2HI is exact here */
					lo = t*ln2LO_0;
				}
				x  = hi - lo;
			} else if (hx < 0x3e300000)  {	 /* when |x|<2**-28 */
				if (huge + x > one){
					ret = one + x; /* trigger inexact */
					returned = true;
				}
			} else {
				k = 0;
			}
	
			/* x is now in primary range */
			t  = x * x;
			
			//deterministic roundoff, same in cpu and gpu,
			//instead of "c  = x - t*(P1 + t*(P2 + t*(P3 + t*(P4 + t*P5))))" differing between cpu and gpu.
			c = t*P5;
			c = P4+c;
			c = t*c;
			c = P3+c;
			c = t*c;
			c = P2+c;
			c = t*c;
			c = P1+c;
			c = t*c;
			c = x-c;
			
			
			if (k == 0){
				ret = one - ((x*c)/(c - 2.0) - x);
				returned = true;
			}else{
				y = one - ((lo - (x*c)/(2.0 - c)) - hi);
			}
	
			if(!returned){
				int hiY = (int)(as_raw_long(y)>>32);
				if(k >= -1021) {
					//y = __HI(y, __HI(y) + (k << 20)); /* add k to y's exponent */
					//y = __HI(y, hiY + (k << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_raw_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_raw_long(y)&0xffffffffL)|(((long)(hiY + (k << 20))))<<32); /* add k to y's exponent */
					ret = y;
				} else {
					//y = __HI(y, hiY + ((k + 1000) << 20)); /* add k to y's exponent */
					//
					//__HI(double y, int high) == as_double((as_raw_long(y)&0xffffffffL)|(((long)high))<<32)
					y = as_double((as_raw_long(y)&0xffffffffL)|(((long)((k + 1000) << 20)))<<32); /* add k to y's exponent */
					ret = y * twom1000;
				}
			}
		}
		
		ret = Math.max(0, ret); //observed exp(-709.6305714683294) -> -2.000000258125633 without this while StrictMath.exp got 6.476772186088384E-309 which is a subnormal
		
		return ret;
		
	}
	
	
	
	protected static double exp_usingHIAndLOFuncs(double x){
		//TODO split those arrays, so theres only primitives.
		
		final double one	 = 1.0;
		//final double[] half = {0.5, -0.5,};
		final double half_0 = 0.5;
		//final double half_1 = -0.5;
		final double huge	= 1.0e+300;
		final double twom1000=	 0x1.0p-1000;			 //  9.33263618503218878990e-302 = 2^-1000
		final double o_threshold=  0x1.62e42fefa39efp9;	 //  7.09782712893383973096e+02
		final double u_threshold= -0x1.74910d52d3051p9;	 // -7.45133219101941108420e+02;
		//final double[] ln2HI   ={  0x1.62e42feep-1,		 //  6.93147180369123816490e-01
		//										 -0x1.62e42feep-1};		// -6.93147180369123816490e-01
		final double ln2HI_0 = 0x1.62e42feep-1;		 //  6.93147180369123816490e-01
		//final double ln2HI_1 = -0x1.62e42feep-1;
		//final double[] ln2LO   ={  0x1.a39ef35793c76p-33,   //  1.90821492927058770002e-10
		//										 -0x1.a39ef35793c76p-33};  // -1.90821492927058770002e-10
		final double ln2LO_0 = 0x1.a39ef35793c76p-33;   //  1.90821492927058770002e-10
		//final double ln2LO_1 = -0x1.a39ef35793c76p-33;
		
		final double invln2 =	  0x1.71547652b82fep0;	 //  1.44269504088896338700e+00

		final double P1   =  0x1.555555555553ep-3;  //  1.66666666666666019037e-01
		final double P2   = -0x1.6c16c16bebd93p-9;  // -2.77777777770155933842e-03
		final double P3   =  0x1.1566aaf25de2cp-14; //  6.61375632143793436117e-05
		final double P4   = -0x1.bbd41c5d26bf1p-20; // -1.65339022054652515390e-06
		final double P5   =  0x1.6376972bea4d0p-25; //  4.13813679705723846039e-08
		
		double y;
		double hi = 0.0;
		double lo = 0.0;
		double c;
		double t;
		int k = 0;
		int xsb;
		/*unsigned*/ int hx;

		hx  = __HI(x);  /* high word of x */
		xsb = (hx >> 31) & 1;			   /* sign bit of x */
		hx &= 0x7fffffff;			   /* high word of |x| */

		/* filter out non-finite argument */
		if (hx >= 0x40862E42) {				  /* if |x| >= 709.78... */
			if (hx >= 0x7ff00000) {
				if (((hx & 0xfffff) | __LO(x)) != 0)
					return x + x;				/* NaN */
				else
					return (xsb == 0) ? x : 0.0;	/* exp(+-inf) = {inf, 0} */
			}
			if (x > o_threshold)
				return huge * huge; /* overflow */
			if (x < u_threshold) // unsigned compare needed here?
				return twom1000 * twom1000; /* underflow */
		}

		/* argument reduction */
		if (hx > 0x3fd62e42) {		   /* if  |x| > 0.5 ln2 */
			if(hx < 0x3FF0A2B2) {	   /* and |x| < 1.5 ln2 */
				//hi = x - ln2HI[xsb];
				hi = x - (xsb==0 ? ln2HI_0 : -ln2HI_0);
				lo=(xsb==0 ? ln2LO_0 : -ln2LO_0);
				k = 1 - xsb - xsb;
			} else {
				//k  = (int)(invln2 * x + half[xsb]);
				k  = (int)(invln2 * x + (xsb==0 ? half_0 : -half_0));
				t  = k;
				//hi = x - t*ln2HI[0];	/* t*ln2HI is exact here */
				hi = x - t*ln2HI_0;	/* t*ln2HI is exact here */
				lo = t*ln2LO_0;
			}
			x  = hi - lo;
		} else if (hx < 0x3e300000)  {	 /* when |x|<2**-28 */
			if (huge + x > one)
				return one + x; /* trigger inexact */
		} else {
			k = 0;
		}

		/* x is now in primary range */
		t  = x * x;
		
		//deterministic roundoff, same in cpu and gpu,
		//instead of "c  = x - t*(P1 + t*(P2 + t*(P3 + t*(P4 + t*P5))))" differing between cpu and gpu.
		c = t*P5;
		c = P4+c;
		c = t*c;
		c = P3+c;
		c = t*c;
		c = P2+c;
		c = t*c;
		c = P1+c;
		c = t*c;
		c = x-c;
		
		if (k == 0)
			return one - ((x*c)/(c - 2.0) - x);
		else
			y = one - ((lo - (x*c)/(2.0 - c)) - hi);

		if(k >= -1021) {
			y = __HI(y, __HI(y) + (k << 20)); /* add k to y's exponent */
			return y;
		} else {
			y = __HI(y, __HI(y) + ((k + 1000) << 20)); /* add k to y's exponent */
			return y * twom1000;
		}
		
	}
	
	/** Return the low-order 32 bits of the double argument as an int. */
	protected static int __LO(double x){
		return (int)as_raw_long(x);
	}
	
	/** Return the high-order 32 bits of the double argument as an int. */
	protected static int __HI(double x){
		return (int)(as_raw_long(x)>>32);
	}
	
	/** might change this to as_raw_long later if get cpu and gpu to match that way, for speed */
	protected static long as_long(double x){
		return as_normed_long(x);
	}
	
	protected static long as_normed_long(double x){
		return Double.doubleToLongBits(x);
	}
	
	protected static long as_raw_long(double x){
		return Double.doubleToRawLongBits(x);
	}
	
	protected static double as_double(long x){
		return Double.longBitsToDouble(x);
	}

	/** Return a double with its high-order bits of the second argument
	and the low-order bits of the first argument.
	//__HI(double x, int high) == as_double((as_raw_long(x)&0xffffffffL)|(((long)high))<<32)
	*/
	protected static double __HI(double x, int high) {
		return as_double((as_raw_long(x)&0xffffffffL)|(((long)high))<<32);
	}
	
	public static void main(String[] args){
		boolean first = true;
		for(double d=-4.353121424351; d<710; d+=.09123217){
			if(first){
				/*
				> inBits                        1100000010000111000100101010011110110100010011010111010000110000 -738.3318868685801 get exp of this
				>  . . 
				> cpuOutBits                    0000000000000000000000000000000000000000000000000000000111000010 2.223E-321 an exp output
				> cpu2OutBits                   0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
				> gpuOutBits                    0000000000000000000000000000000000000000000000000000000000000000 0.0 an exp output
				>  . . 
				> diffFirst2                    0000000000000000000000000000000000000000000000000000000111000010
				> diffSecond2                   0000000000000000000000000000000000000000000000000000000000000000
				> as doubles do first 2 ==:     false
				> as doubles do secopnd 2 ==:   true
				> as doubles do 1st and 3rd ==: false
				> 
				> FIXME: 2021-3-21 since changing the c= line changed the output of Fdlibm53, todo compute it using BigDecimal (or compute it using boolean[] which I can do very slowly) etc and find which is more precise in math, which is probably StrictMath, but verify that and figure out what order of ops its using and write them in that order. Make all 3 match.
				Exception in thread "main" java.lang.RuntimeException: Test fail at i=62631
					at immutable.lazycl.spec.TestLazyCL.testCpuAndGpuOfExponentsOfEOnDoubles(TestLazyCL.java:368)
				
				"2.223E-321" an exp output. Very near "4.9406564584124654 × 10−324".
				https://en.wikipedia.org/wiki/Double-precision_floating-point_format
				0 00000000000 00000000000000000000000000000000000000000000000000012
				≙ 0000 0000 0000 000116 ≙ +2−1022 × 2−52 = 2−1074
				≈ 4.9406564584124654 × 10−324 (Min. subnormal positive double)
				
				wolframalpha says...
				e^-738.3318868685801 = 2.220934342872... × 10^-321
				https://www.wolframalpha.com/input/?i=e%5E-738.3318868685801
				..
				and says
				e^5.678 =                          292.36411657116124238085756930504066845588286330914615661559509747...
				javascript says Math.exp(5.678) -> 292.3641165711612
				https://www.tutorialspoint.com/compile_java_online.php says
				          StrictMath.exp(5.678) -> 292.3641165711612
				*/
				d = -738.3318868685801;
				first = false;
			}
			double javaSays = StrictMath.exp(d);
			double thisClassSays = expUsingRawDoubleLongTransform(d);
			String s = "exp("+d+") javaSays="+javaSays+" thisClassSays="+thisClassSays+" but as you see in comment of "+Fdlibm53Exp.class.getName()+" I gave up on exactly matching StrictMath.exp to gpu and instead only match this port of StrictMath with the gpu.";
			System.out.println(s);
			if(javaSays != thisClassSays) throw new RuntimeException(s);
		}
		System.out.println("Tests pass");
	}

}