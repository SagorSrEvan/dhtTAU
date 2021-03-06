package io.taucoin.db;

import io.taucoin.core.AccountState;
import io.taucoin.types.Transaction;
import io.taucoin.util.ByteArrayWrapper;
import io.taucoin.util.ByteUtil;

import java.math.BigInteger;
import java.util.*;

// 公钥发布的mutable message也会放在stateDB
public class StateDBImpl implements StateDB {

    private KeyValueDataBase db;

    public StateDBImpl(KeyValueDataBase db) {
        this.db = db;
    }

    /**
     * open db
     * @param path database path which can be accessed
     * @throws Exception
     */
    public void open(String path) throws Exception {
        db.open(path);
    }

    /**
     * close db
     */
    public void close() {
        db.close();
    }

    /**
     * Save a snapshot and start tracking future changes
     *
     * @param chainID  chainID
     * @return the tracker repository
     */
    @Override
    public StateDB startTracking(byte[] chainID) {
        return new StateDBTrack(this, chainID);
    }

    /**
     * Store all the temporary changes made
     * to the repository in the actual database
     */
    @Override
    public void commit() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Undo all the changes made so far
     * to a snapshot of the repository
     */
    @Override
    public void rollback() {
        throw new UnsupportedOperationException();
    }

    /**
     * follow a chain
     * @param chainID
     * @throws Exception
     */
    @Override
    public void followChain(byte[] chainID) throws Exception {
        db.put(PrefixKey.chainKey(chainID), new byte[0]);
    }

    /**
     * if follow a chain
     *
     * @param chainID chain ID
     * @return true:followed, false: not followed
     * @throws Exception
     */
    @Override
    public boolean isChainFollowed(byte[] chainID) throws Exception {
        return  null != db.get(PrefixKey.chainKey(chainID));
    }

    /**
     * get all followed chains
     * @return
     * @throws Exception
     */
    @Override
    public Set<byte[]> getAllFollowedChains() throws Exception {
        Set<byte[]> ret = new HashSet<>();
        Set<byte[]> set = db.retrieveKeysWithPrefix(PrefixKey.chain);
        if (null != set) {
            for(byte[] chainID: set) {
                ret.add(Arrays.copyOfRange(chainID, PrefixKey.chain.length, chainID.length));
            }
        }
        return ret;
    }

    /**
     * unfollow a chain
     * @param chainID
     * @throws Exception
     */
    @Override
    public void unfollowChain(byte[] chainID) throws Exception {
        db.delete(PrefixKey.chainKey(chainID));
    }

    /**
     * set best block hash
     * @param chainID
     * @param hash
     * @throws Exception
     */
    @Override
    public void setBestBlockHash(byte[] chainID, byte[] hash) throws Exception {
        db.put(PrefixKey.bestBlockHashKey(chainID), hash);
    }

    /**
     * get best block hash
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getBestBlockHash(byte[] chainID) throws Exception {
        return db.get(PrefixKey.bestBlockHashKey(chainID));
    }

//    /**
//     * delete best block hash
//     * @param chainID
//     * @throws Exception
//     */
//    @Override
//    public void deleteBestBlockHash(byte[] chainID) throws Exception {
//        db.delete(PrefixKey.bestBlockHashKey(chainID));
//    }

    /**
     * set current chain synced block hash
     *
     * @param chainID
     * @param hash
     * @throws Exception
     */
    @Override
    public void setSyncBlockHash(byte[] chainID, byte[] hash) throws Exception {
        db.put(PrefixKey.syncBlockHashKey(chainID), hash);
    }

    /**
     * get current chain synced block hash
     *
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getSyncBlockHash(byte[] chainID) throws Exception {
        return db.get(PrefixKey.syncBlockHashKey(chainID));
    }

//    /**
//     * delete current chain synced block hash
//     *
//     * @param chainID
//     * @throws Exception
//     */
//    @Override
//    public void deleteSyncBlockHash(byte[] chainID) throws Exception {
//        db.delete(PrefixKey.syncBlockHashKey(chainID));
//    }

    /**
     * set mutable range
     * @param chainID
     * @param number
     * @throws Exception
     */
    @Override
    public void setMutableRange(byte[] chainID, int number) throws Exception {
        db.put(PrefixKey.mutableRangeKey(chainID), ByteUtil.intToBytes(number));
    }

    /**
     * get mutable range
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public int getMutableRange(byte[] chainID) throws Exception {
        return ByteUtil.byteArrayToInt(db.get(PrefixKey.mutableRangeKey(chainID)));
    }

    /**
     * delete mutable range
     * @param chainID
     * @throws Exception
     */
    @Override
    public void deleteMutableRange(byte[] chainID) throws Exception {
        db.delete(PrefixKey.mutableRangeKey(chainID));
    }

    /**
     * add a new peer
     * @param chainID
     * @param pubkey
     * @throws Exception
     */
    @Override
    public void addPeer(byte[] chainID, byte[] pubkey) throws Exception {
        db.put(PrefixKey.peerKey(chainID, pubkey), new byte[0]);
    }

    /**
     * get all peers of a chain
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public Set<byte[]> getPeers(byte[] chainID) throws Exception {
        return db.retrieveKeysWithPrefix(PrefixKey.peerKeyPrefix(chainID));
    }

    /**
     * delete a peer
     * @param chainID
     * @param pubkey
     * @throws Exception
     */
    @Override
    public void deletePeer(byte[] chainID, byte[] pubkey) throws Exception {
        db.delete(PrefixKey.peerKey(chainID, pubkey));
    }

    /**
     * delete all peers of a chain
     * @param chainID
     * @throws Exception
     */
    @Override
    public void deleteAllPeers(byte[] chainID) throws Exception {
        db.removeWithKeyPrefix(PrefixKey.peerKeyPrefix(chainID));
    }

    /**
     * get self transaction pool
     * @param chainID
     * @param pubKey
     * @return
     * @throws Exception
     */
    @Override
    public Set<Transaction> getSelfTxPool(byte[] chainID, byte[] pubKey) throws Exception {
        Set<byte[]> keys = db.retrieveKeysWithPrefix(PrefixKey.txPoolPrefix(chainID, pubKey));
        if (null != keys) {
            Set<Transaction> txs = new HashSet<>();
            for (byte[] key: keys) {
                byte[] rlp = db.get(key);
                txs.add(new Transaction(rlp));
            }
            return txs;
        }
        return null;
    }

    /**
     * put transaction into pool
     * @param chainID
     * @param tx
     * @throws Exception
     */
    @Override
    public void putTxIntoSelfTxPool(byte[] chainID, Transaction tx) throws Exception {
        db.put(PrefixKey.txPoolKey(chainID, tx.getSenderPubkey(), tx.getTxID()), tx.getEncoded());
    }

    /**
     * delete self transaction pool
     * @param chainID
     * @param pubKey
     * @throws Exception
     */
    @Override
    public void deleteSelfTxPool(byte[] chainID, byte[] pubKey) throws Exception {
        db.delete(PrefixKey.txPoolPrefix(chainID, pubKey));
    }

    /**
     * set immutable point block hash
     * @param chainID
     * @param hash
     * @throws Exception
     */
    @Override
    public void setImmutablePointBlockHash(byte[] chainID, byte[] hash) throws Exception {
        db.put(PrefixKey.immutablePointBlockHashKey(chainID), hash);
    }

    /**
     * get immutable point block hash
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getImmutablePointBlockHash(byte[] chainID) throws Exception {
        return db.get(PrefixKey.immutablePointBlockHashKey(chainID));
    }

    /**
     * delete immutable point block hash
     * @param chainID
     * @throws Exception
     */
    @Override
    public void deleteImmutablePointBlockHash(byte[] chainID) throws Exception {
        db.delete(PrefixKey.immutablePointBlockHashKey(chainID));
    }

    /**
     * set votes counting point block hash
     * @param chainID
     * @param hash
     * @throws Exception
     */
    @Override
    public void setVotesCountingPointBlockHash(byte[] chainID, byte[] hash) throws Exception {
        db.put(PrefixKey.votesCountingPointBlockHashKey(chainID), hash);
    }

    /**
     * get votes counting point block hash
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public byte[] getVotesCountingPointBlockHash(byte[] chainID) throws Exception {
        return db.get(PrefixKey.votesCountingPointBlockHashKey(chainID));
    }

    /**
     * delete votes counting point block hash
     * @param chainID
     * @throws Exception
     */
    @Override
    public void deleteVotesCountingPointBlockHash(byte[] chainID) throws Exception {
        db.delete(PrefixKey.votesCountingPointBlockHashKey(chainID));
    }

    /**
     * update accounts state
     * @param chainID
     * @param accountStateMap
     * @throws Exception
     */
    @Override
    public void updateAccounts(byte[] chainID, Map<ByteArrayWrapper, AccountState> accountStateMap) throws Exception {
        if (null != accountStateMap) {
            Map<byte[], byte[]> map = new HashMap<>(accountStateMap.size());
            for (Map.Entry<ByteArrayWrapper, AccountState> entry: accountStateMap.entrySet()) {
                map.put(PrefixKey.accountKey(chainID, entry.getKey().getData()), entry.getValue().getEncoded());
            }
            db.updateBatch(map);
        }
    }

    /**
     * update account state
     *
     * @param chainID
     * @param pubKey
     * @param account
     * @throws Exception
     */
    @Override
    public void updateAccount(byte[] chainID, byte[] pubKey, AccountState account) throws Exception {
        if (null != account) {
            db.put(PrefixKey.accountKey(chainID, pubKey), account.getEncoded());
        }
    }

    /**
     * get a account state
     * @param chainID
     * @param pubKey
     * @return
     * @throws Exception
     */
    @Override
    public AccountState getAccount(byte[] chainID, byte[] pubKey) throws Exception {
        byte[] rlp = db.get(PrefixKey.accountKey(chainID, pubKey));
        if (null != rlp) {
            return new AccountState(rlp);
        }
        return null;
    }

    /**
     * get nonce by pubKey
     *
     * @param chainID
     * @param pubKey
     * @return
     * @throws Exception
     */
    @Override
    public BigInteger getNonce(byte[] chainID, byte[] pubKey) throws Exception {
        byte[] rlp = db.get(PrefixKey.accountKey(chainID, pubKey));
        if (null != rlp) {
            AccountState accountState = new AccountState(rlp);
            return accountState.getNonce();
        }
        return BigInteger.ZERO;
    }

//    /**
//     * delete a account
//     * @param chainID
//     * @param pubKey
//     * @throws Exception
//     */
//    @Override
//    public void deleteAccount(byte[] chainID, byte[] pubKey) throws Exception {
//        db.delete(PrefixKey.accountKey(chainID, pubKey));
//    }


    /**
     * Write batch into the database.
     *
     * @param rows key-value batch
     * @throws Exception
     */
    @Override
    public void updateBatch(Map<byte[], byte[]> rows) throws Exception {
        if (null != rows) {
            this.db.updateBatch(rows);
        }
    }

    /**
     * clear all state data
     *
     * @param chainID
     * @throws Exception
     */
    @Override
    public void clearAllState(byte[] chainID) throws Exception {
        this.db.removeWithKeyPrefix(PrefixKey.accountPrefix(chainID));
        this.db.delete(PrefixKey.bestBlockHashKey(chainID));
        this.db.delete(PrefixKey.syncBlockHashKey(chainID));
    }
}

