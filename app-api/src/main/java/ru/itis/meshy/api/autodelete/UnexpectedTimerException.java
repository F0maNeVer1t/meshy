package ru.itis.meshy.api.autodelete;

import ru.itis.messaging_engine.api.db.DbException;

/**
 * Thrown when a database operation is attempted as part of message storing
 * and the operation is expecting a different timer state. This
 * exception may occur due to concurrent updates and does not indicate a
 * database error.
 */
public class UnexpectedTimerException extends DbException {
}
