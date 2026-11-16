package task04;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DrawPad - Completed and Repaired Implementation
 * Fixes mouseClicked override signature, stores points persistently
 * across OS repaints/resizes, and centres 6x6 filled circles on click locations.
 */
public class DrawPad extends Frame {

    private final List<Point> points = new ArrayList<>();
    private final Label status = new Label("Clicks: 0");

    public DrawPad() {
        super("Draw Pad");
        add(status, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            // TODO 1: Override mouseClicked correctly: store the point, update the
            // label and request a repaint
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(new Point(e.getX(), e.getY()));
                status.setText("Clicks: " + points.size());
                repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    @Override
    public void paint(Graphics g) {
        // TODO 2: Draw every stored point as a 6 × 6 filled circle centred on the point
        super.paint(g);
        g.setColor(new Color(40, 100, 200));
        for (Point p : points) {
            // Centering a 6x6 circle requires offsetting by radius = 3
            g.fillOval(p.x - 3, p.y - 3, 6, 6);
        }
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public String getStatusText() {
        return status.getText();
    }

    // Programmatic simulation method for test suites
    public void simulateClick(int x, int y) {
        MouseEvent event = new MouseEvent(
                this,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                x, y,
                1,
                false
        );
        for (java.awt.event.MouseListener l : getMouseListeners()) {
            l.mouseClicked(event);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Running DrawPad programmatic forensics test...");
            DrawPad pad = new DrawPad();
            System.out.println("Initial status: " + pad.getStatusText());

            pad.simulateClick(50, 60);
            pad.simulateClick(120, 150);
            pad.simulateClick(200, 80);

            System.out.println("Status after 3 clicks: " + pad.getStatusText());
            System.out.println("Stored points count: " + pad.getPoints().size());
            assert pad.getPoints().size() == 3 : "Points size must be 3";
            assert pad.getStatusText().equals("Clicks: 3") : "Label must display Clicks: 3";

            System.out.println("DrawPad Forensics verification PASSED.");
            System.exit(0);
        }

        new DrawPad().setVisible(true);
    }
}
