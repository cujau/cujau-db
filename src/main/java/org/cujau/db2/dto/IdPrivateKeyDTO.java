package org.cujau.db2.dto;

/**
 * A data transfer object (DTO) whose private key is an identity (id) value.
 *
 * Typically the id value is auto-incremented by the database, using a sequence per-table.
 */
public interface IdPrivateKeyDTO {

    /**
     * Get the database identity (id) value. If this object is not yet connected to a database row, this
     * method will return -1.
     *
     * @return The value of the database identity column for this record.
     */
    long getId();

    /**
     * Set the database identity (id) value of the row represented by this object.
     *
     * @param id
     *         The database identity value.
     */
    void setId(long id);

}
