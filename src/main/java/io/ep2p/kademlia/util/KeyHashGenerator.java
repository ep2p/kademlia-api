package io.ep2p.kademlia.util;

import io.ep2p.kademlia.NodeSettings;

public interface KeyHashGenerator<ID extends Number, K> {

    /**
     * @param key key of data to persist
     * @return hash of same type as node id
     */
    ID generate(K key);

    class Default<ID extends Number, K> implements KeyHashGenerator<ID, K> {
        private final BoundedHashUtil boundedHashUtil;
        private final Class<ID> nodeIdClass;

        public Default(Class<ID> nodeIdClass) {
            this(nodeIdClass, NodeSettings.Default.build());
        }

        public Default(Class<ID> nodeIdClass, NodeSettings nodeSettings) {
            this.nodeIdClass = nodeIdClass;
            this.boundedHashUtil = new BoundedHashUtil(nodeSettings.getIdentifierSize());
        }

        @Override
        public ID generate(K key) {
            return this.boundedHashUtil.hash(key.hashCode(), this.nodeIdClass);
        }
    }

}
