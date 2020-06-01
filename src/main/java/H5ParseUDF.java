import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.io.FileInputStream;


public class H5ParseUDF {
    public static final Pattern pattern = Pattern.compile("\"(.*)\\\\\"");
    public static final Set<String> meta_set = new HashSet<String>();

    static {
        meta_set.add("keyword");
        meta_set.add("description");
    }

    public String evaluate(String t1) {
        if (t1 == null || t1.length() < 1) {
            return "";
        }

        String s = t1.replaceAll("儑", "=");
        try {
            String re = parseDetails(s);
            return re;
        } catch (Exception e) {
            return "";
        }
    }
    public  static String parseText(Elements contentDivs){
        String text = "";
        return text;
    }
    public static StringBuilder parsePic(Element contentDivs){
        StringBuilder srcs = new StringBuilder();
        int pic_cnt = 0;
        for (Element element : contentDivs.getAllElements()) {
            if ("img".equalsIgnoreCase(element.tag().getName())) {
                String src = element.attr("src");
                if (src.contains("p1.meituan.net")
                        || src.contains("p0.meituan.com")
                        || src.contains("p0.meituan.net")
                        || src.contains("p1.meituan.net")
                        || src.contains("p1.meituan.com")
                        || src.contains("vfile.meituan.net")
                        || src.contains("osp.meituan.net")
                        || src.contains("img.meituan.net")) {
                    if (src != null && !"".equalsIgnoreCase(src) && src.contains("http")) {
                        if (src.contains("\"")) {
                            Matcher matcher = pattern.matcher(src);
                            if (matcher.find(1)) {
                                srcs.append(matcher.group(1) + ";");
                                pic_cnt++;
                            }
                        } else {
                            srcs.append(src + ";");
                            pic_cnt++;
                        }
                    } else if (src != null && !"".equalsIgnoreCase(src)) {
                        srcs.append("https:" + src + ";");
                    }
                }
            } else if (element.attr("style").contains("background-image") || (element.attr("style").contains("background") && element.attr("style").contains("url"))) {
                String attr = element.attr("style");
                if (attr.contains("p1.meituan.net")
                        || attr.contains("p0.meituan.com")
                        || attr.contains("p0.meituan.net")
                        || attr.contains("p1.meituan.net")
                        || attr.contains("p1.meituan.com")
                        || attr.contains("vfile.meituan.net")
                        || attr.contains("osp.meituan.net")
                        || attr.contains("img.meituan.net")) {
                    if (attr.contains("&quot;")) {
                        if (attr.contains("https")) {
                            srcs.append(attr.substring(attr.indexOf("https://"), attr.indexOf("&quot;")) + ";");
                            pic_cnt++;

                        } else if (attr.contains("http")) {
                            srcs.append(attr.substring(attr.indexOf("http://"), attr.indexOf("&quot;")) + ";");
                            pic_cnt++;

                        }
                    } else {
                        if (attr.contains("https")) {
                            srcs.append(attr.substring(attr.indexOf("https://"), attr.indexOf(")")).replaceAll("\"", "") + ";");
                            pic_cnt++;

                        } else if (attr.contains("http")) {
                            srcs.append(attr.substring(attr.indexOf("http://"), attr.indexOf(")")).replaceAll("\"", "") + ";");
                            pic_cnt++;
                        }

                    }
                }

            }
        }
        return srcs;
    }

    public static String parseDetails(String str) {
        double score = 0.0;
        Pattern pattern = Pattern.compile("\"(.*)\\\\\"");
        Document doc = Jsoup.parse(str);
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        //文本
        String total = doc.text();

        //head
        String title = doc.getElementsByTag("head").first().getElementsByTag("title").text();
        Elements metas = doc.getElementsByTag("head").first().getElementsByTag("meta");
        for (Element element : metas) {
            if (meta_set.contains(element.attr("name"))) {
                jsonObject.put(element.attr("name"), element.attr("content"));
            }
        }

        // body
        JSONArray treeMap = new JSONArray();
        Integer id = 0;
        List<Node> bodyChilds =  doc.getElementsByTag("body").get(0).childNodes();
        // 解析第一层 取到content
        for(Node bodyElment1 : bodyChilds){ // body儿子
            // 解析body 中的div
            if(bodyElment1.getClass().toString().equals("class org.jsoup.nodes.Element") && ((Element) bodyElment1).tag().toString().equals("div")) {
                for(Node bodyElment2: bodyElment1.childNodes()){ // body孙子
                        if(bodyElment2.getClass().toString().equals("class org.jsoup.nodes.Element") && ((Element) bodyElment2).tag().toString().equals("div")){
                            for(Node bodyElment3: bodyElment2.childNodes()){
                                if(bodyElment3.getClass().toString().equals("class org.jsoup.nodes.Element") && ((Element) bodyElment3).tag().toString().equals("div")){
                                    // 解析 bodyElment2 中的text和图片
//                                    System.out.println("________________________");
                                    String text = ((Element) bodyElment3).text();
//                                    System.out.println(text);
//                                    System.out.println(parsePic((Element) bodyElment3));
                                    JSONObject object = new JSONObject();
                                    object.put("context",text);
                                    object.put("pics",parsePic((Element) bodyElment3));
                                    treeMap.add(object);
                                }
                            }
                        }
                    }
                }
            }
        return treeMap.toString();
    }


    public static void main(String[] args) throws IOException {
//        FileReader fileReader = new FileReader("context.html");
        File f = new File("/Users/xuyonghui/IdeaProjects/H5Parse/src/main/java/index.html");
        // 输入流
        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");

        BufferedReader br = new BufferedReader(fileReader);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while((line=br.readLine())!= null){
            sb.append(line);
        }
        String re = sb.toString().replaceAll("儑","=").replaceAll("\t","").replaceAll("\t","");
        System.out.println(parseDetails(re));
    }
}
