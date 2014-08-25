package org.monarchinitiative.owlsim.compute.mica.impl;

public class NoRootException extends Exception {

	public NoRootException(int i, int j, String iid, String jid) {
		super("No root: "+i+" "+j+" I:"+iid+" J:"+jid);
	}

}
