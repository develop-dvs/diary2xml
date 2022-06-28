package diary2xml;

import utils.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author dcrm
 */
public class DiaryComment {
    
    public String authorName; // 
    public String authorHref;
    //public String authorPhoto;
    public String text;
    public String date;
    public String datetime;
    public String timestamp;

    public DiaryComment(String authorName, String authorHref, String text, String date, String datetime, String timestamp) {
        this.authorName = authorName;
        this.authorHref = authorHref;
        this.text = text;
        this.date = date;
        this.datetime = datetime;
        this.timestamp = timestamp;
    }

    public DiaryComment(String authorName, String date, String authorHref, String text) {
        this.authorName = authorName;
        this.authorHref = authorHref;
        this.date = date;
        this.text = ParserDiary.removeDiv(text);
        this.setDatetime(date);
        this.authorName = this.replaceNameAdmin(this.authorName);
        this.text = this.replaceNameAdmin(this.text);
    }
    
    private String replaceNameAdmin(String str) {
        return str.replace("& [DELETED user]", "").replace("[DELETED user]", "").replace("& [DELETED USER]", "").replace("[DELETED USER]", "").trim();
    }
    
    private void setDatetime(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd в HH:mm");
            DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            Date parse = format.parse(date);
            this.datetime = dtFormat.format(parse);
            this.timestamp = Long.toString(parse.getTime());
        } catch (ParseException ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }
}