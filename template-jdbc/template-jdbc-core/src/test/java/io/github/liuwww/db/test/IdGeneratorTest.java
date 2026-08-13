package io.github.liuwww.db.test;

import org.junit.Test;
import io.github.liuwww.common.Idgen.DatabaseAutoIncrementIdGenerator;
import io.github.liuwww.common.Idgen.IdGenerator;
import io.github.liuwww.common.Idgen.IdGenerators;
import io.github.liuwww.common.Idgen.NanoIdGenerator;
import io.github.liuwww.common.Idgen.SequentialUUIDGenerator;
import io.github.liuwww.common.Idgen.SnowflakeIdGenerator;
import io.github.liuwww.common.Idgen.UUIDGenerator;
import io.github.liuwww.common.Idgen.IdInfo;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IdGeneratorTest {

    @Test
    public void testSnowflakeIdGeneratorUnique() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            String id = generator.nextKey(null);
            assertTrue("ID should be unique", ids.add(id));
        }
    }

    @Test
    public void testSnowflakeIdGeneratorTrend() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);

        long id1 = Long.parseLong(generator.nextKey(null));
        long id2 = Long.parseLong(generator.nextKey(null));

        assertTrue("IDs should be increasing", id2 > id1);
    }

    @Test
    public void testSnowflakeIdGeneratorNextLongKey() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);

        long id = generator.nextLongKey(null);
        assertTrue("ID should be positive", id > 0);
    }

    @Test
    public void testSnowflakeIdGeneratorNextIntKey() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);

        try {
            int id = generator.nextIntKey(null);
            assertTrue("ID should be within integer range", id <= Integer.MAX_VALUE);
        }
        catch (ArithmeticException e) {
            assertTrue("If exception thrown, it should be about integer overflow",
                    e.getMessage().contains("out of integer range"));
        }
    }

    @Test
    public void testSnowflakeIdGeneratorIsCreatedByDatabase() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);

        assertFalse("SnowflakeIdGenerator should not use database auto-generation",
                generator.isCreatedByDatabase(null));
    }

    @Test
    public void testSnowflakeIdGeneratorGetIdInfo() {
        long workerId = 1;
        long datacenterId = 2;
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(workerId, datacenterId);

        long beforeTime = System.currentTimeMillis();
        long id = generator.nextId();
        long afterTime = System.currentTimeMillis();
        IdInfo idInfo = generator.getIdInfo(id);

        assertNotNull("IdInfo should not be null", idInfo);
        assertEquals("WorkerId should match", workerId, idInfo.getWorkerId());
        assertEquals("DatacenterId should match", datacenterId, idInfo.getDatacenterId());
        assertNotNull("Timestamp should not be null", idInfo.getTimestamp());
        assertNotNull("Sequence should not be null", idInfo.getSequence());

        // 验证时间戳精度，误差不超过1毫秒
        long parsedTimestamp = idInfo.getTimestamp();
        assertTrue("Timestamp should be close to generation time (before)", parsedTimestamp >= beforeTime);
        assertTrue("Timestamp should be close to generation time (after)", parsedTimestamp <= afterTime);
        assertTrue("Timestamp error should not exceed 1ms", Math.abs(parsedTimestamp - beforeTime) <= 1);
        System.out.println(idInfo);
    }

    @Test
    public void testSnowflakeIdGeneratorConfiguration() {
        long workerId = 3;
        long datacenterId = 1;
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(workerId, datacenterId);

        assertEquals("WorkerId should match", workerId, generator.getWorkerId());
        assertEquals("DatacenterId should match", datacenterId, generator.getDatacenterId());
        assertEquals("WorkerIdBits should be 3", 3L, generator.getWorkerIdBits());
        assertEquals("DatacenterIdBits should be 2", 2L, generator.getDatacenterIdBits());
        assertEquals("SequenceBits should be 12", 12L, generator.getSequenceBits());
        assertNotNull("Twepoch should not be null", generator.getTwepoch());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSnowflakeIdGeneratorInvalidWorkerId() {
        new SnowflakeIdGenerator(8, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSnowflakeIdGeneratorInvalidDatacenterId() {
        new SnowflakeIdGenerator(1, 4);
    }

    @Test
    public void testSnowflakeIdGeneratorGetType() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);

        assertEquals("Type should be class simple name", "SnowflakeIdGenerator", generator.getType());
    }

    @Test
    public void testUUIDGeneratorUnique() {
        IdGenerator generator = new UUIDGenerator();
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String id = generator.nextKey(null);
            assertTrue("UUID should be unique", ids.add(id));
        }
    }

    @Test
    public void testUUIDGeneratorFormat() {
        IdGenerator generator = new UUIDGenerator();

        String id = generator.nextKey(null);

        assertNotNull("ID should not be null", id);
        assertEquals("UUID should be 32 characters (without dashes)", 32, id.length());
        assertTrue("UUID should contain only hex characters", id.matches("[0-9a-f]+"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUUIDGeneratorNextIntKey() {
        IdGenerator generator = new UUIDGenerator();
        generator.nextIntKey(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUUIDGeneratorNextLongKey() {
        IdGenerator generator = new UUIDGenerator();
        generator.nextLongKey(null);
    }

    @Test
    public void testUUIDGeneratorIsCreatedByDatabase() {
        IdGenerator generator = new UUIDGenerator();

        assertFalse("UUIDGenerator should not use database auto-generation", generator.isCreatedByDatabase(null));
    }

    @Test
    public void testUUIDGeneratorGetType() {
        IdGenerator generator = new UUIDGenerator();

        assertEquals("Type should be class simple name", "UUIDGenerator", generator.getType());
    }

    @Test
    public void testSnowflakeIdGeneratorHighConcurrency() {
        IdGenerator generator = new SnowflakeIdGenerator(1, 1);
        Set<String> ids = new HashSet<>();
        int threadCount = 10;
        int idsPerThread = 1000;

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    String id = generator.nextKey(null);
                    synchronized (ids) {
                        ids.add(id);
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                fail("Thread interrupted: " + e.getMessage());
            }
        }

        assertEquals("All IDs should be unique even under concurrency", threadCount * idsPerThread, ids.size());
    }

    // ===== idShift 基准值偏移测试 =====

    @Test
    public void testSnowflakeIdGeneratorIdShift() {
        long idShift = 100000L;
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 2, idShift);

        assertEquals("idShift should match", idShift, generator.getIdShift());

        long beforeTime = System.currentTimeMillis();
        long id = generator.nextId();
        long afterTime = System.currentTimeMillis();

        // getIdInfo 内部会加回 idShift 还原，故解析出的时间戳应接近生成时间
        IdInfo info = generator.getIdInfo(id);
        assertEquals("WorkerId should match after idShift round-trip", 1L, info.getWorkerId());
        assertEquals("DatacenterId should match after idShift round-trip", 2L, info.getDatacenterId());
        assertTrue("Timestamp should be close to generation time", info.getTimestamp() >= beforeTime);
        assertTrue("Timestamp should be close to generation time", info.getTimestamp() <= afterTime);
    }

    @Test
    public void testSnowflakeIdGeneratorIdShiftLowersId() {
        // 同一基准时间附近，带 idShift 的 ID 应明显小于不带 idShift 的 ID
        long idShift = 500000000L;
        SnowflakeIdGenerator withShift = new SnowflakeIdGenerator(1, 1, idShift);
        SnowflakeIdGenerator noShift = new SnowflakeIdGenerator(1, 1);

        long idWithShift = withShift.nextId();
        long idNoShift = noShift.nextId();

        assertTrue("ID with idShift should be smaller than without", idWithShift < idNoShift);
        assertTrue("ID difference should be close to idShift", Math.abs((idNoShift - idWithShift) - idShift) < idShift);
    }

    @Test
    public void testSnowflakeIdGeneratorIdShiftZero() {
        // idShift=0 时行为应与 2 参数构造方法一致
        SnowflakeIdGenerator g1 = new SnowflakeIdGenerator(1, 1, 0L);
        SnowflakeIdGenerator g2 = new SnowflakeIdGenerator(1, 1);

        assertEquals("idShift should be 0", 0L, g1.getIdShift());
        long id = g1.nextId();
        IdInfo info = g1.getIdInfo(id);
        assertEquals("WorkerId should match", 1L, info.getWorkerId());

        // g2 也能正常生成并解析
        long id2 = g2.nextId();
        assertNotNull(g2.getIdInfo(id2));
    }

    // ===== twepoch 时间偏移值配置测试 =====

    @Test
    public void testSnowflakeIdGeneratorDefaultTwepoch() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

        // 默认 twepoch: 2026-01-01 00:00:00 UTC = 1767225600000L
        assertEquals("Default twepoch should be 2026-01-01 UTC", 1767225600000L, generator.getTwepoch());
    }

    @Test
    public void testSnowflakeIdGeneratorCustomTwepoch() {
        long customTwepoch = 946684800000L; // 2000-01-01 00:00:00 UTC
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, 0L, customTwepoch);

        assertEquals("Custom twepoch should match", customTwepoch, generator.getTwepoch());

        long id = generator.nextId();
        assertTrue("ID should still be positive", id > 0);
        IdInfo info = generator.getIdInfo(id);
        assertEquals("WorkerId should match", 1L, info.getWorkerId());
    }

    @Test
    public void testSnowflakeIdGeneratorTwepochAffectsIdMagnitude() {
        // 较大的 twepoch 会使 (timestamp - twepoch) 较小，从而 ID 较小
        long smallTwepoch = 946684800000L;   // 2000-01-01
        long largeTwepoch = 1767225600000L;  // 2026-01-01
        SnowflakeIdGenerator gSmall = new SnowflakeIdGenerator(1, 1, 0L, smallTwepoch);
        SnowflakeIdGenerator gLarge = new SnowflakeIdGenerator(1, 1, 0L, largeTwepoch);

        long idSmall = gSmall.nextId();
        long idLarge = gLarge.nextId();

        assertTrue("Smaller twepoch should produce larger ID (larger timestamp delta)",
                idSmall > idLarge);
    }

    // ===== 时钟回流异常测试 =====

    @Test(expected = RuntimeException.class)
    public void testSnowflakeIdGeneratorClockBackwards() {
        final long baseTime = System.currentTimeMillis();
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1) {
            private boolean first = true;

            @Override
            protected long timeGen() {
                if (first) {
                    first = false;
                    return baseTime;
                }
                return baseTime - 1; // 模拟时钟回流
            }
        };

        generator.nextId(); // 正常生成，lastTimestamp = baseTime
        generator.nextId(); // timeGen = baseTime - 1 < lastTimestamp，应抛异常
    }

    // ===== 构造方法边界值测试 =====

    @Test
    public void testSnowflakeIdGeneratorBoundaryWorkerId() {
        // workerId 边界值 0 和 7（maxWorkerId = 7）
        SnowflakeIdGenerator g0 = new SnowflakeIdGenerator(0, 1);
        SnowflakeIdGenerator g7 = new SnowflakeIdGenerator(7, 1);

        long id0 = g0.nextId();
        long id7 = g7.nextId();

        IdInfo info0 = g0.getIdInfo(id0);
        IdInfo info7 = g7.getIdInfo(id7);

        assertEquals("Boundary workerId 0", 0L, info0.getWorkerId());
        assertEquals("Boundary workerId 7", 7L, info7.getWorkerId());
    }

    @Test
    public void testSnowflakeIdGeneratorBoundaryDatacenterId() {
        // datacenterId 边界值 0 和 3（maxDatacenterId = 3）
        SnowflakeIdGenerator g0 = new SnowflakeIdGenerator(1, 0);
        SnowflakeIdGenerator g3 = new SnowflakeIdGenerator(1, 3);

        long id0 = g0.nextId();
        long id3 = g3.nextId();

        IdInfo info0 = g0.getIdInfo(id0);
        IdInfo info3 = g3.getIdInfo(id3);

        assertEquals("Boundary datacenterId 0", 0L, info0.getDatacenterId());
        assertEquals("Boundary datacenterId 3", 3L, info3.getDatacenterId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSnowflakeIdGeneratorNegativeWorkerId() {
        new SnowflakeIdGenerator(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSnowflakeIdGeneratorNegativeDatacenterId() {
        new SnowflakeIdGenerator(1, -1);
    }

    // ===== IdGenerators 中立工具类测试 =====

    @Test
    public void testIdGeneratorsDefault() {
        // 默认全局实例应可正常工作
        IdGenerator gen = IdGenerators.getDefault();
        assertNotNull("Default generator should not be null", gen);

        long id = IdGenerators.nextId();
        assertTrue("Default ID should be positive", id > 0);

        String sid = IdGenerators.nextKey();
        assertNotNull("nextKey should not return null", sid);
    }

    @Test
    public void testIdGeneratorsSetDefault() {
        IdGenerator original = IdGenerators.getDefault();
        SnowflakeIdGenerator custom = new SnowflakeIdGenerator(3, 2, 0L, 946684800000L);
        try {
            IdGenerators.setDefault(custom);
            assertSame("setDefault should replace global instance", custom, IdGenerators.getDefault());

            // 替换后 nextId 走新实例
            SnowflakeIdGenerator current = (SnowflakeIdGenerator) IdGenerators.getDefault();
            assertEquals("Custom workerId should be active", 3L, current.getWorkerId());
            assertEquals("Custom twepoch should be active", 946684800000L, current.getTwepoch());

            long id = IdGenerators.nextId();
            assertTrue("ID should be positive with custom generator", id > 0);
        } finally {
            // 恢复原始全局实例，避免影响其他测试
            IdGenerators.setDefault(original);
        }
    }

    @Test
    public void testIdGeneratorsConcurrency() {
        IdGenerator original = IdGenerators.getDefault();
        IdGenerators.setDefault(new SnowflakeIdGenerator(1, 1));
        try {
            Set<Long> ids = new HashSet<>();
            int threadCount = 8;
            int idsPerThread = 500;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < idsPerThread; j++) {
                        synchronized (ids) {
                            ids.add(IdGenerators.nextId());
                        }
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    fail("Thread interrupted");
                }
            }

            assertEquals("All IDs should be unique under concurrency",
                    threadCount * idsPerThread, ids.size());
        } finally {
            IdGenerators.setDefault(original);
        }
    }

    // ===== DatabaseAutoIncrementIdGenerator 测试 =====

    @Test
    public void testDatabaseAutoIncrementIdGenerator() {
        DatabaseAutoIncrementIdGenerator gen = new DatabaseAutoIncrementIdGenerator();

        assertNull("nextKey should return null (placeholder)", gen.nextKey(null));
        assertTrue("isCreatedByDatabase should be true", gen.isCreatedByDatabase(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDatabaseAutoIncrementIdGeneratorNextIntKey() {
        new DatabaseAutoIncrementIdGenerator().nextIntKey(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDatabaseAutoIncrementIdGeneratorNextLongKey() {
        new DatabaseAutoIncrementIdGenerator().nextLongKey(null);
    }

    // ===== SequentialUUIDGenerator 测试 =====

    @Test
    public void testSequentialUUIDGeneratorFormat() {
        SequentialUUIDGenerator gen = new SequentialUUIDGenerator();
        String id = gen.nextKey(null);

        assertNotNull("ID should not be null", id);
        assertEquals("Should be 32 hex chars (UUID compatible)", 32, id.length());
        assertTrue("Should contain only hex chars", id.matches("[0-9a-f]+"));
    }

    @Test
    public void testSequentialUUIDGeneratorUnique() {
        SequentialUUIDGenerator gen = new SequentialUUIDGenerator();
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            assertTrue("ID should be unique", ids.add(gen.nextKey(null)));
        }
    }

    @Test
    public void testSequentialUUIDGeneratorTrend() throws InterruptedException {
        SequentialUUIDGenerator gen = new SequentialUUIDGenerator();

        String id1 = gen.nextKey(null);
        // 前 12 hex 是毫秒时间戳，稍后生成的 ID 前缀应 >= 前一个
        String prefix1 = id1.substring(0, 12);

        Thread.sleep(2);

        String id2 = gen.nextKey(null);
        String prefix2 = id2.substring(0, 12);

        assertTrue("Timestamp prefix should be non-decreasing",
                prefix2.compareTo(prefix1) >= 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSequentialUUIDGeneratorNextIntKey() {
        new SequentialUUIDGenerator().nextIntKey(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSequentialUUIDGeneratorNextLongKey() {
        new SequentialUUIDGenerator().nextLongKey(null);
    }

    @Test
    public void testSequentialUUIDGeneratorIsCreatedByDatabase() {
        assertFalse(new SequentialUUIDGenerator().isCreatedByDatabase(null));
    }

    // ===== NanoIdGenerator 测试 =====

    @Test
    public void testNanoIdGeneratorDefaultSize() {
        NanoIdGenerator gen = new NanoIdGenerator();
        assertEquals("Default size should be 21", NanoIdGenerator.DEFAULT_SIZE, gen.getSize());

        String id = gen.nextKey(null);
        assertNotNull("ID should not be null", id);
        assertEquals("Should be 21 chars", 21, id.length());
    }

    @Test
    public void testNanoIdGeneratorCustomSize() {
        NanoIdGenerator gen = new NanoIdGenerator(10);
        assertEquals("Custom size should match", 10, gen.getSize());

        String id = gen.nextKey(null);
        assertEquals("Should be 10 chars", 10, id.length());
    }

    @Test
    public void testNanoIdGeneratorCharset() {
        NanoIdGenerator gen = new NanoIdGenerator();
        // URL-safe: A-Za-z0-9_-
        for (int i = 0; i < 1000; i++) {
            String id = gen.nextKey(null);
            assertTrue("ID should be URL-safe: " + id, id.matches("[A-Za-z0-9_-]+"));
        }
    }

    @Test
    public void testNanoIdGeneratorUnique() {
        NanoIdGenerator gen = new NanoIdGenerator();
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            assertTrue("ID should be unique", ids.add(gen.nextKey(null)));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNanoIdGeneratorInvalidSize() {
        new NanoIdGenerator(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNanoIdGeneratorNegativeSize() {
        new NanoIdGenerator(-1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNanoIdGeneratorNextIntKey() {
        new NanoIdGenerator().nextIntKey(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNanoIdGeneratorNextLongKey() {
        new NanoIdGenerator().nextLongKey(null);
    }

    @Test
    public void testNanoIdGeneratorIsCreatedByDatabase() {
        assertFalse(new NanoIdGenerator().isCreatedByDatabase(null));
    }
}
