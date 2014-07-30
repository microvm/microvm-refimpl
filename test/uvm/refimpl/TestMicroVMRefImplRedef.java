package uvm.refimpl;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import uvm.Bundle;
import uvm.CFG;
import uvm.Function;
import uvm.ir.text.input.TestingHelper;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.facade.MicroVMClient;
import uvm.refimpl.itpr.IntBox;
import uvm.refimpl.itpr.InterpreterFrame;
import uvm.refimpl.itpr.InterpreterStack;
import uvm.refimpl.itpr.InterpreterThread;
import uvm.refimpl.itpr.ValueBox;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.ssavalue.Value;

public class TestMicroVMRefImplRedef {

    private static MicroVM microVM;
    private static MicroVMHelper h;

    private static MicroVMClient client = new DumbClient() {
        public void markExternalRoots(ObjectMarker marker) {
            // No external roots.
            // GC may be triggered when doing too many tests and creating too
            // many stacks.
        };

        @Override
        public void onUndefinedFunction(InterpreterThread thread, Function func) {
            undefinedFunctionHandler.onUndefinedFunction(thread, func);
        };

        @Override
        public Long onTrap(InterpreterThread thread, ValueBox trapValue) {
            return trapHandler.onTrap(thread, trapValue);
        }

    };

    private static interface TrapHandler {
        public Long onTrap(InterpreterThread thread, ValueBox trapValue);
    }

    private static TrapHandler trapHandler;

    private static interface UndefinedFunctionHandler {
        public void onUndefinedFunction(InterpreterThread thread, Function func);
    }

    private static UndefinedFunctionHandler undefinedFunctionHandler;

    private static void setTrapHandler(TrapHandler trapHandler) {
        TestMicroVMRefImplRedef.trapHandler = trapHandler;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        try {
            microVM = new MicroVM();
            microVM.setClient(client);
            h = new MicroVMHelper(microVM);
            loadTextBundle("tests/uvm-refimpl-test/primitives.uir");
            loadTextBundle("tests/uvm-refimpl-test/redef-file1.uir");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void loadTextBundle(String fileName) {
        try {
            Bundle bundle = TestingHelper.parseUir(fileName,
                    microVM.getGlobalBundle());
            microVM.addBundle(bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private ValueBox getValueBox(InterpreterFrame frame, String name) {
        CFG cfg = frame.getCfg();
        Value val = cfg.getInstNs().getByName(name);
        return frame.getValueBox(val);
    }

    @Test
    public void testRedef() throws InterruptedException {
        Function func = h.func("@main");
        InterpreterStack stack = h.makeStack(func);

        trapHandler = new TrapHandler() {
            @Override
            public Long onTrap(InterpreterThread thread, ValueBox trapValueBox) {
                String trapName = thread.getStack().getTop().getCurInst()
                        .getName();
                if (trapName.equals("%checkpoint1")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long curMeaning = ((IntBox) kas.get(0)).getValue()
                            .longValue();
                    System.out.printf("curMeaning is %d\n", curMeaning);
                    assertEquals(42, curMeaning);
                } else if (trapName.equals("%checkpoint2")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long fox = ((IntBox) kas.get(0)).getValue().longValue();
                    System.out.printf("fox is %d\n", fox);
                    assertEquals(99, fox);
                } else if (trapName.equals("%change_meaning")) {
                    System.out.printf("Redefining meaning of life ... \n");
                    loadTextBundle("tests/uvm-refimpl-test/redef-file3.uir");
                } else if (trapName.equals("%checkpoint3")) {
                    List<ValueBox> kas = thread.getStack().getTop()
                            .dumpKeepAlives();
                    long newMeaning = ((IntBox) kas.get(0)).getValue()
                            .longValue();
                    System.out.printf("newMeaning is %d\n", newMeaning);
                    assertEquals(43, newMeaning);
                } else {
                    fail("Who set up this trap?");
                }
                return null;
            }
        };

        undefinedFunctionHandler = new UndefinedFunctionHandler() {
            boolean called = false;

            @Override
            public void onUndefinedFunction(InterpreterThread thread,
                    Function func) {
                if (func.getName().equals("@foxsay")) {
                    if (!called) {
                        called = true;
                    } else {
                        fail("@foxsay should not be redefined twice.");
                    }
                    System.out.printf("Loading %s ...\n",
                            "tests/uvm-refimpl-test/redef-file2.uir");
                    loadTextBundle("tests/uvm-refimpl-test/redef-file2.uir");
                    System.out.println("Loaded.");
                } else {
                    fail("What are you redefining? " + func.getName());
                }
            }
        };

        InterpreterThread thread = microVM.newThread(stack);
        thread.join();
    }
}
