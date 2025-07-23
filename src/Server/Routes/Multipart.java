package Server.Routes;

public record Multipart(String filename, String contentType, byte[] content) {
}
