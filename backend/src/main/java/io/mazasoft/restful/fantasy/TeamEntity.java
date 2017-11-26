package io.mazasoft.restful.fantasy;

import com.google.gson.Gson;

/** Entity of type "Team". */
public class TeamEntity implements Entity {
    private static Gson gson = new Gson();

    private String name;
    private Integer id;
    private String city;

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
        TeamEntity jsonInstance = gson.fromJson(json, TeamEntity.class);
        copyFromEntity(jsonInstance);
    }

    @Override
    public void copyFromEntity(Entity entity) {
        if (!(entity instanceof TeamEntity)) {
            throw new IllegalArgumentException("Entity must be of type TeamEntity");
        }

        TeamEntity otherTeam = (TeamEntity) entity;
        this.name = otherTeam.name;
        this.city = otherTeam.city;
    }
}
