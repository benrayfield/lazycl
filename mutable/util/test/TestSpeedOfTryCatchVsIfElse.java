package mutable.util.test;

import mutable.util.Rand;
import mutable.util.Time;

public class TestSpeedOfTryCatchVsIfElse{
	
	public static void main(String[] args){
		for(int repeat=1; repeat<Integer.MAX_VALUE; repeat*=10){ //FIXME ending condition wraps around int range
			for(int z=0; z<10; z++){
				int y = (int)(repeat*.98)+Rand.strongRand.nextInt(repeat/100+1);
				Object x = new Object();
				double timeStart = Time.now();
				int hashcodeSum = 0;
				for(int i=0; i<repeat; i++){
					try{
						hashcodeSum += x.hashCode();
						if(i==y) x = null;
					}catch(NullPointerException e){
						x = new Object();
					}
				}
				double timeEnd = Time.now();
				double durationOfTryWithRareCatch = timeEnd-timeStart;
				timeStart = Time.now();
				x = new Object();
				for(int i=0; i<repeat; i++){
					if(x == null) x = new Object();
					hashcodeSum += x.hashCode();
					if(i==y) x = null;
				}
				timeEnd = Time.now();
				double durationOfIfElse = timeEnd-timeStart;
				System.out.println("repeat="+repeat+" ifElseIsTimesFaster="+(durationOfTryWithRareCatch/durationOfIfElse)+" durationOfTryWithRareCatch="+durationOfTryWithRareCatch+" durationOfIfElse="+durationOfIfElse+" hashcodeSum="+hashcodeSum);
			}
		}
	}

}

/* In adoptopenjdk8 in win10 on intel i9 3.6ghz * 8 cpus liquid variable overclocked to 5 ghz,
but running single threaded.
Based on this, I'm going with the try/catch(NullPointerException) in LazyBlob (eval in catch)
instead of if == null then eval.
Its harder to optimize if/else across function calls than in the same loop,
but I've heard hardware is often optimized to check for null pointers and divide by 0 etc.

repeat=1 ifElseIsTimesFaster=8.0 durationOfTryWithRareCatch=1.9073486328125E-6 durationOfIfElse=2.384185791015625E-7 hashcodeSum=-1308455853
repeat=1 ifElseIsTimesFaster=3.0 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=2.384185791015625E-7 hashcodeSum=2071110918
repeat=1 ifElseIsTimesFaster=2.0 durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=2.384185791015625E-7 hashcodeSum=-1778815232
repeat=1 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=7.152557373046875E-7 hashcodeSum=-988030881
repeat=1 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=4.76837158203125E-7 hashcodeSum=-1901216872
repeat=1 ifElseIsTimesFaster=Infinity durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=0.0 hashcodeSum=-2094801942
repeat=1 ifElseIsTimesFaster=3.0 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=2.384185791015625E-7 hashcodeSum=2103781654
repeat=1 ifElseIsTimesFaster=2.0 durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=2.384185791015625E-7 hashcodeSum=-2106153461
repeat=1 ifElseIsTimesFaster=2.0 durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=2.384185791015625E-7 hashcodeSum=-1974486153
repeat=1 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=4.76837158203125E-7 durationOfIfElse=4.76837158203125E-7 hashcodeSum=-1555105681
repeat=10 ifElseIsTimesFaster=1.3333333333333333 durationOfTryWithRareCatch=9.5367431640625E-7 durationOfIfElse=7.152557373046875E-7 hashcodeSum=-1901094018
repeat=10 ifElseIsTimesFaster=1.3333333333333333 durationOfTryWithRareCatch=9.5367431640625E-7 durationOfIfElse=7.152557373046875E-7 hashcodeSum=-658502160
repeat=10 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=7.152557373046875E-7 hashcodeSum=30547200
repeat=10 ifElseIsTimesFaster=2.0 durationOfTryWithRareCatch=9.5367431640625E-7 durationOfIfElse=4.76837158203125E-7 hashcodeSum=1645773884
repeat=10 ifElseIsTimesFaster=1.5 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=4.76837158203125E-7 hashcodeSum=191656034
repeat=10 ifElseIsTimesFaster=0.05 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=1.430511474609375E-5 hashcodeSum=-532998560
repeat=10 ifElseIsTimesFaster=1.3333333333333333 durationOfTryWithRareCatch=9.5367431640625E-7 durationOfIfElse=7.152557373046875E-7 hashcodeSum=1738543092
repeat=10 ifElseIsTimesFaster=0.75 durationOfTryWithRareCatch=7.152557373046875E-7 durationOfIfElse=9.5367431640625E-7 hashcodeSum=591052506
repeat=10 ifElseIsTimesFaster=2.0 durationOfTryWithRareCatch=9.5367431640625E-7 durationOfIfElse=4.76837158203125E-7 hashcodeSum=60265226
repeat=10 ifElseIsTimesFaster=2.5 durationOfTryWithRareCatch=1.1920928955078125E-6 durationOfIfElse=4.76837158203125E-7 hashcodeSum=-728024260
repeat=100 ifElseIsTimesFaster=8.411764705882353 durationOfTryWithRareCatch=3.409385681152344E-5 durationOfIfElse=4.0531158447265625E-6 hashcodeSum=-1825701596
repeat=100 ifElseIsTimesFaster=2.75 durationOfTryWithRareCatch=1.049041748046875E-5 durationOfIfElse=3.814697265625E-6 hashcodeSum=-1607720319
repeat=100 ifElseIsTimesFaster=2.7857142857142856 durationOfTryWithRareCatch=9.298324584960938E-6 durationOfIfElse=3.337860107421875E-6 hashcodeSum=-267528967
repeat=100 ifElseIsTimesFaster=1.0714285714285714 durationOfTryWithRareCatch=3.5762786865234375E-6 durationOfIfElse=3.337860107421875E-6 hashcodeSum=604683784
repeat=100 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=3.5762786865234375E-6 durationOfIfElse=3.5762786865234375E-6 hashcodeSum=294788640
repeat=100 ifElseIsTimesFaster=0.5833333333333334 durationOfTryWithRareCatch=3.337860107421875E-6 durationOfIfElse=5.7220458984375E-6 hashcodeSum=-425434944
repeat=100 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=3.337860107421875E-6 durationOfIfElse=3.337860107421875E-6 hashcodeSum=-1318833184
repeat=100 ifElseIsTimesFaster=1.0714285714285714 durationOfTryWithRareCatch=3.5762786865234375E-6 durationOfIfElse=3.337860107421875E-6 hashcodeSum=1147742504
repeat=100 ifElseIsTimesFaster=1.0666666666666667 durationOfTryWithRareCatch=3.814697265625E-6 durationOfIfElse=3.5762786865234375E-6 hashcodeSum=-1404879908
repeat=100 ifElseIsTimesFaster=1.0 durationOfTryWithRareCatch=3.5762786865234375E-6 durationOfIfElse=3.5762786865234375E-6 hashcodeSum=-1593455796
repeat=1000 ifElseIsTimesFaster=1.1071428571428572 durationOfTryWithRareCatch=3.695487976074219E-5 durationOfIfElse=3.337860107421875E-5 hashcodeSum=2093309763
repeat=1000 ifElseIsTimesFaster=1.1 durationOfTryWithRareCatch=3.6716461181640625E-5 durationOfIfElse=3.337860107421875E-5 hashcodeSum=-1840674234
repeat=1000 ifElseIsTimesFaster=1.1079136690647482 durationOfTryWithRareCatch=3.6716461181640625E-5 durationOfIfElse=3.314018249511719E-5 hashcodeSum=-1424056738
repeat=1000 ifElseIsTimesFaster=1.1353383458646618 durationOfTryWithRareCatch=3.600120544433594E-5 durationOfIfElse=3.170967102050781E-5 hashcodeSum=-1810282123
repeat=1000 ifElseIsTimesFaster=1.0633802816901408 durationOfTryWithRareCatch=3.600120544433594E-5 durationOfIfElse=3.3855438232421875E-5 hashcodeSum=-1756491631
repeat=1000 ifElseIsTimesFaster=1.1185185185185185 durationOfTryWithRareCatch=3.600120544433594E-5 durationOfIfElse=3.218650817871094E-5 hashcodeSum=-1572191834
repeat=1000 ifElseIsTimesFaster=1.1014492753623188 durationOfTryWithRareCatch=3.62396240234375E-5 durationOfIfElse=3.2901763916015625E-5 hashcodeSum=1365306481
repeat=1000 ifElseIsTimesFaster=1.1014492753623188 durationOfTryWithRareCatch=3.62396240234375E-5 durationOfIfElse=3.2901763916015625E-5 hashcodeSum=-1660883930
repeat=1000 ifElseIsTimesFaster=1.1240875912408759 durationOfTryWithRareCatch=3.6716461181640625E-5 durationOfIfElse=3.266334533691406E-5 hashcodeSum=-1137534829
repeat=1000 ifElseIsTimesFaster=1.1333333333333333 durationOfTryWithRareCatch=3.647804260253906E-5 durationOfIfElse=3.218650817871094E-5 hashcodeSum=-900861606
repeat=10000 ifElseIsTimesFaster=0.9233603537214443 durationOfTryWithRareCatch=2.987384796142578E-4 durationOfIfElse=3.235340118408203E-4 hashcodeSum=-1914893535
repeat=10000 ifElseIsTimesFaster=0.9444027047332832 durationOfTryWithRareCatch=2.9969215393066406E-4 durationOfIfElse=3.173351287841797E-4 hashcodeSum=-740721878
repeat=10000 ifElseIsTimesFaster=0.9480812641083521 durationOfTryWithRareCatch=3.0040740966796875E-4 durationOfIfElse=3.1685829162597656E-4 hashcodeSum=-1652702598
repeat=10000 ifElseIsTimesFaster=0.9141193595342066 durationOfTryWithRareCatch=2.994537353515625E-4 durationOfIfElse=3.275871276855469E-4 hashcodeSum=-1374799439
repeat=10000 ifElseIsTimesFaster=0.9313367421475529 durationOfTryWithRareCatch=3.039836883544922E-4 durationOfIfElse=3.2639503479003906E-4 hashcodeSum=1704410646
repeat=10000 ifElseIsTimesFaster=0.9514200298953662 durationOfTryWithRareCatch=3.0350685119628906E-4 durationOfIfElse=3.190040588378906E-4 hashcodeSum=-1491859352
repeat=10000 ifElseIsTimesFaster=0.940387481371088 durationOfTryWithRareCatch=3.008842468261719E-4 durationOfIfElse=3.199577331542969E-4 hashcodeSum=1222272433
repeat=10000 ifElseIsTimesFaster=0.9350746268656717 durationOfTryWithRareCatch=2.987384796142578E-4 durationOfIfElse=3.1948089599609375E-4 hashcodeSum=-2129986033
repeat=10000 ifElseIsTimesFaster=0.943904263275991 durationOfTryWithRareCatch=3.008842468261719E-4 durationOfIfElse=3.1876564025878906E-4 hashcodeSum=943074826
repeat=10000 ifElseIsTimesFaster=0.3042212518195051 durationOfTryWithRareCatch=2.989768981933594E-4 durationOfIfElse=9.827613830566406E-4 hashcodeSum=-2008039373
repeat=100000 ifElseIsTimesFaster=16.547288776796975 durationOfTryWithRareCatch=0.003128528594970703 durationOfIfElse=1.8906593322753906E-4 hashcodeSum=550634360
repeat=100000 ifElseIsTimesFaster=1.1861861861861862 durationOfTryWithRareCatch=1.8835067749023438E-4 durationOfIfElse=1.5878677368164062E-4 hashcodeSum=2024808374
repeat=100000 ifElseIsTimesFaster=0.6421052631578947 durationOfTryWithRareCatch=1.0180473327636719E-4 durationOfIfElse=1.5854835510253906E-4 hashcodeSum=-343240449
repeat=100000 ifElseIsTimesFaster=1.106766917293233 durationOfTryWithRareCatch=1.7547607421875E-4 durationOfIfElse=1.5854835510253906E-4 hashcodeSum=-1447066791
repeat=100000 ifElseIsTimesFaster=1.092953523238381 durationOfTryWithRareCatch=1.7380714416503906E-4 durationOfIfElse=1.590251922607422E-4 hashcodeSum=-2141108203
repeat=100000 ifElseIsTimesFaster=1.0881913303437967 durationOfTryWithRareCatch=1.735687255859375E-4 durationOfIfElse=1.595020294189453E-4 hashcodeSum=1638434759
repeat=100000 ifElseIsTimesFaster=0.9142857142857143 durationOfTryWithRareCatch=1.4495849609375E-4 durationOfIfElse=1.5854835510253906E-4 hashcodeSum=-1358244281
repeat=100000 ifElseIsTimesFaster=1.1004497751124438 durationOfTryWithRareCatch=1.7499923706054688E-4 durationOfIfElse=1.590251922607422E-4 hashcodeSum=573680617
repeat=100000 ifElseIsTimesFaster=0.976083707025411 durationOfTryWithRareCatch=1.556873321533203E-4 durationOfIfElse=1.595020294189453E-4 hashcodeSum=559751291
repeat=100000 ifElseIsTimesFaster=1.037593984962406 durationOfTryWithRareCatch=1.6450881958007812E-4 durationOfIfElse=1.5854835510253906E-4 hashcodeSum=1308543416
repeat=1000000 ifElseIsTimesFaster=1.14792543595911 durationOfTryWithRareCatch=0.0018205642700195312 durationOfIfElse=0.0015859603881835938 hashcodeSum=1396876954
repeat=1000000 ifElseIsTimesFaster=1.142406489409644 durationOfTryWithRareCatch=0.0018131732940673828 durationOfIfElse=0.0015871524810791016 hashcodeSum=1884508312
repeat=1000000 ifElseIsTimesFaster=1.0462949045543364 durationOfTryWithRareCatch=0.0016596317291259766 durationOfIfElse=0.0015861988067626953 hashcodeSum=2015768657
repeat=1000000 ifElseIsTimesFaster=1.049212303075769 durationOfTryWithRareCatch=0.0016672611236572266 durationOfIfElse=0.001589059829711914 hashcodeSum=1310494055
repeat=1000000 ifElseIsTimesFaster=1.0971368610403238 durationOfTryWithRareCatch=0.001744985580444336 durationOfIfElse=0.0015904903411865234 hashcodeSum=816958156
repeat=1000000 ifElseIsTimesFaster=1.0849496316343408 durationOfTryWithRareCatch=0.001720428466796875 durationOfIfElse=0.0015857219696044922 hashcodeSum=1960992863
repeat=1000000 ifElseIsTimesFaster=1.0335086401202103 durationOfTryWithRareCatch=0.0016398429870605469 durationOfIfElse=0.0015866756439208984 hashcodeSum=-1832454790
repeat=1000000 ifElseIsTimesFaster=0.8866366366366366 durationOfTryWithRareCatch=0.0014078617095947266 durationOfIfElse=0.0015878677368164062 hashcodeSum=797362134
repeat=1000000 ifElseIsTimesFaster=0.9033228086002105 durationOfTryWithRareCatch=0.0014324188232421875 durationOfIfElse=0.0015857219696044922 hashcodeSum=-1522654830
repeat=1000000 ifElseIsTimesFaster=1.1507137490608565 durationOfTryWithRareCatch=0.0018258094787597656 durationOfIfElse=0.0015866756439208984 hashcodeSum=634919744
repeat=10000000 ifElseIsTimesFaster=0.99586919622069 durationOfTryWithRareCatch=0.01580667495727539 durationOfIfElse=0.01587224006652832 hashcodeSum=-39875550
repeat=10000000 ifElseIsTimesFaster=1.0492970439798126 durationOfTryWithRareCatch=0.016655445098876953 durationOfIfElse=0.015872955322265625 hashcodeSum=-1687983260
repeat=10000000 ifElseIsTimesFaster=0.9450346350918845 durationOfTryWithRareCatch=0.01499485969543457 durationOfIfElse=0.015866994857788086 hashcodeSum=-798295173
repeat=10000000 ifElseIsTimesFaster=0.8911331529906823 durationOfTryWithRareCatch=0.01413726806640625 durationOfIfElse=0.01586437225341797 hashcodeSum=-1183638181
repeat=10000000 ifElseIsTimesFaster=0.9102577590741715 durationOfTryWithRareCatch=0.014439582824707031 durationOfIfElse=0.01586318016052246 hashcodeSum=1813239454
repeat=10000000 ifElseIsTimesFaster=0.8551140632983679 durationOfTryWithRareCatch=0.013566255569458008 durationOfIfElse=0.015864849090576172 hashcodeSum=2008794561
repeat=10000000 ifElseIsTimesFaster=0.8050872115281542 durationOfTryWithRareCatch=0.012534379959106445 durationOfIfElse=0.015568971633911133 hashcodeSum=1306156511
repeat=10000000 ifElseIsTimesFaster=0.6534155239652787 durationOfTryWithRareCatch=0.01114511489868164 durationOfIfElse=0.017056703567504883 hashcodeSum=-1322178037
repeat=10000000 ifElseIsTimesFaster=0.8125448740471426 durationOfTryWithRareCatch=0.01268148422241211 durationOfIfElse=0.015607118606567383 hashcodeSum=-1477187731
repeat=10000000 ifElseIsTimesFaster=0.842045645884676 durationOfTryWithRareCatch=0.013115406036376953 durationOfIfElse=0.015575647354125977 hashcodeSum=-9388579
repeat=100000000 ifElseIsTimesFaster=0.8536972933468583 durationOfTryWithRareCatch=0.13423752784729004 durationOfIfElse=0.1572425365447998 hashcodeSum=602201010
repeat=100000000 ifElseIsTimesFaster=0.7921063901379585 durationOfTryWithRareCatch=0.12347579002380371 durationOfIfElse=0.1558828353881836 hashcodeSum=-470044218
repeat=100000000 ifElseIsTimesFaster=0.8131217225914155 durationOfTryWithRareCatch=0.126694917678833 durationOfIfElse=0.15581297874450684 hashcodeSum=-797070534
repeat=100000000 ifElseIsTimesFaster=0.7510170125399745 durationOfTryWithRareCatch=0.11730074882507324 durationOfIfElse=0.1561892032623291 hashcodeSum=-20232296
repeat=100000000 ifElseIsTimesFaster=0.8127197285900153 durationOfTryWithRareCatch=0.12599420547485352 durationOfIfElse=0.1550278663635254 hashcodeSum=-908687266
repeat=100000000 ifElseIsTimesFaster=0.7630155386073213 durationOfTryWithRareCatch=0.11864304542541504 durationOfIfElse=0.15549230575561523 hashcodeSum=-971850301
repeat=100000000 ifElseIsTimesFaster=0.7192796461529107 durationOfTryWithRareCatch=0.10999464988708496 durationOfIfElse=0.1529233455657959 hashcodeSum=441493725
repeat=100000000 ifElseIsTimesFaster=0.5443620969480983 durationOfTryWithRareCatch=0.11435317993164062 durationOfIfElse=0.2100682258605957 hashcodeSum=-1195450822
repeat=100000000 ifElseIsTimesFaster=0.7103829113924051 durationOfTryWithRareCatch=0.1070408821105957 durationOfIfElse=0.1506805419921875 hashcodeSum=1380411092
repeat=100000000 ifElseIsTimesFaster=0.7218356634795974 durationOfTryWithRareCatch=0.10895228385925293 durationOfIfElse=0.15093779563903809 hashcodeSum=1188550759
repeat=1000000000 ifElseIsTimesFaster=0.7183387882873873 durationOfTryWithRareCatch=1.0908117294311523 durationOfIfElse=1.5185198783874512 hashcodeSum=-755482629
repeat=1000000000 ifElseIsTimesFaster=0.7376367854193893 durationOfTryWithRareCatch=1.1111583709716797 durationOfIfElse=1.506376028060913 hashcodeSum=754915636
repeat=1000000000 ifElseIsTimesFaster=0.7475721180971089 durationOfTryWithRareCatch=1.1214747428894043 durationOfIfElse=1.5001559257507324 hashcodeSum=-1184903988
repeat=1000000000 ifElseIsTimesFaster=0.7942213466104444 durationOfTryWithRareCatch=1.2152678966522217 durationOfIfElse=1.530137538909912 hashcodeSum=447962652
repeat=1000000000 ifElseIsTimesFaster=0.7470513285959731 durationOfTryWithRareCatch=1.1553583145141602 durationOfIfElse=1.546558141708374 hashcodeSum=421193027
repeat=1000000000 ifElseIsTimesFaster=0.7756983363364631 durationOfTryWithRareCatch=1.191420316696167 durationOfIfElse=1.5359325408935547 hashcodeSum=-1563585401
repeat=1000000000 ifElseIsTimesFaster=0.7534214759402156 durationOfTryWithRareCatch=1.1369144916534424 durationOfIfElse=1.5090019702911377 hashcodeSum=-2070235178
repeat=1000000000 ifElseIsTimesFaster=0.7379268235113849 durationOfTryWithRareCatch=1.1367111206054688 durationOfIfElse=1.5404117107391357 hashcodeSum=-672717900
repeat=1000000000 ifElseIsTimesFaster=0.7698921683707008 durationOfTryWithRareCatch=1.1657872200012207 durationOfIfElse=1.514221429824829 hashcodeSum=-1040751245
repeat=1000000000 ifElseIsTimesFaster=0.7355252343520075 durationOfTryWithRareCatch=1.120805025100708 durationOfIfElse=1.5238158702850342 hashcodeSum=159236500
repeat=1410065408 ifElseIsTimesFaster=0.7581581715931298 durationOfTryWithRareCatch=1.6349947452545166 durationOfIfElse=2.1565351486206055 hashcodeSum=469159682
repeat=1410065408 ifElseIsTimesFaster=0.7391369881847235 durationOfTryWithRareCatch=1.5701689720153809 durationOfIfElse=2.1243274211883545 hashcodeSum=-1615105674
repeat=1410065408 ifElseIsTimesFaster=0.7685462455413165 durationOfTryWithRareCatch=1.6734955310821533 durationOfIfElse=2.1774818897247314 hashcodeSum=-2040376190
repeat=1410065408 ifElseIsTimesFaster=0.7326731497550808 durationOfTryWithRareCatch=1.5740196704864502 durationOfIfElse=2.148324489593506 hashcodeSum=779854620
repeat=1410065408 ifElseIsTimesFaster=0.7599261068629498 durationOfTryWithRareCatch=1.641613483428955 durationOfIfElse=2.1602277755737305 hashcodeSum=208372049
repeat=1410065408 ifElseIsTimesFaster=0.7507933416684219 durationOfTryWithRareCatch=1.6002368927001953 durationOfIfElse=2.131394624710083 hashcodeSum=-683580418
repeat=1410065408 ifElseIsTimesFaster=0.7614736972346341 durationOfTryWithRareCatch=1.6316545009613037 durationOfIfElse=2.142758846282959 hashcodeSum=1145802358
repeat=1410065408 ifElseIsTimesFaster=0.735185001233806 durationOfTryWithRareCatch=1.5598835945129395 durationOfIfElse=2.1217565536499023 hashcodeSum=808549471
*/