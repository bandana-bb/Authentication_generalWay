package dev.bandana.user_auth_service.models;


import jakarta.persistence.Entity;

@Entity(name="roles")
public class Role extends BaseModel {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
