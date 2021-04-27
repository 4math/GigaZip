class LNode { 
	byte bt;
	LNode next;
	
	public LNode() {
	}
	
	public LNode(byte bt) {
		next = null;
		this.bt = bt;
	}

	public LNode(byte bt, LNode next) {
		this.bt = bt;
		this.next = next;
	}

	public void setNext(LNode next) {
		this.next = next;
	}

	public void setData(byte bt) {
		this.bt = bt;
	}

	public LNode getLink() {
		return next;
	}

	public int getData() {
		return bt;
	}
}