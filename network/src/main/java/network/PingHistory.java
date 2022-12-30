package network;

import java.util.ArrayList;

public class PingHistory extends ArrayList<Integer> {
	public PingHistory() {
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

	public Double getAverage() {
		Double ret = 0.0;

		for (Integer i : this) {
			ret += i;
		}

		return ret / this.size();
	}

	public int getMedian() {
		return (this.get(this.size() / 2) + this.get(this.size() / 2 - 1)) / 2;
	}

	public void clean() {
		for (int i = 0; i < this.size() - 100; i++) {
			this.dequeue();
		}
	}
}
