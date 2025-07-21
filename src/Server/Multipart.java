package Server;

public class Multipart {
    private final String filename;
    private final String contentType;
    private final byte[] content;

    public Multipart(String filename, String contentType, byte[] content){
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
    }

    public String getFilename() {return this.filename;}
    public String getContentType() {return this.contentType;}
    public byte[] getContent() {return this.content;}
}
