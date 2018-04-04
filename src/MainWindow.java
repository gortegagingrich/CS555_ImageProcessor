import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.function.Function;

class MainWindow extends JFrame {
   private JScrollPane inputPane, outputPane;
   private JLabel inputLabel, outputLabel;
   
   private BufferedImage inputImage;
   private BufferedImage outputImage;
   
   public static int bitDepth = 8;
   
   private Vector<Function<int[][], int[][]>> filters;
   
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
   
   private void updateOutputImage() {
      outputLabel.setIcon(new ImageIcon(outputImage));
   }
   
   private void initComponents() {
      inputLabel = new JLabel(new ImageIcon(inputImage));
      outputLabel = new JLabel(new ImageIcon(outputImage));
      
      inputPane = new JScrollPane(inputLabel);
      inputPane.setPreferredSize(new Dimension(512, 512));
      
      outputPane = new JScrollPane(outputLabel);
      outputPane.setPreferredSize(new Dimension(512, 512));
      
      add(inputPane);
      add(outputPane);
      
      JMenuBar menuBar = new JMenuBar();
      
      JMenuItem updateButton = new JMenuItem("test actions");
      updateButton.addActionListener(l -> setTestActions());
      updateButton.setAccelerator(KeyStroke.getKeyStroke("F5"));
      
      JMenu file = new JMenu("file");
      JMenuItem loadButton = new JMenuItem("Load Image");
      loadButton.addActionListener(l -> loadImage());
      file.add(loadButton);
      
      JMenu edit = new JMenu("edit");
      
      JMenu scale = new JMenu("resolution");
      JMenu spatial = new JMenu("spatial");
      JMenu grayscale = new JMenu("gray scale");
      
      JMenuItem nearestNeighbor = new JMenuItem("Nearest Neighbor");
      spatial.add(nearestNeighbor);
      
      JMenuItem linearInterpolation = new JMenuItem("Linear Interpolation");
      spatial.add(linearInterpolation);
      
      JMenuItem bilinearInterpolation = new JMenuItem("Bilinear Interpolation");
      spatial.add(bilinearInterpolation);
      
      scale.add(spatial);
      edit.add(scale);
      
      JMenu bitDepthMenu = new JMenu("bit depth");
      for (int i = 1; i < 9; i++) {
         JMenuItem b = new JMenuItem(String.format("%d", i));
         int finalI = i;
         b.addActionListener(l -> {
            filters.add(x -> Assignment1.changeBitDepth(x, finalI));
            applyFilters();
         });
         bitDepthMenu.add(b);
      }
      grayscale.add(bitDepthMenu);
      
      scale.add(grayscale);
      
      JMenuItem clear = new JMenuItem("clear");
      clear.addActionListener(l -> clearFilters());
      
      edit.add(new JSeparator());
      edit.add(updateButton);
      edit.add(clear);
      
      menuBar.add(file);
      menuBar.add(edit);
      setJMenuBar(menuBar);
   }
   
   private void setTestActions() {
      
      filters = new Vector<Function<int[][], int[][]>>() {{
         add(x -> Assignment1.scaleNearestNeighbor(x, 128, 128));
         add(x -> Assignment1.linearInterpolation(x, 480, 480, true));
         add(x -> Assignment1.changeBitDepth(x, 4));
      }};
      
      applyFilters();
   }
   
   private void applyFilters() {
      bitDepth = 8;
      Function<int[][], int[][]>[] fs = new Function[filters.size()];
      filters.toArray(fs);
      outputImage = new GrayscaleImage(inputImage).apply(fs).toImage(bitDepth);
      updateOutputImage();
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
}
