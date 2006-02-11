package tiled.core;

/**
 * This exception is thrown when an attempt is made to perform a modification
 * on a locked layer.
 *
 * @version $Id$
 */
public class LayerLockedException extends Throwable {
    public LayerLockedException(String s) {
        super(s);
    }
}
