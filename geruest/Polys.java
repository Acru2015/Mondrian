import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.CopyOnWriteArrayList;

public class Polys {
    private CopyOnWriteArrayList<Polygon> polyList;
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

    public void drawPolys(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        this.polyList.forEach(g2d::fill);        //fill each polygon
    }

    public void checkPolys(CopyOnWriteArrayList<Point2D> currentPointList, boolean[][] allPoints) {                      //check whether the line reached a polygon
        this.allPoints = allPoints;

        int size = currentPointList.size();
        if (size > 0) {
            if (allPoints[((int) currentPointList.get(size - 1).getX())][((int) currentPointList.get(size - 1).getY())]) {
                flood(currentPointList.get(size - 1));
                currentPointList.clear();
            }
            /*for (Polygon p : polyList) {
                if (p.contains(currentPointList.get(size - 1))) {
                    flood(currentPointList.get(size - 1));  //starte flood von Punkt x der Linie aus
                    //addPoly(currentPointList);
                    currentPointList.clear();
                    return;
                }
            }*/
        }
    }

    private void addPoly(CopyOnWriteArrayList<Point2D> points) {      //Method to add a Polynome out of a CopyOnWriteArrayList of Points, since polygons need 2 seperate arrays of x cords and y cords
        int n = points.size();
        int[] x = new int[n];
        int[] y = new int[n];

        for (int i = 0; i < n; i++) {
            int xHelper = (int) points.get(i).getX();
            int yHelper = (int) points.get(i).getX();

            x[i] = xHelper;
            y[i] = yHelper;
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
        ReturnData[] listReturns = new ReturnData[]{leftUpData, leftDownData, rightUpData, rightDownData};
        ReturnData second = getSecond(listReturns);
        //addPoly(second.getCopyOnWriteArrayList());
        pointsToMerge = second.getBooleans();
    }

    private ReturnData floodList(int[] cords, CopyOnWriteArrayList<Point2D> wraps, boolean[][] visitedBoolArray) {     //generate a list of free points
        int x = cords[0];
        int y = cords[1];
        visitedBoolArray[x][y] = true;
        if (x > 0) {
            if (allPoints[x - 1][y]) {
                wraps.add(new Point(x - 1, y));
            } else if (!visitedBoolArray[x - 1][y]) {
                floodList(new int[]{x - 1, y}, wraps, visitedBoolArray);
            }
        }
        if (x < 199) {
            if (allPoints[x + 1][y]) {
                wraps.add(new Point(x + 1, y));
            } else if (!visitedBoolArray[x + 1][y]) {
                floodList(new int[]{x + 1, y}, wraps, visitedBoolArray);
            }
        }
        if (y > 0) {
            if (allPoints[x][y - 1]) {
                wraps.add(new Point(x, y - 1));
            } else if (!visitedBoolArray[x][y - 1]) {
                floodList(new int[]{x, y - 1}, wraps, visitedBoolArray);
            }
        }
        if (y < 199) {
            if (allPoints[x][y + 1]) {
                wraps.add(new Point(x, y + 1));
            } else if (!visitedBoolArray[x][y + 1]) {
                floodList(new int[]{x, y + 1}, wraps, visitedBoolArray);
            }
        }
        return new ReturnData(wraps, visitedBoolArray);
    }

    private ReturnData getSecond(ReturnData[] returnDataArray) {        //get second highest element to fill, biggest is to be left free,
        for (int i = 0; i < returnDataArray.length; i++) {
            if (returnDataArray[i] == null) {
                returnDataArray[i] = new ReturnData();
            }
        }
        sortBubble(returnDataArray);
        System.out.println(returnDataArray[returnDataArray.length - 1].size());
        System.out.println(returnDataArray[returnDataArray.length - 2].size());
        System.out.println(returnDataArray[returnDataArray.length - 3].size());
        System.out.println(returnDataArray[returnDataArray.length - 4].size());
        if (returnDataArray[returnDataArray.length - 2].size() == returnDataArray[returnDataArray.length - 1].size()) {
            return returnDataArray[0];
        }
        return returnDataArray[returnDataArray.length - 2];
    }

    private void sortBubble(ReturnData[] arrayReturns) {
        int swaps;
        int size = arrayReturns.length;
        do {
            swaps = 0;
            for (int i = 0; i < size - 1; i++) {
                if (arrayReturns[i].size() > arrayReturns[i + 1].size()) {
                    ReturnData helper = arrayReturns[i];
                    arrayReturns[i] = arrayReturns[i + 1];
                    arrayReturns[i + 1] = helper;
                    swaps++;
                }
            }
        } while (swaps != 0);
    }
}
