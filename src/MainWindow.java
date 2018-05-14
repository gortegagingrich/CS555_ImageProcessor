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
import java.util.function.BiFunction;
import java.util.function.Function;

class MainWindow extends JFrame {
   public static int bitDepth = 8;
   private final Vector<Function<int[][], int[][]>> filters;
   private int lastFilter;
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
      lastFilter = 0;
      
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
   
   private void genericAddScale(JMenu menu, String name, ScaleFunction<int[][], int[][], Integer, Integer> fn) {
      // make panel with spinners for output width and height
      // make menu item
      JMenuItem item = new JMenuItem(name);
      
      // action listener for menu item
      item.addActionListener(l -> {
         JPanel panel;
         JSpinner w, h;
         JLabel wl, hl;
         int res;
         
         panel = new JPanel();
         panel.setLayout(new GridLayout(2, 2));
         wl = new JLabel("width: ");
         hl = new JLabel("height: ");
         w = new JSpinner();
         w.getModel().setValue(outputImage.getWidth());
         h = new JSpinner();
         h.getModel().setValue(outputImage.getHeight());
         panel.add(wl);
         panel.add(w);
         panel.add(hl);
         panel.add(h);
         
         // check if
         res = JOptionPane.showConfirmDialog(this, panel);
         
         if (res == JOptionPane.OK_OPTION) {
            filters.add(x -> fn.apply(x, (Integer) w.getValue(),
                                      (Integer) h.getValue()));
            applyFilters();
         }
      });
      
      menu.add(item);
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
      genericAddScale(spatial, "nearest neighbor",
                      (a, b, c) -> Assignment1.scaleNearestNeighbor(a, b, c));
      
      // menu item for spatial scaling with linear interpolation
      JMenuItem linearInterpolation = new JMenuItem("linear interpolation");
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
      genericAddScale(spatial, "bilinear interpolation",
                      Assignment1::bilinearInterp);
      
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
      addFilterItem((a, b) -> Assignment2.laplacianSharpen(a, b),
                    "sharpening Laplacian", filterMenu);
      
      // median filter with selectable local region size
      addFilterItem((a, b) -> Assignment2.medianFilter(a, b), "median",
                    filterMenu);
      
      // smoothing filter using local means of given region size
      addFilterItem((a, b) -> Assignment2.smooth(a, b), "smoothing",
                    filterMenu);
      
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
      addFilterItem((a, b) -> Assignment2.localHE(a, b), "local", histEQ);
      
      edit.add(histEQ);
      edit.add(filterMenu);
   }
   
   private void addAssignment3(JMenu edit) {
      
      // arithmetic mean
      addFilterItem((a, b) -> Assignment3.arithMeanFilter(a, b),
                    "arithmetic mean", edit);
      
      // geometric mean
      addFilterItem((a, b) -> Assignment3.geomMeanFilter(a, b),
                    "geometric mean", edit);
      
      // harmonic mean
      addFilterItem((a, b) -> Assignment3.harmonicMeanFilter(a, b),
                    "harmonic mean", edit);
      
      // contraharmonic mean
      addFilterItem((a, b) -> Assignment3.contraharmonicMeanFilter(a, b),
                    "contraharmonic mean", edit);
      
      // max filter
      addFilterItem((a, b) -> Assignment3.maxFilter(a, b), "max", edit);
      
      // min filter
      addFilterItem((a, b) -> Assignment3.minFilter(a, b), "min", edit);
      
      // midpiont filter
      addFilterItem((a, b) -> Assignment3.midpointFilter(a, b), "midpoint",
                    edit);
      
      // alpha-trimmed mean filter
      addFilterItem((a, b) -> Assignment3.alphaTrimmedMeanFilter(a, b),
                    "alpha-trimmed mean", edit);
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
      // filter was added
      if (filters.size() > lastFilter) {
         outputImage = new GrayscaleImage(outputImage).apply(
                 filters.get(lastFilter++)).toImage();
      } else { // filter at least one filter was removed
         lastFilter = filters.size();
         bitDepth = 8;
         Function<int[][], int[][]>[] fs = new Function[filters.size()];
         filters.toArray(fs);
         outputImage = new GrayscaleImage(inputImage).apply(fs).toImage();
      }
      updateOutputImage();
   }
   
   /**
    * updates output image
    */
   private void updateOutputImage() {
      outputLabel.setIcon(new ImageIcon(outputImage));
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
   
   private void addFilterItem(BiFunction<int[][], Integer, int[][]> f, String label, JMenu menu) {
      JMenuItem item = new JMenuItem(label);
      item.addActionListener(l -> addFilterWithSize(f));
      menu.add(item);
   }
   
   private void addFilterWithSize(BiFunction<int[][], Integer, int[][]> f) {
      final int size = showIntOption("Size of local region: ", 3);
      filters.add(x -> f.apply(x, size));
      applyFilters();
   }
}
