/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 1
 * Description: Implementation of several basic algorithms for changing spatial
 * and gray-scale resolution
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;

class MainWindow extends JFrame {
   public static int bitDepth = 8;
   private final Vector<Function<int[][], int[][]>> filters;
   private JScrollPane inputPane, outputPane;
   private JLabel inputLabel, outputLabel;
   private BufferedImage inputImage;
   private BufferedImage outputImage;
   
   MainWindow() {
      // set title
      setTitle("CS-555 Image Processor");
      setLayout(new GridLayout(1, 2));
      
      try {
         File f = new File("lena.jpg");
         inputImage = ImageIO.read(f);
         outputImage = ImageIO.read(f);
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      filters = new Vector<>();
      
      initComponents();
      
      // display
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   }
   
   private void initComponents() {
      // setup picture display
      inputLabel = new JLabel(new ImageIcon(inputImage));
      outputLabel = new JLabel(new ImageIcon(outputImage));
      
      inputPane = new JScrollPane(inputLabel);
      inputPane.setPreferredSize(new Dimension(512, 512));
      
      outputPane = new JScrollPane(outputLabel);
      outputPane.setPreferredSize(new Dimension(512, 512));
      
      add(inputPane);
      add(outputPane);
   
      // setup menus
      JMenuBar menuBar = new JMenuBar();
   
      // reading image from file
      JMenu file = new JMenu("file");
      JMenuItem loadButton = new JMenuItem("Load Image");
      loadButton.addActionListener(l -> loadImage());
      file.add(loadButton);
   
      // edit menu
      JMenu edit = new JMenu("edit");
   
      addAssignment1(edit);
      addAssignment2(edit);
   
      // allow for clearing all filters
      JMenuItem clear = new JMenuItem("clear");
      clear.addActionListener(l -> clearFilters());
      
      edit.add(new JSeparator());
      edit.add(clear);
      
      menuBar.add(file);
      menuBar.add(edit);
      setJMenuBar(menuBar);
   }
   
   private void addAssignment1(JMenu edit) {
      // add submenus
      JMenu scale = new JMenu("resolution");
      JMenu spatial = new JMenu("spatial");
      JMenu grayscale = new JMenu("gray-scale");
      
      // menu item for nearest neighbor spatial scaling
      JMenuItem nearestNeighbor = new JMenuItem("Nearest Neighbor");
      nearestNeighbor.addActionListener(l -> {
         JTextField w, h;
         JLabel wl, hl;
         w = new JTextField(String.format("%d", outputImage.getWidth()));
         h = new JTextField(String.format("%d", outputImage.getHeight()));
         wl = new JLabel("width");
         hl = new JLabel("height");
         w.setColumns(5);
         h.setColumns(5);
         
         JOptionPane.showMessageDialog(this, new JPanel() {{
            setLayout(new FlowLayout());
            add(wl);
            add(w);
            add(hl);
            add(h);
         }});
         
         if (w.getText().length() != 0 && h.getText().length() != 0) {
            filters.add(x -> Assignment1.scaleNearestNeighbor(x,
                                                              Integer.parseInt(
                                                                      w.getText()),
                                                              Integer.parseInt(
                                                                      h.getText())));
            applyFilters();
         }
         ;
      });
      spatial.add(nearestNeighbor);
      
      // menu item for spatial scaling with linear interpolation
      JMenuItem linearInterpolation = new JMenuItem("Linear Interpolation");
      linearInterpolation.addActionListener(l -> {
         JTextField w, h;
         JLabel wl, hl;
         w = new JTextField(String.format("%d", outputImage.getWidth()));
         h = new JTextField(String.format("%d", outputImage.getHeight()));
         wl = new JLabel("width");
         hl = new JLabel("height");
         w.setColumns(5);
         h.setColumns(5);
         
         // choose horizontal/vertical with combo box
         JComboBox jcb = new JComboBox();
         jcb.addItem("horizontal");
         //noinspection unchecked,unchecked
         jcb.addItem("vertical");
         
         JOptionPane.showMessageDialog(this, new JPanel() {{
            add(wl);
            add(w);
            add(hl);
            add(h);
            add(jcb);
         }});
         
         if (w.getText().length() != 0 && h.getText().length() != 0) {
            filters.add(x -> Assignment1
                    .linearInterp(
                            x,
                            Integer.parseInt(w.getText()),
                            Integer.parseInt(h.getText()),
                            (Objects.requireNonNull(jcb.getSelectedItem()))
                                    .equals("horizontal")));
            applyFilters();
         }
         ;
      });
      spatial.add(linearInterpolation);
      
      // menu item for scaling by bilinear interpolation
      JMenuItem bilinearInterp = new JMenuItem("Bilinear Interpolation");
      bilinearInterp.addActionListener(this::scaleBilinear);
      spatial.add(bilinearInterp);
      
      scale.add(spatial);
      edit.add(scale);
      
      // menu for gray-scale resolution
      for (int i = 1; i < 9; i++) {
         // add buttons for 1-8 bits
         JMenuItem b = new JMenuItem(String.format("%d", i));
         int finalI = i;
         b.addActionListener(l -> {
            filters.add(x -> Assignment1.changeBitDepth(x, finalI));
            applyFilters();
         });
         grayscale.add(b);
      }
      
      scale.add(grayscale);
   }
   
   private void addAssignment2(JMenu edit) {
      JMenuItem toggleBitPlanes = new JMenuItem("Toggle bitplanes");
      toggleBitPlanes.addActionListener(this::selectBitPlanes);
      edit.add(toggleBitPlanes);
      
      JMenu filterMenu = new JMenu("filters");
   
      JMenuItem laplacianSharpen = new JMenuItem("sharpening Laplacian");
      laplacianSharpen.addActionListener(l -> {
         int size = showIntOption(3);
         filters.add(x -> Assignment2.laplacianSharpen(x, size));
         applyFilters();
      });
      filterMenu.add(laplacianSharpen);
   
      JMenuItem medianFilter = new JMenuItem("median filter");
      medianFilter.addActionListener(l -> {
         int size = showIntOption(3);
         filters.add(x -> Assignment2.medianFilter(x, size));
         applyFilters();
      });
      filterMenu.add(medianFilter);
   
      JMenuItem avgFilter = new JMenuItem("smoothing filter");
      avgFilter.addActionListener(l -> {
         int size = showIntOption(3);
         filters.add(x -> Assignment2.smooth(x, size));
         applyFilters();
      });
      filterMenu.add(avgFilter);
   
      JMenuItem highBoosting = new JMenuItem("high-boosting filter");
      highBoosting.addActionListener(l -> {
         int size = showIntOption(3);
         filters.add(x -> Assignment2.highBoostingFilter(x, size));
         applyFilters();
      });
      filterMenu.add(highBoosting);
   
      JMenu histEQ = new JMenu("histogram equalization");
   
      JMenuItem globalHEQ = new JMenuItem("global");
      globalHEQ.addActionListener(l -> {
         filters.add(x -> Assignment2.globalHE(x));
         applyFilters();
      });
      histEQ.add(globalHEQ);
   
      JMenuItem localHEQ = new JMenuItem("local");
      localHEQ.addActionListener(l -> {
         int size = showIntOption(3);
         filters.add(x -> Assignment2.localHE(x, size));
         applyFilters();
      });
      histEQ.add(localHEQ);
   
      edit.add(histEQ);
      edit.add(filterMenu);
   }
   
   private void loadImage() {
      File f;
      JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
      
      int retVal = jfc.showOpenDialog(this);
      
      if (retVal == JFileChooser.APPROVE_OPTION) {
         f = jfc.getSelectedFile();
         try {
            inputImage = ImageIO.read(f);
            inputLabel.setIcon(new ImageIcon(inputImage));
            updateOutputImage();
         } catch (IOException e) {
         }
      }
      
      applyFilters();
   }
   
   private void clearFilters() {
      filters.clear();
      bitDepth = 8;
      applyFilters();
   }
   
   private void selectBitPlanes(ActionEvent l) {
      JPanel panel = new JPanel();
      JCheckBox[] boxes = new JCheckBox[8];
      int planes = 0;
      
      panel.setLayout(new GridLayout(8, 1));
      
      for (int i = 0; i < 8; i++) {
         boxes[i] = new JCheckBox(String.format("%d", i));
         boxes[i].setSelected(true);
         panel.add(boxes[i], i, 0);
      }
      
      JOptionPane.showMessageDialog(this, panel);
      
      for (int i = 0; i < 8; i++) {
         planes |= boxes[i].isSelected() ? 1 << i : 0;
      }
      
      final int p = planes;
      
      filters.add(x -> Assignment2.setBitPlanes(x, p));
      applyFilters();
   }
   
   private void applyFilters() {
      bitDepth = 8;
      Function<int[][], int[][]>[] fs = new Function[filters.size()];
      filters.toArray(fs);
      outputImage = new GrayscaleImage(inputImage).apply(fs).toImage();
      updateOutputImage();
   }
   
   private void updateOutputImage() {
      outputLabel.setIcon(new ImageIcon(outputImage));
   }
   
   private void scaleBilinear(ActionEvent l) {
      JTextField w, h;
      JLabel wl, hl;
      w = new JTextField(String.format("%d", outputImage.getWidth()));
      h = new JTextField(String.format("%d", outputImage.getHeight()));
      wl = new JLabel("width");
      hl = new JLabel("height");
      w.setColumns(5);
      h.setColumns(5);
      
      JOptionPane.showMessageDialog(this, new JPanel() {{
         add(wl);
         add(w);
         add(hl);
         add(h);
      }});
      
      if (w.getText().length() != 0 && h.getText().length() != 0) {
         filters.add(x -> Assignment1
                 .bilinearInterp(
                         x,
                         Integer.parseInt(w.getText()),
                         Integer.parseInt(h.getText())));
         applyFilters();
      }
   }
   
   private int showIntOption(int def) {
      JSpinner spinner = new JSpinner();
      spinner.getModel().setValue(def);
      
      int result = JOptionPane.showConfirmDialog(this, spinner);
      
      if (result == JOptionPane.OK_OPTION) {
         def = Math.max(1, (Integer) spinner.getValue());
      }
      
      return def;
   }
}
