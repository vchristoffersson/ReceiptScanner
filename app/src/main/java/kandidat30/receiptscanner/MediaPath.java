package kandidat30.receiptscanner;

import android.graphics.Bitmap;

/**
 * Class for associating image data with a filepath
 */
public class MediaPath {

    private String path;
    private byte[] data;
    private Bitmap image;
    private String name;

    /**
     * Constructor with image data as byte array
     * @param path path to folder of image
     * @param data image data as byte array
     * @param name name of the image
     */
    public MediaPath(String path, byte[] data, String name){
        this.path = path;
        this.data = data;
        this.name = name;
    }

    /**
     * Constructor with image data as bitmap
     * @param path path to folder of image
     * @param image image data as bitmap
     * @param name name of the image
     */
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
