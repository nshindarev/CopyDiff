package nshindarev.copydiff.search;

import javafx.util.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// Turbo Boyer-Moore
// алгоритм взят отсюда: http://www-igm.univ-mlv.fr/~lecroq/string/node15.html
// доработан под нужды задачи
public class TurboBoyerMoore {

    private int m;
    private int[]  bmGs, bmBc;
    private byte[] x;

    public static TurboBoyerMoore compile(byte[] pattern) {
        int m = pattern.length;

        int[] bmGs = new int[m];
        int[] bmBc = new int[65536];

        preBmGs(pattern, bmGs);
        preBmBc(pattern, bmBc);

        return new TurboBoyerMoore(m, bmGs, bmBc, pattern);
    }

    public Pair<Integer,List<Integer>> findOne(ByteBuffer source, AtomicBoolean stopFlag) {
        // Поскольку в алгоритме всегда в список заносится значение и оно не null, то на NullPointerException не рповеряем
        return copy().find(source, stopFlag, false);
    }

    public Pair<Integer,List<Integer>> findOne(ByteBuffer source) {
        return findOne(source, new AtomicBoolean(Boolean.FALSE));
    }

    public Pair<Integer,List<Integer>> findAll(ByteBuffer source, AtomicBoolean stopFlag) {
        return copy().find(source, stopFlag, true);
    }

    public Pair<Integer,List<Integer>> findAll(ByteBuffer source) {
        return findAll(source, new AtomicBoolean(Boolean.FALSE));
    }


    private TurboBoyerMoore(int m, int[] bmGs, int[] bmBc, byte[] x) {
        this.m = m;
        this.bmGs = bmGs;
        this.bmBc = bmBc;
        this.x = x;
    }

    private static void preBmBc(byte[] x, int bmBc[]) {
        int i, m = x.length;

        for (i = 0; i < bmBc.length; ++i)
            bmBc[i] = m;
        for (i = 0; i < m - 1; ++i)
            bmBc[((int)x[i]) & 0xFF] = m - i - 1;
    }

    private static void suffixes(byte[] x, int[] suff) {
        int f = 0, g, i, m = x.length;

        suff[m - 1] = m;
        g = m - 1;
        for (i = m - 2; i >= 0; --i) {
            if (i > g && suff[i + m - 1 - f] < i - g)
                suff[i] = suff[i + m - 1 - f];
            else {
                if (i < g)
                    g = i;
                f = i;
                while (g >= 0 && x[g] == x[g + m - 1 - f])
                    --g;
                suff[i] = f - g;
            }
        }
    }

    private static void preBmGs(byte[] x, int bmGs[]) {
        int i, j, m = x.length;
        int[] suff = new int[m];

        suffixes(x, suff);

        for (i = 0; i < m; ++i)
            bmGs[i] = m;
        j = 0;
        for (i = m - 1; i >= 0; --i)
            if (suff[i] == i + 1)
                for (; j < m - 1 - i; ++j)
                    if (bmGs[j] == m)
                        bmGs[j] = m - 1 - i;
        for (i = 0; i <= m - 2; ++i)
            bmGs[m - 1 - suff[i]] = m - 1 - i;
    }

    private TurboBoyerMoore copy() {
        return new TurboBoyerMoore(m, Arrays.copyOf(bmGs, bmGs.length), Arrays.copyOf(bmBc, bmBc.length), x);
    }

    /**
     *
     * Данный метод ищет вхождения заданного при компиляции паттерна в буффере
     *
     * @param source    Искомый массив символов
     * @param stopFlag  Внешний сигнал о прекращении поиска (вернётся то, что успели найти)
     * @param all       Признак того, что ищем все вхождения паттерна
     * @return          Список положительных смещений вхождения паттерна в буффер
     *                  P.S.> Если ищем все вхождения, то в результат вставляется завершающее смещение
     *                  по которому можно взять суффикс буффера и скрепить его с префиксом следующего буфера
     *                  для поиска в несколько итераций с учётом разрыва искомого паттерна границей буфферов.
     *                  Если ищем первое вхождение, то надо учитывать, что смещение будет  на  уровне  после
     *                  первого появления паттерна  и  не  реркомендуется использовать для последовательного
     *                  поиска без прерывания
     */
    private Pair<Integer,List<Integer>> find(ByteBuffer source, AtomicBoolean stopFlag, boolean all) {
        int bcShift, i, j, shift, u, v, turboShift, n = source.limit();
        List<Integer> result = new ArrayList<Integer>();
        j = u = 0;
        shift = m;
        while (j <= n - m) {
            i = m - 1;
            while (i >= 0 && x[i] == source.get(i + j)) {
                --i;
                if (u != 0 && i == m - 1 - shift)
                    i -= u;
            }
            if (i < 0) {
                result.add(j);
                if (!all) {
                    break;
                }
                shift = bmGs[0];
                u = m - shift;
            } else {
                v = m - 1 - i;
                turboShift = u - v;
                bcShift = bmBc[((int)source.get(i + j)) & 0xFF] - m + 1 + i;
                shift = Math.max(turboShift, bcShift);
                shift = Math.max(shift, bmGs[i]);
                if (shift == bmGs[i])
                    u = Math.min(m - shift, v);
                else {
                    if (turboShift < bcShift)
                        shift = Math.max(shift, u + 1);
                    u = 0;
                }
            }
            j += shift;
            if (stopFlag.get()) {
                break;
            }
        }
        return new Pair<>(j, result);
    }

}