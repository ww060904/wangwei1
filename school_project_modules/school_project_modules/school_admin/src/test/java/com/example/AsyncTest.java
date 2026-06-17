package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executor;

@SpringBootTest
public class AsyncTest {
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;   //将我们的线程池装配Executor执行器中

    @Test
    public void testThreadPool() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            int taskNum = i;
            // 正确的调用方式：executor.execute(Runnable task)
            taskExecutor.execute(() -> {
                try {
                    // 打印当前执行的线程名和任务号
                    System.out.println(Thread.currentThread().getName() + " 执行任务 " + taskNum);
                    Thread.sleep(2000); // 模拟任务执行2秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // 让主线程等待，方便观察输出
        Thread.sleep(10000);
    }
}
