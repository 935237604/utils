package com.joe.utils.img;

import org.junit.Test;
import reactor.core.support.Assert;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 二维码工具类测试
 *
 * @author joe
 * @version 2018.05.24 18:47
 */
public class IQRCodeTest {
    @Test
    public void doCreate() throws IOException {
        IQRCode.create("123456", System.out, 100, 100);
    }

    @Test
    public void createImg() throws IOException {
        BufferedImage image = IQRCode.createImg("123456", 100, 100);
        Assert.notNull(image);
    }
}
