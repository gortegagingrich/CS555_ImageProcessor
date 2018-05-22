package project;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MultiChannelWindow extends JFrame {
   TabbedLayers outputChannels;
   JScrollPane inputPane;
   ImageIcon inputImageIcon;
   BufferedImage inputImage;
   
   public MultiChannelWindow() {
      setLayout(new GridLayout(1, 2));
      
      inputPane = new JScrollPane();
      
      inputPane.setPreferredSize(new Dimension(512, 512));
      
      try {
         inputImage = ImageIO.read(new File("test_rgb.jpg"));
         inputImageIcon = new ImageIcon(inputImage);
         inputPane = new JScrollPane(new JLabel(inputImageIcon));
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      inputPane.setHorizontalScrollBarPolicy(
              JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      inputPane.setVerticalScrollBarPolicy(
              JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      add(inputPane);
      
      
      outputChannels = new TabbedLayers(inputImage);
      add(outputChannels);
      
      // display
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   }
}

