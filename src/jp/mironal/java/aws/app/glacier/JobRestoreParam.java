
package jp.mironal.java.aws.app.glacier;

/**
 * InitiateしたJobを後で参照するために必要な情報.
 */
class JobRestoreParam {

    public static class Builder {
        // 明示的にnullで初期化しておく
        /** The name of the Vault */
        private String vaultName = null;

        /** The ID of the job */
        private String jobId = null;

        private String snsSubscriptionArn = null;
        private String snsTopicArn = null;
        private String sqsQueueUrl = null;

        /**
         * JobRestoreParamインスタンスを生成する.<br>
         * パラメータが足りていないときは例外を投げる.
         * 
         * @return Instance of JobRestoreParam
         */
        public JobRestoreParam build() {
            if (this.vaultName == null) {
                throw new NullPointerException("vaultName is null.");
            }
            if (this.jobId == null) {
                throw new NullPointerException("jobId is null.");
            }
            if (this.snsSubscriptionArn == null) {
                throw new NullPointerException("snsSubscriptionArn is null.");
            }
            if (this.snsTopicArn == null) {
                throw new NullPointerException("snsTopicArn is null.");
            }
            if (this.sqsQueueUrl == null) {
                throw new NullPointerException("sqsQueueUrl is null.");
            }

            return new JobRestoreParam(this);
        }

        /**
         * Set the name of the Vault.
         * 
         * @param vaultname The name of the Vault.
         * @return　Themselves
         */
        public Builder setVaultName(String vaultname) {
            this.vaultName = vaultname;
            return this;
        }

        /**
         * Set the ID of the job.
         * 
         * @param jobId The ID of the job.
         * @return Themselves
         */
        public Builder setJobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        /**
         * Set the String snsSubscriptionArn.
         * 
         * @param snsSubscriptionArn
         * @return Themselves
         */
        public Builder setSnsSubscriptionArn(String snsSubscriptionArn) {
            this.snsSubscriptionArn = snsSubscriptionArn;
            return this;
        }

        /**
         * Set the String setSnsTopicArn.
         * 
         * @param snsTopicArn
         * @return Themselves
         */
        public Builder setSnsTopicArn(String snsTopicArn) {
            this.snsTopicArn = snsTopicArn;
            return this;
        }

        /**
         * Set the String sqsQueueUrl
         * 
         * @param sqsQueueUrl
         * @return Themselves
         */
        public Builder setSqsQueueUrl(String sqsQueueUrl) {
            this.sqsQueueUrl = sqsQueueUrl;
            return this;
        }

    }

    /** The name of the Vault */
    private final String vaultName;

    /** The ID of the job */
    private final String jobId;
    private final String snsSubscriptionArn;
    private final String snsTopicArn;
    private final String sqsQueueUrl;

    /**
     * Builder以外からは作れないようにprivateにした.<br>
     * 
     * @param builder Builderのインスタンス
     */
    private JobRestoreParam(Builder builder) {
        this.vaultName = builder.vaultName;
        this.jobId = builder.jobId;
        this.snsSubscriptionArn = builder.snsSubscriptionArn;
        this.snsTopicArn = builder.snsTopicArn;
        this.sqsQueueUrl = builder.sqsQueueUrl;
    }

    /**
     * Get the name of the Vault.
     * 
     * @return Vault name.
     */
    public String getVaultName() {
        return vaultName;
    }

    /**
     * Get the ID of the Job.
     * 
     * @return ID of job.
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Get the snsSubscriptionArn.
     * 
     * @return snsSubscriptionArn
     */
    public String getSnsSubscriptionArn() {
        return snsSubscriptionArn;
    }

    /**
     * Get the snsTopicArn.
     * 
     * @return snsTopicArn
     */
    public String getSnsTopicArn() {
        return snsTopicArn;
    }

    /**
     * Get the sqsQueueUrl.
     * 
     * @return sqsQueueUrl
     */
    public String getSqsQueueUrl() {
        return sqsQueueUrl;
    }

}
