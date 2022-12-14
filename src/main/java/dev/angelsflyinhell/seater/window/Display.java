package dev.angelsflyinhell.seater.window;

import dev.angelsflyinhell.seater.window.graphics.PixelManager;
import dev.angelsflyinhell.seater.window.graphics.SequenceHandler;
import dev.angelsflyinhell.seater.window.graphics.SpriteUtil;

import javax.swing.*;
import java.awt.*;

public class Display extends JPanel implements Runnable {

    Thread graphicsManager;
    PixelManager pixelManager;
    int scalingFactor;
    int resolution;
    long frames;

    public Display(int resolution, int scalingFactor) {
        System.out.println("Created new display (" + resolution * scalingFactor + "px)");

        this.scalingFactor = scalingFactor;
        this.resolution = resolution;
        frames = 0;

        setSize(resolution * scalingFactor, resolution * scalingFactor);
        setVisible(true);
        setBackground(Color.WHITE);

        graphicsManager = new Thread(this);
        pixelManager = new PixelManager(resolution);

        SpriteUtil.renderToDisplay(pixelManager, "background", 0, 0);
        SequenceHandler.addAnimation("title_screen", 5, 0, 0);
        SequenceHandler.addAnimation("press_btn", 2, 0, 75);

        SequenceHandler.updateAnimations(pixelManager);
        graphicsManager.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000 / 60;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (graphicsManager != null) {
            repaint();

            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1_000_000;

                if (remainingTime < 0) {
                    System.out.println("Frame " + frames + " took " + Math.sqrt(Math.pow(remainingTime, 2)) + "ms.");
                    remainingTime = 0;
                }
                frames++;

                if(frames % 24 == 0)
                    SequenceHandler.updateAnimations(pixelManager);

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        System.out.print("[live] Frame " + frames + "\r");

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(null);

        Color[][] upscaled = new Color[resolution * scalingFactor][resolution * scalingFactor];
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                upscaled[x * scalingFactor][y * scalingFactor] = pixelManager.getPixel(x, y);
                upscaled[x * scalingFactor + 1][y * scalingFactor] = pixelManager.getPixel(x, y);
                upscaled[x * scalingFactor][y * scalingFactor + 1] = pixelManager.getPixel(x, y);
                upscaled[x * scalingFactor + 1][y * scalingFactor + 1] = pixelManager.getPixel(x, y);
            }
        }

        for (int x = 0; x < resolution * scalingFactor; x += scalingFactor) {
            for (int y = 0; y < resolution * scalingFactor; y += scalingFactor) {
                g2.setColor(upscaled[x][y]);
                g2.fillRect(x, y, scalingFactor, scalingFactor);
            }
        }

        g2.dispose();
    }
}
