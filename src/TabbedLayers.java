import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TabbedLayers extends JTabbedPane {
   GenericImage image;
   JScrollPane[] panes;
   ImageIcon[] icons;
   
   public TabbedLayers(BufferedImage img) {
      image = new GenericImage(img,
                               x -> Color.HSBtoRGB(x[0], x[1], x[2]),
                               x -> Color.RGBtoHSB(x[0], x[1], x[2], null), 3);
      image.apply(x -> FloatFilters.guided(x, x, 2, 0.2f), 2);
      
      BufferedImage[] images = image.getChannels(255);
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
              image.toImage())));
      addTab("recomposed", panes[i]);
      panes[2].add(new PopupMenu());
      
      setPreferredSize(new Dimension(512, 512));
   }
}
