package edu.sunysb.perceptron;

class Count {
	int index;
	int count;

	Count(int index, int count) {
		this.index = index;
		this.count = count;
	}

	@Override
	public String toString() {
		return index + " " + count;

	}

	public boolean isUnknown() {
		if ((count) > 1) {
			return false;
		} else {
			return true;
		}
	}

	public void increamentCount(){
		count++;
	}
}
