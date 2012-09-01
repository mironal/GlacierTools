
package jp.mironal.java.aws.app.glacier.cmd;

class InvalidRegionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2856420193487083382L;

    public InvalidRegionException(String msg) {
        super(msg);
    }
}
