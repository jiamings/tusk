package tusk.utils.docprec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class DocPrec {

	static void readDoc(String nam) throws Exception
	{
		String pref = nam.substring(0, nam.length() - 4);
		String namx = pref + ".txt";
		String ret = "";
		InputStream is = new FileInputStream(nam);  
	    WordExtractor extractor = new WordExtractor(is); 
	    ret = extractor.getText();
	    is.close();
		FileOutputStream fos = new FileOutputStream(namx);
		fos.write(ret.getBytes());
		fos.close();
	}
	
	static void readDocx(String nam) throws Exception
	{
		String namx = nam.substring(0, nam.length() - 5) + ".txt";
		String ret = "";
		InputStream is = new FileInputStream(nam);  
		XWPFDocument p = new XWPFDocument(is);
		Iterator<XWPFParagraph> i = p.getParagraphsIterator();
		while(i.hasNext())
		{
			XWPFParagraph a = i.next();
			ret += a.getText();
		}
		is.close();
		FileOutputStream fos = new FileOutputStream(namx);
		fos.write(ret.getBytes());
		fos.close();
	}
	
	static void readPdf(String nam) throws Exception
	{
		String pref = nam.substring(0, nam.length() - 4);
		String namx = pref + ".txt";
		PDDocument doc = PDDocument.load(nam);
		PDFImageWriter writ = new PDFImageWriter();
		PDFTextStripper fi = new PDFTextStripper();
		String ret = fi.getText(doc);
		writ.writeImage(doc, "png", "", 1, 2, "");
		doc.close();
		FileOutputStream fos = new FileOutputStream(namx);
		fos.write(ret.getBytes());
		fos.close();
		File f = new File("1.png");
		f.renameTo(new File(pref+".png"));
	}
	
	public static void main(String[] args) throws Exception{
		File[] flst = new File(".").listFiles();
		for(File f : flst)
		{
			if(!f.isFile())
				continue;
			String nam = f.getName();
			if(nam.endsWith(".pdf"))
				readPdf(nam);
			else
			if(nam.endsWith(".doc"))
				readDoc(nam);
			else
			if(nam.endsWith(".docx"))
				readDocx(nam);
		}
	}

}
