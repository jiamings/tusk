package tusk.utils.prof;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProfCrawler {

	static Connection con;
	static Statement stat;
	
	static String dir = "../data/prof";
	static String domain = "http://www.cs.tsinghua.edu.cn"; 
	
	private String downloadPage(URL pageUrl) throws Exception 
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
		String line;
		StringBuffer pageBuffer = new StringBuffer();
		while ((line = reader.readLine()) != null) 
			pageBuffer.append(line);
		return pageBuffer.toString();
	}
	
	static void work(URL url) throws Exception
	{
		try
		{
			Document doc = Jsoup.parse(url, 0);
			Element g = doc.getElementById("s2_right_con");
			String u = url.toString();
			System.out.println(u);
			String avatar = domain + g.getElementsByTag("img").first().attr("src");
			String nam = "", position = "", tel = "", mail = "", page = "", orientation = "";
			String sql;
			for(Element i : g.getElementsByTag("p"))
			{
				String s = i.text();
				if(s.startsWith("������"))
					nam = s.substring(3);
				if(s.startsWith("��"))
					nam = s;
				if(s.startsWith("��")||s.startsWith("����"))
					position = s;
				if(s.startsWith("�����ʼ�"))
					mail = i.getElementsByTag("a").first().text();
				if(s.startsWith("ְ�ƣ�"))
					position = s.substring(3);
				if(s.startsWith("���䣺"))
					mail = s.substring(3);
				if(s.startsWith("�绰��")||s.startsWith("�绰 "))
					tel = s.substring(3);
				if(s.startsWith("��ҳ��"))
					page = s.substring(3);
			}
			for(Element i : g.getElementsByTag("h4"))
			{
				String s = i.text();
				if(s.equals("�о�����"))
				{
					Element j = i.nextElementSibling();
					orientation = j.text();
				}
			}
			if(nam.length() <= 1)
				return;
			sql = "INSERT INTO prof (name, position, tel, mail, page, orientation, url) VALUES ('" + nam + "', '" + 
					position + "', '" + 
					tel + "', '" + 
					mail + "', '" + 
					page + "', '" + 
					orientation + "', '" + 
					url + "');";
			stat.execute(sql);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] arg) throws Exception
	{
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/SE", "root", "admin");
		stat = con.createStatement();
		stat.execute("delete from prof where id > 0;");
        URL url = new URL("http://www.cs.tsinghua.edu.cn/publish/cs/4797/index.html");
        Document doc = Jsoup.parse(url, 0);
        Elements lst = doc.getElementsByClass("box_detail");
        for(Element p : lst)
        {
        	Elements a = p.getElementsByTag("a");
        	for(Element i : a)
        	{
        		String s = domain + i.attr("href");
        		work(new URL(s));
        	}
        }
		stat.close();
		con.close();
	}
}
