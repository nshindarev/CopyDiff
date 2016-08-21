package nshindarev.copydiff.appl.filter;

import nshindarev.copydiff.appl.config.Parameters;

import java.nio.ByteBuffer;

/**
 * Created by nshindarev on 21.08.16.
 *
 * Данный интерфейс предназначен для обработки буффера данных. В качестве результата возвращается объем  обработанных
 * данных. При последовательной обработке остаток данных перемещается в начало буффера, а буфер догружается. Параметр
 * filePosition указывает на смещение относительно начала файла, которое соответствует position() буфера.
 *
 * Если результат null, то считаем, что обработан весь буфер и больше данный фильтр в сравнении для текущего файла ис-
 * пользовать не надо.
 *
 */
public interface BufferFileFilter extends ContinueFilter {

    Integer bufferCheck(ByteBuffer byteBuffer, long filePosition, Parameters parameters, Checker checker);

}
