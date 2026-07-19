package com.cs2esp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class Overlay extends JFrame {
    
    private BufferedImage flashBuffer;

    public Overlay(int width, int height) {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setSize(width, height);
        setType(JFrame.Type.UTILITY); 
        setBackground(new Color(0, 0, 0, 0)); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setFocusableWindowState(false);
    }

    public synchronized void updateFrame(BufferedImage newFrame) {
        this.flashBuffer = newFrame;
        repaint(); 
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); 
        if (flashBuffer != null) {
            g.drawImage(flashBuffer, 0, 0, null);
        }
    }
}

