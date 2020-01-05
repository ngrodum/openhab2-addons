package org.openhab.binding.adaxheater.cloudapi;

public class UserLoginData {
    private Long id;
    private String name;
    private String privateKey;

    public UserLoginData(Long id, String name, String privateKey) {
        this.id = id;
        this.name = name;
        this.privateKey = privateKey;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }
}
