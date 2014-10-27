package org.monarchinitiative.owlsim.compute.stats;

import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;

public class SumMatrixChangingVisitor extends DefaultRealMatrixPreservingVisitor{

	private double sum;
	
	@Override
	public double end() {
		return this.sum;
	}

	@Override
	public void start(int rows, int columns, int startRow, int endRow,
			int startColumn, int endColumn) {
		this.sum = 0.0;
	}

	@Override
	public void visit(int row, int column, double value) {
		this.sum += value;
	}
}
