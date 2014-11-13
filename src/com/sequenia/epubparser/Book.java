package com.sequenia.epubparser;

import java.util.ArrayList;

public class Book {
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
}
