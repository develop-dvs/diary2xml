package diary2xml;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.MessageDigest;
import org.ini4j.Wini;
import utils.Log;
import xml2cms.Bitrix;
import xml2cms.WP;

/**
 * @author dcrm
 */
public class Diary2xml {

    public static String LOCATION = "./";
    public static String LOCATION_IMAGE = "archive-images"; // --
    public static String SERVER_IMAGE = "/archive-images/";
    public static String SAVE_DIR = "/result/";
    public static String CMS_DIR = "/cms/";
    public static String login = "";
    public static String pwd = "";
    public static int from = 0;
    private static String url = "";
    public static ParserDiary parser;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Diary2xml.LOCATION = Diary2xml.getFileLocation(Diary2xml.class.getProtectionDomain().getCodeSource());
        
        Diary2xml.SAVE_DIR = Diary2xml.LOCATION + "parser/";
        initDir(Diary2xml.SAVE_DIR);
                
        switch (args.length) {
            case 5:
                Log.debug = true;
            case 4:
                from = Integer.parseInt(args[3]);
            case 3:
                setUrl(args[2]);
            case 2:
                login = args[0];
                pwd = args[1];

                Diary2xml.LOCATION_IMAGE = Diary2xml.SAVE_DIR + Diary2xml.SERVER_IMAGE;
                initDir(Diary2xml.LOCATION_IMAGE);

                if (args.length < 3) {
                    loadConfig();
                }

                parser = new ParserDiary();
                parser.init();
                break;
            case 1:
                Log.msg("XML2CMS");               
                CMS_DIR = SAVE_DIR + "cms/";
                initDir(Diary2xml.CMS_DIR);
                
                
                switch (args[0]) {
                    default:
                    Log.msg("CMS = wp/bitrix");
                    break;
                    
                    case "wp":
                        new WP();
                    break;
                    
                    case "bitrix":
                        new Bitrix();
                    break;
                }
                break;
            default:
                Log.msg("Arguments: LOGIN PWD URL FROM / CMS[wp]");
                break;
        }
    }

    public static void saveConfig(int nFrom) {
        try {
            File file = new File(SAVE_DIR + "config.ini");
            file.createNewFile();
            Wini ini = new Wini(file);
            ini.put("main", "from", nFrom);
            ini.store();
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }

    public static void loadConfig() {
        try {
            Wini ini = new Wini(new File(SAVE_DIR + "config.ini"));
            from = ini.fetch("main", "from", int.class);
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
            from = 0;
        }
    }

    public static void initDir(String dirPath) {
        try {
            Log.msg(dirPath);
            File dir = new File(dirPath);
            dir.mkdirs();
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }

    public static String getFileLocation(CodeSource codeSource) {
        try {
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            return jarFile.getParentFile().getPath() + "/";
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
            return "./";
        }

    }

    public static String getStringFileExt(String fileName) {
        
        /*if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";*/
        
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        if (extension.length()>4) return "blob";
        return extension;
    }

    public static String getMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(str));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
        return "";
    }

    public static void setUrl(String url) {
        Diary2xml.url = "http://" + url;
    }

    public static String getUrl() {
        return Diary2xml.url;
    }
}