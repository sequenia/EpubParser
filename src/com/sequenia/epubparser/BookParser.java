package com.sequenia.epubparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sequenia.epubparser.FilesTree.FilesNode;

import android.content.res.Resources;

public class BookParser {
	public BookParser() {}
	
	public class ManifestItem {
		public String id;
		public String href;
		public String type;
		
		public ManifestItem(String _id, String _href, String _type) {
			id = _id;
			href = _href;
			type = _type;
		}
	}
	
	public class SpineItem {
		public String idref;
		
		public SpineItem(String _idref) {
			idref = _idref;
		}
	}
	
	public Book parseEpub(Resources r, int id) {
		String containerFileName = "META-INF/container.xml";

		Book book = new Book();
		FilesTree files = zipToFiles(r, id);
		
		org.w3c.dom.Document containerXml = getDomDocumentFromBuffer(files.findNode(containerFileName).getData());
		String rootFileName = getRootFileName(containerXml);
		
		org.w3c.dom.Document rootFile = getDomDocumentFromBuffer(files.findNode(rootFileName).getData());
		if(!parseMetadata(rootFile, book)) { return null; }
		
		HashMap<String, ManifestItem> manifest = parseManifest(rootFile);
		if(manifest == null) { return null; }
		
		ArrayList<SpineItem> spine = parseSpine(rootFile);
		if(spine == null) { return null; }
		
		parseBookContent(book, files, manifest, spine, rootFileName);
		
		return book;
	}
	
	private boolean parseBookContent(Book book, FilesTree files, HashMap<String, ManifestItem> manifest, ArrayList<SpineItem> spine, String rootFileName) {
		
		String rootPath = "";
		
		String[] names = rootFileName.split("/");
		for(int i = 0; i < names.length - 1; i++) {
			rootPath += names[i] + "/";
		}
		
		for(int i = 0; i < spine.size(); i++) {
			ManifestItem manifestItem = manifest.get(spine.get(i).idref);
			
			if(manifestItem.type.equals("application/xhtml+xml")) {
				FilesNode node = findNode(manifestItem, files, rootPath);
				org.w3c.dom.Document file = getDomDocumentFromBuffer(node.getData());
				parseSpineFile(file, book);
			}
		}
		
		return true;
	}
	
	private boolean parseSpineFile(org.w3c.dom.Document file, Book book) {
		if(file == null || book == null) {
			System.out.println("ОШИБКА: parseSpineFile - xml or book is null");
			return false;
		}
		
		NodeList nList = file.getElementsByTagName(EpubInfo.bodyTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpineFile - отсутствует body в документе.");
			return false; 
		}
		
		Element body = (Element)nList.item(0);
		body.getTextContent();
		
		return true;
	}
	
	private FilesNode findNode(ManifestItem manifestItem, FilesTree files, String rootPath) {
		String filename = manifestItem.href;
		FilesNode node = files.findNode(filename);
		if(node == null) {
			node = files.findNode(rootPath + filename);
		}
		
		return node;
	}
	
	private ArrayList<SpineItem> parseSpine(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: parseSpine - xml is null");
			return null;
		}
		
		NodeList nList = xml.getElementsByTagName(EpubInfo.spineTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpine - отсутствует spine.");
			return null; 
		}
		
		Element spine = (Element)nList.item(0);
		ArrayList<SpineItem> spineItems = new ArrayList<SpineItem>();
		
		nList = spine.getElementsByTagName(EpubInfo.itemrefTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			Element elem = (Element) nNode;
			String idref = elem.getAttribute(EpubInfo.idrefAttrName);
			SpineItem item = new SpineItem(idref);
			spineItems.add(item);
		}
		
		return spineItems;
	}
	
	private boolean parseMetadata(org.w3c.dom.Document xml, Book book) {
		if(book == null || xml == null) {
			System.out.println("ОШИБКА: parseMetadata - xml or book is null");
			return false;
		}

		NodeList nList = xml.getElementsByTagName(EpubInfo.metadataTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствуют метаданные.");
			return false; 
		}
		
		Element metadata = (Element)nList.item(0);
				
		nList =  metadata.getElementsByTagName(EpubInfo.titleTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.titles.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.dateTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.dates.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.creatorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.creators.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.contributorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.contributors.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.publisherTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.publishers.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.descriptionTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.descriptions.add(nNode.getTextContent());
		}
		
		return true;
	}
	
	private HashMap<String, ManifestItem> parseManifest(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: parseManifest - xml is null");
			return null;
		}
		
		NodeList nList = xml.getElementsByTagName(EpubInfo.manifestTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствует манифест.");
			return null; 
		}
		
		Element manifest = (Element)nList.item(0);
		HashMap<String, ManifestItem> manifestItems = new HashMap<String, ManifestItem>();
		
		nList = manifest.getElementsByTagName(EpubInfo.itemTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			Element elem = (Element) nNode;
			String id = elem.getAttribute(EpubInfo.idAttrName);
			String href = elem.getAttribute(EpubInfo.hrefAttrName);
			String type = elem.getAttribute(EpubInfo.typeAttrName);
			ManifestItem item = new ManifestItem(id, href, type);
			manifestItems.put(id, item);
		}
		
		return manifestItems;
	}
	
	private String getRootFileName(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: getRootFileName - xml is null");
			return null;
		}

		NodeList nList = xml.getElementsByTagName(EpubInfo.rootFileTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: no rootfile tag in container xml");
			return null;
		}
		
		Node nNode = nList.item(0);
		Element eElement = (Element) nNode;
		String rootFileName = eElement.getAttribute(EpubInfo.fullPathAttrName);
		
		return rootFileName;
	}
	
	private FilesTree zipToFiles(Resources r, int id) {
		
		InputStream is = r.openRawResource(R.raw.bol);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

		FilesTree tree = new FilesTree(zis);

		return tree;
	}
	
	private org.w3c.dom.Document getDomDocumentFromBuffer(ByteArrayOutputStream buffer) {
		if(buffer == null) {
			System.out.println("ОШИБКА: getDomDocumentFromBuffer - buffer is null");
			return null; 
		}

		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			try {
				doc = dBuilder.parse(new ByteArrayInputStream(buffer.toByteArray()));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
}
