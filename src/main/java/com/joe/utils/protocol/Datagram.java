package com.joe.utils.protocol;

/**
 * 数据报，用户不能直接构建，可以通过DatagramUtil构建 socket通讯发送的数据，该数据为最小数据，不能再分
 * <p>
 * 数据报head为固定长度16个字节
 * <p>
 * 第一个字节为版本号
 * <p>
 * 第二到第五个字节为数据报长度（请求体的长度，不包含请求头）
 * <p>
 * 第六个字节为数据报数据类型
 * <p>
 * 第七到第十六字节为数据报编码
 * <p>
 * 第十七到五十六字节为ID
 * <p>
 * 数据报body为变长，长度等于数据报head里边的数据报长度
 * <p>
 *
 * @author joe
 */
@lombok.Data
public class Datagram {

    private static final byte[] EMPTY_DATA = new byte[0];

    /**
     * 需要ACK的数据类型
     */
    private static final byte[] ACKS = {DatagramConst.Type.MVC, DatagramConst.Type.FILE, DatagramConst.Type.BACK};

    /**
     * 存放数据报数据，包含头信息，只读信息，只要创建出来后就无法更改
     */
    private final byte[] data;

    /**
     * 该长度不包含头信息的长度，只有body的长度
     */
    private final int size;

    /**
     * 数据报版本
     */
    private final byte version;

    /**
     * 数据报body的编码
     */
    private final String charset;

    /**
     * 数据报数据类型（0：心跳包；1：内置MVC数据处理器数据类型；2：文件传输；3：ACK；4：后端主动发往前端的数据；
     * <p>
     * 除了0、1、2、3、4外可以自己定义数据类型）
     */
    private final byte type;

    /**
     * 数据报的ID
     */
    private final byte[] id;

    /**
     * 初始化数据报，不会对入参进行校验，默认认为入参是正确的
     *
     * @param data
     *            包含头信息的data
     * @param size
     *            该长度不包含头信息的长度，只有body的长度
     * @param version
     *            数据报版本号
     * @param charset
     *            字符集
     * @param type
     *            数据报数据类型（1：接口请求）
     * @param id
     *            数据报的ID
     */
    Datagram(byte[] data, int size, byte version, String charset, byte type, byte[] id) {
        if (data == null) {
            this.data = EMPTY_DATA;
        } else {
            this.data = new byte[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
        }

        this.size = size;
        this.version = version;
        this.charset = charset;
        this.type = type;
        this.id = id;
    }

    public byte[] getData() {
        if (this.data == null || this.data.length == 0) {
            return EMPTY_DATA;
        }
        byte[] data = new byte[this.data.length];
        System.arraycopy(this.data, 0, data, 0, data.length);
        return data;
    }

    public byte[] getBody() {
        if (this.size <= 0) {
            return EMPTY_DATA;
        }
        byte[] body = new byte[size];
        System.arraycopy(this.data, DatagramConst.Position.HEADER_LEN, body, 0, size);
        return body;
    }

    /**
     * 是否需要ACK
     *
     * @return 返回true表示需要ACK
     */
    public boolean ack() {
        return shouldAck(type);
    }

    /**
     * 是否需要ACK
     *
     * @param type
     *            数据类型
     * @return 返回true表示该类型的数据需要ACK
     */
    public static boolean shouldAck(byte type) {
        for (byte b : ACKS) {
            if (type == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是ACK数据类型
     *
     * @param type
     *            数据类型
     * @return 返回true表示该数据类型是ACK类型
     */
    public static boolean isAck(byte type) {
        return type == DatagramConst.Type.ACK;
    }

    /**
     * 是否是心跳包
     *
     * @param type
     *            数据类型
     * @return 如果数据是心跳包返回true
     */
    public static boolean isHeartbeat(byte type) {
        return type == DatagramConst.Type.HEARTBEAT;
    }

    /**
     * 是否是文件上传包
     *
     * @param type
     *            类型
     * @return 返回true表示该类型是文件上传类型
     */
    public static boolean isFile(byte type) {
        return type == DatagramConst.Type.FILE;
    }

    /**
     * 数据报类型是否是后端主动发送的消息
     *
     * @param type
     *            类型
     * @return 返回true表示该类型是后端主动发送的消息类型
     */
    public static boolean isBack(byte type) {
        return type == DatagramConst.Type.BACK;
    }
}