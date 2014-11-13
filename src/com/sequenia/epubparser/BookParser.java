package com.sequenia.epubparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.res.Resources;

public class BookParser {
	String containerFileName = "META-INF/container.xml";
	
	public BookParser() {}
	
	public boolean parseEpub(Resources r, int id) {
		Book book = new Book();
		HashMap<String, ByteArrayOutputStream> files = zipToFiles(r, id);
		
		org.w3c.dom.Document containerXml = getXmlFromBuffer(files.get(containerFileName));
		String rootFileName = getRootFileName(containerXml);
		
		org.w3c.dom.Document rootFile = getXmlFromBuffer(files.get(rootFileName));
		writeInfoToBook(book, rootFile);
		
		return true;
	}
	
	private boolean writeInfoToBook(Book book, org.w3c.dom.Document xml) {
		if(book == null || xml == null) { return false; }
		
		String titleTagName = "dc:title";
		String dateTagName = "dc:date";
		String creatorTagName = "dc:creator";
		String contributorTagName = "dc:contributor";
		String publisherTagName = "dc:publisher";
		String descriptionTagName = "dc:description";
				
		NodeList nList = xml.getElementsByTagName(titleTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.titles.add(nNode.getTextContent());
		}
		
		nList = xml.getElementsByTagName(dateTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.dates.add(nNode.getTextContent());
		}
		
		nList = xml.getElementsByTagName(creatorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.creators.add(nNode.getTextContent());
		}
		
		nList = xml.getElementsByTagName(contributorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.contributors.add(nNode.getTextContent());
		}
		
		nList = xml.getElementsByTagName(publisherTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.publishers.add(nNode.getTextContent());
		}
		
		nList = xml.getElementsByTagName(descriptionTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.descriptions.add(nNode.getTextContent());
		}
		
		return true;
	}
	
	private String getRootFileName(org.w3c.dom.Document xml) {
		if(xml == null) { return null; }

		String tagName = "rootfile";
		String attrName = "full-path";

		NodeList nList = xml.getElementsByTagName(tagName);
		if(nList.getLength() == 0) { return null; }
		
		Node nNode = nList.item(0);
		Element eElement = (Element) nNode;
		String rootFileName = eElement.getAttribute(attrName);
		
		return rootFileName;
	}
	
	private HashMap<String, ByteArrayOutputStream> zipToFiles(Resources r, int id) {
		InputStream is;
		ZipInputStream zis;
		HashMap<String, ByteArrayOutputStream> filesContent = new HashMap<String, ByteArrayOutputStream>();
		
		try {
			String filename;
			ByteArrayOutputStream file;
			is = r.openRawResource(R.raw.bol);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;

			while((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				
				if (ze.isDirectory()) {
	                continue;
	            }
				
				file = getByteBuffer(zis);
				if(file != null) {
					filesContent.put(filename, file);
				} else {
					return null;
				}
				
				zis.closeEntry();
			}
			
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return filesContent;
	}
	
	private ByteArrayOutputStream getByteBuffer(ZipInputStream zis) {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		try {
			int n;
			while ((n = zis.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, n);
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
			return null;
		}
		
		return byteBuffer;
	}
	
	private org.w3c.dom.Document getXmlFromBuffer(ByteArrayOutputStream buffer) {
		if(buffer == null) { return null; }

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
