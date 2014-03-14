package edu.sunysb.perceptron;


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