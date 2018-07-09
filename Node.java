package ringproject;
import java.util.concurrent.ThreadLocalRandom;
import java.util.LinkedList;

public class Node {
	private boolean passive;
	private int est;
	private int id;
	private LinkedList<Message> messageList;
	private int idRange;
	private Node nextNode;

	Node(int idRange){
		this.idRange = idRange;
		est = 1;
		pickNewId();
		messageList = new LinkedList<Message>();
		passive = false;
	}

	public void linkNode (Node nextNode) {
		this.nextNode = nextNode;
		nextNode.sendMessage(new Message(1,id,1));
	}

	public void initializeSmartMessage () {
		est = 0;
		messageList.removeFirst();
		nextNode.sendMessage(new Message(1, id, 0));
	}

	public void processRandomMessage() {//processes a random message from this node
		if(hasWork()) {
			processMessage(messageList.remove(ThreadLocalRandom.current().nextInt(0, messageList.size())));
		}
	}

	public void processRandomMessageNoIdChange() {
		if(hasWork()) {
			processMessageNoIdChange(messageList.remove(ThreadLocalRandom.current().nextInt(0, messageList.size())));
		}
	}

	public void processRandomSmartMessage() {
		if(hasWork()) {
			processSmartMessage(messageList.remove(ThreadLocalRandom.current().nextInt(0, messageList.size())));
		}
	}
	
	public void processBestSmartMessage () {
		if(hasWork()) {
			processSmartMessage(messageList.remove(getBestSmartMessageId()));
		}
	}
	
	public void processBestMessage() {
		if(hasWork()) {
			processMessage(messageList.remove(getBestMessageId()));
		}
	}

	

	private int getBestSmartMessageId() {
		if(messageList.size() == 1) return 0;
		int result = 0;
		for (int i=0; i<messageList.size(); i++) {
			if (messageList.get(i).est > messageList.get(result).est) {
				result = i;
			} else {
				if (messageList.get(i).est == messageList.get(result).est) {
					if (messageList.get(i).id != id) {
						result = i;
					}
				}
			}
		}
		return result;
	}

	private int getBestMessageId() {//return the id of the highest priority message
		if(messageList.size() == 1) return 0;
		int mId = 0;
		Message hiProM = messageList.getFirst();
		for(int i=1;i<messageList.size();i++) {
			if(hiProM.est < messageList.get(i).est) {
				hiProM = messageList.get(i);
				mId=i;
			} else if (hiProM.est == messageList.get(i).est){
				if (hiProM.hop < messageList.get(i).hop) {
					hiProM = messageList.get(i);
					mId=i;
				}
			} else {}
		}
		return mId;
	}

	private void processMessage(Message m) {
		if (m.est>est) {
			if (m.est == m.hop) {
				est = m.est + 1;
				pickNewId();
				nextNode.sendMessage(new Message(est,id,1));
			} else {
				nextNode.sendMessage(m.increase());
				est = m.est;
				pickNewId();
				nextNode.sendMessage(new Message(est,id,1));
			}
		} else if(m.est == est) {
			if (m.hop < m.est) {
				nextNode.sendMessage(m.increase());
			} else {
				if(m.id != id) {
					est = m.est + 1;
					pickNewId();
					nextNode.sendMessage(new Message(est,id,1));
				}
			}
		} else {}
	}
	
	private void processMessageNoIdChange(Message m) {
		if (m.est>est) {
			if (m.est == m.hop) {
				est = m.est + 1;
				nextNode.sendMessage(new Message(est,id,1));
			} else {
				nextNode.sendMessage(m.increase());
				est = m.est;
				nextNode.sendMessage(new Message(est,id,1));
			}
		} else if(m.est == est) {
			if (m.hop < m.est) {
				nextNode.sendMessage(m.increase());
			} else {
				if(m.id != id) {
					est = m.est + 1;
					nextNode.sendMessage(new Message(est,id,1));
				}
			}
		} else {}
	}
	
	public void processSmartMessage(Message m) {
		//System.out.printf("processing %d\n", m.est);
		if (est > m.est) {
			nextNode.sendMessage(m.increaseEst());
		} else if (est == m.est) {
			if (id == m.id) {
				passive = true;
			} else {
				est = m.est + 1;
				nextNode.sendMessage(m.increaseEst());
				if (passive) {
					passive = false;
					nextNode.sendMessage(new Message(est, id, 0));
				}
			}
		} else if (est < m.est) {
			if (passive) {
				passive = false;
				nextNode.sendMessage(new Message(est+1, id, 0));
			}
			if (id != m.id) {
				est = m.est + 1;
				nextNode.sendMessage(m.increaseEst());	
			} else {
				est = m.est;
				passive = true;
			}
		}
	}

	private void pickNewId() {
		id = ThreadLocalRandom.current().nextInt(1, idRange + 1);
	}

	public void sendMessage(Message m) {
		messageList.add(m);
	}

	public int getEstimate() {
		return est;
	}

	public boolean hasWork() {
		return !messageList.isEmpty();
	}

	public int getId() {
		return id;
	}

	public boolean isPassive () {
		return passive;
	}
}

/* public boolean processHighestEstimateMessage() {//processes the message with the highest estimate and hop count
		if (!messageList.isEmpty()) {
			int mId = 0;
			Message hiProM = messageList.getFirst();
			for(int i=0;i<messageList.size();i++) {
				if(hiProM.est < messageList.get(i).est) {
					hiProM = messageList.get(i);
					mId=i;
				} else if (hiProM.est == messageList.get(i).est){
					if (hiProM.hop < messageList.get(i).hop) {
						hiProM = messageList.get(i);
						mId=i;
					}
				} else {}
			}
			processMessage(messageList.remove(mId));
			return true;			
		}

		return false;
	}
	public void processSameId(Message m) {
		if (seen > m.est) {
			nextNode.sendMessage(new Message(m.est+1,m.id,1));
		} else {		
			if (m.id == id) {
				if (m.est > est) {
					if (passive) {
						nextNode.sendMessage(new Message(est+1,id,1));
						est = m.est;
					} else {
						passive = true;
						est = m.est;
					}
				} else {
					nextNode.sendMessage(new Message(m.est+1,m.id,1));			
				}				
			} else {
				nextNode.sendMessage(new Message (m.est+1,m.id,1));

			}
		} 
		if (m.est > seen) {
			seen = m.est;
			if (passive && est < seen) {
				passive = false;
				nextNode.sendMessage(new Message (est+1,id,1));
			}
		}
	}
	public void processRandomSameIdMessage() {
		processSameId(messageList.remove(ThreadLocalRandom.current().nextInt(0, messageList.size())));
	}
 */
