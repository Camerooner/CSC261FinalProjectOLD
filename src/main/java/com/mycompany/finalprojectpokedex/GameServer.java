/*
 
Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
*/
package com.mycompany.finalprojectpokedex;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;


/**
 *
 
@author Cameron*/
public class GameServer{
    private HttpServer server;


    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new TestHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static class TestHandler implements HttpHandler{
        // IF someone calls '/test' on my server address, im gonna handle with this handler, which calls calls handle, giving me the exchange.
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if(ex.getRequestMethod().equals("POST")){
                Gson gson = new Gson();

                InputStreamReader ism = new InputStreamReader(ex.getRequestBody());
                 TestData td =  gson.fromJson(ism, TestData.class);
                 System.out.println(td);
            }

           Random random = new Random();
           int randomInt = random.nextInt(); // Generates a random integer
           String message = "{\"msg\":\"Hello Poke!\", \"id\":" + randomInt + '}';
           OutputStream os = ex.getResponseBody();
           System.out.println();
           ex.getResponseHeaders().set("Content-Type", "application/json");
           ex.sendResponseHeaders(200, message.length());
           os.write(message.getBytes());
           os.close();

        } 
    }

    private static class TestData{
        int id;
        String msg;

        @Override
        public String toString(){
            return msg + ", " + id;
        }
    }
}