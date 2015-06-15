

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.Assert.*;

/**
 * Servlet implementation class PageSearch
 */
@WebServlet("/PageSearch")
public class PageSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public PageSearch() {
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
		    Directory directory = FSDirectory.open(new File("index/news").toPath());
		    DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    // Parse a simple query that searches for "text":
		    QueryParser parser = new QueryParser("content", analyzer);
		    Query query = parser.parse(request.getParameter("search"));
		    TopScoreDocCollector results = TopScoreDocCollector.create((page - 1) * itemPerPage + itemPerPage);
		    isearcher.search(query, results);
		    ScoreDoc[] hits = results.topDocs((page - 1) * itemPerPage).scoreDocs;
		    
		    // Iterate through the results:
		    JSONObject ret = new JSONObject();
		    JSONArray lst = new JSONArray();
		    for (int i = 0; i < hits.length; i++) {
		      Document doc = isearcher.doc(hits[i].doc);
		      JSONObject p = new JSONObject();
		      p.put("type", "page");
		      p.put("title", doc.get("title"));
		      p.put("desc", doc.get("content"));
		      p.put("url", doc.get("url"));
		      p.put("source", doc.get("source"));
		      lst.put(p);
		    }
		    ireader.close();
		    directory.close();
		    
			long ten = Calendar.getInstance().getTimeInMillis();
			PrintWriter out = response.getWriter();
		    ret.put("elapse", ten - tst);
		    ret.put("result", lst);
		    out.print(ret.toString());
			out.close();
			
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
