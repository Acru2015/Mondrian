import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.CopyOnWriteArrayList;

public class Board extends JPanel implements ActionListener {

    private final int DELAY = 10;
    Polys polys = new Polys();
    private Timer timer;
    private Player player;
    private CopyOnWriteArrayList<Point2D> currentPointList = new CopyOnWriteArrayList<>();
    private boolean[][] allPoints = new boolean[200][200];

    public Board() {
        initBoard();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.white);

        player = new Player();

        timer = new Timer(DELAY, this);
        timer.start();

        polys.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{0, 0, 1, 1}, 4));
        polys.add(new Polygon(new int[]{193, 200, 200, 193}, new int[]{0, 0, 200, 200}, 4));
        polys.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{171, 171, 200, 200}, 4));
        polys.add(new Polygon(new int[]{0, 1, 1, 0}, new int[]{0, 0, 200, 200}, 4));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.move();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);

        Toolkit.getDefaultToolkit().sync();
    }

    private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);

        int x = player.getX();
        int y = player.getY();
        //System.out.println("X: " + x + " Y: " + y);
        //System.out.println(allPoints[x][y]);

        if (x >= 0 && x < 200 && y >= 0 && y < 200 && !allPoints[x][y]) {
            allPoints[x][y] = true;
            currentPointList.add(new Point(x, y));
        }

        g2d.setStroke(new BasicStroke(2));

        g2d.setColor(Color.blue);
        polys.drawPolys(g2d);

        g2d.setColor(Color.red);
        drawLine(g2d);

        g2d.setColor(Color.green);
        g2d.fillOval(x, y, player.getWidth(), player.getHeight());

    }

    private void drawLine(Graphics g) { //draw a line out of the current points
        Graphics2D g2d = (Graphics2D) g;

        for (Point2D c : currentPointList) {
            g2d.drawLine((int) c.getX(), (int) c.getY(), (int) c.getX(), (int) c.getY());
        }
    }

    public void mergePoints(boolean[][] booleans) {    //Merges all Points of floodfill into allPoints
        for (int i = 0; i < booleans.length; i++) {
            for (int j = 0; j < booleans[i].length; j++) {
                if (booleans[i][j]) {
                    allPoints[i][j] = true;
                }
            }
        }
    }

    private void mergePoints(CopyOnWriteArrayList<Point2D> currentPointList) {
        for (Point2D aCurrentPointList : currentPointList) {
            allPoints[((int) aCurrentPointList.getY())][((int) aCurrentPointList.getY())] = true;
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
            polys.checkPolys(currentPointList, allPoints);
            mergePoints(polys.getPointsToMerge());
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            polys.checkPolys(currentPointList, allPoints);
            mergePoints(polys.getPointsToMerge());
        }
    }
}