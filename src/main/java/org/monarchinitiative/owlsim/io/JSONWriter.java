package org.monarchinitiative.owlsim.io;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Writes model objects as JSON
 * 
 * Note that the model package is authored Gson-ready - objects should directly
 * serialize using Gson
 * 
 * @author cjm
 *
 */
public class JSONWriter {
	
	private PrintStream stream;

	/**
	 * @param stream
	 */
	public JSONWriter(PrintStream stream) {
		super();
		this.stream = stream;
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public JSONWriter(String file) throws FileNotFoundException {
		super();
		setStream(file);
	}
	
	private void setStream(String file) throws FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(file);
		stream = new PrintStream(new BufferedOutputStream(fos));
	}
	
	/**
	 * Writes a Gson-ready object
	 * 
	 * @param obj
	 */
	public void write(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(obj);
		stream.println(json);
		stream.flush();
	}
	
	public String toString() {
		return "JSONWriter file: "+stream;
	}

}
