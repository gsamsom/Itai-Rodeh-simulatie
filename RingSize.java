package ringproject;
import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class RingSize {
	private final static PrintStream out = new PrintStream(System.out);
	static Node[] ring;
	static int identifierDomain;
	static int runCount;
	static Node[] createRingArray(int length){
		return new Node[length];
	}

	private static boolean isInteger(String s) {
		if (s == null || s.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c < '0' || c > '9') {
					return false;
				}			
			}
			return true;
		}
	}
	private static void checkIdentifierDomain() {
		if (identifierDomain < 2) {
			out.println("domain is too small");
			System.exit(1);
		}
	}
	private static void checkRingSize() {
		if (ring.length < 2) {
			out.println("ringsize is too small");
			System.exit(1);
		}
	}
	private static void createRing(String s) {
		ring = createRingArray(Integer.parseInt(s));
	}
	private static void setIdentifierDomain(String s) {
		identifierDomain = Integer.parseInt(s);
	}
	private static void setRunCount(String s) {
		runCount = Integer.parseInt(s);
	}
	private static void parseInput(String args[]) {
		if (!isInteger(args[0])) { 
			out.println("first argument must contain ringsize integer");
			System.exit(1);
		}
		if (!isInteger(args[1])) {
			out.println("second argument must contain domain range integer");
			System.exit(1);
		}
		if (args.length == 3) {
			if (!isInteger(args[2])) { 
				out.println("third argument must be a runcount integer");
				System.exit(1);
			}
			setRunCount(args[2]);
		} else {
			setRunCount("1");
		}
		createRing(args[0]);
		checkRingSize();
		setIdentifierDomain(args[1]);
		checkIdentifierDomain();
	}

	private static void initializeNodes() {
		for (int i=0; i<ring.length;i++) {
			ring[i] = new Node(identifierDomain); //fill the nodes with actual nodes
		}
		for (int i=0; i<ring.length;i++) {
			if (i+1 == ring.length) {
				ring[i].linkNode(ring[0]); //link the last node to the first node
			} else {
				ring[i].linkNode(ring[i+1]); //link each node a neighbour to send messages to
			}
		}
	}

	private static void initializeSmartNodes () {
		initializeNodes();
		for (int i=0; i<ring.length;i++) {
			ring[i].initializeSmartMessage();
		}
	}

	private static LinkedList<Node> getActiveNodes(){ //returns a list of all the nodes which have a non-empty messagelist
		LinkedList<Node> activeNodes = new LinkedList<Node>();
		for (int i=0; i<ring.length; i++) {
			if (ring[i].hasWork()) {
				activeNodes.add(ring[i]); 
			}
		}
		return activeNodes;
	}

	private static Node getRandomNode(LinkedList<Node> nodes) { //returns a random node from a given list of nodes
		return nodes.get(ThreadLocalRandom.current().nextInt(0, nodes.size()));
	}

	private static Node getRandomNode(Node[] nodes) {
		return nodes[ThreadLocalRandom.current().nextInt(0, nodes.length)];
	}

	private static Node getRandomActiveNode() { //returns a random node which has messages
		return getRandomNode(getActiveNodes());
	}

	private static boolean isActive() { //check the whole ring for messages, returns false if no messages found
		for (int i=0;i<ring.length;i++) {
			if(ring[i].hasWork()) {
				return true;
			}
		}
		return false;
	}

	private static void run3() {
		initializeSmartNodes();
		while (isActive()) {
			for(int i=0;i<100;i++) {
				getRandomNode(ring).processBestSmartMessage();
			}
		}	
	}

	private static void run2() {
		initializeNodes();
		while (isActive()) {
			for(int i=0;i<1000;i++) {
				getRandomNode(ring).processRandomSmartMessage();
			}
		}
	}

	private static void run() {
		initializeNodes();
		while(isActive()) {
			getRandomActiveNode().processRandomMessage();
		}
	}

	private static void runTimes() { //run i times
		//int[] breaks = calculateBreaks(runCount);
		int[] fails = new int[ring.length];
		//int k = 0;
		for (int j=0;j<runCount;j++) {
			//k = checkBreaks(breaks, j, k);
			//out.printf("%d\n", j);
			run2();
			if (!goodResult()) {
				fails[failEst()]++;
				//out.printf("the incorrect result was %d\n", ring[0].getEstimate());
			}
		}
		printEndResults(fails);
	}
	public static void runTimesTimes() {
		for(int i=5;i<33;i++) {
			ring = createRingArray(i);
			runTimes();
		}
	}

	public static void main(String[] args) {
		parseInput(args);
		out.println("parsing complete");
		runTimesTimes();
		out.println("run complete");
	}

	private static boolean goodResult() {//checks whether the estimate of the first node matches the other
		int result = ring[0].getEstimate(); //if that is the case, returns whether the result is correct or not
		for (int i=0;i<ring.length;i++) {
			if(result != ring[i].getEstimate()) {
				out.println("not terminated, different estimates detected");
				printRing();
				System.exit(1);
			}
		}
		return (result == ring.length);
	}

	private static void printRing() {
		for(int i=0;i<ring.length;i++) {
			out.printf("%d %d %d %b\n", i, ring[i].getId(), ring[i].getEstimate(), ring[i].isPassive());
		}
	}

	private static double theoreticSuccesRate() {//calculates the succes rate based on a prime number
		return 100*( Math.pow(1.0 - Math.pow(1.0/identifierDomain, ring.length-1), ring.length-2));
	}

	private static int failEst() {
		int est = ring[0].getEstimate();
		for(int i=1;i<ring.length;i++) {
			if(ring[i].getEstimate() != est) {
				return 0;
			}
		}
		return est;
	}

	private static void printEndResults(int[] fails) {
		double succesRate = 100*(1.0-(((double)totalFails(fails))/((double)runCount)));
		//out.printf("%d times it failed on an estimate of 2\n", twoFails);
		//out.printf("there were %d fails on %d tries\n", totalFails(fails), runCount);
		//out.printf("succesrate was %.4f%% which was in theory %.4f%%\n",succesRate,theoreticSuccesRate());
		printFailArray(fails);
	}

	private static void printFailArray(int[] fails) {
		//out.printf("failarray:");
		out.printf("%d\t", ring.length);
		for (int i=1;i<fails.length;i++) {
			out.printf("%d\t", fails[i]);
		}
		out.printf("\n");
	}

	private static int totalFails(int[] fails) {
		int result = 0;
		for (int i=0;i<fails.length;i++) {
			result += fails[i];
		}
		return result;
	}
	private static int[] calculateBreaks(int i) {
		int[] breaks = new int[10];
		for (int j=0;j<breaks.length;j++) {
			breaks[j] = (j+1)*(i/10);
		}
		return breaks;
	}
	private static int checkBreaks(int[] breaks, int currentRun, int breakRun) {
		if (breaks[breakRun] == currentRun) {
			out.printf("%d%%\n", (breakRun+1)*10);
			return breakRun + 1;
		}
		return breakRun;
	}
}
/*
private static void printResults(){
	int result = ring[0].getEstimate();
	for (int i=0;i<ring.length;i++) {
		if(result != ring[i].getEstimate()) {
			out.println("not terminated, different estimates detected");
			System.exit(1);
		}
	}
	//out.printf("the ring size is at least %d, it was actually %d\n", result, ring.length);
}

private static void run() {
	initializeNodes();
	boolean active = true;
	while (active) {
		active = false;
		for (int i=0;i<ring.length;i++) {
			if(ring[i].processRandomMessage()) {
				active = true;
			}
		}
	}
}
private static Node getRandomActiveNode() { //add all nodes with a non-empty messagelist and return one at random
	LinkedList<Node> activeNodes = new LinkedList<Node>();
	for (int i=0; i<ring.length; i++) {
		if (ring[i].hasWork()) {
			activeNodes.add(ring[i]); 
		}
	}
	return activeNodes.get(ThreadLocalRandom.current().nextInt(0, activeNodes.size()));
}
 */
