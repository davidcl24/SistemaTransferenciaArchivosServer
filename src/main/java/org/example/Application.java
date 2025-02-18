package org.example;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Application {
    public ServerSocket serverSocket;
    private static final int PORT = 44444;
    public static int CONNECTIONS = 0;
    public static int MAX = 10;
    public static Socket[] socketTable = new Socket[10];
    public final static String DIRECTORY = "serverFiles";


    public static void main(String[] args) throws IOException {
        File directory = new File(DIRECTORY);
        if (!directory.exists())
            directory.mkdir();

        new ConnectedsThread().start();
        Application server = new Application();
        server.serverSocket = new ServerSocket(PORT);
        while (CONNECTIONS < MAX) { //Se ejecutará mientras las conexiones actuales sean menores que las maximas
            Socket socket = null;
            try {
                socket = server.serverSocket.accept(); //se espera a que el cliente conecte
            } catch (SocketException e) {
                System.err.println(e.getLocalizedMessage());
            }
            socketTable[CONNECTIONS] = socket; //almacenamos el socket creado
            CONNECTIONS++;
            assert socket != null;
            Thread serverThread = new Thread(new ServerThread(socket));
            serverThread.start(); //iniciamos el hilo que maneja la interaccion con el cliente
        }
    }
}