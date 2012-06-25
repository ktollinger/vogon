/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

/**
 * File import exception class
 *
 * @author Dmitry Zolotukhin
 */
public class VogonImportException extends Exception {

    /**
     * Default constructor
     */
    public VogonImportException() {
    }

    /**
     * Constructor based on a human-readable message
     *
     * @param message The message
     */
    public VogonImportException(String message) {
	super(message);
    }

    /**
     * Constructor based on a throwable object
     *
     * @param cause A throwable object
     */
    public VogonImportException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructor based on a human-readable and throwable object
     *
     * @param message The message
     * @param cause A throwable object
     */
    public VogonImportException(String message, Throwable cause) {
	super(message, cause);
    }
}
