package io.mazasoft.restful.fantasy;

/** An object that is designed for in-memory storage and lookup. */
public interface Entity {
    /** Gets the id of the entity. */
    Integer getId();

    /** Sets the id of the entity. */
    void setId(Integer id);

    /** Returns the json-serialized form of this entity. */
    String toJson();

    /** Copys state from a json blob. */
    void copyFromJson(String json);

    /** Copys state from an existing entity. */
    void copyFromEntity(Entity entity);
}
