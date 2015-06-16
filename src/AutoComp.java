

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * Servlet implementation class AutoComp
 */
@WebServlet("/AutoComp")
public class AutoComp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Connection con;
	Statement stat;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AutoComp(){
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try
		{
			response.setContentType("text/html;charset=utf-8");
			String s = request.getParameter("autocomp").replace('\'', ' ');
			ResultSet res = stat.executeQuery("SELECT * FROM autocomp WHERE text LIKE '" + s + "%' ORDER BY count DESC");
			PrintWriter out = response.getWriter();
		    JSONArray lst = new JSONArray();
			while(res!=null&&res.next())
				lst.put(res.getString(2));
			out.print(lst);
			out.close();
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
