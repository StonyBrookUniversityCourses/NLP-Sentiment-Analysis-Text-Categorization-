package edu.sunysb.lm;

class Count {
	int posCount;
	int negCount;

	Count(int posCount, int negCount) {
		this.posCount = posCount;
		this.negCount = negCount;
	}

	@Override
	public String toString() {
		return posCount + " " + negCount;

	}

	public boolean isUnknown() {
		if ((posCount + negCount) > 1) {
			return false;
		} else {
			return true;
		}
	}
}
