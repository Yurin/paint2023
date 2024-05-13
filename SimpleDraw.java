import javax.swing.JFrame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseAdapter;

public class SimpleDraw extends JFrame implements MouseMotionListener {
    DrawPanel panel;
    int prevX = -1, prevY = -1;

    public static void main(String[] args) {
        SimpleDraw frame = new SimpleDraw();
        frame.init();
    }

    private void init() {
        this.setTitle("Simple Draw");
        this.setSize(1000, 600);
        panel = new DrawPanel();
        panel.addMouseMotionListener(this);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }
        });

        this.getContentPane().add(panel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void mouseDragged(MouseEvent arg0) {
        int currentX = arg0.getX();
        int currentY = arg0.getY();
        if (!panel.isMoveBalloonMode()) {  // 吹き出し移動モードが有効でない場合のみ線を描く
            panel.drawLine(prevX, prevY, currentX, currentY);
        }
        prevX = currentX;
        prevY = currentY;
}

    public void mouseMoved(MouseEvent arg0) {
    }
}

