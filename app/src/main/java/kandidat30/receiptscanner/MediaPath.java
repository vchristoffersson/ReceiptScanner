package kandidat30.receiptscanner;

import android.graphics.Bitmap;

public class MediaPath {

    private String path;
    private byte[] data;
    private Bitmap image;
    private String name;

    public MediaPath(String path, byte[] data, String name){
        this.path = path;
        this.data = data;
        this.name = name;
    }

    public MediaPath(String path, Bitmap image, String name){
        this.path = path;
        this.image = image;
        this.name = name;
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

    public String getName() {
        return name;
    }
}
