/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diary2xml;

import utils.Log;
import utils.Translit;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author dcrm
 */
public class DiaryPost implements Comparable<DiaryPost> {

    public static final int titleExplode = 4;

    public String id;
    public boolean hide = false;
    public String url;

    public String title;
    public String date;
    public String time;
    public List<DiaryTag> tags;
    private HtmlElement body;
    public List<DiaryComment> comments;

    public String code;
    public String translit;

    public String datetime;
    public String datetimemini;
    public String timestamp;

    public String preview;
    public String detail;

    public DiaryPost(String id, boolean hide, String url, String title, String date, String time, String code, String translit, String datetime, String datetimemini, String timestamp) {
        this.id = id;
        this.hide = hide;
        this.url = url;
        this.title = title;
        this.date = date;
        this.time = time;
        this.code = code;
        this.translit = translit;
        this.datetime = datetime;
        this.datetimemini = datetimemini;
        this.timestamp = timestamp;
    }

    public DiaryPost(String id, boolean isHidden, String url, String title, String date, String time, List<HtmlElement> tags, HtmlElement body, List<DiaryComment> comments) {
        this.id = id;
        this.url = url;
        this.hide = isHidden;

        this.title = title;
        this.body = body;

        this.date = date;
        this.time = time;

        this.comments = comments;

        ParserDiary.grabImage(this.body);

        this.splitText(this.body);
        this.setDatetime(this.date, this.time);

        this.setTitle();

        this.setCode(this.title);
        this.initTags(tags);

        //Log.out(this.tags);
        //this.tags = tags;
    }

    private void initTags(List<HtmlElement> tags) {
        this.tags = new ArrayList<>();
        for (Iterator<HtmlElement> iterator = tags.iterator(); iterator.hasNext();) {
            HtmlElement tag = iterator.next();
            this.tags.add(new DiaryTag(tag.getAttribute("href").replaceAll("(.*)tag\\=", ""), tag.getTextContent()));
        }
    }

    private void setTitle() {
        if ("".equals(this.title)) {
            String[] text = this.preview.split("\\.");
            if (text.length > 1) {
                String[] first = text[0].split(" ");

                String[] onlyTitle = Arrays.copyOfRange(first, 0, (first.length > DiaryPost.titleExplode) ? DiaryPost.titleExplode : first.length);
                this.title = String.join(" ", onlyTitle);
            }
        }
        //this.title = ("".equals(this.title) ? this.code : this.title);
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setComments(List<DiaryComment> comments) {
        this.comments = comments;
    }

    public void setTags(List<DiaryTag> tags) {
        this.tags = tags;
    }

    private void splitText(HtmlElement content) {
        String text = ParserDiary.removeDiv(content.asXml());
        //Log.out(text);

        if (text.indexOf("<a name=\"more1\">") > 0) {
            text = text.replaceFirst("<a name=\"more1\">", "#REPLACER#<a name=\"more1\">");

            text = Pattern.compile("<a name=\"more([0-9]+)(end)?\">(.*?)<\\/a>", Pattern.DOTALL).matcher(text).replaceAll("");

            String[] explodeText = text.split("#REPLACER#");
            if (explodeText.length == 2) {
                this.preview = explodeText[0].trim();
                this.detail = explodeText[1].trim();
            } else {
                this.preview = this.detail = explodeText[0].trim();
            }
        } else {
            this.preview = this.detail = text.trim();
        }
    }

    private void setDatetime(String date, String time) {
        try {
            String onlyTime = time;

            Locale ru = new Locale("ru");
            String[] weekday = {"", "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
            String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
            DateFormatSymbols symbols = DateFormatSymbols.getInstance(ru);
            symbols.setMonths(months);
            symbols.setWeekdays(weekday);

            SimpleDateFormat format = new SimpleDateFormat("EEEEEE, dd MMM yyyy, HH:mm", ru);
            DateFormat dtFormatMini = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            format.setDateFormatSymbols(symbols);

            /*String[] title = time.split(" "); // WTF
            if (title.length > 1) {
                onlyTime = title[0];
                String[] onlyTitle = Arrays.copyOfRange(title, 1, title.length);
                this.title = String.join(" ", onlyTitle);
            }*/
            Date parse = format.parse(date + ", " + onlyTime);
            this.datetime = dtFormat.format(parse);
            this.datetimemini = dtFormatMini.format(parse);

            this.timestamp = Long.toString(parse.getTime());
        } catch (ParseException ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }

    private void setCode(String title) {
        String tTitle = Translit.toTranslit(title.toLowerCase()).replace(" ", "_").replaceAll("\\W|^_", "").replace("_", "-");
        this.translit = tTitle;
        this.code = (this.translit.isEmpty() ? this.datetime : this.translit);
    }

    @Override
    public int compareTo(DiaryPost o) {
        if (this.timestamp.compareTo(o.timestamp) > 0) {
            return 1;
        } else if (this.timestamp.compareTo(o.timestamp) < 0) {
            return -1;
        } else {
            return 0;
        }
    }

}
