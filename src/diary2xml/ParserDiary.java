/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diary2xml;

import utils.Log;
import utils.XML;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.HtmlUnitDriver;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

/**
 *
 * @author dcrm
 */
public class ParserDiary implements Runnable {

    private String host = "http://www.diary.ru/options/";
    private String hostPostfix = "/?order=frombegin&from=";
    private final int stepFrom = 20;
    private int startFrom;
    private int maxFrom;
    private int currentFrom;
    private Thread thread;
    private WebClient webClient;

    private HashMap<String, DiaryPost> posts;

    public ParserDiary() {
        this.posts = new HashMap<>();
    }

    public void init() {
        this.startFrom = Diary2xml.from;
        thread = new Thread(this);
        thread.setName("ParserDiary");
        thread.start();
    }

    private void auth() {
        try {
            HtmlPage htmlAuthPage = webClient.getPage(host);

            List<HtmlForm> htmlForm = htmlAuthPage.getForms();
            htmlForm.get(0).getInputByName("user_login").setValueAttribute(Diary2xml.login);
            htmlForm.get(0).getInputByName("user_pass").setValueAttribute(Diary2xml.pwd);

            Log.msg("Try auth");
            HtmlPage htmlPage1 = htmlForm.get(0).getInputByValue("Войти").click();
            List<HtmlAnchor> byXPath = htmlPage1.getByXPath("//*[@id=\"m_menu\"]/li[1]/a");

            //Log.err(byXPath);
            if (byXPath.size() == 0) {
                Log.err("Auth fail");
                thread.interrupt();
                return;
            }
            if (Diary2xml.getUrl() == "") {
                Diary2xml.setUrl(byXPath.get(0).getHrefAttribute());
                Log.msg("Set diary URL = " + Diary2xml.getUrl());
            }
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }

    private int getMaxPostPage(String urlPage) {
        int maxPage = 0;
        try {
            Log.out("Start find max page posts [" + urlPage + "]");
            HtmlPage htmlPage = webClient.getPage(urlPage);

            List<HtmlAnchor> navLinks = htmlPage.getByXPath("//*[@id=\"pageBar\"]//a");
            String strMaxPage = navLinks.get(navLinks.size() - 1).getHrefAttribute().replaceAll("(.*)from\\=", "");
            maxPage = Integer.parseInt(strMaxPage);
        } catch (Exception ex) {
            maxPage = 0;
            Log.err(ex.getLocalizedMessage());
        }
        Log.msg("MAX page = " + maxPage);
        return maxPage;
    }

    public void load(String urlFrom) throws IOException {
        //if (++level>2) return null;
        Log.out("Start parse [" + urlFrom + "]");

        HtmlPage htmlPage = webClient.getPage(urlFrom);

        List<HtmlDivision> singlePosts = htmlPage.getByXPath("//*[@id=\"postsArea\"]/div[contains(@class,\"singlePost\")]");
        Log.out("Found [" + singlePosts.size() + "] posts");
        String prevDate = "";
        for (HtmlDivision postBody : singlePosts) {
            try {
                prevDate = this.parse(postBody, prevDate);
            } catch (Exception e) {
                Log.err("ERR: ["+postBody.getId()+"] "+e.getLocalizedMessage());
            }
        }
    }

    public String parse(HtmlElement postBody, String prevDate) throws IOException {
        /*String rawHtmlPage = webClient.getWebResponse().getContentAsString();
            Pattern p = Pattern.compile("<a name='more([0-9]+)m1start'></a>(.*)<a name='more([0-9]+)m1end'>");
            Matcher m = p.matcher(rawHtmlPage);
            while(m.find()) {
                Log.out(m.group(1));
            }*/
        
        /*if ("post202466951".equals(postBody.getId())) {
            Log.out(postBody.getId());
        }*/
        //Log.out(postBody.getId());
        List<HtmlAnchor> commentCountHref = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//span[@class=\"comments_count_link\"]/a"); //commentCountHref.get(0)
        List<HtmlAnchor> postHref = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//span[@class=\"urlLink\"]/a");
        List<HtmlElement> postTitle = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//h2");
        List<HtmlElement> postTime = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]/div[2]/span");
        //Log.out("//*[@id=\""+postBody.getId()+"\"]/div[2]/span");
        List<HtmlElement> postDate = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]/div[1]/span");
        List<HtmlElement> postTags = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//p[@class=\"atTag\"]/em/a");
        List<HtmlElement> postContent = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//div[@class=\"paragraph\"]/div");
        List<HtmlElement> postHidden = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//h2/img");
        List<HtmlAnchor> postMore = postBody.getByXPath("//*[@id=\"" + postBody.getId() + "\"]//a[@class=\"LinkMore\"]");

        List<HtmlDivision> singleComments = postBody.getByXPath("//*[@id=\"commentsArea\"]/div[contains(@class,\"singleComment\")]");

        /*for (Iterator<HtmlElement> iteratorInputElement = postMore.iterator(); iteratorInputElement.hasNext();) {
                HtmlElement inputElement = iteratorInputElement.next();
                Log.out(inputElement.asXml());
            }*/
        boolean isHidden = (!postHidden.isEmpty());

        // Заполняем дату "следующего" поста, т.к. оптимизация
        String date = (postDate.isEmpty() ? "" : postDate.get(0).getTextContent());
        String time = (postTime.isEmpty() ? "" : postTime.get(0).getTextContent());

        if (prevDate != "" && time == "") {
            time = date;
            date = prevDate;
        } else {
            prevDate = date;
        }

        String postUrl = (!postHref.isEmpty())?postHref.get(0).getHrefAttribute():"";

        List<DiaryComment> comments = new ArrayList<>();
        for (HtmlDivision comment : singleComments) {
            List<HtmlElement> authorNameElement = comment.getByXPath("//*[@id=\"" + comment.getId() + "\"]/div[@class=\"authorName\"]//strong");

            List<HtmlAnchor> authorHrefDiaryElement = comment.getByXPath("//*[@id=\"" + comment.getId() + "\"]//li[contains(@class,\"diary\")]/a");
            List<HtmlAnchor> authorHrefProfileElement = comment.getByXPath("//*[@id=\"" + comment.getId() + "\"]//li[contains(@class,\"profile\")]/a");
            List<HtmlElement> authorDateElement = comment.getByXPath("//*[@id=\"" + comment.getId() + "\"]//div[contains(@class,\"postTitle\")]/span");
            
            //postLinks
            List<HtmlElement> authorContentElement = comment.getByXPath("//*[@id=\"" + comment.getId() + "\"]//div[@class=\"paragraph\"]/div");
            grabImage(authorContentElement.get(0));
            comments.add(new DiaryComment(authorNameElement.get(0).asText(), authorDateElement.get(0).asText(),
                    (!authorHrefDiaryElement.isEmpty() ? authorHrefDiaryElement.get(0).getHrefAttribute()
                    : (!authorHrefProfileElement.isEmpty() ? authorHrefProfileElement.get(0).getHrefAttribute() : "")),
                    authorContentElement.get(0).asXml()));
        }

        DiaryPost diaryPost = new DiaryPost(
                postBody.getId(),
                isHidden,
                postUrl,
                postTitle.get(0).getTextContent(),
                date,
                time,
                postTags,
                postContent.get(0),
                comments
        );

        boolean needGoInside = false;
        if (commentCountHref.isEmpty()) {
            Log.out(postBody.getId() + " 0 comments");
        } else {
            Log.out("Found " + commentCountHref.get(0).getTextContent() + " comments, go inside");
            needGoInside = true;
            //postUrl = commentCountHref.get(0).getHrefAttribute();
        }

        if (!postMore.isEmpty()) {
            Log.out(postBody.getId() + " found MORE link, go inside");
            needGoInside = true;
            postUrl = postMore.get(0).getHrefAttribute();
        }

        if (needGoInside) {
            this.load(postUrl); //level
        } else {
            this.addPost(diaryPost);
        }
        return prevDate;
        //Log.out(commentCountHref.get(0).getTextContent());
    }

    public HashMap<String, DiaryPost> getPosts() {
        //return (HashMap<String, DiaryPost>) this.posts.clone();
        return this.posts;
    }

    public void clearPosts() {
        this.posts.clear();
    }

    public void addPost(DiaryPost post) {
        if (this.posts.containsKey(post.id)) {
            Log.out(post.id + " updated");
            this.posts.replace(post.id, post);
        } else {
            Log.out(post.id + " add");
            this.posts.put(post.id, post);
        }
    }

    @Override
    public void run() {
        try {
            Log.msg("Site loading");
            webClient = new WebClient(BrowserVersion.CHROME);

            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setAppletEnabled(false);

            //webClient.set
            WebConnectionWrapper webConnectionWrapper = new WebConnectionWrapper(webClient);

            this.auth();
            
            if (Diary2xml.getUrl() == "") {
                throw new Exception("URL personal diary not found");
            }

            this.currentFrom = Integer.MAX_VALUE;
            String urlFrom = Diary2xml.getUrl() + hostPostfix + this.currentFrom;
            this.maxFrom = this.getMaxPostPage(urlFrom);

            this.currentFrom = this.startFrom;
            while (this.currentFrom <= this.maxFrom) {
                Log.msg("Page: [" + this.currentFrom + "/" + this.maxFrom + "]");
                urlFrom = Diary2xml.getUrl() + hostPostfix + this.currentFrom;
                this.load(urlFrom);

                XML.saveXml(this.getPosts(), this.currentFrom);
                this.clearPosts();

                this.currentFrom += this.stepFrom;
                Diary2xml.saveConfig(this.currentFrom);
            }

            Log.out("");
        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }

    public static String removeDiv(String str) {
        return clearText(str.replaceAll("[<](/)?div[^>]*[>]", ""));
    }

    public static String clearText(String str) {
        String ret = str.replaceAll("\\s+", " ").replaceAll(" ", " ").trim();
        return ret;
    }

    public static void grabImage(HtmlElement content) {
        DomNodeList<HtmlElement> images = content.getElementsByTagName("img");
        if (!images.isEmpty()) {
            for (Iterator<HtmlElement> iterator = images.iterator(); iterator.hasNext();) {
                try {
                    HtmlElement image = iterator.next();
                    HtmlImage htmlImage = (HtmlImage) image;
                    String origUrl = htmlImage.getSrcAttribute();
                    String ext = Diary2xml.getStringFileExt(origUrl);
                    String fileName = Diary2xml.getMD5(origUrl) + "." + ext;
                    File imageObject = new File(Diary2xml.LOCATION_IMAGE + fileName);

                    if (!imageObject.exists()) {
                        Log.out("Save [" + origUrl + "] -> " + Diary2xml.SERVER_IMAGE + fileName);
                        boolean imgOK = true;

                        try {
                            htmlImage.saveAs(imageObject);
                            if (!imageObject.exists()) { // Нельзя понять что картинка не сохранилась по таймауту
                                imgOK = false;
                            }
                        } catch (VerifyError verr) {
                            Log.err(verr.getMessage()); // 404
                            imgOK = false;
                        }
                        
                        // Дополнительная проверка по типу файла
                        if (imgOK) {
                            try {
                                MagicMatch match = Magic.getMagicMatch(imageObject, false);
                                //System.out.println(match.getMimeType());
                                if ("text/html".equals(match.getMimeType())) {
                                    imgOK = false;
                                }
                                if (ext.equals("blob")) {
                                    imgOK = false;
                                }
                            } catch (NoClassDefFoundError | MagicException | MagicMatchNotFoundException | MagicParseException cerr) {
                                Log.err(cerr.getMessage());
                                imgOK = false;
                            }
                        }

                        if (!imgOK) {
                            imageObject.delete();

                            Log.out("Not found image [" + origUrl + "] -> " + Diary2xml.SERVER_IMAGE + "404.png");
                            image.setAttribute("src", Diary2xml.SERVER_IMAGE + "404.png");

                            //iterator.remove();
                            //continue;
                        } else {
                            image.setAttribute("src", Diary2xml.SERVER_IMAGE + fileName);
                        }
                    } else {
                        image.setAttribute("src", Diary2xml.SERVER_IMAGE + fileName);
                    }
                    
                    image.removeAttribute("onload");
                } catch (IOException ex) {
                    Log.err(ex.getLocalizedMessage());
                }
            }
        }
    }
}
