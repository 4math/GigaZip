
class SingleLinkedList { 
	LNode head;
	LNode tail;
	int size;

	public SingleLinkedList() { 
		head = null;
		tail = null;
		size = 0;
	}

	public boolean isEmpty() {
		return head == null;
	}

	public int getSize() {
		return size;
	}

	public void insertAtEnd(byte bt) {
		LNode node = new LNode(bt, null);
		size++;
		if (head == null) {
			head = node;
			tail = head;
		} else {
			tail.setNext(node);
			tail = node;
		}
	}

}