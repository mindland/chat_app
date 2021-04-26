package com.appchat;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.*;

/**
 *
 * @author Pham Vo Duc Phong
 */
public class ServerUI extends javax.swing.JFrame {

	// Socket Related
	public static SimpleDateFormat formatter = new SimpleDateFormat("[hh:mm a]");
	private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();
	private static final int MAX_CONNECTED = 50;
	private static int PORT = 1111;
	private static ServerSocket server = null;
	private static volatile boolean exit = false;

	// Variables declaration - do not modify
	private JLabel jLabel1;
	private JScrollPane jScrollPane1;
	private JTextArea txtArea;
	private JLabel portLabel;
	private JButton startBtn;
	// End of variables declaration

	public ServerUI() {
		initComponents();
	}

	private void initComponents() {

		jLabel1 = new JLabel();
		jScrollPane1 = new JScrollPane();
		txtArea = new JTextArea();
		startBtn = new JButton();
		portLabel = new JLabel();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("SERVER UI");
		setResizable(false);

		jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
		jLabel1.setText("SERVER CHAT");
		jLabel1.setVerifyInputWhenFocusTarget(false);

		txtArea.setBackground(Color.BLACK);
		txtArea.setForeground(Color.GREEN);
		txtArea.setFont(new Font("Monospaced", 0, 14)); // NOI18N
		jScrollPane1.setViewportView(txtArea);

		startBtn.setFont(new Font("Tahoma", 1, 14)); // NOI18N
		startBtn.setText("START");
		startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBtnAction(e);
			}
		});

		portLabel.setFont(new Font("Tahoma", 0, 12)); // NOI18N
		portLabel.setText("Port:");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addComponent(portLabel, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
						.addGap(65, 65, 65))
				.addGroup(GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addContainerGap(242, Short.MAX_VALUE)
								.addComponent(startBtn, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
								.addGap(222, 222, 222)));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 44,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(portLabel))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 302, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(startBtn, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
								.addContainerGap(8, Short.MAX_VALUE)));

		pack();
		setLocationRelativeTo(null);
	}// </editor-fold>

	private void startBtnAction(ActionEvent evt) {
		if (startBtn.getText().equalsIgnoreCase("start")) {
			start();
			startBtn.setText("STOP");
		} else {
			startBtn.setText("START");
			stop();
		}

		// refresh UI
		refreshUIComponents();
	}

	public void refreshUIComponents() {
		portLabel.setText("Port: " + (!exit ? PORT : ""));
	}

	public static void start() {
		exit = false;
		new Thread(new ServerHandler()).start();
	}

	public static void stop() {
		if (!server.isClosed())
			try {	
				addToLogs("Chat server stopped...");
				exit = true;
				broadcastMessage("Server stopped");
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void broadcastMessage(String message) {
		for (PrintWriter p : connectedClients.values()) {
			p.println(message);
		}
	}

	public static void addToLogs(String message) {
		System.out.printf("%s %s\n", formatter.format(new Date()), message);
	}

	private static class ServerHandler implements Runnable{
		@Override
		public void run() {
			try {
				server = new ServerSocket(PORT);
				addToLogs("Server started on port: " + PORT);
				addToLogs("Now listening for connections...");
				if(exit) server.close();
				while(true) {
					if(exit) {
						break;
					}
					if (connectedClients.size() <= MAX_CONNECTED){
						new Thread(new ClientHandler(server.accept())).start();
					}
				}
			}
			catch (Exception e) {
//				e.printStackTrace();
			}
		}
	}

	// Start of Client Handler
	private static class ClientHandler implements Runnable {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String name;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			addToLogs("Client connected: " + socket.getInetAddress());
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				while (true) {
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (connectedClients) {
						if (!name.isEmpty() && !connectedClients.keySet().contains(name))
							break;
						else
							out.println("INVALIDNAME");
					}
				}
				out.println("Welcome to the chat group, " + name.toUpperCase() + "!");
				addToLogs(name.toUpperCase() + " has joined.");
				broadcastMessage("[SYSTEM] " + name.toUpperCase() + " has joined.");
				connectedClients.put(name, out);
				String message;
				out.println("You may join the chat now...");
				while ((message = in.readLine()) != null && !exit) {
					if (!message.isEmpty()) {
						if (message.toLowerCase().equals("/quit"))
							break;
						broadcastMessage(String.format("[%s] %s", name, message));
					}
				}
			} catch (Exception e) {
				addToLogs(e.getMessage());
			} finally {
				if (name != null) {
					addToLogs(name + " is leaving");
					connectedClients.remove(name);
					broadcastMessage(name + " has left");
				}
			}
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerUI frame = new ServerUI();
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					SwingUtilities.updateComponentTreeUI(frame);
					// Logs
					System.setOut(new PrintStream(new TextAreaOutputStream(frame.txtArea)));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
