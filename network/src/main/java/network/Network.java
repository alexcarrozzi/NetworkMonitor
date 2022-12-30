package network;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

public class Network extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -799006946956772480L;

	private PingHistory history;

	private Long connectedSince = 0l;
	private Long disconnectedSince = 0l;

	private JPanel pConnContainer;
	private JPanel pStatsContainer;

	private JLabel lConn;
	private JLabel lSince;

	private JLabel lAvglat;
	private JLabel lMedlat;
	private JLabel lPingUp;
	private JLabel lTimeUp;
	private JLabel lTotDown;

	private boolean initalized = false;

	private NumberFormat formatter;

	private static Long globalStartTime = 0l;
	private Long lastDisconnectedAt = 0l;

	XYLineAndShapeRenderer renderer = null;

	private Long totalDownTime = 0l;
	private int totalPingsMissed = 0;

	private int durationDisconn = 1;
	private int lastSuccessfulLat = 0;

	private String hostname;
	private XYSeries series;

	public static final int MAX_CHART_WIDTH = 100;

	private boolean CONNECTED = false;
	private boolean PRIOR_CONN_STATUS = false;

	private DisconnectSquare dcSQ;

	Integer lastStep = 0;
	Integer lastLat = 0;
	Integer resStatus = -1;
	Integer resTime = -1;

	Integer disconnStep = 0;
	Integer disconnLat = 0;

	private Double AVGLATENCY = -1.0;
	private int MEDLATENCY = -1;
	private double PCNT_PING_UP = -1.00f;
	private double PCNT_TIME_TUP = -1.00f;

	public Network(String host) {
		// Timestamp Global Start
		globalStartTime = this.timestamp();
		connectedSince = globalStartTime;
		disconnectedSince = globalStartTime;

		formatter = new DecimalFormat("#0.00");

		history = new PingHistory();

		this.hostname = host;
		series = new XYSeries("Latency (ms)");

		// Chart Components
		var coll = new XYSeriesCollection();
		coll.addSeries(series);
		XYDataset dataset = coll;
		JFreeChart chart = createChart(dataset);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);

		pConnContainer = new JPanel();
		pConnContainer.setLayout(new FlowLayout(FlowLayout.LEADING));
		pConnContainer.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		pConnContainer.setBackground(Color.black);

		pStatsContainer = new JPanel();
		pStatsContainer.setLayout(new FlowLayout());
		pStatsContainer.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		Font statsFont = new Font("Dialog", Font.ITALIC, 12);
		// pStatsContainer.setFont(statsFont);

		lConn = new JLabel();
		lSince = new JLabel();
		lSince.setForeground(Color.WHITE);

		lAvglat = new JLabel();
		lMedlat = new JLabel();
		lPingUp = new JLabel();
		lTimeUp = new JLabel();
		lTotDown = new JLabel();

		lAvglat.setFont(statsFont);
		lMedlat.setFont(statsFont);
		lPingUp.setFont(statsFont);
		lTimeUp.setFont(statsFont);
		lTotDown.setFont(statsFont);
		lSince.setFont(statsFont);

		// Call Update Stats
		updateStatLabels(formatter);

		pConnContainer.add(lConn);
		pConnContainer.add(lSince);

		pStatsContainer.add(lAvglat);
		pStatsContainer.add(lMedlat);
		pStatsContainer.add(lPingUp);
		pStatsContainer.add(lTimeUp);
		pStatsContainer.add(lTotDown);

		add(pConnContainer, BorderLayout.PAGE_START);
		add(pStatsContainer);
		add(chartPanel, BorderLayout.PAGE_END);

		pConnContainer.setVisible(true);
		pConnContainer.add(lConn);
		pConnContainer.add(lSince);

		lConn.setVisible(true);
		lSince.setVisible(true);

		pStatsContainer.setVisible(true);
		lAvglat.setVisible(true);
		lMedlat.setVisible(true);
		lPingUp.setVisible(true);
		lTimeUp.setVisible(true);
		lTotDown.setVisible(true);

		pack();
		setTitle("Line chart");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void updateStatLabels(NumberFormat f) {
		lAvglat.setText("Average Latency: " + f.format(AVGLATENCY));
		lMedlat.setText("Median Latency: " + f.format(MEDLATENCY));
		lPingUp.setText("% Ping Up: " + f.format(PCNT_PING_UP) + "%");
		lTimeUp.setText("% Time Up: " + f.format(PCNT_TIME_TUP) + "%");
		lTotDown.setText("Total Downtime: " + totalDownTime / 1000.0 + " seconds");
		lConn.setForeground(CONNECTED ? Color.GREEN : Color.RED);
		lConn.setText(CONNECTED ? "CONNECTED" : "DISCONNECTED");
		lSince.setText(" since " + formatDateTimestamp(CONNECTED ? connectedSince : disconnectedSince).toString());
	}

	private JFreeChart createChart(XYDataset dataset) {

		/*
		 * JFreeChart chart = ChartFactory.createXYLineChart("Current Latency", "",
		 * "Milliseconds", dataset, PlotOrientation.VERTICAL, true, true, false);
		 */

		NumberAxis xAxis = new NumberAxis("Time");
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		Font xFont = new Font("Dialog", Font.PLAIN, 14);
		xAxis.setTickLabelFont(xFont);

		// Manually set range when updating?
		xAxis.setFixedAutoRange(MAX_CHART_WIDTH);

		NumberAxis yAxis = new NumberAxis("Milliseconds");
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		Font yFont = new Font("Dialog", Font.PLAIN, 14);
		yAxis.setTickLabelFont(yFont);

		XYPlot plot = new XYPlot(new XYSeriesCollection(series), xAxis, yAxis, new XYLineAndShapeRenderer(true, false));

		LegendTitle l = new LegendTitle(plot);

		JFreeChart chart = new JFreeChart("Current Latency", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		chart.addLegend(l);
		// XYPlot plot = chart.getXYPlot();

		renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesStroke(0, new BasicStroke(1.0f));
		renderer.setSeriesShapesVisible(0, false);

		plot.setRenderer(renderer);
		plot.setBackgroundPaint(Color.white);

		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);

		chart.getLegend().setFrame(BlockBorder.NONE);

		chart.setTitle(new TextTitle("Network Latency", new Font("Serif", java.awt.Font.BOLD, 18)));

		return chart;
	}

	public void startTimer(int frequencySecs, int queueTicks) {
		// queue = new LatencyQueue();

		new java.util.Timer().schedule(new TimerTask() {
			Timer pingTimer = new Timer();
			int steps = 0;

			@Override
			public void run() {
				Integer[] resp = new Integer[2];
				// lastStep = 0;
				// lastLat = 0;
				resStatus = -1;
				resTime = -1;

				// IF DISCONNECTD
				if (!CONNECTED && lastDisconnectedAt != 0l) {
					totalDownTime += (timestamp() - lastDisconnectedAt);
					totalPingsMissed++;
				}

				addOrUpdateDisconSquare(CONNECTED);

				// CALCULATE STATS
				if (history.size() > 1) {
					initalized = true;
					AVGLATENCY = history.getAverage();
					MEDLATENCY = history.getMedian();
					if (totalPingsMissed == 0) {
						PCNT_PING_UP = 100;
					} else {
						PCNT_PING_UP = ((steps - totalPingsMissed) / (steps * 1.0)) * 100.0;
					}
					if (totalPingsMissed == 0) {
						PCNT_TIME_TUP = 100;
					} else {
						PCNT_TIME_TUP = (((timestamp() - globalStartTime) - totalDownTime)
								/ (timestamp() - globalStartTime) * 1.0) * 100.0;
					}

					// Update Stats
					updateStatLabels(formatter);
					// history.clean();
				}

				try {
					PRIOR_CONN_STATUS = CONNECTED;
					resp = singlePing();
					resStatus = resp[0];
					resTime = resp[1];

					System.out.println("Ping: " + resTime + "; Avg Latency: " + formatter.format(AVGLATENCY)
							+ "; Med Latency: " + MEDLATENCY + "; % Ping Up: " + formatter.format(PCNT_PING_UP)
							+ "%; % Time Up:" + formatter.format(PCNT_TIME_TUP) + "%" + "; Total Downtime: "
							+ totalDownTime / 1000.0 + " seconds");

					// queue.enqueue(resTime);
					history.enqueue(resTime);

					// series.add(steps++, queue.peek());
					series.add(steps++, resTime);

					CONNECTED = true;

					lastDisconnectedAt = 0l;
					lastSuccessfulLat = resTime;
				} catch (BackupException e) {
					CONNECTED = false;
					lastDisconnectedAt = timestamp();
					series.add(steps++, lastLat);
					System.err.println(e + "! Avg Latency: " + formatter.format(AVGLATENCY) + "; Med Latency: "
							+ MEDLATENCY + "; % Ping Up: " + formatter.format(PCNT_PING_UP) + "%; % Time Up:"
							+ formatter.format(PCNT_TIME_TUP) + "%" + "; Total Downtime: " + totalDownTime / 1000.0
							+ " seconds");
				}
				lastLat = resTime;
				lastStep = steps;
			}

		}, 1000 * frequencySecs, 1000 * frequencySecs);

	}

	private void addOrUpdateDisconSquare(boolean conn) {
		if (initalized) {
			if (!conn && PRIOR_CONN_STATUS) {
				// IF NEWLY DISCONNECTED
				// grab last max before disconnect
				// set recrangle height
				disconnectedSince = timestamp();
				connectedSince = 0l;
				disconnStep = lastStep;
				disconnLat = lastSuccessfulLat;
				dcSQ = new DisconnectSquare(disconnStep, 0, 1, disconnLat);
				System.out.println(dcSQ.toString());
				renderer.addAnnotation(dcSQ.getAnnotation(), Layer.BACKGROUND);
			} else if (!conn && !PRIOR_CONN_STATUS) {
				// IF STILL DISCONNECTED
				// update rectangle width with latest ping
				dcSQ.updateSquare(disconnStep, 0, durationDisconn++, disconnLat);
				System.out.println(dcSQ.toString());
			} else if (conn && !PRIOR_CONN_STATUS) {
				// IF FRESHLY RECONNECTED
				// terminate updating rectangle width (do nothing?)
				durationDisconn = 1;
				disconnectedSince = 0l;
				connectedSince = timestamp();
				disconnStep = 0;
				disconnLat = 0;
			}
		}
	}

	private Integer[] singlePing() throws BackupException {
		String ip = this.hostname;
		Integer[] retArr = new Integer[2];
		Integer resTime = -1;
		Integer status = -1;

		String pingCmd = "ping -n 1 " + ip;
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);

			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				// System.out.println("Raw Res:" + inputLine);
				if (inputLine.contains("=") && inputLine.contains("Reply from")) {
					resTime = Integer
							.valueOf(inputLine.substring(inputLine.indexOf("time=") + 5, inputLine.indexOf("ms")));
					status = 1;

					retArr[0] = status;
					retArr[1] = resTime;

					// queue.enqueue(resTime);

				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println(e);
		}

		if (resTime == null || status == null || resTime == -1 || status == -1) {
			throw new BackupException("Ping Is Latent");
		}
		// if (queue.size() > 0) {
		// queue.dequeue();
		// }
		// System.out.println(queue.dump());

		return retArr;
	}

	public void pingHost() {
		Long baselineStart = this.timestamp();
		String ip = this.hostname;
		int resTime = 0;
		Long currentX = 0L;

		String pingCmd = "ping -t " + ip;
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(pingCmd);

			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("=")) {
					resTime = Integer.valueOf(inputLine.substring(inputLine.indexOf("=") + 1, inputLine.indexOf("ms")));
				}
				currentX = (this.timestamp() - baselineStart) / 1000;
				System.out.println("X:" + currentX + " Y:" + resTime);

				series.add(Double.valueOf(currentX), Double.valueOf(resTime));
			}
			in.close();

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private Long timestamp() {
		return System.currentTimeMillis();
	}

	private Date formatDateInstant(Instant i) {
		return Date.from(i);
	}

	private Date formatDateTimestamp(Long l) {
		return Date.from(Instant.ofEpochMilli(l));
	}
}
