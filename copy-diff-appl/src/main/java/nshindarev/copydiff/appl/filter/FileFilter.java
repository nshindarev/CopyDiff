package nshindarev.copydiff.appl.filter;

import nshindarev.copydiff.appl.config.Parameters;

/**
 * Created by nshindarev on 20.08.16.
 */
public interface FileFilter extends Filter {

    void check(Parameters parameters, Checker checker);

}
