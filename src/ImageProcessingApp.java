import org.opencv.core.*;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageProcessingApp extends JFrame {

    private JLabel from;
    private JLabel to;
    private String path;

    public ImageProcessingApp() {
        super("Image Processing App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1920, 1080);

        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Labels
        from = new JLabel();
        to = new JLabel();

        // Buttons
        JButton openButton = new JButton("Open Image");
        JButton erodeButton = new JButton("Erode Image");
        JButton dilateButton = new JButton("Dilate Image");
        JButton segmentImageButton = new JButton("Segment Image");



        // Event handlers
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openImage();
            }
        });

        erodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                erodePic();
            }
        });

        dilateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dilatePic();
            }
        });
        segmentImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                segmentImage();
            }
        });

        // Layout
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(openButton);
        panel.add(erodeButton);
        panel.add(dilateButton);
        panel.add(segmentImageButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(from, BorderLayout.WEST);
        getContentPane().add(to, BorderLayout.EAST);
    }

    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Изображения", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            path = selectedFile.getAbsolutePath();

            ImageIcon imageIcon = new ImageIcon(path);
            from.setIcon(imageIcon);
            to.setIcon(null); // Очистить обработанное изображение
        }
    }





    private void erodePic() {
        if (from.getIcon() != null) {
            BufferedImage bufferedImage = new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            from.paint(g);
            g.dispose();

            Mat img = bufferedImageToMat(bufferedImage);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

            Mat to_img = new Mat();

            if (!img.empty()) {
                Imgproc.erode(img, to_img, kernel);
                displayProcessed(to_img);
            } else {
                System.err.println("Ошибка: Исходное изображение пустое.");
            }
        }
    }

    private void dilatePic() {
        if (from.getIcon() != null) {
            BufferedImage bufferedImage = new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            from.paint(g);
            g.dispose();

            Mat img = bufferedImageToMat(bufferedImage);

            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

            Mat to_img = new Mat();

            if (!img.empty()) {
                Imgproc.dilate(img, to_img, kernel);
                displayProcessed(to_img);
            } else {
                System.err.println("Ошибка: Исходное изображение пустое.");
            }
        }
    }

    private void displayProcessed(Mat processedImage) {
        MatOfByte buf = new MatOfByte();
        Imgcodecs.imencode(".jpg", processedImage, buf);

        try {
            InputStream in = new ByteArrayInputStream(buf.toArray());
            BufferedImage bufferedImage = ImageIO.read(in);

            // Фиксированный размер для отображения обработанного изображения
            int width = 400;
            int height = 600;

            Image scaledImage = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            to.setIcon(new ImageIcon(scaledImage));
            to.repaint(); // Обновляем компонент JLabel
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void segmentImage() {
        if (from.getIcon() != null) {
            BufferedImage bufferedImage = new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            from.paint(g);
            g.dispose();

            Mat img = bufferedImageToMat(bufferedImage);
            Mat grayImage = new Mat();
            Imgproc.cvtColor(img, grayImage, Imgproc.COLOR_BGR2GRAY);

            // Применение Canny Edge Detector для обнаружения краев
            Mat edges = new Mat();
            Imgproc.Canny(grayImage, edges, 50, 150);

            // Вывод обработанного изображения
            displayProcessed(edges);
        }
    }

    // пример использования фильтра Собеля:
    /*private void segmentImage() {
        if (from.getIcon() != null) {
            BufferedImage bufferedImage = new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            from.paint(g);
            g.dispose();

            Mat img = bufferedImageToMat(bufferedImage);
            Mat grayImage = new Mat();
            Imgproc.cvtColor(img, grayImage, Imgproc.COLOR_BGR2GRAY);

            Mat sobelX = new Mat();
            Mat sobelY = new Mat();
            Imgproc.Sobel(grayImage, sobelX, CvType.CV_16S, 1, 0);
            Imgproc.Sobel(grayImage, sobelY, CvType.CV_16S, 0, 1);

            Mat absGradX = new Mat();
            Mat absGradY = new Mat();
            Core.convertScaleAbs(sobelX, absGradX);
            Core.convertScaleAbs(sobelY, absGradY);

            Mat gradient = new Mat();
            Core.addWeighted(absGradX, 0.5, absGradY, 0.5, 0, gradient);

            // Вывод обработанного изображения
            displayProcessed(gradient);
        }
    }*/





    private Mat bufferedImageToMat(BufferedImage bufferedImage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException e) {
            e.printStackTrace();
            return new Mat();
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ImageProcessingApp().setVisible(true);
            }
        });
    }
}

