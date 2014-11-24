package prince.app.sphotos.tools;


public class Albums {
	/** Unique ID of the Album */
	public String mAlbumID;  	
	
	/** ID of Owner of the Album */
	public String mAlbumOwnerID;
	
	/** Name of the Owner */
	public String mAlbumOwnerName;		    
	
	/** Album name */
	public String mAlbumName;
	
	/** Album cover photo ID */
	public String mAlbumCoverPhotoID;	    
	
	/** Number of Images in the Album */
	public int mAlbumSize;			
	
	/** Type of Album */
	public String mAlbumType;
	
	/** Location of the album */
	public String mAlbumLocation;
	
	/** Time created */
	public String mAlbumCT;					
	
	/** Time Updated */
	public String mAlbumUT;					
	
	/** Privacy */
	public String mAlbumPrivacy;
	
	/** Local album Path */
	public String mAlbumPath;				
	
	/** Album Cover URL */
	public String mAlbumCoverURL = "";
	
	/** Can we upload */
	public boolean mAlbumUpload;
}