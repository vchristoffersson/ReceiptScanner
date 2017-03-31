package kandidat30.receiptscanner;

import android.graphics.Bitmap;

public class MediaPath {

    private String path;
    private byte[] data;
    private Bitmap image;

    public MediaPath(String path, byte[] data){
        this.path = path;
        this.data = data;
    }

    public MediaPath(String path, Bitmap image){
        this.path = path;
        this.image = image;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    public Bitmap getImage() {
        return image;
    }
}
