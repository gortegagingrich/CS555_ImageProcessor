import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class MainWindow extends JFrame {
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
            JFileChooser jfc = new JFileChooser();

            /*
            int retVal = jfc.showOpenDialog(this);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
            */

            inputImage = ImageIO.read(f);
            outputImage = ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        JMenu menu = new JMenu("rename this");
        JMenuItem updateButton = new JMenuItem("test actions");
        updateButton.addActionListener(l -> testActions());
        menu.add(updateButton);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void testActions() {
        ProcessableImage processableImage = new ProcessableImage(inputImage);
        processableImage = processableImage.apply(
                x -> Assignment1.scaleNearestNeighbor(x, 256, 256)
                //,x-> Assignment1.scaleNearestNeighbor(x, 512, 512)
                ,x -> ProcessableImage.linearInterpolation(x,512, 512, true)
        );
        outputImage = processableImage.toImage();
        updateOutputImage();
    }
}
