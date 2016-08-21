package nshindarev.copydiff.appl.filter;

import nshindarev.copydiff.appl.config.Parameters;

import java.nio.ByteBuffer;

/**
 * Created by nshindarev on 21.08.16.
 *
 * Данный интерфейс предназначен для обработки буффера данных. В качестве результата возвращается объем обработанных
 * данных. При последовательной обработке остаток данных перемещается в начало буффера, а буфер догружается.
 *
 */
public interface BufferFilter extends ContinueFilter {

    Integer bufferCheck(ByteBuffer byteBuffer, Parameters parameters, Checker checker);

}
