package org.apache.hadoop.fs.qinu.kodo.performance.openfile.sequence;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.qinu.kodo.performance.QiniuKodoPerformanceBaseTest;
import org.apache.hadoop.fs.qinu.kodo.performance.openfile.OpenBigFileCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public abstract class ASequenceOpenBigFileTest extends QiniuKodoPerformanceBaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(ASequenceOpenBigFileTest.class);
    protected int blockSize = 4 * 1024 * 1024;
    protected int blocks = 10;
    protected int readers = 1;
    protected int readerBufferSize = 4 * 1024 * 1024;


    @Override
    protected long testImpl(String testDir, FileSystem fs, ExecutorService service) throws Exception {
        // 总计20 * 4MB * 2 = 160MB
        Path p = OpenBigFileCommonUtil.makeSureExistsBigFile(testDir, fs, blockSize, blocks);

        long ms = System.currentTimeMillis();

        for (int i = 0; i < readers; i++) {
            service.submit(() -> {
                try {
                    FSDataInputStream fis = fs.open(p);
                    byte[] buf = new byte[readerBufferSize];

                    boolean eof;
                    do {
                        eof = fis.read(buf) == -1;
                    } while (!eof);
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            LOG.debug("submit task create file: {}", p);
        }
        awaitAllExecutors(service);
        return System.currentTimeMillis() - ms;
    }
}
