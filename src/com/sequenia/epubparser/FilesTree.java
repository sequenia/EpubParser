package com.sequenia.epubparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FilesTree {
	private FilesNode root;
	
	public FilesTree(ZipInputStream zis) {
		root = new FilesNode();
		root.setParent(null);
		root.setDirectory(true);
		
		ZipEntry ze;

		try {
			while((ze = zis.getNextEntry()) != null) {
				String filename = ze.getName();
				String[] names = filename.split("/");
				int length = names.length;
				String shortName = names[length - 1];

				String path = "";
				for(int i = 0; i < length -1; i++) {
					path += names[i] + "/";
				}
				
				FilesNode parent = findNode(path);
				FilesNode newNode = new FilesNode();
				newNode.setParent(parent);
				newNode.setDirectory(ze.isDirectory());
				parent.addChild(shortName, newNode);
				
				if (ze.isDirectory()) {
	                continue;
	            }
				
				ByteArrayOutputStream file = getByteBuffer(zis);
				if(file != null) {
					newNode.setData(file);
				}
				
				zis.closeEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FilesNode findNode(String name) {
		if(name.equals("")) {
			return root;
		}

		FilesNode rootNode = root;
		String[] names = name.split("/");
		
		for(int i = 0; i < names.length && rootNode != null; i++) {
			if(names[i].equals("")) {
				continue;
			}

			if(names[i].equals(".")) {
				rootNode = rootNode.getParent();
			} else {
				rootNode = rootNode.getChild(names[i]);
			}
		}
		
		return rootNode;
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
	
	public class FilesNode {
		private FilesNode parent;
		private HashMap<String, FilesNode> children;
		private boolean directory;
		private ByteArrayOutputStream data;
		
		public FilesNode() {
			children = new HashMap<String, FilesNode>();
		}
		
		public void setParent(FilesNode _parent) {
			parent = _parent;
		}
		
		public FilesNode getParent() {
			return parent;
		}
		
		public void setDirectory(boolean _directory) {
			directory = _directory;
		}
		
		public boolean getDirectory() {
			return directory;
		}
		
		public void addChild(String name, FilesNode child) {
			children.put(name, child);
		}
		
		public FilesNode getChild(String name) {
			return children.get(name);
		}
		
		public void setData(ByteArrayOutputStream _data) {
			data = _data;
		}
		
		public ByteArrayOutputStream getData() {
			return data;
		}
	}
}