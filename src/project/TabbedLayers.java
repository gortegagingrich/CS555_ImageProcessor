/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Project
 * Description: Implementation of similar reflectance prior based facial
 * illumination transfer
 */

package project;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TabbedLayers extends JTabbedPane {
   GenericImage image;
   JScrollPane[] panes;
   ImageIcon[] icons;
   
   public TabbedLayers(BufferedImage img) throws IOException {
      image = new GenericImage(img,
                               x -> Color.HSBtoRGB(x[0], x[1], x[2]),
                               x -> Color.RGBtoHSB(x[0], x[1], x[2], null), 3);
      //image.apply(x -> FloatFilters.guided(x, x, 2, 0.2f), 2);
      
      // create reference image
      GenericImage ref = new GenericImage(ImageIO.read(new File("ref.png")),
                                          x -> Color.HSBtoRGB(x[0], x[1], x[2]),
                                          x -> Color.RGBtoHSB(x[0], x[1], x[2],
                                                              null),
                                          3);
      
      float[][] srcV = image.getChannel(2);
      float[][] refV = ref.getChannel(2);
      
      // get mask
      float[][] mask = FloatFilters.getReflectanceMask(image.getChannel(1));
      
      // get large scale
      float[][] srcLS = FloatFilters.guided(srcV, srcV, mask, 21f);
      refV = FloatFilters.guided(refV, refV, mask, 21f);
      // get detail for source
      // avoid dividing by zero
      
      refV = FloatFilters.normalize(refV);
      refV = Util.div(srcLS, refV);
      
      // make sure all values are in range
      srcV = Util.div(image.getChannel(2), refV);
      for (int i = 0; i < srcV.length; i++) {
         for (int j = 0; j < srcV[i].length; j++) {
            if (srcV[i][j] > 1) {
               srcV[i][j] = 1;
            }
         }
      }
      
      image.setChannel(2, srcV);
      
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
