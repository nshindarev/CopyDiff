package nshindarev.copydiff.search;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by nshindarev on 14.08.16.
 */
public class TurboBoyerMooreTest {

    private static final String text = "Арозаупаланалапуазора";
    private static final byte[] buff = text.getBytes(Charset.forName("CP1251"));

    @Test
    public void findOneTest() throws Exception {
        TurboBoyerMoore pattern =  TurboBoyerMoore.compile("лапу".getBytes("CP1251"));
        Pair<Integer,List<Integer>> result = pattern.findOne(ByteBuffer.wrap(buff));
        Assert.assertEquals("Проверка вхождения в строку", 12, result.getValue().get(0).intValue());
    }

    @Test
    public void findAllTest() throws Exception {
        TurboBoyerMoore pattern =  TurboBoyerMoore.compile("ла".getBytes("CP1251"));
        Pair<Integer,List<Integer>> result = pattern.findAll(ByteBuffer.wrap(buff));
        Assert.assertEquals("Проверка первого вхождения в строку", 8, result.getValue().get(0).intValue());
        Assert.assertEquals("Проверка первого вхождения в строку", 12, result.getValue().get(1).intValue());
    }

    @Test
    public void compileTest() throws Exception {
        TurboBoyerMoore pattern =  TurboBoyerMoore.compile("ла".getBytes("CP1251"));
        Pair<Integer,List<Integer>> result = pattern.findOne(ByteBuffer.wrap(buff));
        Assert.assertEquals("Проверка вхождения в строку", 8, result.getValue().get(0).intValue());
    }

    @Test
    public void concatTest() throws Exception {
        TurboBoyerMoore pattern =  TurboBoyerMoore.compile("ораАроза".getBytes("CP1251"));
        Pair<Integer,List<Integer>> result = pattern.findOne(ByteBuffer.wrap(buff));
        Assert.assertTrue("Должен быть возвращён результат без значений", result.getValue().isEmpty());
        Assert.assertTrue("Количество обработанных элементов должно быть меньше limit() буфера", result.getKey().intValue() < buff.length);
        // Записываем в буфер оставшуюся необработанную часть буффера
        int j = result.getKey();
        int n = buff.length;
        ByteBuffer bbr = ByteBuffer.wrap(buff, j, n-j);
        ByteBuffer bb = ByteBuffer.allocate(n*2 - j);
        // Записываем в новый буффер оставшуюся часть и дописываем тем же значением.
        // Теперь искомый pattern должен появиться в буфере в результате склейки
        bb.put(bbr).put(buff);
        byte[] buff = bb.array();
        result = pattern.findOne(ByteBuffer.wrap(buff));
        Assert.assertTrue("Подстрока должна быть обнаружена в позиции 3", result.getValue().size() > 0 && result.getValue().get(0).equals(3));
    }

}