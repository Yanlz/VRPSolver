package main;

public class Strategy {

	String selector, acceptor, ruin, insertion;
	double share, probability, alpha, warmup;
	int variable;
	boolean isVariable;
	
	public Strategy(String selector, String acceptor, String ruin,
			String insertion, double share, double probability, double alpha,
			double warmup) {
		this.selector = selector;
		this.acceptor = acceptor;
		this.ruin = ruin;
		this.insertion = insertion;
		this.share = share;
		this.probability = probability;
		this.alpha = alpha;
		this.warmup = warmup;
		isVariable = false;
		variable = -1;
	}

	public void setVariableValue(double value) {
		switch (variable){
		case 0:
			this.share = value;
			break;
		case 1:
			this.probability = value;
			break;
		case 2:
			this.alpha = value;
			break;
		case 3:
			this.warmup = value;
			break;
		}
	}
	
	public String getVariableName() {
		switch (variable){
		case 0:
			return "Share";
		case 1:
			return "Probability";
		case 2:
			return "Alpha";
		case 3:
			return "Warmup";
		}
		return null;
	}
	
	public void setVariable(int variable) {
		isVariable = true;
		this.variable = variable;
	}
}
