package io.taucoin.torrent.publishing.core.storage.sqlite;


import androidx.annotation.NonNull;
import io.taucoin.torrent.publishing.core.storage.sqlite.entity.Member;

/**
 * 提供操作Member数据的接口
 */
public interface MemberRepository {

    /**
     * 添加新的Member
     * @param member User对象
     * @return 结果
     */
    long addMember(@NonNull Member member);

    /**
     * 更新Member数据
     * @param member User对象
     * @return 结果
     */
    int updateMember(@NonNull Member member);

    /**
     * 获取Member根据公钥和链ID
     * @param chainID 社区链ID
     * @param publicKey 公钥
     * @return Member
     */
    Member getMemberByChainIDAndPk(@NonNull String chainID, @NonNull String publicKey);
}
