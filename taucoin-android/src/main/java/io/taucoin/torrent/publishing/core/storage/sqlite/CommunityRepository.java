package io.taucoin.torrent.publishing.core.storage.sqlite;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.taucoin.torrent.publishing.core.model.data.CommunityAndMember;
import io.taucoin.torrent.publishing.core.storage.sqlite.entity.Community;

/**
 * 提供操作Community数据的接口
 */
public interface CommunityRepository {

    /**
     * 添加新的社区
     * @param community 社区数据
     */
    long addCommunity(@NonNull Community community);

//    int updateCommunity(@NonNull Community community);
//
//    Community getCommunityBychainID(@NonNull String chainID);

    /**
     * 观察不在黑名单的社区列表数据变化
     * @return 被观察的社区数据列表
     */
    Flowable<List<CommunityAndMember>> observeCommunitiesNotInBlacklist();

    /**
     * 获取在黑名单的社区列表
     * @return List<Community>
     */
    List<Community> getCommunitiesInBlacklist();

    /**
     * 添加社区黑名单实现
     * @param chainID 社区chainID
     * @param blacklist 是否加入黑名单
     */
    void setCommunityBlacklist(String chainID, boolean blacklist);

    /**
     * 获取用户加入的社区列表
     * @param chainID 社区chainID
     */
    List<Community> getJoinedCommunityList(String chainID);

    /**
     * 根据chainID查询社区
     * @param chainID 社区chainID
     */
    Single<Community> getCommunityByChainIDSingle(String chainID);
}
