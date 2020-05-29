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
    public static String parsePic(Elements contentDivs){
        String pics = "";
        return pics;
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
//        int modelIndex = 1;
        TreeMap<String, TreeMap> modelTreeMap = new TreeMap<String, TreeMap>();
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
//                                    System.out.println(((Element) bodyElment3).text());
//                                    System.out.println("________________________");
                                    String text = ((Element) bodyElment3).text();
                                    System.out.println(text);
                                }
                            }
                        }
                    }
                }
            }

//        int len = bodyChilds.size();
//        for(int i=0;i<len;i++){
//            System.out.println(bodyChilds.get(i).getClass().toString());
//        }

//            Elements contentDivs = bodyElement.getElementsByTag("div");
//            // 解析层 存入model  key为model名字
//            TreeMap<String, String> modelValue = new TreeMap<String, String>();
//            modelValue.put("Context", parseText(contentDivs));
//            modelValue.put("Pics",parsePic(contentDivs));
////            modelTreeMap.put(contentDivs.get)
//        }

        //图片
        StringBuilder srcs = new StringBuilder();
        int pic_cnt = 0;
        for (Element element : doc.getAllElements()) {
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

        //超链接
        StringBuilder hrefs = new StringBuilder();
        int href_cnt = 0;
        for (Element element : doc.getElementsByAttribute("href")) {
            String href = element.attr("href");
            if (href.contains("http")
                    && !href.contains(".css")
                    && !href.contains(".js")
                    && !href.contains(".png")
                    && !href.contains(".jpg")
                    && !href.contains(".mp4")) {
                hrefs.append(href + ";");
                href_cnt++;
            }
        }

        //标题
        StringBuilder titles = new StringBuilder();
        TreeMap<Integer, String> title_treeMap = new TreeMap<Integer, String>();
        int index = 0;
        for (Element element : doc.getElementsByAttributeValueContaining("class", "title")) {
            String text = element.text().trim();
            if (text.length() > 0) {
//                titles.append(text+"䀠䀠"+element.parents().size()+"儑儑");
                // 该处匹配需优化，要匹配到title
                index = total.indexOf(text,index);
                title_treeMap.put(index, text + "䀠䀠" + element.parents().size() + "儑儑");
            }

        }
//        String hs[] = new String[]{"h1", "h2", "h3", "h4", "h5"};
//        for (String h : hs) {
//            for (Element element : doc.select(h)) {
//                String text = element.text().trim();
//                if (text.length() > 0) {
////                    titles.append(text+"䀠䀠"+element.parents().size()+"儑儑");
//                    title_treeMap.put(total.indexOf(text), text + "䀠䀠" + element.parents().size() + "儑儑");
//                }
//
//            }
//        }

        for (Map.Entry<Integer, String> entry : title_treeMap.entrySet()) {
            titles.append(entry.getValue());
        }

        // title下的文本解析
        // 解析每两个title之间的文本，图片
        // 结果样式 map {评论:{文本:文本内容,图片:图片内容}} 双层map
        // 函数传入该模块的html内容，进行文本解析和图片即可
        // 起始位置存储在 title_treeMap中 （key是模块开始位置，value是title名字），获取当前key和下一个key，可以直接把key存入一个列表中

        // txt直接抽取total之间的内容即可
        // 图片

        // 识别模块，识别模块之下的文本和



        try {
            jsonObject.put("text", total.trim());
            jsonObject.put("title", title.trim());
//            if (StringUtils.isNotEmpty(jsonObject.getString("keywords"))) {
//                score += 10;
//            }
//            if (StringUtils.isNotEmpty(jsonObject.getString("description"))) {
//                score += 10;
//            }
            if (srcs.length() > 0) {
                jsonObject.put("pics", srcs.deleteCharAt(srcs.length() - 1).toString());
                score += 10;
                score += pic_cnt / 10;

            }
            if (hrefs.length() > 0) {
                jsonObject.put("hrefs", hrefs.deleteCharAt(hrefs.length() - 1).toString());
                score += 10;
                score += href_cnt / 10;
            }
            if (titles.length() > 0) {
                jsonObject.put("subtitles", titles.deleteCharAt(titles.length() - 1).deleteCharAt(titles.length() - 1).toString());
            }
//            if (StringUtils.isNotEmpty(jsonObject.getString("text"))) {
//                score += 10;
//            }
//
//            if (StringUtils.isNotEmpty(jsonObject.getString("title"))) {
//                score += 10;
//            }
            jsonObject.put("score", score);
        } catch (com.alibaba.fastjson.JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
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
