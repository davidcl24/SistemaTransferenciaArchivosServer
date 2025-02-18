package org.example;


import java.io.*;
import java.net.Socket;

import static org.example.Application.CONNECTIONS;
import static org.example.Application.DIRECTORY;

public class ServerThread implements Runnable{
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader reader;
    private PrintWriter printWriter;
    private final byte[] buffer = new byte[16*1024];


    public ServerThread(Socket socket) {
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream)); //se asocia el bufferedReader al inputStream para poder leer texto que envien desde el stream
            printWriter = new PrintWriter(outputStream, true); //se asocia el printWriter al outputStream para poder mandar texto por stream al cliente a traves del socket
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFile() throws IOException {
        String fileName =  reader.readLine(); //se recibe el nombre del archivo a traves del bufferedReader
        File file = new File(DIRECTORY + "/" + fileName);
        long fileSize = Long.parseLong(reader.readLine());
        if (fileSize == 0) { //recibimos el nombre del archivo y comprobamos su tamaño, si es 0, termina
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            long totalRead = 0;
            int count;
            while (totalRead < fileSize && (count = inputStream.read(buffer)) != -1) { // se comprobará que el total leido (totalRead) es menor que el tamaño del archivo, y que no hemos llegado al fin del archivo
                fileOutputStream.write(buffer, 0, count); //mientras ambas sean correctas, se escribira el archivo recibido en uno nuevo y se aumentará el total leido
                totalRead += count;
            }
        }
    }

    private void sendFile() throws IOException {
        String fileName = reader.readLine();
        File file = new File(DIRECTORY + "/" + fileName);
        if (file.exists()) {
            printWriter.println(file.length()); //si el archivo existe, se mandará su longitud, para que el cliente sepa cuando terminar de recomponer el archivo
            printWriter.flush();
            try (FileInputStream fileInputStream = new FileInputStream(file)){
                int count;
                while ((count = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, count); //se irá mandando el archivo por bytes hasta llegar a final del archivo
                }
                outputStream.flush();
            }
        } else {
            printWriter.println(0); //se envia 0 si no existe, asi el cliente no recorrerá el archivo
            printWriter.flush();
        }
    }

    private void listFiles() {
        String[] files = new File(DIRECTORY).list();
        if (files != null ) {
            for (String file : files) { //se listan los archivos y se envian al cliente a traves de printWriter
                printWriter.println(file);
            }
        }
    }


    @Override
    public void run() {
        try {
            boolean end = false;
            while (!end) { //mientras el boolean sea falso, se ejecutará
                String command = reader.readLine(); //se recoge el commando por el reader, enviado desde el cliente
                if (command.equals("END")){ //si el comando es "END" se termina el bucle
                    end = true;
                    CONNECTIONS--;
                }
                else {
                    switch (command.toLowerCase().trim()) {
                        case "upload":
                            receiveFile();
                            break;
                        case "download":
                            sendFile();
                            break;
                        case "list":
                            listFiles();
                            printWriter.println("DONE"); //se envia "DONE" al cliente para que sepa que tiene que acabar de listar
                            break;
                    }
                }
                printWriter.flush();
                outputStream.flush();
            }

        } catch (IOException e) {
            CONNECTIONS--;
        } finally {
            try {
                socket.close();
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
