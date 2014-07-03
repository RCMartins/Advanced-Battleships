//package pt.rmartins.battleships.network;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.List;
//
//import pt.rmartins.battleships.objects.Coordinate;
//import pt.rmartins.battleships.objects.Fleet;
//import pt.rmartins.battleships.objects.Game;
//import pt.rmartins.battleships.objects.GameClass;
//import pt.rmartins.battleships.objects.Player;
//import pt.rmartins.battleships.objects.Ship;
//import pt.rmartins.battleships.objects.ShipClass;
//import pt.rmartins.battleships.objects.Player.ShotAllResults;
//import pt.rmartins.battleships.objects.modes.GameMode;
//import pt.rmartins.battleships.utilities.DataLoader;
//
//public class ConnectionTCP implements Connection {
//
//	public static int PORT = 24242;
//
//	public static final int RANDOMSIZE = 20;
//
//	private class PortListener implements Runnable {
//
//		private static final int MAXBYTES = 1 << 16;
//		private final String address;
//		private final int port;
//		private ConnectionStatus connectionStatus;
//		private Socket socket;
//		private InputStream inputStream;
//		private OutputStream outputStream;
//		private boolean kill;
//
//		public PortListener(String ip, int port) throws UnknownHostException {
//			this.address = ip;
//			this.port = port;
//			connectionStatus = ConnectionStatus.NoComunication;
//			kill = false;
//			try {
//				serverSocket = new ServerSocket(port);
//			} catch (final IOException e) {
//			}
//		}
//
//		private boolean connect() {
//			try {
//				if (isPlayer1) {
//					socket = new Socket(address, port);
//				} else {
//					serverSocket.setSoTimeout(100);
//					socket = serverSocket.accept();
//				}
//
//				connectionStatus = ConnectionStatus.ConnectionEstablished;
//
//				inputStream = socket.getInputStream();
//				outputStream = socket.getOutputStream();
//				return true;
//			} catch (final IOException e) {
//				return false;
//			}
//		}
//
//		@Override
//		public void run() {
//			for (;;) {
//				while (connectionStatus == ConnectionStatus.NoComunication && !connect()) {
//					if (kill) {
//						return;
//					}
//				}
//				try {
//					handleMessage(inputStream);
//				} catch (final Exception e) {
//					connectionStatus = ConnectionStatus.NoComunication;
//					//					JOptionPane.showConfirmDialog(null, e.getMessage() + "\nTrying to restablished connection...",
//					//							"Error", JOptionPane.OK_CANCEL_OPTION);
//					//					if (isPlayer1) {
//					//						// try {
//					//						// // wait(100); // TODO não funciona - ver na net os problemas que dá por wait num thread sem
//					//						// // ter
//					//						// // monitors bla bla
//					//						// } catch (final InterruptedException e2) {
//					//						// continue;
//					//						// }
//					//					}
//				}
//			}
//		}
//
//		private void handleMessage(InputStream in) throws Exception {
//			Player enemy;
//
//			final byte[] buf = new byte[MAXBYTES];
//			int size;
//			for (;;) {
//				readBytes(in, buf, 1);
//				System.out.println("buf: " + buf[0]);
//				switch (buf[0]) {
//				case 0: // receive <Boolean (Player1 is playing first)>
//					readBytes(in, buf, 1);
//					game.setPlayer1First(buf[0] == 1);
//
//					readBytes(in, buf, RANDOMSIZE);
//					List<Byte> values = new ArrayList<Byte>();
//					for (int k = 0; k < RANDOMSIZE; k++) {
//						values.add(buf[k]);
//					}
//					game.setRandomVar(values);
//
//					game.initializePlacingShips(false);
//
//					break;
//				case 1: // Ships V1
//					readBytes(in, buf, 3);
//					int maxX = buf[0];
//					int maxY = buf[1];
//					size = buf[2];
//					readBytes(in, buf, size);
//
//					List<Integer> fleet = new ArrayList<Integer>(size);
//					for (int i = 0; i < size; i++) {
//						fleet.add((int) buf[i]);
//					}
//					game.setFleet(new Fleet(maxX, maxY, fleet));
//
//					break;
//				case 5: // GameMode
//					readBytes(in, buf, 2);
//					size = buf[0] * 100 + buf[1];
//
//					readBytes(in, buf, size);
//					StringBuilder originalFile = new StringBuilder(size);
//
//					for (int i = 0; i < size; i++) {
//						originalFile.append((char) buf[i]);
//					}
//
//					List<GameMode> list = new ArrayList<GameMode>(1);
//					DataLoader.loadGameModes(list, originalFile.toString());
//					game.setGameMode(list.get(0));
//
//					break;
//				case 10: // Receive Enemy Fleet Position
//					readBytes(in, buf, 1);
//					final int nShips = buf[0];
//					readBytes(in, buf, nShips * 4);
//
//					enemy = getPlayer().getEnemy();
//
//					ShipClass ship;
//					for (int i = 0, k = 0; k < nShips; i += 4, k++) {
//						ship = new ShipClass(buf[i], buf[i + 1], buf[i + 2], buf[i + 3]);
//						enemy.placeShipNetwork(ship);
//					}
//					enemy.tryToSetReady();
//
//					break;
//				case 11: // Cancel Player Ready
//					enemy = getPlayer().getEnemy();
//					enemy.cancelSetReady();
//					break;
//				// case 20: // Receive Play
//				// readBytes(in, buf, 1);
//				// int nShoots = buf[0];
//				// readBytes(in, buf, nShoots * 2);
//				//
//				// enemy = getPlayer().getEnemy();
//				// for (int i = 0, k = 0; k < nShoots; i += 2, k++) {
//				// enemy.setPosition(new Coordinate(buf[i], buf[i + 1]));
//				// enemy.chooseTarget();
//				// }
//				// enemy.setNetworkCounterList(new ArrayList<Coordinate>(0));
//				// enemy.shotAll();
//				//
//				// break;
//				case 21: // Receive Play & Counters
//
//					readBytes(in, buf, 2);
//					int nShoots = buf[0];
//					int nCounters = buf[1];
//					readBytes(in, buf, nShoots * 2);
//
//					enemy = getPlayer().getEnemy();
//					for (int i = 0, k = 0; k < nShoots; i += 2, k++) {
//						final Coordinate newPosition = new Coordinate(buf[i], buf[i + 1]);
//						System.out.print(newPosition);
//						enemy.setPosition(newPosition);
//						enemy.chooseTarget();
//					}
//					System.out.println();
//
//					readBytes(in, buf, nCounters * 2);
//					List<Coordinate> counters = new ArrayList<Coordinate>(nCounters);
//					for (int i = 0, k = 0; k < nCounters; i += 2, k++) {
//						counters.add(new Coordinate(buf[i], buf[i + 1]));
//					}
//					enemy.setNetworkCounterList(counters);
//
//					while (enemy.shotAll() != ShotAllResults.ShotsFired)
//						// TODO: aqui está resolvido o problema da rede (while)
//						;
//
//					break;
//				// case 22:
//				// readBytes(in, buf, 1);
//				// nShoots = buf[0];
//				// readBytes(in, buf, nShoots * 2);
//				//
//				// enemy = getPlayer().getEnemy();
//				// final List<Coordinate> explodeCoordinates = new ArrayList<Coordinate>(nShoots);
//				// for (int i = 0, k = 0; k < nShoots; i += 2, k++) {
//				// final Coordinate newPosition = new Coordinate(buf[i], buf[i + 1]);
//				// explodeCoordinates.add(newPosition);
//				// }
//				// enemy.setNetworkExplosion(explodeCoordinates);
//				//
//				// break;
//				case 30: // Pause / Unpause
//					game.pauseUnpauseGame(false);
//					break;
//				// case 31:
//				// readBytes(in, buf, 1);
//				// game.setPlayer1First(buf[0] == 1);
//				// game.setGameMenu(GameMenu.PlacingShips);
//				// break;
//				}
//			}
//		}
//
//		private void readBytes(InputStream in, byte[] buf, int n) throws Exception {
//			if (n > 0) {
//				final int b = in.read(buf, 0, n);
//				if (b < n)
//					throw new Exception("Connection Lost");
//			}
//		}
//
//		private Player getPlayer() {
//			return isPlayer1 ? game.getPlayer1() : game.getPlayer2();
//		}
//
//		public void sendMessage(byte[] data) throws IOException {
//			sendMessage(data, data.length);
//		}
//
//		public void sendMessage(byte[] data, int n) throws IOException {
//			outputStream.write(data, 0, n);
//		}
//
//		// public void setIP(String ip) throws UnknownHostException {
//		// serverAddress = ip == null ? null : InetAddress.getByName(ip);
//		// }
//
//		public ConnectionStatus getStatus() {
//			return connectionStatus;
//		}
//
//	}
//
//	private final Game game;
//	private final boolean isPlayer1;
//	private final PortListener portListener;
//	private Thread listenerThread;
//	private ServerSocket serverSocket;
//
//	public ConnectionTCP(Game game, boolean isPlayer1, String ip) throws UnknownHostException {
//		this.game = game;
//		this.isPlayer1 = isPlayer1;
//		portListener = new PortListener(ip, PORT);
//		reconect();
//	}
//
//	@Override
//	public void sendInitializingInformation(boolean player1First) {
//		final byte[] data = new byte[2 + RANDOMSIZE];
//		data[0] = 0;
//		data[1] = (byte) (player1First ? 1 : 0);
//		byte[] bytes = new byte[RANDOMSIZE];
//		GameClass.random.nextBytes(bytes);
//		game.setRandomVar(bytes);
//		for (int i = 0, k = 2; i < RANDOMSIZE; i++, k++) {
//			data[k] = bytes[i];
//		}
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	// @Override
//	// public void sendConnectionEstablished() {
//	// byte[] data = new byte[1];
//	// data[0] = 1;
//	// try {
//	// portListener.sendMessage(data);
//	// } catch (IOException e) {
//	// return;
//	// }
//	// }
//
//	@Override
//	public void sendShipsV1(Fleet fleet) {
//		final byte[] data = new byte[4 + ShipClass.numberOfShips()];
//		data[0] = 1;
//		data[1] = (byte) fleet.maxX;
//		data[2] = (byte) fleet.maxY;
//		data[3] = (byte) ShipClass.numberOfShips();
//		int k = 4;
//		for (final int n : fleet.getFleetNumbers()) {
//			data[k] = (byte) n;
//			k++;
//		}
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	@Override
//	public void sendGameMode(GameMode mode) {
//		String file = mode.toFileLanguage();
//		if (file.length() > 100 * 255)
//			throw new RuntimeException();
//
//		final byte[] data = new byte[3 + file.length()];
//		data[0] = 5;
//		data[2] = (byte) (file.length() % 100);
//		data[1] = (byte) ((file.length() - data[2]) / 100);
//
//		int k = 3;
//		for (char c : file.toCharArray()) {
//			data[k] = (byte) c;
//			k++;
//		}
//
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	@Override
//	public void sendShipsPosition(List<Ship> ships) {
//		final byte[] data = new byte[2 + ships.size() * ShipClass.SHIPDATASIZE];
//		data[0] = 10;
//		data[1] = (byte) ships.size();
//		int k = 2;
//		for (final Ship ship : ships) {
//			final byte[] shipData = ShipClass.getShipBytes(ship);
//			for (int i = 0; i < shipData.length; i++) {
//				data[k + i] = shipData[i];
//			}
//			k += shipData.length;
//		}
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	@Override
//	public void sendCancelPlaceShips() {
//		final byte[] data = new byte[1];
//		data[0] = 11;
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	// @Override
//	// public void sendShots(List<Coordinate> shots) {
//	// final byte[] data = new byte[2 + shots.size() * 2];
//	// data[0] = 20;
//	// data[1] = (byte) shots.size();
//	// int k = 2;
//	// for (final Coordinate shot : shots) {
//	// data[k + 0] = (byte) shot.x;
//	// data[k + 1] = (byte) shot.y;
//	// k += 2;
//	// }
//	// try {
//	// portListener.sendMessage(data);
//	// } catch (final IOException e) {
//	// return;
//	// }
//	// }
//
//	@Override
//	public void sendShotsAndCounters(List<Coordinate> shots, List<Coordinate> counters) {
//		final byte[] data = new byte[3 + shots.size() * 2 + counters.size() * 2];// + RANDOMSIZE];
//		data[0] = 21;
//		data[1] = (byte) shots.size();
//		data[2] = (byte) counters.size();
//		int k = 3;
//		for (final Coordinate shot : shots) {
//			data[k + 0] = (byte) shot.x;
//			data[k + 1] = (byte) shot.y;
//			k += 2;
//		}
//		for (final Coordinate shot : counters) {
//			data[k + 0] = (byte) shot.x;
//			data[k + 1] = (byte) shot.y;
//			k += 2;
//		}
//		// byte[] bytes = new byte[RANDOMSIZE];
//		// GameClass.random.nextBytes(bytes);
//		// game.setRandomVar(bytes);
//		// for (int i = 0; i < RANDOMSIZE; i++, k++) {
//		// data[k] = bytes[i];
//		// }
//		try {
//			portListener.sendMessage(data);
//			System.out.println("shots sent! -> " + shots + " | " + counters);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	// @Override
//	// public void sendExplosion(List<Coordinate> explodeCoordinates) {
//	// final byte[] data = new byte[2 + explodeCoordinates.size() * 2];
//	// data[0] = 22;
//	// data[1] = (byte) explodeCoordinates.size();
//	// int k = 2;
//	// for (final Coordinate shot : explodeCoordinates) {
//	// data[k + 0] = (byte) shot.x;
//	// data[k + 1] = (byte) shot.y;
//	// k += 2;
//	// }
//	// try {
//	// portListener.sendMessage(data);
//	// // System.out.println("shots sent! -> " + shots + " | " + counters);
//	// } catch (final IOException e) {
//	// return;
//	// }
//	// }
//
//	@Override
//	public void sendPauseUnPause() {
//		final byte[] data = new byte[1];
//		data[0] = 30;
//		try {
//			portListener.sendMessage(data);
//		} catch (final IOException e) {
//			return;
//		}
//	}
//
//	// @Override
//	// public void sendRestartGame(boolean nextPlayer1First) {
//	// final byte[] data = new byte[2];
//	// data[0] = 0;
//	// data[1] = (byte) (nextPlayer1First ? 1 : 0);
//	// try {
//	// portListener.sendMessage(data);
//	// } catch (final IOException e) {
//	// return;
//	// }
//	// }
//
//	@Override
//	public ConnectionStatus getStatus() {
//		return portListener.getStatus();
//	}
//
//	@Override
//	public void closeConnection() {
//		portListener.kill = true;
//	}
//
//	@Override
//	public void reconect() {
//		listenerThread = new Thread(portListener);
//		listenerThread.setDaemon(true);
//		listenerThread.start();
//	}
//
//}
