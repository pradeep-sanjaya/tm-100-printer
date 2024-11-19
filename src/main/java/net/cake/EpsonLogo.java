package net.cake;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class EpsonLogo {
    public static void main(String[] args) throws Exception {
        String imagePath = EpsonLogo.class.getClassLoader().getResource("logo.jpg").getPath();
        String printerIp = "10.10.10.102";    // Printer's IP address
        int printerPort = 9100;               // Printer's port
        int printerWidth = 320;               // Printer's max width in dots (40mm)

        // Print the logo
        int width = printerWidth;
        PrintJPEG.printLogo(printerIp, printerPort, imagePath, width);

        System.out.println("Logo sent to printer.");
    }
}
