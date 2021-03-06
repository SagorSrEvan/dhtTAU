package io.taucoin.torrent.publishing.core.storage.sqlite;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.taucoin.torrent.publishing.core.storage.sqlite.entity.User;

/**
 * 提供外部操作User数据的接口
 */
public interface UserRepository {

    /**
     * 添加新的User/Seed
     * @param user User实例
     * @return 结果
     */
    long addUser(@NonNull User user);

    /**
     * 更新的User/Seed
     * @param user User实例
     * @return 结果
     */
    int updateUser(@NonNull User user);

    /**
     * 获取当前的用户
     * @return 当前用户User实例
     */
    User getCurrentUser();

    List<User> getUserList();

    /**
     * 观察当前用户信息是否变化
     * @return 当前用户最新User实例
     */
    Flowable<User> observeCurrentUser();

    /**
     * 设置当前用户是否是当前用户
     * @param isCurrentUser 是否是当前用户
     */
    void setCurrentUser(String publicKey, boolean isCurrentUser);

    /**
     * 获取在黑名单的用户列表
     * @return  List<User>
     */
    List<User> getUsersInBlacklist();

    /**
     * 设置用户是否加入黑名单
     * @param publicKey 公钥
     * @param blacklist 是否加入黑名单
     */
    void setUserBlacklist(String publicKey, boolean blacklist);

    /**
     * 观察Sees历史列表
     */
    Flowable<List<User>> observeSeedHistoryList();

    /**
     * 根据公钥获取用户
     * @param publicKey 公钥
     * @return 当前用户User实例
     */
    User getUserByPublicKey(String publicKey);

    /**
     * 添加新的多个User
     */
    long[] addUsers(User... user);
}
