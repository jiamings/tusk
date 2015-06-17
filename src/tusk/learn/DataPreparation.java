package tusk.learn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.lang.model.util.Elements;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.mozilla.universalchardet.UniversalDetector;

import static org.junit.Assert.*;

public class DataPreparation {

	static ArrayList<File> flst = new ArrayList<File>();
    
    static int cutLength;
    static String src;
    static FileOutputStream fo;
	
	static void dfs(File path) throws Exception
	{
		File[] F = path.listFiles();
		for(File f : F)
		{
			if(f.isDirectory())
				dfs(f);
			else
				if(f.isFile())
				{
					String s = f.getName();
					if(s.contains("index"))
						continue;
					if(s.endsWith("html")||s.endsWith("htm"))
						work(f);
				}
		}
	}
	
	static void work(File f) throws Exception
	{
		System.out.println(f.getAbsolutePath());
		UniversalDetector det = new UniversalDetector(null);
		FileInputStream fi = new FileInputStream(f);
		Document doc = new Document();
		int nread;
		byte[] buf = new byte[4096];
	    while ((nread = fi.read(buf)) > 0 && !det.isDone()) 
	    	det.handleData(buf, 0, nread);
	    det.dataEnd();
	    String s = det.getDetectedCharset();
		org.jsoup.nodes.Document d = Jsoup.parse(f, s);
		org.jsoup.select.Elements tags = d.getElementsByTag("p");
		String content = "";
		for(Element tag: tags)
			content += StringEscapeUtils.unescapeHtml4(tag.text());
	    String nam = f.getAbsolutePath();
	    nam = nam.substring(cutLength);
	    String title = d.getElementsByTag("title").text();
	    title.replace(',', '_');
	    content.replace(',', '_');
	    int value = 0;
	    if(content.contains("机器学习"))
	    	value = 1;
	    fo.write((title + ',' + content + ',' + value + '\n').getBytes());
	}
	
	public static void main(String[] args) throws Exception{ 
		fo = new FileOutputStream(new File("ml/data.txt"));
	    File f = new File("../data/清华新闻");
	    cutLength = f.getAbsolutePath().length() + 1;
	    src = "清华新闻";
	    dfs(f);
	    System.out.println("fin.");
	    fo.close();
	}
}
