package com.nettydemo.performance;

import java.io.*;
import java.util.Arrays;

public class AsyncConnectionPerformanceTest {

    private AsyncConnectionPerformanceTest() {};

    public static void main(String[] args) throws IOException, InterruptedException {
/*        String[] serverCommands = new String[3];
        serverCommands[0] = "cmd";
        serverCommands[1] = "cd C:\\DocumentsPC\\Upwork\\contracts\\Netty\\project\\src\\test\\resources\\";
        serverCommands[2] = "java -jar asyncServer.jar";
        ProcessBuilder server = new ProcessBuilder(serverCommands);*/
        ProcessBuilder pb1 = new ProcessBuilder("java", "-jar", "C:\\DocumentsPC\\Upwork\\contracts\\Netty\\project\\src\\test\\resources\\asyncServer.jar");
        Process process1 = pb1.start();
        InputStream is1 = process1.getInputStream();
        InputStreamReader isr1 = new InputStreamReader(is1);
        BufferedReader br1 = new BufferedReader(isr1);
        String line;
        System.out.println(1);
/*        do {
            line = br1.readLine();
            System.out.println("Server: " + line);
        } while (line != null && !line.equals(""));*/
        System.out.println(2);
        OutputStream os1 = process1.getOutputStream();
        OutputStreamWriter osw1 = new OutputStreamWriter(os1);
        System.out.println(3);

        ProcessBuilder pb2 = new ProcessBuilder("java", "-jar", "C:\\DocumentsPC\\Upwork\\contracts\\Netty\\project\\src\\test\\resources\\asyncClient.jar");
        Process process2 = pb2.start();
        InputStream is2 = process2.getInputStream();
        InputStreamReader isr2 = new InputStreamReader(is2);
        BufferedReader br2 = new BufferedReader(isr2);
        System.out.println(4);
/*        do {
            line = br2.readLine();
            System.out.println("Server: " + line);
        } while (line != null && !line.equals(""));*/
        System.out.println(5);
        OutputStream os2 = process2.getOutputStream();
        OutputStreamWriter osw2 = new OutputStreamWriter(os2);
        System.out.println(6);
        for (int i = 0; i < 7; i++) {
            line = br2.readLine();
            System.out.println("Client: " + line);
        }
        System.out.println(7);
        osw2.write("login1\n\r");
        osw2.flush();
        System.out.println(8);
        while ((line = br2.readLine()) != null) {
            System.out.println(line);
        }
    }

    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }
}
