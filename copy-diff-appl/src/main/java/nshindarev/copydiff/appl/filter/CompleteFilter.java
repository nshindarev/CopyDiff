package nshindarev.copydiff.appl.filter;

import nshindarev.copydiff.appl.config.Parameters;

/**
 * Created by nshindarev on 21.08.16.
 *
 * Данный интерфейс предназначен для вызова после завершения процесса обработки буферов.
 *
 */
public interface CompleteFilter extends ContinueFilter {

    void completeCheck(Parameters parameters, Checker checker);

}
