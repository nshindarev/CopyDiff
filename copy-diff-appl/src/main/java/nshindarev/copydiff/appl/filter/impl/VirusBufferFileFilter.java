package nshindarev.copydiff.appl.filter.impl;

import javafx.util.Pair;
import nshindarev.copydiff.appl.config.Parameters;
import nshindarev.copydiff.appl.filter.BufferFileFilter;
import nshindarev.copydiff.appl.filter.Checker;
import nshindarev.copydiff.search.TurboBoyerMoore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by nshindarev on 21.08.16.
 */
public class VirusBufferFileFilter implements BufferFileFilter {

    private static final Logger logger = LoggerFactory.getLogger(VirusBufferFileFilter.class);

    TurboBoyerMoore tbm;

    public VirusBufferFileFilter(byte[] virus) {
        this.tbm = TurboBoyerMoore.compile(virus);
    }

    @Override
    public Integer bufferCheck(ByteBuffer byteBuffer, long filePosition, Parameters parameters, Checker checker) {
        assert byteBuffer != null;
        Pair<Integer, List<Integer>> result = tbm.findOne(byteBuffer);
        if (result.getValue().size() > 0) {
            logger.info("Обнаружен вирус в файле '{}' [#{}]", parameters.getSourcePath().toFile().getAbsolutePath(), filePosition + result.getValue().get(0));
            checker.reject(this);
            return null;
        }
        return result.getKey();
    }

}
