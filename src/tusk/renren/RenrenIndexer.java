package tusk.renren;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

public class RenrenIndexer {
	
    static IndexWriter iwriter;
    
    public static String delHTMLTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //����script��������ʽ 
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //����style��������ʽ 
        String regEx_html="<[^>]+>"; //����HTML��ǩ��������ʽ 
         
        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script=p_script.matcher(htmlStr); 
        htmlStr=m_script.replaceAll(""); //����script��ǩ 
         
        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style=p_style.matcher(htmlStr); 
        htmlStr=m_style.replaceAll(""); //����style��ǩ 
         
        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html=p_html.matcher(htmlStr); 
        htmlStr=m_html.replaceAll(""); //����html��ǩ 

        return htmlStr.trim(); //�����ı��ַ��� 
    } 
    
	public static void main(String[] args) throws Exception
	{
		Analyzer analyzer = new SmartChineseAnalyzer();
	    // To store an index on disk, use this instead:
	    Directory directory = FSDirectory.open(new File("index/news").toPath());
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    iwriter = new IndexWriter(directory, config);
	    Scanner fi = new Scanner(new FileInputStream("../data/renren/data.txt"));
	    int no = 0;
	    while(fi.hasNextLine())
	    {
	    	no += 1;
	    	System.out.println(no);
	    	String l = fi.nextLine();
	    	String[] s = l.split(",");
	    	s[0] = delHTMLTag(s[0]);
	    	Document doc = new Document();
		    doc.add(new Field("content", delHTMLTag(s[1]), Store.YES, Index.ANALYZED));
		    doc.add(new Field("url", s[1], Store.YES, Index.NO));
		    doc.add(new Field("title", s[0], Store.YES, Index.ANALYZED));
		    doc.add(new Field("source", "������ - " + s[2], Store.YES, Index.NO));
		    iwriter.addDocument(doc);
	    }
	    iwriter.close();
	}

}
