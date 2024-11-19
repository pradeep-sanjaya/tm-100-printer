package net.cake;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import javax.imageio.ImageIO;

public class PrintJPEG {
    public static byte[] convertJPEGToESCPosData(String imagePath, int printerWidth) throws Exception {
        // Load the JPEG image
        BufferedImage originalImage = ImageIO.read(new File(imagePath));

        // Resize the image to fit the printer's width
        int width = Math.min(printerWidth - 80, originalImage.getWidth()); // Add padding (80 dots)
        int height = (originalImage.getHeight() * width) / originalImage.getWidth();

        // Add padding to the left
        int paddedWidth = printerWidth;
        BufferedImage paddedImage = new BufferedImage(paddedWidth, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = paddedImage.createGraphics();
        g.setColor(Color.WHITE); // Fill background with white
        g.fillRect(0, 0, paddedWidth, height);
        g.drawImage(originalImage, 80, 0, width, height, null); // Add 80-dot padding
        g.dispose();

        // Convert to ESC/POS raster data
        int bytesPerRow = (paddedWidth + 7) / 8; // Round up to nearest byte
        byte[] imageData = new byte[bytesPerRow * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < paddedWidth; x++) {
                int color = paddedImage.getRGB(x, y);
                if ((color & 0xFFFFFF) == 0x000000) { // Black pixel
                    imageData[y * bytesPerRow + (x / 8)] |= (1 << (7 - (x % 8)));
                }
            }
        }
        return imageData;
    }

    public static void printLogo(String printerIp, int printerPort, String imagePath, int printerWidth) throws Exception {
        // Convert the JPEG to ESC/POS raster data
        byte[] imageData = convertJPEGToESCPosData(imagePath, printerWidth);

        int bytesPerRow = (printerWidth + 7) / 8; // Width in bytes
        int height = imageData.length / bytesPerRow; // Height in dots

        // ESC/POS command for printing a raster image
        byte[] printCommand = new byte[]{
                0x1D, 0x76, 0x30, 0x00,               // GS v 0 (Normal mode)
                (byte) (bytesPerRow & 0xFF),          // xL (low byte of width in bytes)
                (byte) ((bytesPerRow >> 8) & 0xFF),   // xH (high byte of width in bytes)
                (byte) (height & 0xFF),               // yL (low byte of height in dots)
                (byte) ((height >> 8) & 0xFF)         // yH (high byte of height in dots)
        };

        byte[] disableSoundCommand = new byte[]{
                0x1B, 0x63, 0x35, 0x00 // Disable buzzer
        };

        byte[] feedAndCutCommand = new byte[]{
                0x1D, 0x56, 0x42, (byte) 0xE8, (byte) 0x03 // Feed 1000 dots and cut
        };

        // Combine the command and image data
        byte[] fullCommand = new byte[disableSoundCommand.length + feedAndCutCommand.length + printCommand.length + imageData.length + 4];

        System.arraycopy(printCommand, 0, fullCommand, 0, printCommand.length);
        System.arraycopy(imageData, 0, fullCommand, printCommand.length, imageData.length);

        // Add feed and cut command at the end (Feed 160 dots, then cut)
        fullCommand[fullCommand.length - 4] = 0x1D; // GS
        fullCommand[fullCommand.length - 3] = 0x56; // V
        fullCommand[fullCommand.length - 2] = 0x42; // Feed and Cut
        fullCommand[fullCommand.length - 1] = (byte) 1000; // Feed 1000 dots (100mm)

        // Send the command to the printer
        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {
            outputStream.write(fullCommand);
            outputStream.flush();
        }
    }
}