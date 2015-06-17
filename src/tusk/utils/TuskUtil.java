package tusk.utils;

public class TuskUtil {

	public static String getAbstract(String src, String search)
	{
		int st = src.indexOf(search) - 5;
		if(st < 0)	st = 0;
		int en = st + 200;
		if(en > src.length())
			en = src.length();
		return src.substring(st, en);
	}
	
	public static String wrapJson(String s, String callback)
	{
		return callback + "(" + s + ");";
	}
	
}
