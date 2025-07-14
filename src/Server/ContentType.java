package Server;

public enum ContentType {
    TEXT_HTML;

    public static ContentType confirm(String input) {
        try {
            return ContentType.valueOf(input);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status Code Not Found! " + input);
        }
    }
}
