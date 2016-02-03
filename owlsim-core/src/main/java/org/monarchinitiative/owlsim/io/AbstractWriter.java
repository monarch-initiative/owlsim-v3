package org.monarchinitiative.owlsim.io;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public abstract class AbstractWriter {
	
	protected BMKnowledgeBase knowledgeBase;
	protected PrintStream stream;

	
	public AbstractWriter(BMKnowledgeBase knowledgeBase, PrintStream stream) {
		super();
		this.knowledgeBase = knowledgeBase;
		this.stream = stream;
	}

	/**
	 * @param stream
	 */
	public AbstractWriter(PrintStream stream) {
		super();
		this.stream = stream;
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public AbstractWriter(String file) throws FileNotFoundException {
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
	public abstract void write(Object obj);
	
	public String toString() {
		return "JSONWriter file: "+stream;
	}

}
