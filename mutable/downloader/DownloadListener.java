/** Ben F Rayfield offers this benrayfield.* software opensource MIT license */
package mutable.downloader;

import java.io.InputStream;

/** OLD COMMENTS from CodeSimian software where I (Ben F Rayfield) copied this code out of...
<br><br>
Watches a download of a file or URL many times as more and more bytes are downloaded.
During the download, those bytes can be accessed by an InputStream.
After the download, all the bytes are returned as a byte[] array.
<br><br>
Specifies when updates should be given and what to do when the download ends correctly or in error.
Specifies how long to wait in total and after each incremental download, before ending in error.
<br><br>
NEED TO ADD InputStream parameters parallel to String fileNameOrURL.
Or move that to a different interface?
*/
public interface DownloadListener{
	
	//TODO, a function similar to timeOutIncrement except its minimum bytes downloaded per timeOutIncrement, instead of allowing it to download only 1 byte and stay active.
	
	/** end the download if whole download not done in this many seconds */
	public double timeOutAll();
	
	/** end the download if download some bytes, but not download any more bytes for this many seconds *
	public double timeOutIncrement();
	*/

	/** downloader should call update() when this many more bytes have
	been downloaded since the last update() or downloadStarted()
	*/
	public int updateIncrementInBytes();

	/** update() will not be called if its too soon after the last update() */
	public double minimumDisplayUpdateIncrementInSeconds();

	/** the connection is made, but no bytes downloaded yet */
	public void downloadStarted(String fileNameOrURL, double startTime);

	/** update() is called when updateIncrementInBytes more bytes are downloaded,
	but not if minimumUpdateIncrementInSeconds time has not yet elapsed.
	<br><br>
	The InputStream contains all bytes downloaded so far, returned by InputStream.available().
	If you wait until later calls of update() or done(), that same InputStream will have more bytes.
	*/
	public void update(String fileNameOrURL, GrowingByteArrayInputStream bytes, double secondsSoFar);

	/** when the download is done, all the bytes are returned,
	for efficiency it should be the same byte[] array from the InputStream in update()
	*/
	public void done(String fileNameOrURL, byte bytes[]);

	/** if the download ends with some error */
	public void failed(String fileNameOrURL, Throwable error);

}
