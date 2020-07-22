package io.taucoin.torrent.publishing.core.storage;

import android.content.Context;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.room.Query;
import io.reactivex.Flowable;
import io.taucoin.torrent.publishing.core.model.data.ReplyAndTx;
import io.taucoin.torrent.publishing.core.storage.entity.Tx;
import io.taucoin.torrent.publishing.core.utils.DateUtil;
import io.taucoin.types.MsgType;

/**
 * TxRepository接口实现
 */
public class TxRepositoryImpl implements TxRepository{

    private Context appContext;
    private AppDatabase db;

    /**
     * CommunityRepositoryImpl 构造函数
     * @param appContext 上下文
     * @param db 数据库实例
     */
    TxRepositoryImpl(@NonNull Context appContext, @NonNull AppDatabase db) {
        this.appContext = appContext;
        this.db = db;
    }

    /**
     * 添加新的交易
     */
    @Override
    public long addTransaction(Tx transaction){
        return db.txDao().addTransaction(transaction);
    }

    /**
     * 更新交易
     */
    @Override
    public int updateTransaction(Tx transaction){
        return db.txDao().updateTransaction(transaction);
    }

    /**
     * 根据chainID查询社区
     * @param chainID 社区链id
     */
    @Override
    public List<ReplyAndTx> getTxsByChainID(String chainID){
        return db.txDao().getTxsByChainID(MsgType.IdentityAnnouncement.getVaLue(), chainID);
    }

    /**
     * 根据chainID获取社区的交易的被被观察者
     * @param chainID 社区链id
     */
    @Override
    public Flowable<List<ReplyAndTx>> observeTxsByChainID(String chainID){
        return db.txDao().observeTxsByChainID(MsgType.IdentityAnnouncement.getVaLue(), chainID);
    }

    @Override
    public DataSource.Factory<Integer, ReplyAndTx> queryCommunityTxs(String chainID){
        return db.txDao().queryCommunityTxs(MsgType.IdentityAnnouncement.getVaLue(), chainID);
    }

    /**
     * 获取社区里用户未上链并且未过期的交易数
     * @param chainID chainID
     * @param senderPk 公钥
     * @param expireTime 过期时间时长
     * @return int
     */
    @Override
    public int getPendingTxsNotExpired(String chainID, String senderPk, long expireTime){
        long expireTimePoint = DateUtil.getTime() - expireTime;
        return db.txDao().getPendingTxsNotExpired(chainID, senderPk, expireTimePoint);
    }

    /**
     * 获取社区里用户未上链并且过期的最早的交易
     * @param chainID chainID
     * @param senderPk 公钥
     * @param expireTime 过期时间时长
     * @return int
     */
    @Override
    public Tx getEarliestExpireTx(String chainID, String senderPk, long expireTime){
        long expireTimePoint = DateUtil.getTime() - expireTime;
        return db.txDao().getEarliestExpireTx(chainID, senderPk, expireTimePoint);
    }
}
