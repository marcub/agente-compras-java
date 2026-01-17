package br.com.marcusferraz.agentecompras.utils;

public class Base62Utils {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();

    public static String encode(long value) {
        StringBuilder sb = new StringBuilder();
        do {
            int remainder = (int)(value % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            value /= BASE;
        } while (value > 0);
        return sb.reverse().toString();
    }

    public static long decode(String str) {
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            result = result * BASE + BASE62_CHARS.indexOf(str.charAt(i));
        }
        return result;
    }
}
