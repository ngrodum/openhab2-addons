package org.openhab.binding.adaxheater.cloudapi;

public class ClientErrorHolder {
    private long errorTypeId;

    public ClientErrorHolder(long errorTypeId) {
        this.errorTypeId = errorTypeId;
    }

    public ClientErrorHolder(ClientErrorType errorTypeId) {
        this.errorTypeId = errorTypeId.getId();
    }

    public ClientErrorHolder() {
        this(0);
    }

    public void set(ClientErrorHolder other) {
        setErrorTypeId(other != null ? other.getErrorTypeId() : 0);
    }

    public void setErrorTypeId(long errorTypeId) {
        this.errorTypeId = errorTypeId;
    }

    public long getErrorTypeId() {
        return this.errorTypeId;
    }
}
