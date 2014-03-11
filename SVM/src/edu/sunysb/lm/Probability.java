package edu.sunysb.lm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

class Probability {
	double posProb;
	double negProb;

	Probability(double posProb, double negProb) {
		this.posProb = posProb;
		this.negProb = negProb;
	}

	@Override
	public String toString() {
		return posProb + " " + negProb;

	}

	

}