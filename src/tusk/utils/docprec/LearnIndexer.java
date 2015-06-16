package tusk.utils.docprec;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.Thumbnail;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.AbstractWordUtils;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;


public class LearnIndexer {

	static ArrayList<File> flst = new ArrayList<File>();
    static IndexWriter iwriter;

    static int cutLength;
    static String src;

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
	
	static void readPpt(String nam)
	{
		try
		{
			File fil = new File(nam);
			
			String namx = nam.substring(0, nam.length() - 5);
			InputStream is = new FileInputStream(nam);
			POITextExtractor ext = ExtractorFactory.createExtractor(is);
			String text = ext.getText();
			is.close();
			is = new FileInputStream(nam);
			SlideShow ppt = new SlideShow(is);
			
			Dimension pgsize = ppt.getPageSize();
			AffineTransform at = new AffineTransform();
			float zoom = 1.5f;
			at.setToScale(zoom, zoom);
			Slide[] lst = ppt.getSlides();
			Slide slide = lst[0];
			
			BufferedImage img = new BufferedImage((int)Math.ceil(pgsize.width*zoom), (int)Math.ceil(pgsize.height*zoom), BufferedImage.TYPE_INT_RGB);
	        Graphics2D graphics = img.createGraphics();
	        graphics.setTransform(at);
	
	        graphics.setPaint(Color.white);
	        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
	        slide.draw(graphics);
	        FileOutputStream out = new FileOutputStream(namx + ".png");
	        javax.imageio.ImageIO.write(img, "png", out);
	        out.close();
	        /*
	        out = new FileOutputStream(namx + ".txt");
	        out.write(text.getBytes());
	        out.close();
	        */
	
			// document generation
			System.out.println(text.substring(0, 20));
			Document d = new Document();
			d.add(new Field("content", text, Store.YES, Index.ANALYZED));
			String path = fil.getAbsolutePath();
			String sthumb = (new File(namx + ".png")).getAbsolutePath();
			path = path.substring(cutLength);
			sthumb = sthumb.substring(cutLength);
			d.add(new Field("url", path, Store.YES, Index.NO));
			d.add(new Field("thumbnail", sthumb, Store.YES, Index.NO));
			d.add(new Field("doctype", "ppt", Store.YES, Index.NO));
			d.add(new Field("title", fil.getName(), Store.YES, Index.NO));
		    iwriter.addDocument(d);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static void readPdf(String nam)
	{
		try
		{
			File fil = new File(nam);
			// data preparation
			String pref = nam.substring(0, nam.length() - 4);
			String namx = pref + ".txt";
			PDDocument doc = PDDocument.load(nam);
			PDFImageWriter writ = new PDFImageWriter();
			PDFTextStripper fi = new PDFTextStripper();
			String ret = fi.getText(doc);
			writ.writeImage(doc, "png", "", 1, 2, "");
			doc.close();
			/*
			FileOutputStream fos = new FileOutputStream(namx);
			fos.write(ret.getBytes());
			fos.close();
			*/
			File f = new File("1.png");
			f.renameTo(new File(pref+".png"));
			
			// document generation
			Document d = new Document();
			System.out.println(ret.substring(0, 20));
			d.add(new Field("content", ret, Store.YES, Index.ANALYZED));
			String path = fil.getAbsolutePath();
			String sthumb = (new File(pref + ".png")).getAbsolutePath();
			path = path.substring(cutLength);
			sthumb = sthumb.substring(cutLength);
			d.add(new Field("url", path, Store.YES, Index.NO));
			d.add(new Field("thumbnail", sthumb, Store.YES, Index.NO));
			d.add(new Field("doctype", "pdf", Store.YES, Index.NO));
			d.add(new Field("title", fil.getName(), Store.YES, Index.NO));
		    iwriter.addDocument(d);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		Analyzer analyzer = new SmartChineseAnalyzer();
	    // To store an index on disk, use this instead:
	    Directory directory = FSDirectory.open(new File("index/learn").toPath());
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    iwriter = new IndexWriter(directory, config);
	    File dir = new File("../data/learn");
	    cutLength = (new File("../")).getAbsolutePath().length() + 1;
	    src = "ÍøÂçÑ§ÌÃ";
		File[] flst = dir.listFiles();
		for(File f : flst)
		{
			if(!f.isFile())
				continue;
			String nam = f.getAbsolutePath();
			System.out.println(nam);
			if(nam.endsWith(".pdf"))
				readPdf(nam);
			else
			if(nam.endsWith(".doc"))
				readDoc(nam);
			else
			if(nam.endsWith(".docx"))
				readDocx(nam);
			else
			if(nam.endsWith(".ppt"))
				readPpt(nam);
		}
	    System.out.println("fin.");
	    iwriter.close(); 
	}

}
