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
package de.greenrobot.dao.identityscope;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.dao.internal.LongHashMap;

/**
 * The context for entity identities. Provides the scope in which entities will be tracked and managed.
 * 
 * @author Markus
 * @param <T>
 *            Entity
 */
public class IdentityScopeLong<T> implements IdentityScope<Long, T> {
    private final LongHashMap<ValuedWeakReference<T>> map;
    private final ReentrantLock lock;
    private final ReferenceQueue<T> referenceQueue;

    public IdentityScopeLong() {
        map = new LongHashMap<ValuedWeakReference<T>>();
        lock = new ReentrantLock();
        referenceQueue = new ReferenceQueue<T>();
    }

    @Override
    public T get(Long key) {
        return get2(key);
    }

    @Override
    public T getNoLock(Long key) {
        return get2NoLock(key);
    }

    public T get2(long key) {
        lock.lock();
        Reference<T> ref;
        try {
            ref = map.get(key);
        } finally {
            lock.unlock();
        }
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    public T get2NoLock(long key) {
        Reference<T> ref = map.get(key);
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    @Override
    public void put(Long key, T entity) {
        put2(key, entity);
    }

    @Override
    public void putNoLock(Long key, T entity) {
        put2NoLock(key, entity);
    }

    public void put2(long key, T entity) {
        lock.lock();
        try {
            checkReferenceQueue();
            map.put(key, new ValuedWeakReference<T>(key, entity, referenceQueue));
        } finally {
            lock.unlock();
        }
    }

    public void put2NoLock(long key, T entity) {
        checkReferenceQueue();
        map.put(key, new ValuedWeakReference<T>(key, entity, referenceQueue));
    }

    private void checkReferenceQueue() {
        Reference<? extends T> next;
        while((next = referenceQueue.poll()) != null) {
            long key = ((ValuedWeakReference<?>)next).key;
            map.remove(key);
        }
        map.checkForCompact();
    }

    @Override
    public boolean detach(Long key, T entity) {
        lock.lock();
        try {
            long k = key;
            ValuedWeakReference<T> reference = map.remove(k);
            T object = reference.get();
            if (object == null) {
                return false;
            }
            if (object == entity) {
                return true;
            }
            map.put(k, reference);
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(Long key) {
        lock.lock();
        try {
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(Iterable<Long> keys) {
        lock.lock();
        try {
            for (Long key : keys) {
                map.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            map.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public void reserveRoom(int count) {
        map.reserveRoom(count);
    }

    private static class ValuedWeakReference<T> extends WeakReference<T> {

        private final long key;

        public ValuedWeakReference(long key, T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
            this.key = key;
        }
    }

}
