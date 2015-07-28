import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Board extends JPanel implements ActionListener {

    private final int DELAY = 10;
    private Timer timer;
    private Player player;
    private CopyOnWriteArrayList<Point2D> currentPointList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Polygon> polyList = new CopyOnWriteArrayList<>();
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

        polyList.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{0, 0, 1, 1}, 4));
        polyList.add(new Polygon(new int[]{193, 200, 200, 193}, new int[]{0, 0, 200, 200}, 4));
        polyList.add(new Polygon(new int[]{0, 200, 200, 0}, new int[]{171, 171, 200, 200}, 4));
        polyList.add(new Polygon(new int[]{0, 1, 1, 0}, new int[]{0, 0, 200, 200}, 4));
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
        drawPolys(g2d);

        g2d.setColor(Color.red);
        drawLine(g2d);

        g2d.setColor(Color.green);
        g2d.fillOval(x, y, player.getWidth(), player.getHeight());

    }

    private void drawPolys(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        polyList.forEach(g2d::fill);        //fill each polynome
    }

    private void drawLine(Graphics g) { //draw a line out of the current points
        Graphics2D g2d = (Graphics2D) g;

        for (Point2D c : currentPointList) {
            g2d.drawLine((int) c.getX(), (int) c.getY(), (int) c.getX(), (int) c.getY());
        }
    }

    public void checkPolys() {                      //check whether the line reached a polynome
        int size = currentPointList.size();
        if (size > 0) {
            for (Polygon p : polyList) {
                if (p.contains(currentPointList.get(size - 1))) {
                    flood(currentPointList.get(size - 1));  //starte flood von der hälfte der Linie aus
                    addPoly(currentPointList);
                    currentPointList.clear();
                    return;
                }
            }
        }
    }

    private void flood(Point2D p2d) {       //coordinates the calculation of the sizes of the empty spaces and the filling of them
        int x = (int) p2d.getX();
        int y = (int) p2d.getY();

        int[] leftUp = null;
        int[] leftDown = null;
        int[] rightUp = null;
        int[] rightDown = null;

        ReturnData leftUpData = null;
        ReturnData leftDownData = null;
        ReturnData rightUpData = null;
        ReturnData rightDownData = null;

        if (x > 0) {
            if (y > 0 && !allPoints[x - 1][y - 1]) {
                leftUp = new int[]{x - 1, y - 1};
            }
            if (y < 199 && !allPoints[x - 1][y + 1]) {
                leftDown = new int[]{x - 1, y + 1};
            }
        }
        if (x < 199) {
            if (y > 0 && !allPoints[x + 1][y - 1]) {
                rightUp = new int[]{x + 1, y - 1};
            }
            if (y < 199 && !allPoints[x + 1][y + 1]) {
                rightDown = new int[]{x + 1, y + 1};
            }
        }
        if (leftUp != null) {
            leftUpData = floodList(leftUp, new CopyOnWriteArrayList<>(), new boolean[200][200]);
        }
        if (leftDown != null) {
            leftDownData = floodList(leftDown, new CopyOnWriteArrayList<>(), new boolean[200][200]);
        }
        if (rightUp != null) {
            rightUpData = floodList(rightUp, new CopyOnWriteArrayList<>(), new boolean[200][200]);
        }
        if (rightDown != null) {
            rightDownData = floodList(rightDown, new CopyOnWriteArrayList<>(), new boolean[200][200]);
        }
        ReturnData second = getSecond(leftUpData, leftDownData, rightUpData, rightDownData);
        floodFill(second.getCopyOnWriteArrayList());
        //if (second.size() < 20000) {
        mergePoints(second.getBooleans());
        //}
        System.out.println("Merged! " + second.size());
    }

    private ReturnData floodList(int[] cords, CopyOnWriteArrayList<Point2D> wraps, boolean[][] visited) {     //generate a list of free points of polynome
        int x = cords[0];
        int y = cords[1];
        visited[x][y] = true;
        if (x > 0) {
            if (allPoints[x - 1][y]) {
                wraps.add(new Point(x - 1, y));
            } else if (!visited[x - 1][y]) {
                floodList(new int[]{x - 1, y}, wraps, visited);
            }
        }
        if (x < 199) {
            if (allPoints[x + 1][y]) {
                wraps.add(new Point(x + 1, y));
            } else if (!visited[x + 1][y]) {
                floodList(new int[]{x + 1, y}, wraps, visited);
            }
        }
        if (y > 0) {
            if (allPoints[x][y - 1]) {
                wraps.add(new Point(x, y - 1));
            } else if (!visited[x][y - 1]) {
                floodList(new int[]{x, y - 1}, wraps, visited);
            }
        }
        if (y < 199) {
            if (allPoints[x][y + 1]) {
                wraps.add(new Point(x, y + 1));
            } else if (!visited[x][y + 1]) {
                floodList(new int[]{x, y + 1}, wraps, visited);
            }
        }
        return new ReturnData(wraps, visited);
    }


    private void floodFill(CopyOnWriteArrayList<Point2D> points) {     //make a polynome out of the fitting points
        addPoly(points);
    }

    private void mergePoints(boolean[][] booleans) {    //Merges all Points of floodfill into allPoints
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

    private ReturnData getSecond(ReturnData leftUpData, ReturnData leftDownData, ReturnData rightUpData, ReturnData rightDownData) {        //get second hightest element to fill, biggest is to be left free,
        if (leftUpData == null) {
            leftUpData = new ReturnData();
        }
        if (leftDownData == null) {
            leftDownData = new ReturnData();
        }
        if (rightUpData == null) {
            rightUpData = new ReturnData();
        }
        if (rightDownData == null) {
            rightDownData = new ReturnData();
        }

        LinkedList<ReturnData> listReturns = new LinkedList<>();
        listReturns.add(leftUpData);
        listReturns.add(leftDownData);
        listReturns.add(rightUpData);
        listReturns.add(rightDownData);
        sortBubble(listReturns);
        return listReturns.get(2);
    }

    private void sortBubble(LinkedList<ReturnData> listReturns) {
        ReturnData[] arrayReturns = new ReturnData[listReturns.size()];
        for (int i = 0; i < arrayReturns.length; i++) {
            arrayReturns[i] = listReturns.get(i);
        }
        int swaps;
        int size = arrayReturns.length;
        do {
            swaps = 0;
            for (int i = 0; i < size - 1; i++) {
                if (arrayReturns[i].size() < arrayReturns[i + 1].size()) {
                    ReturnData helper = arrayReturns[i];
                    arrayReturns[i] = arrayReturns[i + 1];
                    arrayReturns[i + 1] = helper;
                    swaps++;
                }
            }
        } while (swaps != 0);
    }

    private void swap(ReturnData arrayReturn0, ReturnData arrayReturn1) {

    }

    private void addPoly(CopyOnWriteArrayList<Point2D> pointList) {      //Method to add a Polynome out of a CopyOnWriteArrayList of Points, since polygons need 2 seperate arrays of x cords and y cords
        int n = pointList.size();
        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            int xhelper = (int) pointList.get(i).getX();
            x[i] = xhelper;
            y[i] = (int) pointList.get(i).getY();
        }
        this.polyList.add(new Polygon(x, y, n));
    }

    private class ReturnData {      //Data Structure to return the List of the needed Points and the boolarray of the visited Points

        private CopyOnWriteArrayList<Point2D> copyOnWriteArrayList;
        private boolean[][] booleans;

        public ReturnData() {
            copyOnWriteArrayList = new CopyOnWriteArrayList<>();
            booleans = new boolean[200][200];
        }

        public ReturnData(CopyOnWriteArrayList<Point2D> copyOnWriteArrayList, boolean[][] booleans) {
            this.copyOnWriteArrayList = copyOnWriteArrayList;
            this.booleans = booleans;
        }

        public CopyOnWriteArrayList<Point2D> getCopyOnWriteArrayList() {
            return copyOnWriteArrayList;
        }

        public boolean[][] getBooleans() {
            return booleans;
        }

        public int size() {
            return copyOnWriteArrayList.size();
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
            checkPolys();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            checkPolys();
        }
    }
}