/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 2
 * Description: Implementation of several basic spatial filters
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
         File f = new File("test.jpg");
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

      JMenu assignment3Filters = new JMenu("Assignment 3 filters");
      addAssignment3(assignment3Filters);
      edit.add(assignment3Filters);
   
      // allow for clearing all filters
      JMenuItem clear = new JMenuItem("clear");
      clear.addActionListener(l -> clearFilters());

      edit.add(new JSeparator());
      edit.add(clear);
      
      menuBar.add(file);
      menuBar.add(edit);
      setJMenuBar(menuBar);
   }
   
   /**
    * Adds filters required for first submission to given JMenu
    *
    * @param edit Menu filters are to be selected from
    */
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
   
   /**
    * Adds filters required for submission 2 to given JMenu
    *
    * @param edit Menu filters are to be selected from
    */
   private void addAssignment2(JMenu edit) {
      // deactivate selected bitplanes
      JMenuItem toggleBitPlanes = new JMenuItem("Toggle bitplanes");
      toggleBitPlanes.addActionListener(this::bitPlanesAction);
      edit.add(toggleBitPlanes);
      
      JMenu filterMenu = new JMenu("filters");
   
      // sharpening filter with laplacian kernel of chosen size
      JMenuItem laplacianSharpen = new JMenuItem("sharpening Laplacian");
      laplacianSharpen.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment2.laplacianSharpen(x, size));
         applyFilters();
      });
      filterMenu.add(laplacianSharpen);
   
      // median filter with selectable local region size
      JMenuItem medianFilter = new JMenuItem("median filter");
      medianFilter.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment2.medianFilter(x, size));
         applyFilters();
      });
      filterMenu.add(medianFilter);
   
      // smoothing filter using local means of given region size
      JMenuItem avgFilter = new JMenuItem("smoothing filter");
      avgFilter.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment2.smooth(x, size));
         applyFilters();
      });
      filterMenu.add(avgFilter);
   
      // high boosting filter with given region size and value for A
      JMenuItem highBoosting = new JMenuItem("high-boosting filter");
      highBoosting.addActionListener(this::highBoostAction);
      filterMenu.add(highBoosting);
   
      // menu for histogram equalization
      JMenu histEQ = new JMenu("histogram equalization");
   
      // global histogram equalization
      JMenuItem globalHEQ = new JMenuItem("global");
      globalHEQ.addActionListener(l -> {
         filters.add(x -> Assignment2.globalHE(x));
         applyFilters();
      });
      histEQ.add(globalHEQ);
   
      // local histogram equalization with selectable local size
      JMenuItem localHEQ = new JMenuItem("local");
      localHEQ.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment2.localHE(x, size));
         applyFilters();
      });
      histEQ.add(localHEQ);
   
      edit.add(histEQ);
      edit.add(filterMenu);
   }

   private void addAssignment3(JMenu edit) {

      // arithmetic mean
      JMenuItem avgFilter = new JMenuItem("arithmetic mean");
      avgFilter.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment3.arithMeanFilter(x, size));
         applyFilters();
      });
      edit.add(avgFilter);

      // geometric mean
      JMenuItem geoFilter = new JMenuItem("geometric mean");
      geoFilter.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment3.geomMeanFilter(x,size));
         applyFilters();
      });
      edit.add(geoFilter);

      // harmonic mean
      JMenuItem harmFilter = new JMenuItem("harmonic mean");
      harmFilter.addActionListener(l -> {
         int size = showIntOption("Size of local region: ", 3);
         filters.add(x -> Assignment3.harmonicMeanFilter(x, size));
         applyFilters();
      });
      edit.add(harmFilter);
   }
   
   /**
    * Opens a jfilechooser to let user select image file to be processed.
    * Does not clear current filters once it is loaded.
    */
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
   
   /**
    * Clears current filters and updates output image.
    */
   private void clearFilters() {
      filters.clear();
      bitDepth = 8;
      applyFilters();
   }
   
   /**
    * Called when menuitem for disabling bit planes is selected
    *
    * @param l action event not used
    */
   private void bitPlanesAction(ActionEvent l) {
      JPanel panel = new JPanel();
      JCheckBox[] boxes = new JCheckBox[8];
      int planes = 0;
      
      panel.setLayout(new GridLayout(1, 8));
   
      // add checkboxes for each bit (assume 8 total)
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
   
   /**
    * iteratively applies filters to image and displays output
    */
   private void applyFilters() {
      bitDepth = 8;
      Function<int[][], int[][]>[] fs = new Function[filters.size()];
      filters.toArray(fs);
      outputImage = new GrayscaleImage(inputImage).apply(fs).toImage();
      updateOutputImage();
   }
   
   /**
    * updates output image
    */
   private void updateOutputImage() {
      outputLabel.setIcon(new ImageIcon(outputImage));
   }
   
   /**
    * Event for bilinear scaling
    * Lets user choose output resolution
    *
    * @param l actionevent not used
    */
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
   
   /**
    * Helper class for getting single integer input
    *
    * @param desc Text description for spinner
    * @param def  Default value of spinner
    * @return selected value
    */
   private int showIntOption(String desc, int def) {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1, 2));
      
      JSpinner spinner = new JSpinner();
      spinner.getModel().setValue(def);
   
      panel.add(new JLabel(desc));
      panel.add(spinner);
      
      int result = JOptionPane.showConfirmDialog(this, panel);
      
      if (result == JOptionPane.OK_OPTION) {
         def = Math.max(1, (Integer) spinner.getValue());
      }
      
      return def;
   }
   
   /**
    * Called when menu item for high-boost filter is pressed.
    * Gets values for size and a.  Then adds Function to filters
    *
    * @param l action event not used
    */
   private void highBoostAction(ActionEvent l) {
      final int size, a;
      
      // value for local region size
      JSpinner spinner = new JSpinner();
      spinner.getModel().setValue(3); // default is 3x3
      
      // value for A
      JSpinner spinner2 = new JSpinner();
      spinner2.getModel().setValue(3); // default A is 3
      
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(2, 2));
      panel.add(new JLabel("Region Size: "));
      panel.add(spinner);
      panel.add(new JLabel("A: "));
      panel.add(spinner2);
      
      int result = JOptionPane.showConfirmDialog(this, panel);
      
      if (result == JOptionPane.OK_OPTION) {
         size = Math.max(1, (Integer) spinner.getValue());
         a = Math.max(1, (Integer) spinner.getValue());
         filters.add(x -> Assignment2.highBoostingFilter(x, size, a));
         applyFilters();
      }
   }
}
