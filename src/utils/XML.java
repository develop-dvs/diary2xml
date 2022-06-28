package utils;


import diary2xml.DiaryComment;
import diary2xml.DiaryPost;
import diary2xml.DiaryTag;
import static diary2xml.Diary2xml.SAVE_DIR;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author dcrm
 */
public class XML {

    public static Iterable<Node> iterable(final NodeList nodeList) {
        return () -> new Iterator<Node>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeList.item(index++);
            }
        };
    }

    public static HashMap<String, DiaryPost> loadXml() {
        HashMap<String, DiaryPost> ret = new HashMap<>();
        try {
            File dir = new File(SAVE_DIR);

            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            });

            for (File file : files) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                NodeList posts = document.getElementsByTagName("post");
                for (Node post : iterable(posts)) {

                    DiaryPost dPost = new DiaryPost(post.getAttributes().getNamedItem("id").getNodeValue(), // ID
                            (post.getAttributes().getNamedItem("hide").getNodeValue().equals("Y")), // Hidden post
                            post.getAttributes().getNamedItem("url").getNodeValue(),
                            post.getAttributes().getNamedItem("title").getNodeValue(),
                            post.getAttributes().getNamedItem("date").getNodeValue(),
                            post.getAttributes().getNamedItem("time").getNodeValue(),
                            post.getAttributes().getNamedItem("code").getNodeValue(),
                            post.getAttributes().getNamedItem("translit").getNodeValue(),
                            post.getAttributes().getNamedItem("datetime").getNodeValue(),
                            post.getAttributes().getNamedItem("datetimemini").getNodeValue(),
                            post.getAttributes().getNamedItem("timestamp").getNodeValue()
                    );
                    NodeList postInsides = post.getChildNodes();
                    for (Node postInside : iterable(postInsides)) {
                        switch (postInside.getNodeName()) {

                            case "preview":
                                dPost.setPreview(postInside.getTextContent());
                                break;

                            case "detail":
                                dPost.setDetail(postInside.getTextContent());
                                break;

                            case "comments":
                                List<DiaryComment> dComments = new ArrayList<>();

                                NodeList comments = postInside.getChildNodes();
                                for (Node comment : iterable(comments)) {
                                    DiaryComment dComment = new DiaryComment(
                                            comment.getAttributes().getNamedItem("authorName").getNodeValue(),
                                            comment.getAttributes().getNamedItem("authorHref").getNodeValue(),
                                            comment.getTextContent(),
                                            comment.getAttributes().getNamedItem("date").getNodeValue(),
                                            comment.getAttributes().getNamedItem("datetime").getNodeValue(),
                                            comment.getAttributes().getNamedItem("timestamp").getNodeValue()
                                    );
                                    dComments.add(dComment);
                                }
                                dPost.setComments(dComments);
                                break;

                            case "tags":
                                List<DiaryTag> dTags = new ArrayList<>();

                                NodeList tags = postInside.getChildNodes();
                                for (Node tag : iterable(tags)) {
                                    DiaryTag dTag = new DiaryTag(
                                            tag.getAttributes().getNamedItem("id").getNodeValue(),
                                            tag.getTextContent()
                                    );
                                    dTags.add(dTag);
                                }
                                dPost.setTags(dTags);
                                break;

                        }

                        /*if (postInside.getNodeType()==Node.ELEMENT_NODE) {
                            Element element = (Element) postInside;
                            //switch (element.getNodeName())
                        }*/
                    }
                    ret.put(dPost.id, dPost);
                }
            }

        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
        Log.msg("Loaded "+ret.size()+" posts.");
                
        return ret;
    }

    public static void saveXml(HashMap<String, DiaryPost> posts, int currentFrom) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("posts");
            doc.appendChild(rootElement);

            for (HashMap.Entry<String, DiaryPost> ePost : posts.entrySet()) {
                Element post = doc.createElement("post");
                rootElement.appendChild(post);
                DiaryPost dPost = ePost.getValue();
                post.setAttribute("id", dPost.id);
                post.setAttribute("hide", (dPost.hide ? "Y" : "N"));
                post.setAttribute("url", dPost.url);
                post.setAttribute("title", dPost.title);

                post.setAttribute("date", dPost.date);
                post.setAttribute("time", dPost.time);

                post.setAttribute("code", dPost.code);
                post.setAttribute("translit", dPost.translit);

                post.setAttribute("datetime", dPost.datetime);
                post.setAttribute("datetimemini", dPost.datetimemini);
                post.setAttribute("timestamp", dPost.timestamp);

                Element preview = doc.createElement("preview");
                preview.setTextContent(dPost.preview);
                post.appendChild(preview);

                Element detail = doc.createElement("detail");
                detail.setTextContent(dPost.detail);
                post.appendChild(detail);

                Element comments = doc.createElement("comments");
                for (DiaryComment eComment : dPost.comments) {
                    Element comment = doc.createElement("comment");
                    comment.setAttribute("authorName", eComment.authorName);
                    comment.setAttribute("authorHref", eComment.authorHref);
                    comment.setTextContent(eComment.text);

                    comment.setAttribute("date", eComment.date);
                    comment.setAttribute("datetime", eComment.datetime);
                    comment.setAttribute("timestamp", eComment.timestamp);

                    comments.appendChild(comment);
                }
                post.appendChild(comments);

                Element tags = doc.createElement("tags");
                for (DiaryTag eTag : dPost.tags) {
                    Element tag = doc.createElement("tag");
                    tag.setAttribute("id", eTag.id);
                    tag.setTextContent(eTag.name);
                    tags.appendChild(tag);
                }
                post.appendChild(tags);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            String curXmlFilePath = SAVE_DIR + "page-" + currentFrom + ".xml";
            File fileXml = new File(curXmlFilePath);
            fileXml.createNewFile();

            StreamResult result = new StreamResult(fileXml);
            transformer.transform(source, result);

            Log.msg("Save XML [" + curXmlFilePath + "]");

        } catch (Exception ex) {
            Log.err(ex.getLocalizedMessage());
        }
    }
}
