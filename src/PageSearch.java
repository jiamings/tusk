

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import tusk.utils.TuskUtil;
import static org.junit.Assert.*;

/**
 * Servlet implementation class PageSearch
 */
@WebServlet("/PageSearch")
public class PageSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Connection con;
	Statement stat;
	
    /**
     * Default constructor. 
     */
    public PageSearch() {
        try
        {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/SE", "root", "admin");
			stat = con.createStatement();
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
    }

	protected void finalize()
	{
		try
		{
			stat.close();
			con.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

    static int itemPerPage = 20;
    
    JSONObject work(String search, int page) throws Exception
    {
	    JSONObject ret = new JSONObject();
	    JSONArray lst = new JSONArray();
    	ResultSet sql = stat.executeQuery("SELECT * FROM prof WHERE name = '" + search + "';");
    	if(sql.next())
    	{
    		JSONObject p = new JSONObject();
    		p.put("type", "prof");
    		p.put("name", sql.getString("name"));
    		p.put("position", sql.getString("position"));
    		p.put("tel", sql.getString("tel"));
    		p.put("mail", sql.getString("mail"));
    		p.put("page", sql.getString("page"));
    		p.put("orientation", sql.getString("orientation"));
    		p.put("url", sql.getString("url"));
    		p.put("avatar", sql.getString("avatar"));
    		lst.put(p);
    	}
	    if(search.endsWith("校历"))
	    {
	    	JSONObject p = new JSONObject();
	    	p.put("type",  "img");
	    	p.put("url", "http://info.tsinghua.edu.cn/html/lmntw/img/2014-2015-1.png");
	    	p.put("title", "清华大学秋季学期校历");
	    	p.put("desc", "");
	    	p.put("path", "http://info.tsinghua.edu.cn/html/lmntw/img/2014-2015-1.png");
	    	lst.put(p);
	    	p = new JSONObject();
	    	p.put("type",  "img");
	    	p.put("url", "http://info.tsinghua.edu.cn/html/lmntw/img/2014-2015-2.png");
	    	p.put("title", "清华大学春季学期校历");
	    	p.put("desc", "");
	    	p.put("path", "http://info.tsinghua.edu.cn/html/lmntw/img/2014-2015-2.png");
	    	lst.put(p);
		    ret.put("result", lst);
		    return ret;
	    }
    	//String path = getServletContext().getRealPath("/");  
		Analyzer analyzer = new SmartChineseAnalyzer();
	    // Now search the index:
	    Directory directory = FSDirectory.open(new File("index/news").toPath());
	    DirectoryReader ireader = DirectoryReader.open(directory);
	    IndexSearcher isearcher = new IndexSearcher(ireader);
	    Map<String, Float> boosts = new HashMap<String, Float>();
	    boosts.put("content", 0.5207875f);
	    boosts.put("title", 0.4792125f);
	    String[] fieldLst = new String[]{"content", "title"};
	    QueryParser parser = new MultiFieldQueryParser(fieldLst, analyzer, boosts);
	    Query query = parser.parse(search);
	    TopScoreDocCollector results = TopScoreDocCollector.create((page - 1) * itemPerPage + itemPerPage);
	    isearcher.search(query, results);
	    ScoreDoc[] hits = results.topDocs((page - 1) * itemPerPage).scoreDocs;
	    
	    // Iterate through the results:
	    for (int i = 0; i < hits.length; i++) {
	      Document doc = isearcher.doc(hits[i].doc);
	      JSONObject p = new JSONObject();
	      p.put("type", "page");
	      p.put("title", doc.get("title"));
	      p.put("desc", TuskUtil.getAbstract(doc.get("content"), search));
	      p.put("url", doc.get("url"));
	      p.put("source", doc.get("source"));
	      lst.put(p);
	    }
	    ireader.close();
	    directory.close();
	    ret.put("result", lst);
	    return ret;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			response.setContentType("text/html;charset=utf-8");
			long tst = Calendar.getInstance().getTimeInMillis();
			int page = 1;
			if(request.getParameter("page")!=null)
				page = Integer.valueOf(request.getParameter("page"));
		    String search = request.getParameter("search");
		    JSONObject ret = work(search, page);
			long ten = Calendar.getInstance().getTimeInMillis();
		    ret.put("elapse", ten - tst);
			PrintWriter out = response.getWriter();
		    out.print(TuskUtil.wrapJson(ret.toString(), request.getParameter("callback")));
			out.close();
			
			// DB
			search.replace('\'', ' ');
			ResultSet rst = stat.executeQuery("SELECT * FROM autocomp WHERE text = '" + search + "';");
			if(rst.next())
			{
				int cnt = rst.getInt(3) + 1;
				stat.execute("UPDATE autocomp SET count = " + cnt + " WHERE text = '" + search + "';");
			}else
				stat.execute("INSERT INTO autocomp (text, count) VALUES ('" + search + "', 1);");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
