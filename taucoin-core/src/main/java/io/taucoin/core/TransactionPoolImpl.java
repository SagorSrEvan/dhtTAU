package io.taucoin.core;

import io.taucoin.account.AccountManager;
import io.taucoin.db.StateDB;
import io.taucoin.param.ChainParam;
import io.taucoin.types.Transaction;
import io.taucoin.util.ByteArrayWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

public class TransactionPoolImpl implements TransactionPool {
    private static final Logger logger = LoggerFactory.getLogger("TxPool");

    private byte[] chainID;
    private byte[] userPubKey;
    private StateDB stateDB;

//    private TxNoncer pendingNonce;
    // all transaction: txid <-> transaction
    private Map<ByteArrayWrapper, Transaction> all = new HashMap<>();
    // local transaction queue
    public PriorityQueue<LocalTxEntry> locals = new PriorityQueue<>(1, new LocalTxPolicy());
    // remote transaction queue
    public PriorityQueue<MemoryPoolEntry> remotes = new PriorityQueue<>(1, new MemoryPoolPolicy());
    // remote account transaction: pubKey <-> txid
    private Map<ByteArrayWrapper, byte[]> accountTx = new HashMap<>();


    private TransactionPoolImpl() {
    }

    public TransactionPoolImpl(byte[] chainID, byte[] pubKey, StateDB stateDB) {
        this.chainID = chainID;
        this.userPubKey = pubKey;
        this.stateDB = stateDB;
//        this.pendingNonce = new TxNoncer(chainID, repository);
    }

    /**
     * init transaction pool
     */
    @Override
    public void init() {
        getSelfTxsFromDB();
    }

    /**
     * get nonce by pubKey
     * @param pubKey
     * @return current nonce or 0 when null or exception
     */
    private long getNonce(byte[] pubKey) {
        try {
            BigInteger nonce = this.stateDB.getNonce(this.chainID, pubKey);
            if (null != nonce) {
                return nonce.longValue();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * get self transaction from db
     */
    private void getSelfTxsFromDB() {
        try {
            Set<Transaction> transactionSet = this.stateDB.getSelfTxPool(chainID, userPubKey);
            if (null != transactionSet) {
                long currentNonce = getNonce(userPubKey);
                for (Transaction transaction: transactionSet) {
                    // put transactions that are not on chain into pool
                    if (transaction.getNonce() > currentNonce) {
                        locals.offer(LocalTxEntry.with(transaction));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * re-init tx pool
     */
    @Override
    public void reinit() {
        clearPool();
        getSelfTxsFromDB();
    }

    /**
     * clear the pool
     */
    public void clearPool() {
        remotes.clear();
        accountTx.clear();
        locals.clear();
        all.clear();

    }

    /**
     * add local transaction into pool
     *
     * @param tx
     */
    @Override
    public void addLocal(Transaction tx) {
        if (null == tx) {
            logger.error("Tx is null.");
            return;
        }

        if (!tx.isTxParamValidate()) {
            return;
        }

        if (!tx.verifyTransactionSig()) {
            return;
        }

        if (!checkTypeAndBalance(tx)) {
            return;
        }


        // save to db first
        try {
            this.stateDB.putTxIntoSelfTxPool(chainID, tx);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        long currentNonce = getNonce(this.userPubKey);

        if (tx.getNonce() > currentNonce) {
            // put into pool
            all.put(new ByteArrayWrapper(tx.getTxID()), tx);

            // record in local
            locals.offer(LocalTxEntry.with(tx));
        } else {
            logger.info("ChainID[{}]: tx[{}] nonce is not bigger than current nonce.",
                    chainID.toString(), Hex.toHexString(tx.getTxID()));
        }
    }

    /**
     * add local transactions into pool
     *
     * @param list
     */
    @Override
    public void addLocals(List<Transaction> list) {
        if (null != list) {
            for (Transaction transaction: list) {
                addLocal(transaction);
            }
        }
    }

    /**
     * get all local transactions in pool
     *
     * @return
     */
    @Override
    public List<Transaction> getLocals() {
        List<Transaction> list = new ArrayList<>(locals.size());
        Iterator<LocalTxEntry> iterator = locals.iterator();
        while (iterator.hasNext()) {
            Transaction tx = getTransactionByTxid(iterator.next().txid);
            if (null != tx) {
                list.add(tx);
            } else {
                // if not found in all, remove it from local
                iterator.remove();
            }
        }
        return list;
    }

    /**
     * save all local transaction in db
     */
    @Override
    public void saveLocals() {
        Iterator<LocalTxEntry> iterator = locals.iterator();
        while (iterator.hasNext()) {
            Transaction tx = getTransactionByTxid(iterator.next().txid);
            if (null != tx) {
                try {
                    this.stateDB.putTxIntoSelfTxPool(chainID, tx);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                // if not found in all, remove it from local
                iterator.remove();
            }
        }
    }

    /**
     * get a local transaction that meet the requirement of nonce continuity
     *
     * @return best transaction when nonce matches or null
     */
    @Override
    public Transaction getLocalBestTransaction() {
        LocalTxEntry localTxEntry = locals.peek();
        if (null != localTxEntry) {
            Transaction tx = getTransactionByTxid(localTxEntry.txid);
            if (null != tx) {
                long current = getNonce(tx.getSenderPubkey());
                if (tx.getNonce() == current + 1) {
                    return tx;
                }
            } else {
                // if not found in all, remove it from local
                locals.poll();
            }
        }
        return null;
    }

    /**
     * return the size of local transactions
     *
     * @return local size
     */
    @Override
    public int localSize() {
        return locals.size();
    }

    /**
     * add a transaction from the remote
     *
     * @param tx
     */
    @Override
    public void addRemote(Transaction tx) {
        // check if null
        if (null == tx) {
            logger.error("ChainID:[{}]-Add remote null!");
        }

        // check if local
        byte[] pubKey = tx.getSenderPubkey();
        if (Arrays.equals(userPubKey, pubKey)) {
            addLocal(tx);
        }

        if (!tx.isTxParamValidate()) {
            return;
        }

        if (!tx.verifyTransactionSig()) {
            return;
        }

        if (!checkTypeAndBalance(tx)) {
            return;
        }

        // check nonce
        long currentNonce = getNonce(pubKey);
        if (tx.getNonce() != currentNonce + 1) {
            logger.error("ChainID:[{}]-[{}] Nonce mismatch.",
                    chainID.toString(), Hex.toHexString(tx.getTxID()));
            return;
        }

        // all is ok, then add it

        byte[] txid = accountTx.get(new ByteArrayWrapper(pubKey));
        // check if exited
        if (null != txid) {
            if (Arrays.equals(txid, tx.getTxID())) {
                logger.info("ChainID:[{}]-Tx[{}] is already in pool.",
                        chainID.toString(), Hex.toHexString(tx.getTxID()));
                return;
            } else {
                //
                Transaction transaction = getTransactionByTxid(txid);
                if (null != transaction) {
                    if (transaction.getTxFee() >= tx.getTxFee()) {
                        logger.info("ChainID:[{}]-Tx[{}] fee is too little.",
                                chainID.toString(), Hex.toHexString(tx.getTxID()));
                        return;
                    } else {
                        // replace the old tx with the new one: remove the old one, then add the new one later
                        removeRemote(transaction);
                    }
                }
            }
        }

        // put into pool
        all.put(new ByteArrayWrapper(tx.getTxID()), tx);
        // record in the remotes
        accountTx.put(new ByteArrayWrapper(pubKey), tx.getTxID());
        remotes.offer(MemoryPoolEntry.with(tx));
    }

    /**
     * validate tx
     * @param tx
     * @return
     */
    private boolean checkTypeAndBalance(Transaction tx) {
        try {
            switch (tx.getTxData().getMsgType()) {
                case Wiring: {
                    long cost = tx.getTxData().getAmount() + tx.getTxFee();
                    AccountState accountState = this.stateDB.getAccount(this.chainID, tx.getSenderPubkey());
                    if (accountState.getBalance().longValue() < cost) {
                        logger.error("Balance is not enough.");
                        return false;
                    }
                    break;
                }
                case RegularForum: {
                    long cost = tx.getTxFee();
                    AccountState accountState = this.stateDB.getAccount(this.chainID, tx.getSenderPubkey());
                    if (accountState.getBalance().longValue() < cost) {
                        logger.error("Balance is not enough.");
                        return false;
                    }
                    break;
                }
                case ForumComment: {
                    long cost = tx.getTxFee();
                    AccountState accountState = this.stateDB.getAccount(this.chainID, tx.getSenderPubkey());
                    if (accountState.getBalance().longValue() < cost) {
                        logger.error("Balance is not enough.");
                        return false;
                    }
                    break;
                }
                default: {
                    logger.error("Type is not supported!");
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * add transactions from the remote
     *
     * @param list
     */
    @Override
    public void addRemotes(List<Transaction> list) {
        if (null != list) {
            for (Transaction tx: list) {
                addRemote(tx);
                trySlimDownPool();
            }
        }
    }

    /**
     * get a transaction that has the maximum fee
     *
     * @return local first, then the maximum fee in remote, or null when poll is empty
     */
    @Override
    public Transaction getBestTransaction() {
        // local first
        Transaction localBest = getLocalBestTransaction();
        if (null != localBest) {
            return localBest;
        }

        // get transaction that has the maximum fee
        MemoryPoolEntry entry = remotes.peek();
        if (null != entry) {
            Transaction tx = getTransactionByTxid(entry.txid);
            if (null != tx) {
                return tx;
            } else {
                // if not found, remove it from remotes
                remotes.poll();
            }
        }

        return null;
    }

    /**
     * return the size of all remote transactions
     *
     * @return
     */
    @Override
    public int remoteSize() {
        // accountTx.size() or remotes.size()
        return remotes.size();
    }

    /**
     * retrun the size of all transaction, including local and remote ones
     *
     * @return
     */
    @Override
    public int size() {
        return all.size();
    }

//    /**
//     * It should be called on each block imported as <b>BEST</b> <br>
//     * Does several things:
//     * <ul>
//     * <li>removes block's txs from pool</li>
//     * <li>removes outdated txs</li>
//     * </ul>
//     *
//     * @param block block imported into blockchain as a <b>BEST</b> one
//     */
//    @Override
//    public void processBest(Block block) {
//
//    }

    /**
     * get a transaction by txid
     *
     * @return transaction or null when not found
     */
    @Override
    public Transaction getTransactionByTxid(byte[] txid) {
        return all.get(new ByteArrayWrapper(txid));
    }

    /**
     * get remote transactions maximum fee in pool
     *
     * @return
     */
    @Override
    public long getMaxFee() {
        MemoryPoolEntry entry = remotes.peek();

        while (null != entry) {
            Transaction tx = getTransactionByTxid(entry.txid);
            if (null != tx) {
                return tx.getTxFee();
            } else {
                // if not found, remove it from remotes
                remotes.poll();
            }

            entry = remotes.peek();
        }

        return 0;
    }

    /**
     * remove a remote transaction from pool
     * @param tx the transaction to be removed
     */
    private void removeRemote(Transaction tx) {
        accountTx.remove(new ByteArrayWrapper(tx.getSenderPubkey()));
        remotes.remove(MemoryPoolEntry.with(tx));
        all.remove(new ByteArrayWrapper(tx.getTxID()));
    }

    /**
     * remove a local transaction from pool
     * @param tx the transaction to be removed
     */
    private void removeLocal(Transaction tx) {
        locals.remove(LocalTxEntry.with(tx));
        all.remove(new ByteArrayWrapper(tx.getTxID()));
    }

    /**
     * remove a transaction from pool
     *
     * @param tx transaction to be removed
     */
    @Override
    public void removeTransactionFromPool(Transaction tx) {
        if (Arrays.equals(tx.getSenderPubkey(), userPubKey)) {
            removeLocal(tx);
        } else {
            removeRemote(tx);
        }
    }

//    /**
//     * remove transaction by hash
//     *
//     * @param txid
//     */
//    @Override
//    public void removeTransactionByHash(byte[] txid) {
//    }
//
//    /**
//     * remove transaction by account
//     *
//     * @param pubKey
//     */
//    @Override
//    public void removeTransactionByAccount(byte[] pubKey) {
//
//    }


    /**
     * check if a transaction is in pool
     *
     * @param txid target to check
     * @return
     */
    @Override
    public boolean isInPool(byte[] txid) {
        return all.containsKey(new ByteArrayWrapper(txid));
    }

    /**
     * try to slim down the pool
     * when pool size is over 3 * mutable range, keep half of it
     */
    @Override
    public void trySlimDownPool() {
        if (remotes.size() > ChainParam.SLIM_DOWN_SIZE) {
            slimDownPool();
        }
    }

    /**
     * Keep half of the transaction with a relatively large fee
     */
    public void slimDownPool() {
        int size = remotes.size();

        PriorityQueue<MemoryPoolEntry> originalRemotes = remotes;
        remotes = new PriorityQueue<>(size / 2, new MemoryPoolPolicy());

        // 0~size/2:add, others:delete
        Iterator<MemoryPoolEntry> iterator = originalRemotes.iterator();
        int i = 0;
        // O(n) = n, if use poll, O(n) = n * log(n)

        // add
        while (iterator.hasNext() && i < size / 2) {
            remotes.offer(iterator.next());
            i++;
        }

        // delete
        while (iterator.hasNext()) {
            MemoryPoolEntry entry = iterator.next();
            Transaction tx = getTransactionByTxid(entry.txid);
            if (null != tx) {
                all.remove(new ByteArrayWrapper(entry.txid));
                if (accountTx.containsValue(entry.txid)) {
                    accountTx.remove(new ByteArrayWrapper(tx.getSenderPubkey()));
                }
            }
        }

        originalRemotes.clear();
    }

    /**
     * get a peer that has latest timestamp
     *
     * @return myself pubKey when there is no tx in pool
     */
    @Override
    public byte[] getOptimalPeer() {
        // get transaction that has the maximum fee
        MemoryPoolEntry entry = remotes.peek();
        if (null != entry) {
            Transaction tx = getTransactionByTxid(entry.txid);
            if (null != tx) {
                return tx.getSenderPubkey();
            } else {
                // if not found, remove it from remotes
                remotes.poll();
            }
        }

        return AccountManager.getInstance().getKeyPair().first;
    }

    /**
     * re-check the legality of the corresponding account transaction
     *
     * @param pubKey
     */
    @Override
    public void recheckAccoutTx(byte[] pubKey) {
        try {
            if (Arrays.equals(this.userPubKey, pubKey)) {
                Transaction tx = getLocalBestTransaction();
                if (null != tx) {
                    // check nonce
                    AccountState accountState = this.stateDB.getAccount(this.chainID, tx.getSenderPubkey());
                    // if local best tx nonce is too little, remove it until big enough
                    if (accountState.getNonce().longValue() >= tx.getNonce()) {
                        removeRemote(tx);
                        recheckAccoutTx(this.userPubKey);
                    }

                }
            } else {
                byte[] txid = this.accountTx.get(new ByteArrayWrapper(pubKey));
                if (null == txid) {
                    return;
                }

                Transaction tx = this.all.get(new ByteArrayWrapper(txid));
                if (null != tx) {
                    // check nonce
                    AccountState accountState = this.stateDB.getAccount(this.chainID, tx.getSenderPubkey());
                    if (accountState.getNonce().longValue() + 1 != tx.getNonce()) {
                        logger.error("Nonce is discontinuity.");
                        removeRemote(tx);
                    }

                    // check type and balance
                    switch (tx.getTxData().getMsgType()) {
                        case Wiring: {
                            long cost = tx.getTxData().getAmount() + tx.getTxFee();

                            if (accountState.getBalance().longValue() < cost) {
                                logger.error("Balance is not enough.");
                                removeRemote(tx);
                            }
                            break;
                        }
                        case RegularForum: {
                            long cost = tx.getTxFee();
                            if (accountState.getBalance().longValue() < cost) {
                                logger.error("Balance is not enough.");
                                removeRemote(tx);
                            }
                            break;
                        }
                        case ForumComment: {
                            long cost = tx.getTxFee();
                            if (accountState.getBalance().longValue() < cost) {
                                logger.error("Balance is not enough.");
                                removeRemote(tx);
                            }
                            break;
                        }
                        default: {
                            logger.error("Type is not supported!");
                            removeRemote(tx);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * re-check the legality of the corresponding accounts transaction
     *
     * @param accounts public key set
     */
    @Override
    public void recheckAccoutTx(Set<ByteArrayWrapper> accounts) {
        if (null != accounts) {
            for (ByteArrayWrapper account: accounts) {
                recheckAccoutTx(account.getData());
            }
        }
    }
}

