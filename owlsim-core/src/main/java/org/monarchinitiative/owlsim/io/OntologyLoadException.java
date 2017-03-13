package org.monarchinitiative.owlsim.io;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OntologyLoadException extends RuntimeException {

    OntologyLoadException(String message) {
        super(message);
    }

    OntologyLoadException(Exception e) {
    }
}
