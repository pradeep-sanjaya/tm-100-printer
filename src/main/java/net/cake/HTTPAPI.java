package net.cake;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPAPI {
    public static void main(String[] args) {
        String printerUrl = "https://10.10.10.102/PrinterAPI"; // Replace with your printer's URL

        String payload = "{"
                + "\"job_name\":\"LabelPrint\","
                + "\"media\":\"receipt\","
                + "\"content\":\"Hello World!\\nCut here!\","
                + "\"cut\":\"true\""
                + "}";

        try {
            URL url = new URL(printerUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            if (connection.getResponseCode() == 200) {
                System.out.println("Label sent successfully.");
            } else {
                System.err.println("Failed to print label. HTTP code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
