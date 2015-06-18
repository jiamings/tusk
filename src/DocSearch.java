

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import tusk.utils.TuskUtil;

/**
 * Servlet implementation class DocSearch
 */
@WebServlet("/DocSearch")
public class DocSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Connection con;
	Statement stat;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DocSearch() {
        super();
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
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			long tst = Calendar.getInstance().getTimeInMillis();
			int page = 1;
			if(request.getParameter("page")!=null)
				page = Integer.valueOf(request.getParameter("page"));
	    	//String path = getServletContext().getRealPath("/");  
			Analyzer analyzer = new SmartChineseAnalyzer();
			response.setContentType("text/html;charset=utf-8");
		    // Now search the index:
		    Directory directory = FSDirectory.open(new File("index/learn").toPath());
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    // Parse a simple query that searches for "text":
		    Map<String, Float> boosts = new HashMap<String, Float>();
		    boosts.put("content", 0.7813718f);
		    boosts.put("title", 0.2186282f);
		    String[] fieldLst = new String[]{"content", "title"};
		    QueryParser parser = new MultiFieldQueryParser(fieldLst, analyzer, boosts);
		    String search = request.getParameter("search");
		    Query query = parser.parse(search);
		    TopScoreDocCollector results = TopScoreDocCollector.create((page - 1) * itemPerPage + itemPerPage);
		    isearcher.search(query, results);
		    ScoreDoc[] hits = results.topDocs((page - 1) * itemPerPage).scoreDocs;
		    
		    // Iterate through the results:
		    JSONObject ret = new JSONObject();
		    JSONArray lst = new JSONArray();
		    for (int i = 0; i < hits.length; i++) {
		      Document doc = isearcher.doc(hits[i].doc);
		      JSONObject p = new JSONObject();
		      p.put("type", "doc");
		      p.put("title", doc.get("title"));
		      p.put("abstract", TuskUtil.getAbstract(doc.get("content"), search));
		      p.put("url", doc.get("url"));
		      p.put("doctype", doc.get("doctype"));
		      p.put("thumbnail", doc.get("thumbnail"));
		      p.put("source", "Learn");
		      lst.put(p);
		    }
		    ireader.close();
		    directory.close();
		    
			long ten = Calendar.getInstance().getTimeInMillis();
			PrintWriter out = response.getWriter();
		    ret.put("elapse", ten - tst);
		    ret.put("result", lst);
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
		}catch(Exception e)
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
