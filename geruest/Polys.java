import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.CopyOnWriteArrayList;

public class Polys {
    private CopyOnWriteArrayList<Polygon> polyList = new CopyOnWriteArrayList<>();
    private boolean[][] allPoints;
    private boolean[][] pointsToMerge;

    public Polys() {
        this.polyList = new CopyOnWriteArrayList<>();
    }

    public boolean[][] getPointsToMerge() {
        return pointsToMerge;
    }

    public void add(Polygon poly) {
        polyList.add(poly);
    }

    public boolean[][] getAllPoints() {
        return allPoints;
    }

    public void checkPolys(CopyOnWriteArrayList<Point2D> currentPointList, boolean[][] allPoints) {                      //check whether the line reached a polynome
        this.allPoints = allPoints;

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

    public void drawPolys(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        polyList.forEach(g2d::fill);        //fill each polynome
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
        pointsToMerge = second.getBooleans();
        //mergePoints(second.getBooleans());
        //System.out.println("Merged! " + second.size());
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

        CopyOnWriteArrayList<ReturnData> listReturns = new CopyOnWriteArrayList<>();
        listReturns.add(leftUpData);
        listReturns.add(leftDownData);
        listReturns.add(rightUpData);
        listReturns.add(rightDownData);
        sortBubble(listReturns);
        return listReturns.get(2);
    }

    private void sortBubble(CopyOnWriteArrayList<ReturnData> listReturns) {
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


    private void floodFill(CopyOnWriteArrayList<Point2D> points) {     //make a polynome out of the fitting points
        addPoly(points);
    }
}
