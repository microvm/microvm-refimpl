package uvm.refimpl.itpr;

import java.util.List;
import java.util.Map;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IdentifiedHelper;
import uvm.Namespace;
import uvm.ifunc.IFunc;
import uvm.ifunc.IFuncFactory;
import uvm.platformsupport.MemorySupport;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.Mutator;
import uvm.refimpl.mem.TypeSizes;
import uvm.ssavalue.AbstractCall;
import uvm.ssavalue.AbstractIntrinsicCall;
import uvm.ssavalue.AbstractTrap;
import uvm.ssavalue.AtomicOrdering;
import uvm.ssavalue.Constant;
import uvm.ssavalue.ConvOptr;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.InstAlloca;
import uvm.ssavalue.InstAllocaHybrid;
import uvm.ssavalue.InstAtomicRMW;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCCall;
import uvm.ssavalue.InstCall;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstCmpXchg;
import uvm.ssavalue.InstConversion;
import uvm.ssavalue.InstExtractValue;
import uvm.ssavalue.InstFence;
import uvm.ssavalue.InstGetElemIRef;
import uvm.ssavalue.InstGetFieldIRef;
import uvm.ssavalue.InstGetFixedPartIRef;
import uvm.ssavalue.InstGetIRef;
import uvm.ssavalue.InstGetVarPartIRef;
import uvm.ssavalue.InstICall;
import uvm.ssavalue.InstIInvoke;
import uvm.ssavalue.InstInsertValue;
import uvm.ssavalue.InstInvoke;
import uvm.ssavalue.InstLandingPad;
import uvm.ssavalue.InstLoad;
import uvm.ssavalue.InstNew;
import uvm.ssavalue.InstNewHybrid;
import uvm.ssavalue.InstNewStack;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.InstSelect;
import uvm.ssavalue.InstShiftIRef;
import uvm.ssavalue.InstStore;
import uvm.ssavalue.InstSwitch;
import uvm.ssavalue.InstTailCall;
import uvm.ssavalue.InstThrow;
import uvm.ssavalue.InstTrap;
import uvm.ssavalue.InstWatchPoint;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NonTailCall;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.UseBox;
import uvm.ssavalue.Value;
import uvm.ssavalue.ValueVisitor;
import uvm.type.Func;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Type;
import uvm.type.WeakRef;

public class InterpreterThread implements Runnable {

    private int id;

    public int getID() {
        return id;
    }

    private InterpreterStack stack;

    private boolean running = true;

    private InstructionExecutor executor = new InstructionExecutor();

    private MicroVM microVM;

    private Mutator mutator;

    private Thread vmThread;

    public InterpreterThread(int id, MicroVM microVM, InterpreterStack stack,
            Mutator mutator) {
        this.id = id;
        this.microVM = microVM;
        this.stack = stack;
        this.mutator = mutator;

        vmThread = new Thread(this);
    }

    // Resource getters

    private ConstantPool constantPool() {
        return microVM.getConstantPool();
    }

    private MemorySupport memorySupport() {
        return microVM.getMemorySupport();
    }

    private Namespace<Function> funcSpace() {
        return microVM.getGlobalBundle().getFuncNs();
    }

    private ThreadStackManager threadStackManager() {
        return microVM.getThreadStackManager();
    }

    private TrapManager trapManager() {
        return microVM.getTrapManager();
    }

    // Execution

    @Override
    public void run() {
        while (running) {
            getCurInst().accept(executor);
        }
    }

    // Helper methods

    public InterpreterStack getStack() {
        return stack;
    }

    private Function getCurFunc() {
        return stack.getTop().getFunc();
    }

    private BasicBlock getCurBb() {
        return stack.getTop().getCurBb();
    }

    private int getCurInstIndex() {
        return stack.getTop().getCurInstIndex();
    }

    private Instruction getCurInst() {
        return stack.getTop().getCurInst();
    }

    private void incPC() {
        stack.getTop().incPC();
    }

    @SuppressWarnings("unchecked")
    public <T extends ValueBox> T getValueBox(Value value) {
        if (value instanceof Constant) {
            return (T) constantPool().getValueBox((Constant) value);
        } else {
            return (T) stack.getTop().getValueBox(value);
        }
    }

    private void error(String string) {
        error("Function " + IdentifiedHelper.repr(getCurFunc()) + " BB "
                + IdentifiedHelper.repr(getCurBb()) + " inst "
                + IdentifiedHelper.repr(getCurInst()) + " : " + string);
    }

    private long getInt(Value opnd) {
        return ((IntBox) getValueBox(opnd)).getValue();
    }

    private void setInt(Value opnd, long val) {
        ((IntBox) getValueBox(opnd)).setValue(val);
    }

    private float getFloat(Value opnd) {
        return ((FloatBox) getValueBox(opnd)).getValue();
    }

    private void setFloat(Value opnd, float val) {
        ((FloatBox) getValueBox(opnd)).setValue(val);
    }

    private double getDouble(Value opnd) {
        return ((DoubleBox) getValueBox(opnd)).getValue();
    }

    private void setDouble(Value opnd, double val) {
        ((DoubleBox) getValueBox(opnd)).setValue(val);
    }

    private long getRefAddr(Value opnd) {
        return ((RefBox) getValueBox(opnd)).getAddr();
    }

    private void setRef(Value opnd, long addr) {
        ((RefBox) getValueBox(opnd)).setAddr(addr);
    }

    private long getIRefAddr(Value opnd) {
        return ((IRefBox) getValueBox(opnd)).getAddr();
    }

    private void setIRef(Value opnd, long base, long offset) {
        IRefBox box = getValueBox(opnd);
        box.setBase(base);
        box.setOffset(offset);
    }

    private Function getFunc(Value opnd) {
        return ((FuncBox) getValueBox(opnd)).getFunc();
    }

    private void setFunc(Value opnd, Function func) {
        ((FuncBox) getValueBox(opnd)).setFunc(func);
    }

    private InterpreterThread getThread(Value opnd) {
        return ((ThreadBox) getValueBox(opnd)).getThread();
    }

    private void setThread(Value opnd, InterpreterThread thr) {
        ((ThreadBox) getValueBox(opnd)).setThread(thr);
    }

    private InterpreterStack getStack(Value opnd) {
        return ((StackBox) getValueBox(opnd)).getStack();
    }

    private void setStack(Value opnd, InterpreterStack sta) {
        ((StackBox) getValueBox(opnd)).setStack(sta);
    }

    private static long pu(long n, long l) {
        return OpHelper.prepareUnsigned(n, l);
    }

    private static long ps(long n, long l) {
        return OpHelper.prepareSigned(n, l);
    }

    private static long up(long n, long l) {
        return OpHelper.unprepare(n, l);
    }

    private void branchAndMovePC(BasicBlock dest) {
        branchAndMovePC(dest, 0);
    }

    private void branchAndMovePC(BasicBlock dest, long excAddr) {
        BasicBlock curBb = getCurBb();
        int i = 0;
        while (true) {
            Instruction inst = dest.getInsts().get(i);
            if (inst instanceof InstPhi) {
                InstPhi phi = (InstPhi) inst;
                Value val = phi.getValueFrom(curBb);
                ValueBox vb = getValueBox(val);
                ValueBox db = getValueBox(inst);
                db.copyValue(vb);
                i++;
            } else if (inst instanceof InstLandingPad) {
                RefBox db = getValueBox(inst);
                db.setAddr(excAddr);
                i++;
            } else {
                break;
            }
        }
        jump(dest, i);
    }

    private void jump(BasicBlock bb, int ix) {
        stack.getTop().jump(bb, ix);
    }

    private void resolvePotentiallyUndefinedFunction(Function func) {
        while (running) {
            if (!func.isDefined()) {
                trapManager().getUndefinedFunctionHandler()
                        .onUndefinedFunction(InterpreterThread.this, func);
            } else {
                break;
            }
        }
    }

    private void unwindStack(long excAddr) {
        for (InterpreterFrame frame = stack.getTop().getPrevFrame(); frame != null; frame = frame
                .getPrevFrame()) {
            Instruction callerInst = frame.getCurInst();
            if (callerInst instanceof InstInvoke) {
                branchAndMovePC(((InstInvoke) callerInst).getExc(), excAddr);
                return;
            }
        }
        // TODO: Also rewind stack memory.

        error("Thrown unwinded the stack below the last frame.");
    }

    private void swapStack(InterpreterStack newStack) {
        this.stack = newStack;
    }

    private class InstructionExecutor implements ValueVisitor<Void> {

        @Override
        public Void visitIntConstant(IntConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitFloatConstant(FloatConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitDoubleConstant(DoubleConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitStructConstant(StructConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitNullConstant(NullConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitGlobalDataConstant(GlobalDataConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitFunctionConstant(FunctionConstant constant) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitParameter(Parameter parameter) {
            // Not Value. Not used.
            return null;
        }

        @Override
        public Void visitBinOp(InstBinOp inst) {
            Type type = inst.getType();
            if (type instanceof Int) {
                Int t = (Int) type;
                long l = t.getSize();
                long v1 = getInt(inst.getOp1());
                long v2 = getInt(inst.getOp2());
                long rv = 0;

                switch (inst.getOptr()) {
                case ADD:
                    rv = up(pu(v1, l) + pu(v2, l), l);
                    break;
                case SUB:
                    rv = up(pu(v1, l) - pu(v2, l), l);
                    break;
                case MUL:
                    rv = up(pu(v1, l) * pu(v2, l), l);
                    break;
                case UDIV:
                    rv = up(pu(v1, l) / pu(v2, l), l);
                    break;
                case SDIV:
                    rv = up(ps(v1, l) / ps(v2, l), l);
                    break;
                case UREM:
                    rv = up(pu(v1, l) % pu(v2, l), l);
                    break;
                case SREM:
                    rv = up(ps(v1, l) % ps(v2, l), l);
                    break;
                case SHL:
                    rv = up(pu(v1, l) << pu(v2, l), l);
                    break;
                case LSHR:
                    rv = up(pu(v1, l) >>> pu(v2, l), l);
                    break;
                case ASHR:
                    rv = up(ps(v1, l) >> pu(v2, l), l);
                    break;
                case AND:
                    rv = up(pu(v1, l) & pu(v2, l), l);
                    break;
                case OR:
                    rv = up(pu(v1, l) | pu(v2, l), l);
                    break;
                case XOR:
                    rv = up(pu(v1, l) ^ pu(v2, l), l);
                    break;
                default:
                    error("Unexpected op for int binop "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv);
            } else if (type instanceof uvm.type.Float) {
                float v1 = getFloat(inst.getOp1());
                float v2 = getFloat(inst.getOp2());
                float rv = 0;

                switch (inst.getOptr()) {
                case FADD:
                    rv = v1 + v2;
                    break;
                case FSUB:
                    rv = v1 - v2;
                    break;
                case FMUL:
                    rv = v1 * v2;
                    break;
                case FDIV:
                    rv = v1 / v2;
                    break;
                case FREM:
                    rv = v1 % v2;
                    break;
                default:
                    error("Unexpected op for float binop "
                            + inst.getOptr().toString());
                }

                setFloat(inst, rv);
            } else if (type instanceof uvm.type.Double) {
                double v1 = getDouble(inst.getOp1());
                double v2 = getDouble(inst.getOp2());
                double rv = 0;

                switch (inst.getOptr()) {
                case FADD:
                    rv = v1 + v2;
                    break;
                case FSUB:
                    rv = v1 - v2;
                    break;
                case FMUL:
                    rv = v1 * v2;
                    break;
                case FDIV:
                    rv = v1 / v2;
                    break;
                case FREM:
                    rv = v1 % v2;
                    break;
                default:
                    error("Unexpected op for double binop "
                            + inst.getOptr().toString());
                }

                setDouble(inst, rv);
            } else {
                error("Bad type for binary operation: "
                        + type.getClass().getName());
            }
            incPC();
            return null;
        }

        @Override
        public Void visitCmp(InstCmp inst) {
            Type type = inst.getOpndType();
            if (type instanceof Int) {
                Int t = (Int) type;
                long l = t.getSize();
                long v1 = getInt(inst.getOp1());
                long v2 = getInt(inst.getOp2());
                boolean rv = false;

                switch (inst.getOptr()) {
                case EQ:
                    rv = pu(v1, l) == pu(v2, l);
                    break;
                case NE:
                    rv = pu(v1, l) != pu(v2, l);
                    break;
                case ULT:
                    rv = pu(v1, l) < pu(v2, l);
                    break;
                case ULE:
                    rv = pu(v1, l) <= pu(v2, l);
                    break;
                case UGT:
                    rv = pu(v1, l) > pu(v2, l);
                    break;
                case UGE:
                    rv = pu(v1, l) >= pu(v2, l);
                    break;
                case SLT:
                    rv = ps(v1, l) < ps(v2, l);
                    break;
                case SLE:
                    rv = ps(v1, l) <= ps(v2, l);
                    break;
                case SGT:
                    rv = ps(v1, l) > ps(v2, l);
                    break;
                case SGE:
                    rv = ps(v1, l) >= ps(v2, l);
                    break;
                default:
                    error("Unexpected op for int cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else if (type instanceof uvm.type.Float) {
                float v1 = getFloat(inst.getOp1());
                float v2 = getFloat(inst.getOp2());
                boolean rv = false;

                switch (inst.getOptr()) {
                case FTRUE:
                    rv = true;
                    break;
                case FFALSE:
                    rv = false;
                    break;
                case FUNO:
                    rv = Float.isNaN(v1) || Float.isNaN(v2);
                    break;
                case FUEQ:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 == v2;
                    break;
                case FUNE:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 != v2;
                    break;
                case FULT:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 < v2;
                    break;
                case FULE:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 <= v2;
                    break;
                case FUGT:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 > v2;
                    break;
                case FUGE:
                    rv = Float.isNaN(v1) || Float.isNaN(v2) || v1 >= v2;
                    break;
                case FORD:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2);
                    break;
                case FOEQ:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 == v2;
                    break;
                case FONE:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 != v2;
                    break;
                case FOLT:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 < v2;
                    break;
                case FOLE:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 <= v2;
                    break;
                case FOGT:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 > v2;
                    break;
                case FOGE:
                    rv = !Float.isNaN(v1) && !Float.isNaN(v2) && v1 >= v2;
                    break;
                default:
                    error("Unexpected op for float cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else if (type instanceof uvm.type.Double) {
                double v1 = getDouble(inst.getOp1());
                double v2 = getDouble(inst.getOp2());
                boolean rv = false;

                switch (inst.getOptr()) {
                case FTRUE:
                    rv = true;
                    break;
                case FFALSE:
                    rv = false;
                    break;
                case FUNO:
                    rv = Double.isNaN(v1) || Double.isNaN(v2);
                    break;
                case FUEQ:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 == v2;
                    break;
                case FUNE:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 != v2;
                    break;
                case FULT:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 < v2;
                    break;
                case FULE:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 <= v2;
                    break;
                case FUGT:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 > v2;
                    break;
                case FUGE:
                    rv = Double.isNaN(v1) || Double.isNaN(v2) || v1 >= v2;
                    break;
                case FORD:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2);
                    break;
                case FOEQ:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 == v2;
                    break;
                case FONE:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 != v2;
                    break;
                case FOLT:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 < v2;
                    break;
                case FOLE:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 <= v2;
                    break;
                case FOGT:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 > v2;
                    break;
                case FOGE:
                    rv = !Double.isNaN(v1) && !Double.isNaN(v2) && v1 >= v2;
                    break;
                default:
                    error("Unexpected op for double cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else if (type instanceof Ref) {
                long v1 = getRefAddr(inst.getOp1());
                long v2 = getRefAddr(inst.getOp2());
                boolean rv = false;

                switch (inst.getOptr()) {
                case EQ:
                    rv = v1 == v2;
                    break;
                case NE:
                    rv = v1 != v2;
                    break;
                default:
                    error("Unexpected op for Ref cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else if (type instanceof IRef) {
                long v1 = getIRefAddr(inst.getOp1());
                long v2 = getIRefAddr(inst.getOp2());
                boolean rv = false;

                switch (inst.getOptr()) {
                case EQ:
                    rv = v1 == v2;
                    break;
                case NE:
                    rv = v1 != v2;
                    break;
                default:
                    error("Unexpected op for IRef cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else if (type instanceof Func || type instanceof uvm.type.Thread
                    || type instanceof uvm.type.Stack) {
                WrapperBox<?> op1 = getValueBox(inst.getOp1());
                WrapperBox<?> op2 = getValueBox(inst.getOp2());

                Object v1 = op1.getObject();
                Object v2 = op2.getObject();
                boolean rv = false;

                switch (inst.getOptr()) {
                case EQ:
                    rv = v1 == v2;
                    break;
                case NE:
                    rv = v1 != v2;
                    break;
                default:
                    error("Unexpected op for Func/Thread/Stack cmp "
                            + inst.getOptr().toString());
                }

                setInt(inst, rv ? 1 : 0);
            } else {
                error("Bad type for comparison: " + type.getClass().getName());
            }

            incPC();
            return null;

        }

        @Override
        public Void visitConversion(InstConversion inst) {
            Type ft = inst.getFromType();
            Type tt = inst.getToType();
            Value opnd = inst.getOpnd();
            switch (inst.getOptr()) {
            case TRUNC: {
                long tl = ((Int) tt).getSize();
                long od = getInt(opnd);
                long rv = OpHelper.trunc(od, tl);
                setInt(inst, rv);
                break;
            }
            case ZEXT: {
                long fl = ((Int) ft).getSize();
                long tl = ((Int) tt).getSize();
                long od = getInt(opnd);
                long rv = OpHelper.zext(od, fl, tl);
                setInt(inst, rv);
                break;
            }
            case SEXT: {
                long fl = ((Int) ft).getSize();
                long tl = ((Int) tt).getSize();
                long od = getInt(opnd);
                long rv = OpHelper.sext(od, fl, tl);
                setInt(inst, rv);
                break;
            }
            case FPTRUNC: {
                double od = getDouble(opnd);
                float rv = (float) od;
                setFloat(inst, rv);
                break;
            }
            case FPEXT: {
                float od = getFloat(opnd);
                double rv = (double) od;
                setDouble(inst, rv);
                break;
            }
            case FPTOUI:
            case FPTOSI: {
                long tl = ((Int) tt).getSize();
                long rv;
                if (ft instanceof uvm.type.Float) {
                    rv = (long) getFloat(opnd);
                } else if (ft instanceof uvm.type.Double) {
                    rv = (long) getDouble(opnd);
                } else {
                    error("Bad type for FPTOxI: " + IdentifiedHelper.repr(ft));
                    return null;
                }
                rv = OpHelper.truncFromLong(rv, tl);
                setInt(inst, rv);
                break;
            }

            case UITOFP:
            case SITOFP: {
                long fl = ((Int) ft).getSize();
                long fv = getInt(opnd);

                if (inst.getOptr() == ConvOptr.UITOFP) {
                    fv = pu(fv, fl);
                } else {
                    fv = ps(fv, fl);
                }

                if (tt instanceof uvm.type.Float) {
                    float rv = (float) fv;
                    setFloat(inst, rv);
                } else if (tt instanceof uvm.type.Double) {
                    double rv = (double) fv;
                    setDouble(inst, rv);
                } else {
                    error("Bad type for xITOFP: " + IdentifiedHelper.repr(tt));
                    return null;
                }
                break;
            }
            case BITCAST:
                if (ft instanceof Int && ((Int) ft).getSize() == 32
                        && tt instanceof uvm.type.Float) {
                    int fv = (int) pu(getInt(opnd), 32);
                    float rv = Float.intBitsToFloat(fv);
                    setFloat(inst, rv);
                } else if (ft instanceof Int && ((Int) ft).getSize() == 64
                        && tt instanceof uvm.type.Double) {
                    long fv = getInt(opnd);
                    double rv = Double.longBitsToDouble(fv);
                    setDouble(inst, rv);
                } else if (ft instanceof uvm.type.Float && tt instanceof Int
                        && ((Int) tt).getSize() == 32) {
                    float fv = getFloat(opnd);
                    long rv = (long) (Float.floatToRawIntBits(fv));
                    setInt(inst, rv);
                } else if (ft instanceof uvm.type.Double && tt instanceof Int
                        && ((Int) tt).getSize() == 64) {
                    double fv = getDouble(opnd);
                    long rv = Double.doubleToRawLongBits(fv);
                    setInt(inst, rv);
                } else {
                    error("Bad type for BITCAST: " + IdentifiedHelper.repr(ft)
                            + " and " + IdentifiedHelper.repr(tt));
                    return null;
                }
            case REFCAST:
            case IREFCAST:
            case FUNCCAST: {
                ValueBox fb = getValueBox(opnd);
                ValueBox rb = getValueBox(inst);
                rb.copyValue(fb);
                break;
            }
            default:
                error("Unknown conversion operator "
                        + inst.getOptr().toString());
                return null;
            }

            incPC();
            return null;
        }

        @Override
        public Void visitSelect(InstSelect inst) {
            long cond = getInt(inst.getCond());
            ValueBox rb = getValueBox(inst);
            if (cond == 1) {
                rb.copyValue(getValueBox(inst.getIfTrue()));
            } else {
                rb.copyValue(getValueBox(inst.getIfFalse()));
            }

            incPC();
            return null;
        }

        @Override
        public Void visitBranch(InstBranch inst) {
            branchAndMovePC(inst.getDest());
            return null;
        }

        @Override
        public Void visitBranch2(InstBranch2 inst) {
            long cond = getInt(inst.getCond());
            if (cond == 1) {
                branchAndMovePC(inst.getIfTrue());
            } else {
                branchAndMovePC(inst.getIfFalse());
            }
            return null;
        }

        @Override
        public Void visitSwitch(InstSwitch inst) {
            Type type = inst.getOpndType();
            if (type instanceof Int) {
                long length = ((Int) type).getSize();
                long opnd = OpHelper.trunc(getInt(inst.getOpnd()), length);
                for (Map.Entry<UseBox, BasicBlock> e : inst.getCases()
                        .entrySet()) {
                    Value caseVal = e.getKey().getDst();
                    long c = OpHelper.trunc(getInt(caseVal), length);
                    if (opnd == c) {
                        branchAndMovePC(e.getValue());
                        return null;
                    }
                }
                branchAndMovePC(inst.getDefaultDest());
            } else {
                error("Unsupported type for SELECT: "
                        + type.getClass().getName());
            }

            return null;
        }

        @Override
        public Void visitPhi(InstPhi inst) {
            error("Phi is unreachable by direct execution");
            return null;
        }

        private void visitAbstractCall(AbstractCall inst,
                InterpreterFrame prevFrame) {
            Function callee = getFunc(inst.getFunc());

            resolvePotentiallyUndefinedFunction(callee);

            if (!running) {
                return;
            }

            InterpreterFrame newFrame = new InterpreterFrame(callee, prevFrame);
            stack.setTop(newFrame);

            List<UseBox> args = inst.getArgs();
            List<Parameter> params = callee.getCFG().getParams();
            for (int i = 0; i < args.size(); i++) {
                ValueBox argBox = getValueBox(args.get(i).getDst());
                ValueBox paramBox = newFrame.getValueBox(params.get(i));
                paramBox.copyValue(argBox);
            }
        }

        private void visitNonTailCall(NonTailCall inst) {
            visitAbstractCall(inst, stack.getTop());
        }

        @Override
        public Void visitCall(InstCall inst) {
            visitNonTailCall(inst);
            return null;
        }

        @Override
        public Void visitInvoke(InstInvoke inst) {
            visitNonTailCall(inst);
            return null;
        }

        @Override
        public Void visitTailCall(InstTailCall inst) {
            visitAbstractCall(inst, stack.getTop().getPrevFrame());
            return null;
        }

        private void genericReturn(ValueBox rvb) {
            stack.setTop(stack.getTop().getPrevFrame());

            Instruction instCont = getCurInst();

            if (rvb != null) {
                ValueBox instBox = getValueBox(instCont);
                instBox.copyValue(rvb);
            }

            if (instCont instanceof InstCall) {
                incPC();
            } else if (instCont instanceof InstInvoke) {
                branchAndMovePC(((InstInvoke) instCont).getNor());
            }

            // TODO: also rewind stack memory.
        }

        @Override
        public Void visitRet(InstRet inst) {
            ValueBox rvb = getValueBox(inst.getRetVal());
            genericReturn(rvb);
            return null;
        }

        @Override
        public Void visitRetVoid(InstRetVoid inst) {
            genericReturn(null);
            return null;
        }

        @Override
        public Void visitThrow(InstThrow inst) {
            RefBox excBox = getValueBox(inst.getException());
            long excAddr = excBox.getAddr();

            unwindStack(excAddr);
            return null;
        }

        @Override
        public Void visitLandingPad(InstLandingPad inst) {
            error("LANDINGPAD is unreachable in normal flow.");
            return null;
        }

        @Override
        public Void visitExtractValue(InstExtractValue inst) {
            int index = inst.getIndex();
            StructBox opndBox = getValueBox(inst.getOpnd());
            ValueBox instBox = getValueBox(inst);
            instBox.copyValue(opndBox.getBox(index));
            incPC();
            return null;
        }

        @Override
        public Void visitInsertValue(InstInsertValue inst) {
            int index = inst.getIndex();
            StructBox opndBox = getValueBox(inst.getOpnd());
            StructBox instBox = getValueBox(inst);
            ValueBox newValBox = getValueBox(inst.getNewVal());
            int nFields = inst.getStructType().getFieldTypes().size();
            for (int i = 0; i < nFields; i++) {
                if (i != index) {
                    instBox.getBox(i).copyValue(opndBox.getBox(i));
                } else {
                    instBox.getBox(i).copyValue(newValBox);
                }
            }
            incPC();
            return null;
        }

        @Override
        public Void visitNew(InstNew inst) {
            Type type = inst.getAllocType();
            long addr = mutator.newScalar(type);
            setRef(inst, addr);
            incPC();
            return null;
        }

        @Override
        public Void visitNewHybrid(InstNewHybrid inst) {
            Type type = inst.getAllocType();
            long len = getInt(inst.getLength());
            long addr = mutator.newHybrid(type, len);
            setRef(inst, addr);
            incPC();
            return null;
        }

        @Override
        public Void visitAlloca(InstAlloca inst) {
            Type type = inst.getAllocType();
            long addr = stack.getStackMemory().allocaScalar(type);
            setIRef(inst, 0, addr);
            incPC();
            return null;
        }

        @Override
        public Void visitAllocaHybrid(InstAllocaHybrid inst) {
            Type type = inst.getAllocType();
            long len = getInt(inst.getLength());
            long addr = stack.getStackMemory().allocaHybrid(type, len);
            setIRef(inst, 0, addr);
            incPC();
            return null;
        }

        @Override
        public Void visitGetIRef(InstGetIRef inst) {
            long addr = getRefAddr(inst.getOpnd());
            setIRef(inst, addr, 0);
            incPC();
            return null;
        }

        @Override
        public Void visitGetFieldIRef(InstGetFieldIRef inst) {
            IRefBox oldIRef = getValueBox(inst.getOpnd());
            long oldBase = oldIRef.getBase();
            long oldOffset = oldIRef.getOffset();
            long fieldOffset = TypeSizes.fieldOffsetOf(inst.getReferentType(),
                    inst.getIndex());
            long newOffset = oldOffset + fieldOffset;
            setIRef(inst, oldBase, newOffset);
            incPC();
            return null;
        }

        @Override
        public Void visitGetElemIRef(InstGetElemIRef inst) {
            IRefBox oldIRef = getValueBox(inst.getOpnd());
            long oldBase = oldIRef.getBase();
            long oldOffset = oldIRef.getOffset();

            long index = getInt(inst.getIndex());
            long fieldOffset = TypeSizes.shiftOffsetOf(inst.getReferentType()
                    .getElemType(), index);
            long newOffset = oldOffset + fieldOffset;
            setIRef(inst, oldBase, newOffset);
            incPC();
            return null;
        }

        @Override
        public Void visitShiftIRef(InstShiftIRef inst) {
            IRefBox oldIRef = getValueBox(inst.getOpnd());
            long oldBase = oldIRef.getBase();
            long oldOffset = oldIRef.getOffset();

            long index = getInt(inst.getOffset());
            long fieldOffset = TypeSizes.shiftOffsetOf(inst.getReferentType(),
                    index);
            long newOffset = oldOffset + fieldOffset;
            setIRef(inst, oldBase, newOffset);
            incPC();
            return null;
        }

        @Override
        public Void visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
            getValueBox(inst).copyValue(getValueBox(inst.getOpnd()));
            incPC();
            return null;
        }

        @Override
        public Void visitGetVarPartIRef(InstGetVarPartIRef inst) {
            IRefBox oldIRef = getValueBox(inst.getOpnd());
            long oldBase = oldIRef.getBase();
            long oldOffset = oldIRef.getOffset();

            long fixedSize = TypeSizes.sizeOf(inst.getReferentType()
                    .getFixedPart());
            long varOffset = TypeSizes.alignUp(fixedSize,
                    TypeSizes.alignOf(inst.getReferentType().getVarPart()));

            long newOffset = oldOffset + varOffset;
            setIRef(inst, oldBase, newOffset);
            incPC();
            return null;
        }

        @Override
        public Void visitLoad(InstLoad inst) {
            long loc = getIRefAddr(inst.getLocation());
            boolean isAtomic = inst.getOrdering() != AtomicOrdering.NOT_ATOMIC;
            Type rt = inst.getReferentType();
            if (rt instanceof Int) {
                Int irt = (Int) rt;
                long loadSize = TypeSizes.nextPowOfTwo(irt.getSize());
                long val;
                if (loadSize == 8) {
                    val = isAtomic ? memorySupport().loadByteAtomic(loc)
                            : memorySupport().loadByte(loc);
                } else if (loadSize == 16) {
                    val = isAtomic ? memorySupport().loadShortAtomic(loc)
                            : memorySupport().loadShort(loc);
                } else if (loadSize == 32) {
                    val = isAtomic ? memorySupport().loadIntAtomic(loc)
                            : memorySupport().loadInt(loc);
                } else if (loadSize == 64) {
                    val = isAtomic ? memorySupport().loadLongAtomic(loc)
                            : memorySupport().loadLong(loc);
                } else {
                    error("Unsupported Int length for load: " + loadSize);
                    return null;
                }
                setInt(inst, val);
            } else if (rt instanceof uvm.type.Float) {
                float val = isAtomic ? memorySupport().loadFloatAtomic(loc)
                        : memorySupport().loadFloat(loc);
                setFloat(inst, val);
            } else if (rt instanceof uvm.type.Double) {
                double val = isAtomic ? memorySupport().loadDoubleAtomic(loc)
                        : memorySupport().loadDouble(loc);
                setDouble(inst, val);
            } else if (rt instanceof Ref || rt instanceof WeakRef) {
                // Java never reads reference partially. Should it always be
                // atomic?
                long addr = isAtomic ? memorySupport().loadLongAtomic(loc)
                        : memorySupport().loadLong(loc);
                setRef(inst, addr);
            } else if (rt instanceof IRef) {
                if (isAtomic) {
                    error("This implementation does not support loading IRef atomically.");
                } else {
                    long base = memorySupport().loadLong(loc);
                    long offset = memorySupport().loadLong(loc + 8);
                    setIRef(inst, base, offset);
                }
            } else if (rt instanceof Func) {
                long id = isAtomic ? memorySupport().loadLongAtomic(loc)
                        : memorySupport().loadLong(loc);
                Function func = funcSpace().getByID((int) id);
                setFunc(inst, func);
            } else if (rt instanceof uvm.type.Thread) {
                long id = isAtomic ? memorySupport().loadLongAtomic(loc)
                        : memorySupport().loadLong(loc);
                InterpreterThread thr = threadStackManager().getThreadByID(
                        (int) id);
                setThread(inst, thr);
            } else if (rt instanceof uvm.type.Stack) {
                long id = isAtomic ? memorySupport().loadLongAtomic(loc)
                        : memorySupport().loadLong(loc);
                InterpreterStack sta = threadStackManager().getStackByID(
                        (int) id);
                setStack(inst, sta);
            } else {
                error("Unsupported type to load: " + rt.getClass().getName());
            }

            incPC();
            return null;
        }

        @Override
        public Void visitStore(InstStore inst) {
            long loc = getIRefAddr(inst.getLocation());
            boolean isAtomic = inst.getOrdering() != AtomicOrdering.NOT_ATOMIC;
            Type rt = inst.getReferentType();
            if (rt instanceof Int) {
                Int irt = (Int) rt;
                long loadSize = TypeSizes.nextPowOfTwo(irt.getSize());
                long val = getInt(inst.getNewVal());
                if (loadSize == 8) {
                    if (isAtomic) {
                        memorySupport().storeByteAtomic(loc, (byte) val);
                    } else {
                        memorySupport().storeByte(loc, (byte) val);
                    }
                } else if (loadSize == 16) {
                    if (isAtomic) {
                        memorySupport().storeShortAtomic(loc, (short) val);
                    } else {
                        memorySupport().storeShort(loc, (short) val);
                    }
                } else if (loadSize == 32) {
                    if (isAtomic) {
                        memorySupport().storeIntAtomic(loc, (int) val);
                    } else {
                        memorySupport().storeInt(loc, (int) val);
                    }
                } else if (loadSize == 64) {
                    if (isAtomic) {
                        memorySupport().storeLongAtomic(loc, val);
                    } else {
                        memorySupport().storeLong(loc, val);
                    }
                } else {
                    error("Unsupported Int length for store: " + loadSize);
                    return null;
                }
            } else if (rt instanceof uvm.type.Float) {
                float val = getFloat(inst.getNewVal());
                if (isAtomic) {
                    memorySupport().storeFloatAtomic(loc, val);
                } else {
                    memorySupport().storeFloat(loc, val);
                }
            } else if (rt instanceof uvm.type.Double) {
                double val = getDouble(inst.getNewVal());
                if (isAtomic) {
                    memorySupport().storeDoubleAtomic(loc, val);
                } else {
                    memorySupport().storeDouble(loc, val);
                }
            } else if (rt instanceof Ref || rt instanceof WeakRef) {
                // Java never reads reference partially. Should it always be
                // atomic?
                long addr = getRefAddr(inst.getNewVal());
                if (isAtomic) {
                    memorySupport().storeLongAtomic(loc, addr);
                } else {
                    memorySupport().storeLong(loc, addr);
                }
            } else if (rt instanceof IRef) {
                if (isAtomic) {
                    error("This implementation does not support storing IRef atomically.");
                } else {
                    IRefBox irb = getValueBox(inst.getNewVal());
                    long base = irb.getBase();
                    long offset = irb.getOffset();
                    memorySupport().storeLong(loc, base);
                    memorySupport().storeLong(loc + 8, offset);
                }
            } else if (rt instanceof Func) {
                Function func = getFunc(inst.getNewVal());

                long id = func.getID();
                if (isAtomic) {
                    memorySupport().storeLongAtomic(loc, id);
                } else {
                    memorySupport().storeLong(loc, id);
                }
            } else if (rt instanceof uvm.type.Thread) {
                InterpreterThread thr = getThread(inst.getNewVal());
                long id = thr.getID();
                if (isAtomic) {
                    memorySupport().storeLongAtomic(loc, id);
                } else {
                    memorySupport().storeLong(loc, id);
                }
            } else if (rt instanceof uvm.type.Stack) {
                InterpreterStack sta = getStack(inst.getNewVal());
                long id = sta.getID();
                if (isAtomic) {
                    memorySupport().storeLongAtomic(loc, id);
                } else {
                    memorySupport().storeLong(loc, id);
                }
            } else {
                error("Unsupported type to store: " + rt.getClass().getName());
            }

            incPC();
            return null;
        }

        @Override
        public Void visitCmpXchg(InstCmpXchg inst) {
            long loc = getIRefAddr(inst.getLocation());
            Type rt = inst.getReferentType();
            if (rt instanceof Int) {
                Int irt = (Int) rt;
                long loadSize = TypeSizes.nextPowOfTwo(irt.getSize());
                long expected = getInt(inst.getExpected());
                long desired = getInt(inst.getDesired());
                long oldVal;
                if (loadSize == 32) {
                    oldVal = memorySupport().cmpXchgInt(loc, (int) expected,
                            (int) desired);
                } else if (loadSize == 64) {
                    oldVal = memorySupport()
                            .cmpXchgLong(loc, expected, desired);
                } else {
                    error("Unsupported Int length for cmpxchg: " + loadSize);
                    return null;
                }
                setInt(inst, oldVal);
            } else if (rt instanceof Ref || rt instanceof WeakRef) {
                long expected = getRefAddr(inst.getExpected());
                long desired = getRefAddr(inst.getDesired());
                long oldAddr = memorySupport().cmpXchgLong(loc, expected,
                        desired);
                setRef(inst, oldAddr);
            } else {
                error("Unsupported type to cmpxchg: " + rt.getClass().getName());
            }

            incPC();
            return null;
        }

        @Override
        public Void visitAtomicRMW(InstAtomicRMW inst) {
            long loc = getIRefAddr(inst.getLocation());
            Type rt = inst.getReferentType();
            if (rt instanceof Int) {
                Int irt = (Int) rt;
                long loadSize = TypeSizes.nextPowOfTwo(irt.getSize());
                long opnd = getInt(inst.getOpnd());
                long oldVal = 0;
                if (loadSize == 32) {
                    switch (inst.getOptr()) {
                    case XCHG:
                        oldVal = memorySupport().fetchXchgInt(loc, (int) opnd);
                        break;
                    case ADD:
                        oldVal = memorySupport().fetchAddInt(loc, (int) opnd);
                        break;
                    case SUB:
                        oldVal = memorySupport().fetchSubInt(loc, (int) opnd);
                        break;
                    case AND:
                        oldVal = memorySupport().fetchAndInt(loc, (int) opnd);
                        break;
                    case NAND:
                        oldVal = memorySupport().fetchNandInt(loc, (int) opnd);
                        break;
                    case OR:
                        oldVal = memorySupport().fetchOrInt(loc, (int) opnd);
                        break;
                    case XOR:
                        oldVal = memorySupport().fetchXorInt(loc, (int) opnd);
                        break;
                    case MAX:
                        oldVal = memorySupport().fetchMaxInt(loc, (int) opnd);
                        break;
                    case MIN:
                        oldVal = memorySupport().fetchMinInt(loc, (int) opnd);
                        break;
                    case UMAX:
                        oldVal = memorySupport().fetchUmaxInt(loc, (int) opnd);
                        break;
                    case UMIN:
                        oldVal = memorySupport().fetchUminInt(loc, (int) opnd);
                        break;
                    default:
                        error("Unrecognised atomicrmw op: " + inst.getOptr());
                    }
                } else if (loadSize == 64) {
                    switch (inst.getOptr()) {
                    case XCHG:
                        oldVal = memorySupport().fetchXchgLong(loc, opnd);
                        break;
                    case ADD:
                        oldVal = memorySupport().fetchAddLong(loc, opnd);
                        break;
                    case SUB:
                        oldVal = memorySupport().fetchSubLong(loc, opnd);
                        break;
                    case AND:
                        oldVal = memorySupport().fetchAndLong(loc, opnd);
                        break;
                    case NAND:
                        oldVal = memorySupport().fetchNandLong(loc, opnd);
                        break;
                    case OR:
                        oldVal = memorySupport().fetchOrLong(loc, opnd);
                        break;
                    case XOR:
                        oldVal = memorySupport().fetchXorLong(loc, opnd);
                        break;
                    case MAX:
                        oldVal = memorySupport().fetchMaxLong(loc, opnd);
                        break;
                    case MIN:
                        oldVal = memorySupport().fetchMinLong(loc, opnd);
                        break;
                    case UMAX:
                        oldVal = memorySupport().fetchUmaxLong(loc, opnd);
                        break;
                    case UMIN:
                        oldVal = memorySupport().fetchUminLong(loc, opnd);
                        break;
                    default:
                        error("Unrecognised atomicrmw op: " + inst.getOptr());
                    }
                } else {
                    error("Unsupported Int length for atomicrmw: " + loadSize);
                    return null;
                }
                setInt(inst, oldVal);
            } else {
                error("Unsupported type to atomicrmw: "
                        + rt.getClass().getName());
            }

            incPC();
            return null;
        }

        @Override
        public Void visitFence(InstFence inst) {
            memorySupport().fence();
            incPC();
            return null;
        }

        private void visitAbstractTrap(AbstractTrap inst) {
            Long excAddr = trapManager().getTrapHandler().onTrap(
                    InterpreterThread.this);
            if (excAddr == null) {
                branchAndMovePC(inst.getNor());
            } else {
                branchAndMovePC(inst.getExc(), excAddr);
            }
        }

        @Override
        public Void visitTrap(InstTrap inst) {
            visitAbstractTrap(inst);
            return null;
        }

        @Override
        public Void visitWatchPoint(InstWatchPoint inst) {
            boolean isEnabled = trapManager().isWatchpointEnabled(
                    inst.getWatchPointId());
            if (isEnabled) {
                visitAbstractTrap(inst);
            } else {
                branchAndMovePC(inst.getDisabled());
            }
            return null;
        }

        @Override
        public Void visitCCall(InstCCall inst) {
            error("Not implemented in the Java implementation.");
            return null;
        }

        @Override
        public Void visitNewStack(InstNewStack inst) {

            Function func = getFunc(inst.getFunc());

            resolvePotentiallyUndefinedFunction(func);

            if (!running) {
                return null;
            }

            InterpreterStack sta = microVM.newStack(func);

            StackBox instBox = getValueBox(inst);
            instBox.setStack(sta);

            return null;
        }

        @Override
        public Void visitICall(InstICall inst) {
            Long excAddr = handleIntrinsicCall(inst);
            if (!running) {
                return null;
            }
            if (excAddr == null) {
                incPC();
            } else {
                unwindStack(excAddr);
            }
            return null;
        }

        @Override
        public Void visitIInvoke(InstIInvoke inst) {
            Long excAddr = handleIntrinsicCall(inst);
            if (!running) {
                return null;
            }
            if (excAddr == null) {
                branchAndMovePC(inst.getNor());
            } else {
                branchAndMovePC(inst.getExc(), excAddr);
            }
            return null;
        }
    }

    private Long handleIntrinsicCall(AbstractIntrinsicCall inst) {
        IFunc ifunc = inst.getIFunc();
        List<UseBox> args = inst.getArgs();
        switch (ifunc.getID()) {
        case IFuncFactory.IFUNC__UVM__NEW_THREAD: {
            InterpreterStack sta = getStack(args.get(0).getDst());
            InterpreterThread thr = microVM.newThread(sta);
            setThread(inst, thr);
            break;
        }
        case IFuncFactory.IFUNC__UVM__SWAP_STACK: {
            InterpreterStack sta = getStack(args.get(0).getDst());
            swapStack(sta);
            break;
        }
        case IFuncFactory.IFUNC__UVM__KILL_STACK: {
            InterpreterStack sta = getStack(args.get(0).getDst());
            sta.kill();
            break;
        }
        case IFuncFactory.IFUNC__UVM__SWAP_AND_KILL: {
            InterpreterStack sta = getStack(args.get(0).getDst());
            InterpreterStack oldStack = stack;
            swapStack(sta);
            oldStack.kill();
            break;
        }
        case IFuncFactory.IFUNC__UVM__THREAD_EXIT: {
            running = false;
            break;
        }
        default: {
            error("Unimplemented intrinsic function: "
                    + IdentifiedHelper.repr(ifunc));
        }
        }
        return null;
    }

    public void start() {
        vmThread.start();
    }

    public void join() throws InterruptedException {
        vmThread.join();
    }

    public void exit() {
        running = false;
    }

}
