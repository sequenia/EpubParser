package com.sequenia.epubparser;

import java.util.ArrayList;

public class Book {
	public enum ElemType {
		Text, Image
	}
	
	public ArrayList<String> titles;
	public ArrayList<String> dates;
	public ArrayList<String> creators;
	public ArrayList<String> contributors;
	public ArrayList<String> publishers;
	public ArrayList<String> descriptions;
	public String text;
	
	public Book() {
		titles = new ArrayList<String>();
		contributors = new ArrayList<String>();
		dates = new ArrayList<String>();
		creators = new ArrayList<String>();
		publishers = new ArrayList<String>();
		descriptions = new ArrayList<String>();
		text = "";
	}
	
	public class BookElement {
		public ElemType type;
	}
	
	public class TextElem extends BookElement {
		String text;
		
		public TextElem() {
			super();
			type = ElemType.Text;
		}
		
		public String getData() {
			return text;
		}
	}
}
