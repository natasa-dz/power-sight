package com.example.epsnwtbackend.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        // Configure QR code generation settings
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.MARGIN, 1); // Minimum margin
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Character set

        // Generate the BitMatrix for the QR code
        BitMatrix matrix = new MultiFormatWriter().encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);

        // Create a BufferedImage to render the QR code
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        // Fill the BufferedImage with the QR code data
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                int color = matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                image.setRGB(x, y, color);
            }
        }

        return image; // Return the rendered QR code image
    }
}
