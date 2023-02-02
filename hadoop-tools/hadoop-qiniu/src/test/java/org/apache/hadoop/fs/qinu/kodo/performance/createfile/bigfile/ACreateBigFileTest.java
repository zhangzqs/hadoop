package org.apache.hadoop.fs.qinu.kodo.performance.createfile.bigfile;


import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.qinu.kodo.performance.QiniuKodoPerformanceBaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public abstract class ACreateBigFileTest extends QiniuKodoPerformanceBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(ACreateBigFileTest.class);

    protected int files = 5;
    protected int blockSize = 4 * 1024 * 1024;
    protected int blocks = 2;

    @Override
    protected long testImpl(String testDir, FileSystem fs, ExecutorService service) throws Exception {
        // 总计20 * 4MB * 2 = 160MB
        byte[] bs = new byte[blockSize];

        // 建立父目录
        fs.mkdirs(new Path(testDir));

        // 生产
        long ms = System.currentTimeMillis();

        for (int i = 0; i < files; i++) {
            final Path p = new Path(testDir + "/" + i);
            service.submit(() -> {
                try {
                    FSDataOutputStream fos = fs.create(p);
                    for (int j = 0; j < blocks; j++) {
                        fos.write(bs);
                    }
                    fos.close();
                    LOG.debug("task create file terminated: {}", p);
                } catch (IOException ex) {
                    LOG.error("io exception: ", ex);
                    throw new RuntimeException(ex);
                }
            });
            LOG.debug("submit task create file: {}", p);
        }
        awaitAllExecutors(service);
        return System.currentTimeMillis() - ms;
    }
}
