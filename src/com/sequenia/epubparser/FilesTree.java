package com.sequenia.epubparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FilesTree {
	private FilesNode<ByteArrayOutputStream> root;
	
	public FilesTree(ZipInputStream zis) {
		root = new FilesNode<ByteArrayOutputStream>();
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
				
				FilesNode<ByteArrayOutputStream> parent = (FilesNode<ByteArrayOutputStream>) findNode(path);
				FilesNode<ByteArrayOutputStream> newNode = new FilesNode<ByteArrayOutputStream>();
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
	
	public TreeNode<ByteArrayOutputStream> findNode(String name) {
		if(name.equals("")) {
			return root;
		}

		TreeNode<ByteArrayOutputStream> rootNode = root;
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
	
	public class FilesNode<T> extends TreeNode<T> {
		private boolean directory;
		
		public void setDirectory(boolean _directory) {
			directory = _directory;
		}
		
		public boolean getDirectory() {
			return directory;
		}
	}
}