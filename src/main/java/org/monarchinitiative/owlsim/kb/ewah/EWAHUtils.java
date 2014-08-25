package org.monarchinitiative.owlsim.kb.ewah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Utilities for forwing with {@link EWAHCompressedBitmap}s
 * 
 * @author cjm
 *
 */
public class EWAHUtils {

	/**
	 * @param bits - set of bits
	 * @return Bitmap representation of set of ints
	 */
	public static EWAHCompressedBitmap converIndexSetToBitmap(Set<Integer> bits) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		ArrayList<Integer> bitlist = new ArrayList<Integer>(bits);
		Collections.sort(bitlist); // EWAH assumes sorted, otherwise silent failure
		for (Integer i : bitlist) {
			bm.set(i.intValue());
		}
		return bm;
	}

	/**
	 * @param bits - sorted list of bits
	 * @return Bitmap representation of set of ints
	 */
	public static EWAHCompressedBitmap convertSortedIndexListToBitmap(List<Integer> bits) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (Integer i : bits) {
			bm.set(i.intValue());
		}
		return bm;
	}

}
