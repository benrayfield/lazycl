/** Ben F Rayfield offers this benrayfield.* software opensource MIT license */
package mutable.downloader.schedule;

import java.io.File;

/** Downloads files, each with their own schedule for when to update them.
<br><br>
Each url has 2 files in the mutable space and as many files
in the immutable space as unique content downloaded over time.
One of the mutable files is a list of hashName pointing into
the immutable space. The other mutable file is schedule info.
*/
public class SchedulePerFileDownloader{
	
	public final File dir;
	
	protected double defaultInterval;
	
	public SchedulePerFileDownloader(File dir){
		if(dir.isFile()) throw new RuntimeException("Is a file, not a dir: "+dir);
		dir.mkdirs();
		if(!dir.isDirectory()) throw new RuntimeException("Couldnt create dir: "+dir);
		this.dir = dir;
	}
	
	/** Download each file again this often, by default */
	public double getDefaultIntervalPerFile(){
		return defaultInterval; 
	}
	
	/** Does not affect the next download time for each file already scheduled
	but will change the interval after that.
	*/
	public void setDefaultIntervalPerFile(double interval){
		defaultInterval = interval;
	}
	
	

}