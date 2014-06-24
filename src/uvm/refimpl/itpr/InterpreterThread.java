package uvm.refimpl.itpr;

import uvm.BasicBlock;
import uvm.Function;
import uvm.IdentifiedHelper;
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
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.Value;
import uvm.ssavalue.ValueVisitor;
import uvm.type.Int;
import uvm.type.Type;
import uvm.util.ErrorUtils;

public class InterpreterThread implements Runnable {

    private InterpreterStack stack;

    private boolean running = true;

    private InstructionExecutor executor = new InstructionExecutor();

    public InterpreterThread(InterpreterStack stack) {
        this.stack = stack;
    }

    @Override
    public void run() {
        while (running) {
            getCurInst().accept(executor);
        }
    }

    private Function getCurFunc() {
        return stack.getTop().getFunc();
    }

    private BasicBlock getCurBb() {
        return stack.getTop().getCurBb();
    }

    public int getCurInstIndex() {
        return stack.getTop().getCurInstIndex();
    }

    public Instruction getCurInst() {
        return stack.getTop().getCurInst();
    }

    @SuppressWarnings("unchecked")
    public <T extends ValueBox> T getValueBox(Value value) {
        return (T) stack.getTop().getValueBox(value);
    }

    private void error(String string) {
        ErrorUtils.uvmError("Function " + IdentifiedHelper.repr(getCurFunc())
                + " BB " + IdentifiedHelper.repr(getCurBb()) + " inst "
                + IdentifiedHelper.repr(getCurInst()) + " : " + string);
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
                IntBox op1 = getValueBox(inst.getOp1());
                IntBox op2 = getValueBox(inst.getOp2());

                long v1 = op1.getValue();
                long v2 = op2.getValue();
                long rv = 0;

                switch (inst.getOptr()) {
                case ADD:
                    rv = v1 + v2;
                    break;
                case SUB:
                    rv = v1 - v2;
                    break;
                case MUL:
                    rv = v1 * v2;
                    break;
                case UDIV:
                    rv = v1 / v2;
                    break;
                case SDIV:
                    rv = v1 / v2;
                    break;
                case UREM:
                    rv = v1 % v2;
                    break;
                case SREM:
                    rv = v1 % v2;
                    break;
                case SHL:
                    rv = v1 << v2;
                    break;
                case LSHR:
                    rv = v1 >>> v2;
                    break;
                case ASHR:
                    rv = v1 >> v2;
                    break;
                case AND:
                    rv = v1 & v2;
                    break;
                case OR:
                    rv = v1 | v2;
                    break;
                case XOR:
                    rv = v1 ^ v2;
                    break;
                default:
                    error("Unexpected op " + inst.getOptr().toString());
                }

                IntBox rvBox = getValueBox(inst);
                rvBox.setValue(rv);
            } else if (type instanceof uvm.type.Float) {

            } else if (type instanceof uvm.type.Double) {

            } else {
                error("Bad type for binary operation: "
                        + type.getClass().getName());
            }
            return null;
        }

        @Override
        public Void visitCmp(InstCmp inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitConversion(InstConversion inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitSelect(InstSelect inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitBranch(InstBranch inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitBranch2(InstBranch2 inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitSwitch(InstSwitch inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitPhi(InstPhi inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitCall(InstCall inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitInvoke(InstInvoke inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitTailCall(InstTailCall inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitRet(InstRet inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitRetVoid(InstRetVoid inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitThrow(InstThrow inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLandingPad(InstLandingPad inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitExtractValue(InstExtractValue inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitInsertValue(InstInsertValue inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitNew(InstNew inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitNewHybrid(InstNewHybrid inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitAlloca(InstAlloca inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitAllocaHybrid(InstAllocaHybrid inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitGetIRef(InstGetIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitGetFieldIRef(InstGetFieldIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitGetElemIRef(InstGetElemIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitShiftIRef(InstShiftIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitGetVarPartIRef(InstGetVarPartIRef inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLoad(InstLoad inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitStore(InstStore inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitCmpXchg(InstCmpXchg inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitAtomicRMW(InstAtomicRMW inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitFence(InstFence inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitTrap(InstTrap inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitWatchPoint(InstWatchPoint inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitCCall(InstCCall inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitNewStack(InstNewStack inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitICall(InstICall inst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitIInvoke(InstIInvoke inst) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
