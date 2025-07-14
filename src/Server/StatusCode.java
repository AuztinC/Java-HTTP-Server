package Server;

public enum StatusCode {
    OK;

    public static StatusCode confirm(String input) {
        try {
            return StatusCode.valueOf(input);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status Code Not Found! " + input);
        }
    }

}
