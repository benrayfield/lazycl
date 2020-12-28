/** Ben F Rayfield offers this benrayfield.* software opensource MIT license */
package mutable.downloader.schedule;
//import common.Hash;
//import common.Time;
//import common.seqfile.SeqFileLine;

public class ScheduleDownload /*extends SeqFileLine implements Comparable<ScheduleDownload>*/{
	
	/** prefixes normally end with space. Its to say what kind of SeqFileLine it is when reading that file *
	public static final String prefix = "ScheduleDownload ";
	
	/** TODO? Is hashed to get part of 2 mutable filenames described in SchedulePerFileDownloader.
	Or maybe that hashing should be done outside this class. I'll keep it as text of url here.
	*
	public final String url;
	
	/** Earliest time (number of seconds since year 1970) the download can start *
	public final double earliestStart;
	
	/** After each failure, downloading again is scheduled twice as far into the futre as last time,
	so there is max logBase2 number of downloads. Urls that will never work would within that
	many tries get scheduled so far ahead they wont happen (TODO remove them from schedule if that far),
	but urls that only temporarily dont work, even if they start working months later,
	would eventually be tried again.
	*
	public final int howManyFailuresSinceLastGoodDownload;
	
	public ScheduleDownload(String line){
		super(line);
		String tokens[] = line.split("\\s");
		if(tokens.length != 3) throw new RuntimeException(
			"Must be 3 tokens (whitespace separated texts): earliestStart, howManyFailuresSinceLastGoodDownload, and url. line="+line);
		earliestStart = Double.parseDouble(tokens[0]);
		howManyFailuresSinceLastGoodDownload = Integer.parseInt(tokens[1]);
		url = tokens[2];
	}

	public ScheduleDownload(String url, double earliestStart, int howManyFailuresSinceLastGoodDownload){
		super(prefix+url+" "+earliestStart+" "+howManyFailuresSinceLastGoodDownload);
		this.url = url;
		this.earliestStart = earliestStart;
		this.howManyFailuresSinceLastGoodDownload = howManyFailuresSinceLastGoodDownload;
	}

	public int compareTo(ScheduleDownload d){
		if(earliestStart < d.earliestStart) return -1;
		if(earliestStart > d.earliestStart) return 1;
		return url.compareTo(d.url);
	}
	
	public boolean equals(Object o){
		if(!(o instanceof ScheduleDownload)) return false;
		return url.equals( ((ScheduleDownload)o).url );
	}


	/** Is hashed to get part of 2 mutable filenames described in SchedulePerFileDownloader. *
	public String url();
	
	/** As with all time measurements in this software, time is seconds since year 1970 *
	public double soonestDownloadAgain();
	*/

}
