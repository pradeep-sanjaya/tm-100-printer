package net.cake;

import java.io.OutputStream;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class ESCPOSCommand
{
    public static void main(String[] args) {
        String printerIp = "10.10.10.102"; // Replace with your printer's IP
        int printerPort = 9100;

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] disableErrorSoundCommand = new byte[]{
                    0x1B, 0x63, 0x35, 0x00 // Disable buzzer on error (ESC c 5 n)
            };

            byte[] printCommand = new byte[]{
                    0x1B, 0x40,                      // Initialize printer
                    0x1D, 0x57, 0x40, 0x01,          // Set label width to 40mm (320 dots)
                    0x1B, 0x53,                      // Set Standard Mode
                    0x1B, 0x33, 0x18,                // Set line spacing to 24 dots
                    0x1B, 0x20, 0x00,                // Set character spacing to 0
                    0x1B, 0x21, 0x00,                // Default font, normal size
                    0x1B, 0x61, 0x00,                // Left align text
                    'H', 'e', 'l', 'l', 'o', ' ',    // Print text
                    'W', 'o', 'r', 'l', 'd', '!',    // Continue text
                    0x0A,                            // Add line feed
                    0x1D, 0x56, 0x42, 0x50           // Feed 80 dots (10mm) and cut
            };

            byte[] statusCommand = new byte[]{0x10, 0x04, 0x02};

            outputStream.write(disableErrorSoundCommand);
            outputStream.write(printCommand);
            outputStream.flush();
            System.out.println("Label sent to printer.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
