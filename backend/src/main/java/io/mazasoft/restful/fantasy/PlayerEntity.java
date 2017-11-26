package io.mazasoft.restful.fantasy;

import com.google.gson.Gson;

/** Entity of type "Player". */
public class PlayerEntity implements Entity {
    private static Gson gson = new Gson();

    private String name;
    private Integer id;
    private Integer teamId;
    private double ppg;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
    }

    @Override
    public void copyFromJson(String json) {
        PlayerEntity jsonInstance = gson.fromJson(json, PlayerEntity.class);
        copyFromEntity(jsonInstance);
    }

    @Override
    public void copyFromEntity(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            throw new IllegalArgumentException("Entity must be of type PlayerEntity");
        }

        PlayerEntity otherPlayer = (PlayerEntity) entity;
        this.name = otherPlayer.name;
        this.teamId = otherPlayer.teamId;
        this.ppg = otherPlayer.ppg;
    }
}
