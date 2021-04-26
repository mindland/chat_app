package com.appchat;

import java.awt.*;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

/**
 *
 * @author Pham Vo Duc Phong
 */
public class ClientUI extends javax.swing.JFrame {
//	public static SimpleDateFormat formatter = new SimpleDateFormat("[MM/hh/yy hh:mm a]");
	public static SimpleDateFormat formatter = new SimpleDateFormat("[hh:mm a]");
	// Socket Related
	private static Socket clientSocket = null;
	private static int PORT = 1111;
	private PrintWriter out;
	private String clientName;
	
	// Variables declaration - do not modify
	private JButton connectBtn;
	private static JTextArea chatArea;
	private JTextField txtMessage;
	private JLabel clientLabel;
	private JScrollPane jScrollPane1;
	private JTextField nameField;
	private JLabel nameLabel;
	// End of variables declaration
	
	public ClientUI() {
		initComponents();
	}

	private void initComponents() {

		clientLabel = new JLabel();
		nameLabel = new JLabel();
		nameField = new JTextField();
		connectBtn = new JButton();
		jScrollPane1 = new JScrollPane();
		chatArea = new JTextArea();
		txtMessage = new JTextField();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("CLIENT UI");

		clientLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
		clientLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clientLabel.setText("CLIENT");

		nameLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		nameLabel.setText("Name:");

		nameField.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		nameField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nameFieldActionPerformed(evt);
			}
		});

		connectBtn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
		connectBtn.setText("Connect");
		connectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				connectBtnActionPerformed(evt);
			}
		});

		chatArea.setBackground(Color.BLACK);
		chatArea.setForeground(Color.WHITE);
		chatArea.setDisabledTextColor(Color.GREEN);
		chatArea.setFont(new Font("Monospaced", 0, 14)); // NOI18N
		chatArea.setLineWrap(true);
		chatArea.setEnabled(false);
		jScrollPane1.setViewportView(chatArea);

		txtMessage.setFont(new Font("Tahoma", 0, 14)); // NOI18N
        txtMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                keyPressed(evt);
            }
        });

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(clientLabel, GroupLayout.PREFERRED_SIZE, 76,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 283, Short.MAX_VALUE)
						.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 43,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(nameField, GroupLayout.PREFERRED_SIZE, 73,
								GroupLayout.PREFERRED_SIZE)
						.addGap(26, 26, 26).addComponent(connectBtn).addContainerGap())
				.addComponent(jScrollPane1).addComponent(txtMessage));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(clientLabel, GroupLayout.PREFERRED_SIZE, 41,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(nameLabel)
								.addComponent(nameField, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(connectBtn))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 315,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
					.addComponent(txtMessage, GroupLayout.PREFERRED_SIZE, 38,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
		setLocationRelativeTo(null);
	}// </editor-fold>

	private void nameFieldActionPerformed(ActionEvent evt) {

	}

	private void connectBtnActionPerformed(ActionEvent evt) {
		if (connectBtn.getText().equals("Connect")) {
			start();
		} else {
			connectBtn.setText("Connect");
			stop();
		}
	}

	private void keyPressed(ActionEvent  evt) {
		String message = txtMessage.getText().trim();
		if(!message.isEmpty()) {
			out.println(message);
			txtMessage.setText("");
		}
	}

	public void start() {
		try {
			clientName = nameField.getText().trim();
			nameField.setEnabled(false);
			if(clientName.equals("")) {
				addToLogs("Please enter name to connect server ...");
				return;
			}
				 
			
			clientSocket = new Socket("localhost", PORT);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			new Thread(new Listener()).start();
			// send name to server 
			out.println(clientName);
			
			connectBtn.setText("Leave");
		} catch (Exception err) {
			addToLogs("[ERROR] " + err.getLocalizedMessage());
		}
	}

	public void stop() {
		if (clientSocket != null && !clientSocket.isClosed() ) {
			try {
				nameField.setEnabled(true);
				clientSocket.close();
			} catch (IOException e1) {
				System.out.println();
			}
		}
	}

	public static void addToLogs(String message) {
		System.out.printf("%s %s\n", formatter.format(new Date()), message);
	}

	private static class Listener implements Runnable {
		private BufferedReader in;

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String read;
				while (true) {
					read = in.readLine();
					if (read != null && !(read.isEmpty()))
						addToLogs(read);
				}
			} catch (IOException e) {
				return;
			}
		}

	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {	
					ClientUI frame = new ClientUI();
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					SwingUtilities.updateComponentTreeUI(frame);
					//Logs
					System.setOut(new PrintStream(new TextAreaOutputStream(frame.chatArea)));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


}