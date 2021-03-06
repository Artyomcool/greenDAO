/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.dao.query;

import java.util.List;

import android.database.Cursor;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;

/**
 * A repeatable query returning entities.
 * 
 * @author Markus
 * 
 * @param <T>
 *            The enitity class the query will return results for.
 */
// TODO support long, double and other types, not just Strings, for parameters
// TODO Make parameters setable by Property (if unique in paramaters)
// TODO Query for PKs/ROW IDs
public class Query<T> extends AbstractQuery<T, Query<T>> {
    private final static class ThreadLocalQuery<T2> extends ThreadLocal<Query<T2>> {
        private final String sql;
        private final AbstractDao<T2, ?> dao;
        private final String[] initialValues;
        private final int limitPosition;
        private final int offsetPosition;
        private final boolean deep;

        private ThreadLocalQuery(AbstractDao<T2, ?> dao, String sql, String[] initialValues, int limitPosition,
                                 int offsetPosition, boolean deep) {
            this.dao = dao;
            this.sql = sql;
            this.initialValues = initialValues;
            this.limitPosition = limitPosition;
            this.offsetPosition = offsetPosition;
            this.deep = deep;
        }

        @Override
        protected Query<T2> initialValue() {
            return new Query<T2>(this, dao, sql, initialValues.clone(), limitPosition, offsetPosition, deep);
        }
    }

    /** For internal use by greenDAO only. */
    public static <T2> Query<T2> internalCreate(AbstractDao<T2, ?> dao, String sql, Object[] initialValues) {
        return create(dao, sql, initialValues, -1, -1);
    }

    static <T2> Query<T2> create(AbstractDao<T2, ?> dao, String sql, Object[] initialValues, int limitPosition,
            int offsetPosition) {
        ThreadLocalQuery<T2> threadLocal = new ThreadLocalQuery<T2>(dao, sql, toStringArray(initialValues),
                limitPosition, offsetPosition, false);
        return threadLocal.get();
    }

    static <T> Query<T> createDeep(AbstractDao<T, ?> dao, String sql, Object[] initialValues, int limitPosition,
            int offsetPosition) {
        ThreadLocalQuery<T> threadLocal = new ThreadLocalQuery<T>(dao, sql, toStringArray(initialValues),
                limitPosition, offsetPosition, true);
        return threadLocal.get();
    }

    private final boolean deep;
    private final int limitPosition;
    private final int offsetPosition;
    private final ThreadLocalQuery<T> threadLocalQuery;

    private Query(ThreadLocalQuery<T> threadLocalQuery, AbstractDao<T, ?> dao, String sql, String[] initialValues,
            int limitPosition, int offsetPosition, boolean deep) {
        super(dao, sql, initialValues);
        this.threadLocalQuery = threadLocalQuery;
        this.limitPosition = limitPosition;
        this.offsetPosition = offsetPosition;
        this.deep = deep;
    }

    // public void compile() {
    // // TODO implement compile
    // }

    @Override
    public Query<T> forCurrentThread() {
        Query<T> query = threadLocalQuery.get();
        String[] initialValues = threadLocalQuery.initialValues;
        System.arraycopy(initialValues, 0, query.parameters, 0, initialValues.length);
        return query;
    }

    /**
     * Sets the parameter (0 based) using the position in which it was added during building the query.
     */
    public void setParameter(int index, Object parameter) {
        if (index >= 0 && (index == limitPosition || index == offsetPosition)) {
            throw new IllegalArgumentException("Illegal parameter index: " + index);
        }
        super.setParameter(index, parameter);
    }

    /**
     * Sets the limit of the maximum number of results returned by this Query. {@link QueryBuilder#limit(int)} must have
     * been called on the QueryBuilder that created this Query object.
     */
    public void setLimit(int limit) {
        checkThread();
        if (limitPosition == -1) {
            throw new IllegalStateException("Limit must be set with QueryBuilder before it can be used here");
        }
        parameters[limitPosition] = Integer.toString(limit);
    }

    /**
     * Sets the offset for results returned by this Query. {@link QueryBuilder#offset(int)} must have been called on the
     * QueryBuilder that created this Query object.
     */
    public void setOffset(int offset) {
        checkThread();
        if (offsetPosition == -1) {
            throw new IllegalStateException("Offset must be set with QueryBuilder before it can be used here");
        }
        parameters[offsetPosition] = Integer.toString(offset);
    }

    /** Executes the query and returns the result as a list containing all entities loaded into memory. */
    public List<T> list() {
        checkThread();
        Cursor cursor = dao.getDatabase().rawQuery(sql, parameters);
        if (deep) {
            try {
                return dao.loadAllDeepFromCursor(cursor);
            } finally {
                cursor.close();
            }
        }
        return daoAccess.loadAllAndCloseCursor(cursor);
    }

    /**
     * Executes the query and returns the result as a list that lazy loads the entities on first access. Entities are
     * cached, so accessing the same entity more than once will not result in loading an entity from the underlying
     * cursor again.Make sure to close it to close the underlying cursor.
     */
    public LazyList<T> listLazy() {
        checkThread();
        Cursor cursor = dao.getDatabase().rawQuery(sql, parameters);
        return new LazyList<T>(daoAccess, cursor, true, deep);
    }

    /**
     * Executes the query and returns the result as a list that lazy loads the entities on every access (uncached). Make
     * sure to close the list to close the underlying cursor.
     */
    public LazyList<T> listLazyUncached() {
        checkThread();
        Cursor cursor = dao.getDatabase().rawQuery(sql, parameters);
        return new LazyList<T>(daoAccess, cursor, false, deep);
    }

    /**
     * Executes the query and returns the result as a list iterator; make sure to close it to close the underlying
     * cursor. The cursor is closed once the iterator is fully iterated through.
     */
    public CloseableListIterator<T> listIterator() {
        return listLazyUncached().listIteratorAutoClose();
    }

    /**
     * Executes the query and returns the unique result or null.
     * 
     * @throws DaoException
     *             if the result is not unique
     * @return Entity or null if no matching entity was found
     */
    public T unique() {
        checkThread();
        Cursor cursor = dao.getDatabase().rawQuery(sql, parameters);
        return deep ? daoAccess.loadUniqueDeepAndCloseCursor(cursor)
                    : daoAccess.loadUniqueAndCloseCursor(cursor);
    }

    /**
     * Executes the query and returns the unique result (never null).
     * 
     * @throws DaoException
     *             if the result is not unique or no entity was found
     * @return Entity
     */
    public T uniqueOrThrow() {
        T entity = unique();
        if (entity == null) {
            throw new DaoException("No entity found for query");
        }
        return entity;
    }

}
