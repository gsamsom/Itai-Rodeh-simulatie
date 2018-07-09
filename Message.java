package ringproject;

public class Message {
	public int est;
	public int id;
	public int hop;

	Message(int est, int id, int hop){
		this.est = est;
		this.id = id;
		this.hop = hop;
	}
	
	public Message increase() {
		hop++;
		return this;
	}
	public Message increaseEst() {
		est++;
		return this;
	}
}
