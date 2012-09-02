
package jp.mironal.java.aws.app.glacier;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

/**
 * InitiateしたJobを後で参照するために必要な情報.
 */
public class JobRestoreParam {

    public static class Builder {
        // 明示的にnullで初期化しておく
        /** The name of the Vault */
        private String vaultName = null;

        /** The ID of the job */
        private String jobId = null;

        private Region region = null;

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
            if (this.region == null) {
                throw new NullPointerException("region is null.");
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
            if (vaultname == null) {
                throw new NullPointerException("vaultname is null.");
            }

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
            if (jobId == null) {
                throw new NullPointerException("jobId is null.");
            }

            this.jobId = jobId;
            return this;
        }

        public Builder setRegion(Region region) {
            if (region == null) {
                throw new NullPointerException("region is null.");
            }

            this.region = region;
            return this;
        }
    }

    /** The name of the Vault */
    private final String vaultName;

    /** The ID of the job */
    private final String jobId;

    /* region */
    private final Region region;

    /**
     * Builder以外からは作れないようにprivateにした.<br>
     * 
     * @param builder Builderのインスタンス
     */
    private JobRestoreParam(Builder builder) {
        this.vaultName = builder.vaultName;
        this.jobId = builder.jobId;
        this.region = builder.region;
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
     * Get the region.
     * 
     * @return region.
     */
    public Region getRegion() {
        return region;
    }
}
