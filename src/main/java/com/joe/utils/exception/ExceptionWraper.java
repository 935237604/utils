package com.joe.utils.exception;

import com.joe.utils.function.Task;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 异常包装
 *
 * @author JoeKerouac
 * @version $Id: joe, v 0.1 2018年12月18日 10:53 JoeKerouac Exp $
 */
public class ExceptionWraper {

    /**
     * 异常转换，将checked异常转换为runtime异常
     * @param callable 执行函数
     * @param function 转换器
     * @param <T> 结果类型
     * @return 函数执行结果
     */
    public static <T> T convert(Callable<T> callable,
                                Function<Throwable, RuntimeException> function) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw function.apply(e);
        }
    }

    /**
     * 异常转换，将checked异常转换为runtime异常
     * @param task 执行函数
     * @param function 转换器
     * @return 函数执行结果
     */
    public static void convert(Task task, Function<Throwable, RuntimeException> function) {
        try {
            task.run();
        } catch (Throwable e) {
            throw function.apply(e);
        }
    }
}
