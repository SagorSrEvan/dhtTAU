package io.taucoin.torrent.publishing.core.storage.sqlite.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room: 数据库存储Transaction实体类
 */
@Entity(tableName = "Messages")
public class Message implements Parcelable {
    @NonNull
    @PrimaryKey
    public String msgID;                    // 消息的ID
    @NonNull
    public String chainID;                  // 消息所属社区chainID
    @NonNull
    public String senderPk;                 // 消息发送者的公钥
    @NonNull
    public long timestamp;                  // 消息时间戳
    @NonNull
    public String context;                  // 消息内容
    public String replyID;                  // 被回复消息ID
    public boolean favourite = false;       // 消息是否加入收藏

    public Message(@NonNull String chainID, String context){
        this.chainID = chainID;
        this.context = context;
    }

    @Ignore
    private Message(Parcel in) {
        msgID = in.readString();
        chainID = in.readString();
        senderPk = in.readString();
        context = in.readString();
        timestamp = in.readLong();
        replyID = in.readString();
        favourite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgID);
        dest.writeString(chainID);
        dest.writeString(senderPk);
        dest.writeString(context);
        dest.writeLong(timestamp);
        dest.writeString(replyID);
        dest.writeByte((byte) (favourite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int hashCode() {
        return msgID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Message && (o == this || msgID.equals(((Message)o).msgID));
    }
}
