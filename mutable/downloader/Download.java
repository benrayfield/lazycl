/** Ben F Rayfield offers this benrayfield.* software opensource MIT license */
package mutable.downloader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import mutable.util.Stream;
import immutable.util.Text;
import mutable.util.Time;

public class Download{
	private Download(){}
	
	/** ERROR: Must redesign to not require knowledge of size before start, since many urls return -1.
	Failed to download file: http://en.wikipedia.org/wiki/Web_crawler because:
	java.io.IOException: File size is -1. File is: http://en.wikipedia.org/wiki/Web_crawler
	*/
	public static byte[] download(String fileNameOrURL, DownloadListener listener){
		//double timeOutInc = listener.timeOutIncrement();
		double timeOutAll = listener.timeOutAll();
		int updateIncBytes = listener.updateIncrementInBytes();
		double updateInc = listener.minimumDisplayUpdateIncrementInSeconds();
		//int totalFileSize;
		InputStream in = null;
		try{

			try{
				URL url = new URL(fileNameOrURL);
				URLConnection con = url.openConnection();
				//totalFileSize = con.getContentLength();
				in = con.getInputStream();
				System.err.println("in = "+in);
			}catch(MalformedURLException mal){ //maybe its a file instead of a URL
				try{
					File file = new File(fileNameOrURL);
					if(!file.exists()){
						throw new IOException(fileNameOrURL
						+" looks more like a filename than a URL, but that file does not exist.");
					}
					//totalFileSize = (int) file.length();
					in = new FileInputStream(fileNameOrURL);
				}catch(SecurityException sec){
					throw new IOException("I think "+fileNameOrURL+" is a file, but I cant read it because: "+sec);
				}
			}

			//if(totalFileSize < 1) throw new IOException("File size is "+totalFileSize+". File is: "+fileNameOrURL);
			double startTime = Time.now();
			double lastBytesDownloadedWhen = startTime;
			double lastUpdatedWhen = startTime;
			listener.downloadStarted(fileNameOrURL, startTime);
			//byte content[] = new byte[totalFileSize];
			byte content[] = new byte[1]; //array is replaced as more downloaded
			int contentUsed = 0;
			GrowingByteArrayInputStream downloadedSoFar = new GrowingByteArrayInputStream(content,0,contentUsed);
			int updateAt = updateIncBytes; //next byte size to call update() for
			//while(contentUsed < totalFileSize){
			while(true){
				
				/*int available = in.available();
				System.err.println("in.available = "+available);
				if(available == 0) break; //download finished
				int maxImmediateCapacityNeeded = downloadedSoFar.count()+in.available();
				if(downloadedSoFar.capacity() < maxImmediateCapacityNeeded){
					downloadedSoFar.setCapacity(maxImmediateCapacityNeeded*2);
					content = downloadedSoFar.array(); //setCapacity copies to new array
				}
				double time = Time.time();
				//if(available <= 0){ //wait for more bytes to become available, or give up if waited too long already.
				//	double lastBytesDownloadHowLongAgo = time - lastBytesDownloadedWhen;
				//	if(timeOutInc <= lastBytesDownloadHowLongAgo){
				//		throw new IOException("Its been too long ("+lastBytesDownloadHowLongAgo
				//		+" milliseconds) since any bytes were downloaded from: "+fileNameOrURL);
				//	}
				//	Thread.yield();
				//	continue;
				//}
				System.err.println("reading...");
				int readThisManyBytes = in.read(content, contentUsed, available);
				System.err.println("readThisManyBytes = "+readThisManyBytes);
				*/
				
				//Because available() is returning 0 before it ends, which is an error, using the slower in.read()
				int nextByte = in.read();
				double time = Time.now();
				int available = nextByte==-1 ? 0 : 1;
				int readThisManyBytes = nextByte==-1 ? -1 : 1;
				//System.out.println("contentUsed="+contentUsed+" byteAsChar: "+(char)nextByte);
				
				/*if(readThisManyBytes != available){
					throw new IOException("Tried to read exactly "+available+" bytes from file,"
					+" but instead, "+readThisManyBytes+" bytes were read. File is: "+fileNameOrURL);
				}*/
				if(readThisManyBytes == -1) break; //done downloading
				downloadedSoFar.appendByte((byte)nextByte);
				
				lastBytesDownloadedWhen = time;
				contentUsed += readThisManyBytes;
				downloadedSoFar.setCount(contentUsed); //InputStream uses more and more of the same array
				if(updateAt < contentUsed){ //enough bytes to update, but has enough time passed since last update()?
					updateAt = Math.max(updateAt+updateIncBytes, contentUsed+1); //could have downloaded more than updateIncBytes
					double updatedHowLongAgo = time - lastUpdatedWhen;
					double totalDownloadTime = time-startTime;
					if(updateInc <= updatedHowLongAgo){ //its been long enough. update again.
						lastUpdatedWhen = time;
						listener.update(fileNameOrURL, downloadedSoFar, totalDownloadTime);
					}
					if(timeOutAll <= totalDownloadTime){
						throw new IOException("Its been too long ("+totalDownloadTime/60f
						+" minutes) since the download started. File or URL is: "+fileNameOrURL);
					}
				}
			}
			//downloadedSoFar.setCapacity(downloadedSoFar.count());
			content = downloadedSoFar.trim();
			listener.done(fileNameOrURL, content);
			return content;
		}catch(IOException e){
			listener.failed(fileNameOrURL, e);
			return null;
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException e){
					String msg = "Could not close "+in+" because "+e.getMessage();
					System.err.println(msg);
					throw new RuntimeException(msg, e);
				}
			}
		}
	}
	
	public static byte[] downloadWithDefaultOptions(String fileNameOrURL){
		double timeOutAll = 10*60; //10 minutes
		//double timeOutAll = 60*60; //1 hour
		//double timeOutIncrement = 30; //30 seconds
		//double timeOutIncrement = 10; //10 seconds
		//int displayUpdateIncrementInBytes = 100000; //100 kilobytes
		int displayUpdateIncrementInBytes = 1000000; //1 mB
		//int displayUpdateIncrementInBytes = 1000; //1 kilobyte
		double minimumDisplayUpdateIncrementInSeconds = 1; //1 second
		return download(fileNameOrURL, timeOutAll, /*timeOutIncrement,*/
			displayUpdateIncrementInBytes, minimumDisplayUpdateIncrementInSeconds);
	}
	
	public static byte[] download(String fileNameOrURL, final double timeOutAll, /*final double timeOutIncrement,*/
			final int updateIncrementInBytes, final double minimumUpdateIncrementInSeconds){
		DownloadListener listener = new DownloadListener(){
			public double timeOutAll(){  return timeOutAll; }
			//public double timeOutIncrement(){ return timeOutIncrement; }
			public int  updateIncrementInBytes(){ return updateIncrementInBytes; }
			public double minimumDisplayUpdateIncrementInSeconds(){ return 1; } //1 second
			public void downloadStarted(String fileNameOrURL, double startTime){
				println("Download of file "+fileNameOrURL+" started.");
			}
			public void update(String fileNameOrURL, GrowingByteArrayInputStream bytes, double secondsSoFar){
				println("Downloaded "+bytes.count()+" bytes of file/url: "+fileNameOrURL);
			}
			public void done(String fileNameOrURL, byte bytes[]){
				println("Done downloading all "+bytes.length+" bytes of file: "+fileNameOrURL);
			}
			public void failed(String fileNameOrURL, Throwable error){
				println("Failed to download file: "+fileNameOrURL+" because: "+error);
			}
			public void println(String p){
				System.out.println(p);
			}
		};
		return download(fileNameOrURL, listener);
	}
	
	/*public static byte[] experimentalFasterDownload(String url){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try{
			in = new URL(url).openConnection().getInputStream();
			in.transferTo(out);
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			Stream.closeQuiet(in);
		}
		return out.toByteArray();
	}*/
	
	/** test */
	public static void main(String[] args){
		String url = "http://java.com";
		System.out.println("Downloading "+url);
		byte b[] = downloadWithDefaultOptions(url);
		String s = Text.bytesToStr(b);
		System.out.println(s);
		System.out.println("Downloaded "+url+" (content above)");
		
		//the use of localhost url instead of domainname pointing at localhost, makes it many times faster. strange.
		//400mB/sec with localhost. 15mB/sec with domainname to same address.
		
		/*experimentalFasterDownload("http://java.com"); //first call is often much slower
		long start = Time.utcnano();
		byte[] bytes = experimentalFasterDownload("FIXME");
		long end = Time.utcnano();
		double duration = (end-start)*1e-9;
		double bytesPerSec = bytes.length/duration;
		System.out.println("bytesPerSec="+bytesPerSec+" bytes="+bytes.length+" duration="+duration);
		*/
	}

}
