import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {
    private JScrollPane inputPane, outputPane;
    private JLabel inputLabel, outputLabel;

    private BufferedImage inputImage;
    private BufferedImage outputImage;

    private void addComponents(JComponent... args) {
        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        for (JComponent comp: args) {
            gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(comp));
            gl.setVerticalGroup(gl.createParallelGroup().addComponent(comp));
        }

        pack();
    }

    public MainWindow() {
        // set title
        setTitle("CS-555 Image Processor");
        setLayout(new FlowLayout());

        try {
            File f = new File("test.jpg");

            inputImage = ImageIO.read(f);
            outputImage = ImageIO.read(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputLabel = new JLabel(new ImageIcon(inputImage));
        outputLabel = new JLabel(new ImageIcon(outputImage));

        inputPane = new JScrollPane(inputLabel);
        inputPane.setPreferredSize(new Dimension(512,512));

        outputPane = new JScrollPane(outputLabel);
        outputPane.setPreferredSize(new Dimension(512,512));

        add(inputPane);
        add(outputPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("rename this");
        JMenuItem updateButton = new JMenuItem("update");
        updateButton.addActionListener(l -> updateOutputImage());
        menu.add(updateButton);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // display
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        outputImage = new ProcessableImage(inputImage)
                .apply(x -> ProcessableImage.scaleNearestNeighbor(x,64,64))
                .toImage();
    }

    public void updateOutputImage() {
        outputLabel.setIcon(new ImageIcon(outputImage));
    }
}
