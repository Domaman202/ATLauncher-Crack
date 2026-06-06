package ru.DmN.atlcrack2;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StandardHttpProxy {
    private final int port;
    private final ExecutorService threadPool;

    public StandardHttpProxy(int port, int maxThreads) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Standard HTTP proxy started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String requestLine = reader.readLine();
            if (requestLine == null) return;

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String url = parts[1];

            if (method.equalsIgnoreCase("CONNECT")) {
                handleConnect(clientSocket, url);
            } else {
                handleHttp(clientSocket, method, url, reader);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handleConnect(Socket clientSocket, String url) throws IOException {
        String[] hostPort = url.split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);

        try (Socket targetSocket = new Socket(host, port)) {
            OutputStream out = clientSocket.getOutputStream();
            out.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            out.flush();

            tunnel(clientSocket, targetSocket);
        }
    }

    private void handleHttp(Socket clientSocket, String method, String url, BufferedReader reader) throws IOException {
        String host = null;
        String path = url;

        // Если URL полный, извлекаем хост и путь
        if (url.startsWith("http://")) {
            int hostStart = url.indexOf("://") + 3;
            int pathStart = url.indexOf("/", hostStart);
            if (pathStart == -1) {
                host = url.substring(hostStart);
                path = "/";
            } else {
                host = url.substring(hostStart, pathStart);
                path = url.substring(pathStart);
            }
        }

        // Читаем заголовки и ищем Host
        StringBuilder headers = new StringBuilder();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            headers.append(line).append("\r\n");
            if (host == null && line.toLowerCase().startsWith("host:")) {
                host = line.substring(5).trim();
            }
        }
        if (host == null) throw new IOException("Missing Host header");

        int targetPort = 80;
        if (host.contains(":")) {
            String[] hp = host.split(":");
            host = hp[0];
            targetPort = Integer.parseInt(hp[1]);
        }

        try (Socket target = new Socket(host, targetPort)) {
            OutputStream targetOut = target.getOutputStream();
            PrintWriter pw = new PrintWriter(targetOut);
            pw.print(method + " " + path + " HTTP/1.1\r\n");
            pw.print(headers.toString());
            pw.print("\r\n");
            pw.flush();

            // Пересылаем тело запроса (если есть)
            InputStream clientIn = clientSocket.getInputStream();
            forwardData(clientIn, targetOut, true, false);

            // Пересылаем ответ обратно клиенту
            InputStream targetIn = target.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();
            forwardData(targetIn, clientOut, false, true);
        }
    }

    private void forwardData(InputStream from, OutputStream to, boolean closeTo, boolean flush) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = from.read(buf)) != -1) {
            to.write(buf, 0, n);
            if (flush) to.flush();
        }
        if (closeTo) to.close();
    }

    private void tunnel(Socket client, Socket target) {
        Thread t1 = new Thread(() -> {
            try { forwardData(client.getInputStream(), target.getOutputStream(), false, true); } catch (IOException e) {}
        });
        Thread t2 = new Thread(() -> {
            try { forwardData(target.getInputStream(), client.getOutputStream(), false, true); } catch (IOException e) {}
        });
        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws IOException {
        int port = 8888;
        int maxThreads = 50;
        if (args.length > 0) port = Integer.parseInt(args[0]);
        if (args.length > 1) maxThreads = Integer.parseInt(args[1]);
        new StandardHttpProxy(port, maxThreads).start();
    }
}