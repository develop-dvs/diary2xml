package utils;

/**
 *
 * @author dcrm
 */
public class Log {
    public static boolean debug = true;
    
    
    public static void msg(String str) {
        System.out.println(str);
    }
    public static void err(String str) {
        System.err.println(str);
    }
    public static void out(String str) {
        if (debug) {
            msg(str);
        }
    }
}
