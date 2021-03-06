/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.linalg;


import lombok.val;
import org.bytedeco.javacpp.Pointer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.config.ND4JSystemProperties;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.profiler.ProfilerConfig;
import org.nd4j.linalg.util.ArrayUtil;
import org.nd4j.nativeblas.NativeOpsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.*;


/**
 * Base Nd4j test
 * @author Adam Gibson
 */
@RunWith(Parameterized.class)
public abstract class BaseNd4jTest {
    private static Logger log = LoggerFactory.getLogger(BaseNd4jTest.class);

    @Rule
    public TestName testName = new TestName();

    protected long startTime;
    protected int threadCountBefore;

    protected Nd4jBackend backend;
    protected String name;
    public final static String DEFAULT_BACKEND = "org.nd4j.linalg.defaultbackend";

    public BaseNd4jTest() {
        this("", getDefaultBackend());
    }

    public BaseNd4jTest(String name) {
        this(name, getDefaultBackend());
    }

    public BaseNd4jTest(String name, Nd4jBackend backend) {
        this.backend = backend;
        this.name = name;

        //Suppress ND4J initialization - don't need this logged for every test...
        System.setProperty(ND4JSystemProperties.LOG_INITIALIZATION, "false");
        System.gc();
    }

    public BaseNd4jTest(Nd4jBackend backend) {
        this(backend.getClass().getName() + UUID.randomUUID().toString(), backend);

    }

    private static List<Nd4jBackend> backends;
    static {
        ServiceLoader<Nd4jBackend> loadedBackends = ServiceLoader.load(Nd4jBackend.class);
        Iterator<Nd4jBackend> backendIterator = loadedBackends.iterator();
        backends = new ArrayList<>();
        List<String> backendsToRun = Nd4jTestSuite.backendsToRun();

        while (backendIterator.hasNext()) {
            Nd4jBackend backend = backendIterator.next();
            if (backend.canRun() && backendsToRun.contains(backend.getClass().getName()) || backendsToRun.isEmpty())
                backends.add(backend);
        }

    }
    public static void assertArrayEquals(String string, Object[] expecteds, Object[] actuals) {
        org.junit.Assert.assertArrayEquals(string, expecteds, actuals);
    }

    public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
        org.junit.Assert.assertArrayEquals(expecteds, actuals);
    }

    public static void assertArrayEquals(String string, long[] shapeA, long[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB);
    }

    public static void assertArrayEquals(String string, byte[] shapeA, byte[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB);
    }

    public static void assertArrayEquals(byte[] shapeA, byte[] shapeB) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB);
    }

    public static void assertArrayEquals(long[] shapeA, long[] shapeB) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB);
    }

    public static void assertArrayEquals(String string, int[] shapeA, long[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, ArrayUtil.toLongArray(shapeA), shapeB);
    }

    public static void assertArrayEquals(int[] shapeA, long[] shapeB) {
        org.junit.Assert.assertArrayEquals(ArrayUtil.toLongArray(shapeA), shapeB);
    }

    public static void assertArrayEquals(String string, long[] shapeA, int[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, shapeA, ArrayUtil.toLongArray(shapeB));
    }

    public static void assertArrayEquals(long[] shapeA, int[] shapeB) {
        org.junit.Assert.assertArrayEquals(shapeA, ArrayUtil.toLongArray(shapeB));
    }

    public static void assertArrayEquals(String string, int[] shapeA, int[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB);
    }

    public static void assertArrayEquals(int[] shapeA, int[] shapeB) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB);
    }

    public static void assertArrayEquals(String string, boolean[] shapeA, boolean[] shapeB) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB);
    }

    public static void assertArrayEquals(boolean[] shapeA, boolean[] shapeB) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB);
    }


    public static void assertArrayEquals(float[] shapeA, float[] shapeB, float delta) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB, delta);
    }

    public static void assertArrayEquals(double[] shapeA, double[] shapeB, double delta) {
        org.junit.Assert.assertArrayEquals(shapeA, shapeB, delta);
    }

    public static void assertArrayEquals(String string, float[] shapeA, float[] shapeB, float delta) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB, delta);
    }

    public static void assertArrayEquals(String string, double[] shapeA, double[] shapeB, double delta) {
        org.junit.Assert.assertArrayEquals(string, shapeA, shapeB, delta);
    }

    @Parameterized.Parameters(name = "{index}: backend({0})={1}")
    public static Collection<Object[]> configs() {
        List<Object[]> ret = new ArrayList<>();
        for (Nd4jBackend backend : backends)
            ret.add(new Object[] {backend});
        return ret;
    }

    /**
     * Get the default backend (jblas)
     * The default backend can be overridden by also passing:
     * -Dorg.nd4j.linalg.defaultbackend=your.backend.classname
     * @return the default backend based on the
     * given command line arguments
     */
    public static Nd4jBackend getDefaultBackend() {
        String cpuBackend = "org.nd4j.linalg.cpu.nativecpu.CpuBackend";
        //String cpuBackend = "org.nd4j.linalg.cpu.CpuBackend";
        String gpuBackend = "org.nd4j.linalg.jcublas.JCublasBackend";
        String clazz = System.getProperty(DEFAULT_BACKEND, cpuBackend);
        try {
            return (Nd4jBackend) Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @Before
    public void before() throws Exception {
        //
        log.info("Running {}.{} on {}", getClass().getName(), testName.getMethodName(), backend.getClass().getSimpleName());
        Nd4j.getExecutioner().setProfilingConfig(ProfilerConfig.builder().build());
        Nd4j nd4j = new Nd4j();
        nd4j.initWithBackend(backend);
        Nd4j.factory().setOrder(ordering());
        NativeOpsHolder.getInstance().getDeviceNativeOps().enableDebugMode(false);
        Nd4j.getExecutioner().enableDebugMode(false);
        Nd4j.getExecutioner().enableVerboseMode(false);
        Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);
        startTime = System.currentTimeMillis();
        threadCountBefore = ManagementFactory.getThreadMXBean().getThreadCount();
    }

    @After
    public void after() throws Exception {
        long totalTime = System.currentTimeMillis() - startTime;
        Nd4j.getMemoryManager().purgeCaches();

        logTestCompletion(totalTime);
        if (System.getProperties().getProperty("backends") != null
                        && !System.getProperty("backends").contains(backend.getClass().getName()))
            return;
        Nd4j nd4j = new Nd4j();
        nd4j.initWithBackend(backend);
        Nd4j.factory().setOrder(ordering());
        NativeOpsHolder.getInstance().getDeviceNativeOps().enableDebugMode(false);
        Nd4j.getExecutioner().enableDebugMode(false);
        Nd4j.getExecutioner().enableVerboseMode(false);

        //Attempt to keep workspaces isolated between tests
        Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
        val currWS = Nd4j.getMemoryManager().getCurrentWorkspace();
        Nd4j.getMemoryManager().setCurrentWorkspace(null);
        if(currWS != null){
            //Not really safe to continue testing under this situation... other tests will likely fail with obscure
            // errors that are hard to track back to this
            log.error("Open workspace leaked from test! Exiting - {}, isOpen = {} - {}", currWS.getId(), currWS.isScopeActive(), currWS);
            System.exit(1);
        }
    }

    public void logTestCompletion( long totalTime){
        StringBuilder sb = new StringBuilder();
        long maxPhys = Pointer.maxPhysicalBytes();
        long maxBytes = Pointer.maxBytes();
        long currPhys = Pointer.physicalBytes();
        long currBytes = Pointer.totalBytes();

        long jvmTotal = Runtime.getRuntime().totalMemory();
        long jvmMax = Runtime.getRuntime().maxMemory();

        int threadsAfter = ManagementFactory.getThreadMXBean().getThreadCount();
        sb.append(getClass().getSimpleName()).append(".").append(testName.getMethodName())
                .append(": ").append(totalTime).append(" ms")
                .append(", threadCount: (").append(threadCountBefore).append("->").append(threadsAfter).append(")")
                .append(", jvmTotal=").append(jvmTotal)
                .append(", jvmMax=").append(jvmMax)
                .append(", totalBytes=").append(currBytes).append(", maxBytes=").append(maxBytes)
                .append(", currPhys=").append(currPhys).append(", maxPhys=").append(maxPhys);

        List<MemoryWorkspace> ws = Nd4j.getWorkspaceManager().getAllWorkspacesForCurrentThread();
        if(ws != null && ws.size() > 0){
            long currSize = 0;
            for(MemoryWorkspace w : ws){
                currSize += w.getCurrentSize();
            }
            if(currSize > 0){
                sb.append(", threadWSSize=").append(currSize)
                        .append(" (").append(ws.size()).append(" WSs)");
            }
        }


        Properties p = Nd4j.getExecutioner().getEnvironmentInformation();
        Object o = p.get("cuda.devicesInformation");
        if(o instanceof List){
            List<Map<String,Object>> l = (List<Map<String, Object>>) o;
            if(l.size() > 0) {

                sb.append(" [").append(l.size())
                        .append(" GPUs: ");

                for (int i = 0; i < l.size(); i++) {
                    Map<String,Object> m = l.get(i);
                    if(i > 0)
                        sb.append(",");
                    sb.append("(").append(m.get("cuda.freeMemory")).append(" free, ")
                            .append(m.get("cuda.totalMemory")).append(" total)");
                }
                sb.append("]");
            }
        }
        log.info(sb.toString());
    }


    /**
     * The ordering for this test
     * This test will only be invoked for
     * the given test  and ignored for others
     *
     * @return the ordering for this test
     */
    public char ordering() {
        return 'a';
    }



    public String getFailureMessage() {
        return "Failed with backend " + backend.getClass().getName() + " and ordering " + ordering();
    }



}
