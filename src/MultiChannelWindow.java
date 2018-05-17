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

class TabbedLayers extends JTabbedPane {
   MultiChannelImage image;
   JScrollPane[] panes;
   ImageIcon[] icons;
   GenericImage gImage;
   
   public TabbedLayers(BufferedImage img) {
      image = new MultiChannelImage(img, MultiChannelImage.HSV);
      gImage = new GenericImage(img,
              x -> Color.HSBtoRGB(x[0], x[1], x[2]),
              x -> Color.RGBtoHSB(x[0],x[1],x[2],null), 3);
      
      BufferedImage[] images = gImage.getChannels(255);
      panes = new JScrollPane[images.length + 1];
      icons = new ImageIcon[panes.length];
      
      int i = 0;
      
      for (BufferedImage channel : images) {
         icons[i] = new ImageIcon(channel);
         panes[i] = new JScrollPane(new JLabel(icons[i]));
         addTab(String.format("Channel %d", i), panes[i++]);
      }
      
      // first tab is recomposed image
      panes[i] = new JScrollPane(new JLabel(new ImageIcon(
              gImage.toImage())));
      addTab("recomposed", panes[i]);
      panes[2].add(new PopupMenu());
   }
}