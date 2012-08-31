
package jp.mironal.java.aws.app.glacier;

public class CheckJobResult {

    private final boolean messageFound;
    private final boolean jobSuccessful;

    public CheckJobResult(boolean messageFound, boolean jobSuccessful) {
        this.messageFound = messageFound;
        this.jobSuccessful = jobSuccessful;
    }

    /**
     * Messageが見つかったかどうか.
     * 
     * @return true:Messageが見つかった.false:見つからない
     */
    public boolean isMessageFound() {
        return messageFound;
    }

    /**
     * Jobが成功したかどうか
     * 
     * @return true:jobが成功した
     */
    public boolean isJobSuccessful() {
        return jobSuccessful;
    }

    @Override
    public String toString() {
        return "messageFound:" + messageFound + " jobSuccessful:" + jobSuccessful;
    }

}
