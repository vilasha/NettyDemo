package com.nettydemo.performance;

import java.io.*;
import java.net.URISyntaxException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Class tests performance of asynchronous Netty client-server connection
 * It launches server (from /test/resources/asyncServer.jar),
 * launches a client (from  /test/resources/asyncClient.jar),
 * and then creates array lists with 1, 10, 100, 1000, etc elements
 * and sends it to server and receives it back. Duration of each
 * data exchange is written to a performanceTest.log file
 *
 * To have up-to-date evaluation if you changed async implementation,
 * please update files /test/resources/asyncServer.jar and asyncClient.jar
 * from /out/artifacts/asyncServer and /out/artifacts/asyncClient
 * (you need to rename .jar files as well)
 */
public class AsyncConnectionPerformanceTest {

    private AsyncConnectionPerformanceTest() {}

    private static Logger log = Logger.getLogger(AsyncConnectionPerformanceTest.class.getName());

    static {
        FileHandler logHandler;
        try {
            logHandler = new FileHandler("performanceTest.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            log.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        log.info("\n---------------------Asynchronous Netty Connection test---------------------");

        String absolutePath = new File(AsyncConnectionPerformanceTest.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getPath();
        System.out.println(absolutePath);

        ProcessBuilder pb1 = new ProcessBuilder("java", "-jar", absolutePath + "\\asyncServer.jar");
        Process process1 = pb1.start();
        attachErrorStreamReader(process1);

        ProcessBuilder pb2 = new ProcessBuilder("java", "-jar", absolutePath + "\\asyncClient.jar");
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        Process process2 = pb2.start();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(process2.getOutputStream()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process2.getInputStream()));
        String line;
        if ((line = bufferedReader.readLine()) != null) {
            endTime = System.currentTimeMillis();
            System.out.println("Client: " + line);
            log.info("Client boot time = " + (endTime - startTime) + "ms");
        }

        attachErrorStreamReader(process2);


        bufferedWriter.write("login1\r\n");
        bufferedWriter.flush();
        if ((line = bufferedReader.readLine()) != null)
            System.out.println("Client: " + line);

        int maxZeros = 5;
        for (int i = 0; i <= maxZeros; i++) {
            startTime = System.currentTimeMillis();
            bufferedWriter.write("list" + (int)Math.pow(10, i) + "\r\n");
            bufferedWriter.flush();
            while ((line = bufferedReader.readLine()) == null) {
                if (System.currentTimeMillis() - startTime > 20000) {
                    System.out.println("Force stop");
                    break;
                }
                Thread.sleep(10);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Client: " + line);
            log.info("IntList with " + (int)Math.pow(10, i) + " elements: " + (endTime - startTime) + "ms");
            System.out.println("waiting...");
            if (System.currentTimeMillis() - startTime > 20000) {
                System.out.println("Force stop");
                break;
            }
            System.out.println("--");
        }
        System.out.println("--");
        startTime = System.currentTimeMillis();
        bufferedWriter.write("exit\r\n");
        bufferedWriter.flush();
        while ((line = bufferedReader.readLine()) == null)
            Thread.sleep(10);
        endTime = System.currentTimeMillis();
        System.out.println("Client: " + line);
        log.info("Client shutdown time: " + (endTime - startTime) + "ms");

        bufferedReader.close();
        bufferedWriter.close();
        process1.destroy();
    }

    private static void attachErrorStreamReader(Process process1) {
        new Thread(() -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process1.getErrorStream()));
            String line1;
            try {
                while ((line1 = bufferedReader.readLine()) != null){
                    //System.out.println("Server: " + line1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
