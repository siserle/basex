package org.basex.gui.view.map;

import java.util.ArrayList;

/**
 * SplitLayout Algorithm.
 * @author joggele
 *
 */
public class StripAlgo extends MapAlgo{
  @Override
  public ArrayList<MapRect> calcMap(final MapRect r, final MapList l, 
      final double[] weights, final int ns, final int ne, final int level) {
    // stores all calculated rectangles
    ArrayList<MapRect> rects = new ArrayList<MapRect>();
    
    // node iterator
    int ni = ns;
    // first node of current row
    int start = ns;

    // setting initial proportions
    double xx = r.x;
    double yy = r.y;
    double ww = r.w;
    double hh = r.h;

    ArrayList<MapRect> row = new ArrayList<MapRect>();
    double height = 0;
    double weight = 0;
    double sumweight = 1;
    
    while(ni < ne) {
      weight += l.weights[ni];
      height = weight * r.h;
      
      ArrayList<MapRect> tmp = new ArrayList<MapRect>();

      double x = xx;
      for(int i = start; i <= ni; i++) {
        double w = i == ni ? xx + ww - x : l.weights[i] / weight * ww;
        tmp.add(new MapRect((int) x, (int) yy, (int) w, (int) height,
            l.list[i], level));
        x += (int) w;
      }

      // if ar has increased discard tmp and add row
      if(lineRatio(tmp) >= lineRatio(row)) {
        // add rects of row to solution
        rects.addAll(row);
        // preparing next line
        hh -= row.get(0).h;
        yy += row.get(0).h;
        tmp.clear();
        row.clear();
        start = ni;
        sumweight = sumweight - weight;
        weight = 0;
        // sometimes there has to be one rectangles to fill the left space
        if(ne == ni + 1) {
          row.add(new MapRect((int) xx, (int) yy, (int) ww, (int) hh,
              l.list[ni], level));
          break;
        }
      } else {
        row = tmp;
        ni++;
      }
    }

    // adding last row
    rects.addAll(row);
    
    return rects;
  }

  @Override
  String getType() {
    return "StripLayout Layout";
  }
}
