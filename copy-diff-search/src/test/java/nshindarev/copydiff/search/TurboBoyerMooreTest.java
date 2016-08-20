package nshindarev.copydiff.search;

import javafx.util.Pair;
import nshindarev.copydiff.test.Shakespeare;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nshindarev on 14.08.16.
 */
public class TurboBoyerMooreTest {

    private static final Logger logger = LoggerFactory.getLogger(TurboBoyerMooreTest.class);

    private static final String text               = "Арозаупаланалапуазора";
    private static final byte[] buff               = text.getBytes(Charset.forName("CP1251"));
    private static final String shakespearePattern = "PETRUCHIO";

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
        // Записываем в буфер оставшуюся необработанную часть буфера
        int j = result.getKey();
        int n = buff.length;
        ByteBuffer bbr = ByteBuffer.wrap(buff, j, n-j);
        ByteBuffer bb = ByteBuffer.allocate(n*2 - j);
        // Записываем в новый буфер оставшуюся часть и дописываем тем же значением.
        // Теперь искомый pattern должен появиться в буфере в результате склейки
        bb.put(bbr).put(buff);
        byte[] buff = bb.array();
        result = pattern.findOne(ByteBuffer.wrap(buff));
        Assert.assertTrue("Подстрока должна быть обнаружена в позиции 3", result.getValue().size() > 0 && result.getValue().get(0).equals(3));
    }

    @Test
    public void shakespearTest() throws IOException {
        Path shakespeare = Shakespeare.shakespeare();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16*1024);
        FileChannel fileChannel = FileChannel.open(shakespeare, StandardOpenOption.READ);
        TurboBoyerMoore tbm = TurboBoyerMoore.compile(shakespearePattern.getBytes("US-ASCII"));
        Charset.availableCharsets();
        long fileOffset = 0L;
        int  buffOffset = 0;
        List<Integer> results = new ArrayList<>();
        // Берём текущее время в переменную tick
        long tick = System.currentTimeMillis();
        for (int readed=fileChannel.read(byteBuffer, fileOffset); readed > 0;readed=fileChannel.read(byteBuffer, fileOffset)) {
            byteBuffer.limit(buffOffset + readed);
            byteBuffer.flip();
            Pair<Integer, List<Integer>> found = tbm.findAll(byteBuffer);
            for (Integer pos:found.getValue()) {
                results.add((int)(fileOffset - buffOffset + pos));
            }
            fileOffset += found.getKey() - buffOffset;
            if (found.getKey().intValue() < byteBuffer.limit()) {
                byteBuffer.position(found.getKey());
                ByteBuffer slice = byteBuffer.slice();
                byteBuffer.clear();
                byteBuffer.put(slice);
                buffOffset = slice.capacity();
            } else {
                buffOffset = 0;
            }
            fileOffset += buffOffset;
        }
        // Сохраняем длительность операции в файле tick
        tick = System.currentTimeMillis() - tick;
        final long fileSize = shakespeare.toFile().length();
        logger.debug("Размер файла: {}, размер буфера: {}, найдено вхождений: {}, за {} мсек., cкорость поиска: {} мб/сек.", fileSize, byteBuffer.capacity(), results.size(), tick, Math.round(100000.0D*fileSize/tick/1024/1024)/100.0D);
        byteBuffer = ByteBuffer.allocateDirect((int)shakespeare.toFile().length());
        tick = System.currentTimeMillis();
        fileChannel.read(byteBuffer, 0);
        Pair<Integer, List<Integer>> found = tbm.findAll(byteBuffer);
        tick = System.currentTimeMillis() - tick;
        logger.debug("Размер файла: {}, размер буфера: {}, найдено вхождений: {}, за {} мсек., cкорость поиска: {} мб/сек.", fileSize, byteBuffer.capacity(), results.size(), tick, Math.round(100000.0D*fileSize/tick/1024/1024)/100.0D);
        Assert.assertEquals("Результаты поиска по файлу shakespeare.txt кучей маленьких буферов и одним большим буфером должны совпадать.", results, found.getValue());

    }
}