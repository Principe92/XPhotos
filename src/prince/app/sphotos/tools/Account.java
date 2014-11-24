package prince.app.sphotos.tools;

public class Account {
	private int aImageId;
    private String aTitle;
 
    public Account(int imageIds, String titles) {
        this.aImageId = imageIds;
        this.aTitle = titles;
    }
    public int getImageId() {
        return aImageId;
    }
    public void setImageId(int imageId) {
        this.aImageId = imageId;
    }
    public String getTitle() {
        return aTitle;
    }
    public void setTitle(String title) {
        this.aTitle = title;
    }
}
