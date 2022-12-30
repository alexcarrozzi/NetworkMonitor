package network;

import java.util.ArrayList;

public class LatencyQueue extends ArrayList<Integer> {
	private static final long serialVersionUID = 1599820640754556070L;

	public LatencyQueue() {
		super();
	}

	public Integer dequeue() {
		return this.remove(0);
	}

	public void enqueue(Integer add) {
		this.add(add);
	}

	public int purge() {
		int size = this.size();

		for (Integer i : this) {
			this.remove(i);
		}

		return size;
	}

	public Integer peek() {
		return this.get(0);
	}

	public String dump() {
		StringBuilder sb = new StringBuilder();

		for (Integer i : this) {
			sb.append(i.toString());
			sb.append(" ");
		}

		return sb.toString();
	}
}
