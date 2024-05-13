import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;


public class DrawPanel extends JPanel {
    private Color currentColor = Color.BLACK;
    private int penThickness = 2;
    private BufferedImage image;
    private List<Line> lines; 
    private List<BalloonStamp> balloonStamps;
    private BalloonStamp selectedBalloon;
    private boolean moveBalloonMode = false; 
    private JButton increaseSizeButton;
    private JButton decreaseSizeButton;

    public DrawPanel() {
        setLayout(new FlowLayout());
        image = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_ARGB);
        setBackground(Color.WHITE);
        lines = new ArrayList<>();
        balloonStamps = new ArrayList<>();       

        JButton colorButton = new JButton("Select Color");
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(null, "Choose a Color", currentColor);
                if (newColor != null) {
                    setCurrentColor(newColor);
                }
            }
        });

        JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, penThickness);
        thicknessSlider.setMajorTickSpacing(1);
        thicknessSlider.setPaintTicks(true);
        thicknessSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    setPenThickness(source.getValue());
                }
            }
        });

        JButton saveButton = new JButton("Save as PNG");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = JOptionPane.showInputDialog("Enter the file name (with .png extension):");
                if (fileName != null && !fileName.trim().isEmpty()) {
                    saveImageAsPNG(fileName);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid file name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton undoButton = new JButton("やり直す");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        JButton balloonButton = new JButton("吹き出しスタンプ");
        balloonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ボタンがクリックされたときの処理
                addBalloonStamp(100, 100, 170, 170, Color.BLACK, 2);  // 吹き出しスタンプの初期位置とサイズを指定
            }
        });

        JButton moveBalloonOnButton = new JButton("吹き出し移動機能ON");
        moveBalloonOnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBalloonMode = true;
            }
        });

        JButton moveBalloonOffButton = new JButton("ペン機能をON");
        moveBalloonOffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBalloonMode = false;
            }
        });

        // ＋ボタン
        increaseSizeButton = new JButton("＋");
        increaseSizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedBalloon != null) {
                    selectedBalloon.setWidth(selectedBalloon.getWidth() + 10);
                    selectedBalloon.setHeight(selectedBalloon.getHeight() + 10);
                    repaint();
                }
            }
        });

        // −ボタン
        decreaseSizeButton = new JButton("−");
        decreaseSizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedBalloon != null && selectedBalloon.getWidth() > 10 && selectedBalloon.getHeight() > 10) {
                    selectedBalloon.setWidth(selectedBalloon.getWidth() - 10);
                    selectedBalloon.setHeight(selectedBalloon.getHeight() - 10);
                    repaint();
                }
            }
        });

        add(colorButton);
        add(thicknessSlider);
        add(saveButton);
        add(undoButton);
        add(balloonButton);
        add(moveBalloonOnButton);
        add(moveBalloonOffButton);
        add(increaseSizeButton);
        add(decreaseSizeButton);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // マウスが押されたときの処理
                selectedBalloon = getSelectedBalloon(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // マウスが離れたときの処理
                selectedBalloon = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (moveBalloonMode && selectedBalloon != null) {
                    selectedBalloon.setX(e.getX());
                    selectedBalloon.setY(e.getY());
                    repaint();
                } 
                else if(!moveBalloonMode && SwingUtilities.isLeftMouseButton(e)) {
                    drawLine(e.getX(), e.getY(), e.getX(), e.getY());
                }               
            }
        });
    }
    public boolean isMoveBalloonMode() {
        return moveBalloonMode;
    }

    private BalloonStamp getSelectedBalloon(int x, int y) {
        for (BalloonStamp stamp : balloonStamps) {
            if (x >= stamp.getX() && x <= (stamp.getX() + stamp.getWidth())
                    && y >= stamp.getY() && y <= (stamp.getY() + stamp.getHeight())) {
                return stamp;
            }
        }
        return null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
        for (BalloonStamp stamp : balloonStamps) {
            if (stamp != selectedBalloon) {  
                drawBalloonStamp(g, stamp);
            }
        }
        if (selectedBalloon != null) {
            drawBalloonStamp(g, selectedBalloon);
        }
    }

    public void drawBalloonStamp(Graphics g, BalloonStamp stamp) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(stamp.getColor());
            g2d.setStroke(new BasicStroke(stamp.getThickness()));

            int x = stamp.getX();
            int y = stamp.getY();
            int width = stamp.getWidth();
            int height = stamp.getHeight();

            Path2D.Double balloon = new Path2D.Double();
            balloon.moveTo(x, y);
            balloon.lineTo(x + width, y);
            balloon.lineTo(x + width, y + height / 2);
            balloon.lineTo(x + width + 20, y + height / 2);
            balloon.lineTo(x + width, y + height / 2 + 20);
            balloon.lineTo(x + width, y + height);
            balloon.lineTo(x, y + height);
            balloon.closePath();
            g2d.draw(balloon);
            if (!stamp.getText().isEmpty()) {
                g2d.drawString(stamp.getText(), x + 10, y + height / 2);
            }
        } else {
            throw new UnsupportedOperationException("Graphics must be an instance of Graphics2D");
        }
    }
    

    public void addBalloonStamp(int x, int y, int width, int height, Color color, int thickness) {
        BalloonStamp balloonStamp = new BalloonStamp(x, y, width, height, color, thickness);
        balloonStamps.add(balloonStamp);
        add(balloonStamp.getTextField());
        repaint();
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        Graphics g = image.getGraphics();
        g.setColor(currentColor);
        ((Graphics2D) g).setStroke(new BasicStroke(penThickness));
        g.drawLine(x1, y1, x2, y2);
        lines.add(new Line(x1, y1, x2, y2, currentColor, penThickness)); 
        repaint();
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public void setPenThickness(int thickness) {
        this.penThickness = thickness;
    }

    public void saveImageAsPNG(String fileName) {
        try {
            Graphics g = image.getGraphics();
    
            // 吹き出しを描画
            for (BalloonStamp stamp : balloonStamps) {
                drawBalloonStamp(g, stamp);
            }    
            // 画像を保存
            ImageIO.write(image, "png", new File(fileName));
            JOptionPane.showMessageDialog(null, "Image saved successfully!", "Save Image", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void undo() {
        if (!lines.isEmpty()) {
            lines.remove(lines.size() - 1);
            clearImage();
            for (Line line : lines) {
                drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
            }
        }
    }

    public void clearImage() {
        Graphics g = image.getGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private static class Line {
        private int x1, y1, x2, y2;
        private Color color;
        private int thickness;

        public Line(int x1, int y1, int x2, int y2, Color color, int thickness) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.thickness = thickness;
        }

        public int getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public Color getColor() {
            return color;
        }

        public int getThickness() {
            return thickness;
        }
    }

    private static class BalloonStamp {
        private int x, y, width, height;
        private Color color;
        private int thickness;
        private String text = "";
        private JTextField textField;

        public BalloonStamp(int x, int y, int width, int height, Color color, int thickness) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.thickness = thickness;
            this.textField = new JTextField();
        }

        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Color getColor() {
            return color;
        }

        public int getThickness() {
            return thickness;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    
        public void setHeight(int height) {
            this.height = height;
        }

        public String getText() {
            return text;
        }
    
        public void setText(String text) {
            this.text = text;
            textField.setText(text);
        }

        public JTextField getTextField() {
            return textField;
        }
    }
    public void setBalloonText(String text) {
        if (selectedBalloon != null) {
            selectedBalloon.setText(text);
            repaint();
        }
    }

}


