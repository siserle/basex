package org.basex.query.gflwor;

import static org.basex.util.Array.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;


/**
 * FLWOR {@code order by}-expression.
 * @author Leo Woerteler
 */
public class OrderBy extends GFLWOR.Clause {
  /** Variables to sort. */
  Var[] vars;
  /** Sort keys. */
  final Key[] keys;
  /** Stable sort flag. */
  final boolean stable;

  /**
   * Constructor.
   * @param vs variables to sort
   * @param ks sort keys
   * @param stbl stable sort
   */
  public OrderBy(final Var[] vs, final Key[] ks, final boolean stbl) {
    vars = vs;
    keys = ks;
    stable = stbl;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Sorted output tuples. */
      private Value[][] tpls;
      /** Permutation of the values. */
      private int[] perm;
      /** Current position. */
      int pos;
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(tpls == null) init(ctx);
        if(pos == tpls.length) return false;
        final int p = perm[pos++];
        final Value[] tuple = tpls[p];
        // free the space occupied by the tuple
        tpls[p] = null;
        for(int i = 0; i < vars.length; i++) ctx.set(vars[i], tuple[i]);
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param ctx query context
       * @throws QueryException evaluation exception
       */
      private void init(final QueryContext ctx) throws QueryException {
        // keys are stored at off positions, values ad even ones
        List<Value[]> tuples = new ArrayList<Value[]>();
        while(sub.next(ctx)) {
          final Item[] key = new Item[keys.length];
          for(int i = 0; i < keys.length; i++)
            key[i] = keys[i].expr.item(ctx, keys[i].input);
          tuples.add(key);

          final Value[] vals = new Value[vars.length];
          for(int i = 0; i < vars.length; i++) vals[i] = ctx.get(vars[i]);
          tuples.add(vals);
        }

        final int len = tuples.size() >>> 1;
        final Item[][] ks = new Item[len][];
        perm = new int[len];
        tpls = new Value[len][];
        for(int i = 0; i < len; i++) {
          perm[i] = i;
          tpls[i] = tuples.get((i << 1) | 1);
          ks[i] = (Item[]) tuples.get(i << 1);
        }
        // be nice to the garbage collector
        tuples = null;
        sort(ks, 0, len);
      }

      /**
       * Recursively sorts the specified items.
       * The algorithm is derived from {@link Arrays#sort(int[])}.
       * @param start start position
       * @param len end position
       * @throws QueryException query exception
       */
      private void sort(final Item[][] ks, final int start, final int len)
          throws QueryException {
        if(len < 7) {
          // use insertion sort of small arrays
          for(int i = start; i < len + start; i++)
            for(int j = i; j > start && cmp(ks[perm[j - 1]], ks[perm[j]], -1) > 0; j--)
              swap(perm, j, j - 1);
          return;
        }

        // find a good pivot element
        int mid = start + (len >> 1);
        if(len > 7) {
          int left = start, right = start + len - 1;
          if(len > 40) {
            final int k = len >>> 3;
            left = median(ks, left, left + k, left + (k << 1));
            mid = median(ks, mid - k, mid, mid + k);
            right = median(ks, right - (k << 1), right - k, right);
          }
          mid = median(ks, left, mid, right);
        }

        final Item[] pivot = ks[perm[mid]];

        // partition the values
        int a = start, b = a, c = start + len - 1, d = c;
        while(true) {
          while(b <= c) {
            final int h = cmp(ks[perm[b]], pivot, b - mid);
            if(h > 0) break;
            if(h == 0) swap(perm, a++, b);
            ++b;
          }
          while(c >= b) {
            final int h = cmp(ks[perm[c]], pivot, c - mid);
            if(h < 0) break;
            if(h == 0) swap(perm, c, d--);
            --c;
          }
          if(b > c) break;
          swap(perm, b++, c--);
        }

        // Swap pivot elements back to middle
        int k;
        final int n = start + len;
        k = Math.min(a - start, b - a);
        swap(perm, start, b - k, k);
        k = Math.min(d - c, n - d - 1);
        swap(perm, b, n - k, k);

        // recursively sort non-pivot elements
        if((k = b - a) > 1) sort(ks, start, k);
        if((k = d - c) > 1) sort(ks, n - k, k);
      }

      /**
       * Returns the difference of two entries (part of QuickSort).
       * @param a sort keys of first item
       * @param b sort keys of second item
       * @param d sort keys of second item
       * @return result
       * @throws QueryException query exception
       */
      private int cmp(final Item[] a, final Item[] b, final int d) throws QueryException {
        for(int k = 0; k < keys.length; k++) {
          final Key or = keys[k];
          final Item m = a[k], n = b[k];
          final int c = m == null ? n == null ? 0 : or.least ? -1 : 1 :
            n == null ? or.least ? 1 : -1 : m.diff(or.input, n);
          if(c != 0) return or.desc ? -c : c;
        }

        // optional stable sorting
        return stable ? d : 0;
      }

      /**
       * Returns the index of the median of the three indexed integers.
       * @param ks key array
       * @param a first offset
       * @param b second offset
       * @param c thirst offset
       * @return median
       * @throws QueryException query exception
       */
      private int median(final Item[][] ks, final int a, final int b, final int c)
          throws QueryException {
        final Item[] ka = ks[perm[a]], kb = ks[perm[b]], kc = ks[perm[c]];
        return cmp(ka, kb, a - b) < 0
            ? cmp(kb, kc, b - c) < 0 ? b : cmp(ka, kc, a - c) < 0 ? c : a
            : cmp(kb, kc, b - c) > 0 ? b : cmp(ka, kc, a - c) > 0 ? c : a;
      }
    };
  }

  /**
   * Sort key.
   * @author Leo Woerteler
   */
  public static class Key {
    /** Sort key expression. */
    final Expr expr;
    /** Descending order flag. */
    final boolean desc;
    /** Position of empty sort keys. */
    final boolean least;
    /** Input info. */
    final InputInfo input;

    /**
     * Constructor.
     * @param ii input info
     * @param k sort key expression
     * @param dsc descending order
     * @param lst empty least
     */
    public Key(final InputInfo ii, final Expr k, final boolean dsc, final boolean lst) {
      expr = k;
      desc = dsc;
      least = lst;
      input = ii;
    }
  }
}