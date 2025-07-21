package Server;

public record Multipart(String filename, String contentType, byte[] content) {
}
