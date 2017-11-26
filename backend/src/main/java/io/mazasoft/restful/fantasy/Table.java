package io.mazasoft.restful.fantasy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Set of relationships between entities and their respective ids. */
public class Table<T extends Entity> {
    private final HashMap<Integer, T> data;
    private int nextId;
    private final Class<T> type;

    /** Creates a new table for the specified type of entity. */
    public Table(Class<T> type) {
        this.data = new HashMap<>();
        this.nextId = 1;
        this.type = type;
    }

    /** Clones the given entity, gives it an id, and puts it in the table. */
    public T create(T entity) {
        int id = nextId++;

        try {
            // Create a new instance of T - copy state from the provided entity.
            T clonedEntity = type.newInstance();
            clonedEntity.copyFromEntity(entity);
            clonedEntity.setId(id);

            // Map id to clonedEntity.
            data.put(id, clonedEntity);

            // Returns the newly added entity.
            return clonedEntity;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create a new clone of the entity", e);
        }
    }

    /** Gets the corresponding instance of T for the given id. */
    public T read(int id) {
        return data.get(id);
    }

    /** Gets all the instances of T (in the map). */
    public List<T> readAll() {
        return new ArrayList<T>(data.values());
    }

    /** Replaces the instance of T currently associated with the given id. */
    public T update(int id, T entity) {
        try {
            // Create a new instance of T - copy state from the provided entity.
            T clonedEntity = type.newInstance();
            clonedEntity.copyFromEntity(entity);
            clonedEntity.setId(id);

            // Map id to clonedEntity.
            data.put(id, clonedEntity);

            // Returns the newly added entity.
            return clonedEntity;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create a new clone of the entity", e);
        }
    }

    /** Removes the instance of T based on the given id. */
    public boolean delete(int id) {
        T deletedEntity = data.remove(id);
        return deletedEntity != null;
    }
}
