/** Ben F Rayfield offers this software opensource MIT license */
package immutable.lazycl.impl_old;

import java.util.Map;

import immutable.lazycl.spec.LazyBlob;

/** a blob that is whatever is downloaded from a given url. Maybe its code string should be like
"download:https://upload.wikimedia.org/wikipedia/commons/3/34/Labrador_on_Quantock_%282175262184%29.jpg"
*/
public class DownloadBlob extends AbstractLB{
	
	/*
	//public final String sha3_256_bitlen64_hex;
	
	TODO include a hash like sha3_256_hex_with_bitlen64 (like used in github/benrayfield/simpleblobtable)
	and name it that way, or name it as occamsblob which is a tree hash of that and has 448 bit ids,
	which you could still give url(s) to download it but its immutable in that
	it has to be that content downloaded else it wont be used.
	public final String occamsblob_id;
	
	public final String urls_separated_by_space_or_todo_magneturl;
	
	//TODO
	*/
	
	public DownloadBlob(Map<String,LazyBlob> params){
		super(params);
	}

}