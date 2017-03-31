package kandidat30.receiptscanner;

public class MediaPath {

    private String path;
    private byte[] data;

    public MediaPath(String path, byte[] data){
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }
}
