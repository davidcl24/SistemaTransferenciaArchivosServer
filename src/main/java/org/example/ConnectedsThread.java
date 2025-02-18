package org.example;

import static org.example.Application.CONNECTIONS;

public class ConnectedsThread extends Thread{

    @Override
    public void run() {
        while (true) {
            try {
                sleep(1000); //simplemente comprueba el numero de clientes conectados cada segundo
                System.out.println("Clientes conectados: " + CONNECTIONS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
