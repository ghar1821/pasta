package pasta.domain.template;



public class Tuple {

	/**
	 * @param args
	 */
	public double weight;
	public String text;
	public String column;
	public String row;
	public Tuple(double w, String t, String c, String r) {
		weight = w;
		text = t;
		column = c;
		row = r;
	}
	public Tuple(){}
	public String getText()
	{
		return text;
	}

}
