package network;

public class Runner {

	public static void main(String[] args) throws InterruptedException {
		Network nw = new Network("cloud.google.com");
		nw.setVisible(true);
		// nw.pingHost();
		nw.startTimer(1, 0);
	}

}
