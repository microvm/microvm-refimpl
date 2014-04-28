package uvm.irtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uvm.BasicBlock;
import uvm.CFG;
import uvm.IdentifiedHelper;
import uvm.OpCode;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.Instruction;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.Value;

/**
 * This class builds an IR Tree from a CFG.
 */
public class IRTreeBuilder {
    /**
     * Build a IR Tree from a CFG
     * 
     * @param cfg
     *            the CFG
     * @return a FunctionNode for the CFG
     */
    public static <T> FunctionNode<T> build(CFG cfg) {
        FunctionNode<T> fn = new FunctionNode<T>(cfg);

        List<IRTreeNode<T>> rootChildren = fn.getChildren();

        // Remember label nodes so that BRANCH instructions can refer to them
        Map<BasicBlock, LabelNode<T>> bbToLabelNodes = new HashMap<BasicBlock, LabelNode<T>>();

        // Depth-first traverse all basic blocks in the order of the control
        // flow. This guarantees that all uses appear after their definitions.
        List<BasicBlock> bbs = bbDFS(cfg.getEntry());

        for (BasicBlock bb : bbs) {
            LabelNode<T> ln = new LabelNode<T>(bb);
            bbToLabelNodes.put(bb, ln);
        }

        // Record all value nodes so that they will be populated later.
        List<ValueNode<T>> instNodes = new ArrayList<ValueNode<T>>();

        // If another instruction I2 has this instruction I1 as a child,
        // which node should be linked to, a RegisterNode or a ValueNode?
        Map<Instruction, IRTreeNode<T>> instToDepNode = new HashMap<Instruction, IRTreeNode<T>>();

        for (BasicBlock bb : bbs) {
            rootChildren.add(bbToLabelNodes.get(bb));

            // Create nodes
            for (Instruction inst : bb.getInsts()) {
                ValueNode<T> node = new ValueNode<T>(inst);
                instNodes.add(node);

                if (!hasSideEffect(inst) && uniquelyLocallyUsed(inst, bb)) {
                    // This instruction is side-effect free and uniquely used.
                    // This will not appear as an immediate child of the root,
                    // but will be referred by other nodes.
                    instToDepNode.put(inst, node);
                    System.out.println("Added (non-child) " + inst.toString());
                } else {
                    // This node must be a direct child of the root.
                    if (hasValue(inst)) {
                        // It has value. Assign it to a virtual register.
                        RegisterNode<T> regNode = new RegisterNode<T>(node);
                        AssignmentNode<T> asgnNode = new AssignmentNode<T>(
                                regNode, node);
                        instToDepNode.put(inst, regNode);

                        System.out.println("Added (rc-hv) " + inst.toString());
                        rootChildren.add(asgnNode);
                    } else {
                        // Since it is side-effect-only, it will not be used
                        // by other instructions.
                        rootChildren.add(node);
                        System.out.println("Added (rc-seo) " + inst.toString());
                    }
                }
            }

        }

        // Populate children
        for (ValueNode<T> node : instNodes) {
            System.out.println("Populating " + node.getValue());
            populateChild(node, bbToLabelNodes, instToDepNode);
        }

        return fn;
    }

    /**
     * Populate a node's children
     * 
     */
    private static <T> void populateChild(ValueNode<T> node,
            Map<BasicBlock, LabelNode<T>> bbToLabelNodes,
            Map<Instruction, IRTreeNode<T>> instToDepNode) {
        Value value = node.getValue();

        // TODO: Consider using the visitor pattern.
        // Or switch to the Scala languange (which is highly unlikely).
        List<IRTreeNode<T>> children = node.getChildren();

        if (value instanceof InstBinOp) {
            InstBinOp inst = (InstBinOp) value;
            children.add(getChildNode(inst.getOp1(), instToDepNode));
            children.add(getChildNode(inst.getOp2(), instToDepNode));
        } else if (value instanceof InstCmp) {
            InstCmp inst = (InstCmp) value;
            children.add(getChildNode(inst.getOp1(), instToDepNode));
            children.add(getChildNode(inst.getOp2(), instToDepNode));
        } else if (value instanceof InstBranch) {
            InstBranch inst = (InstBranch) value;
            children.add(bbToLabelNodes.get(inst.getTarget()));
        } else if (value instanceof InstBranch2) {
            InstBranch2 inst = (InstBranch2) value;
            children.add(getChildNode(inst.getCond(), instToDepNode));
            children.add(bbToLabelNodes.get(inst.getIfTrue()));
            children.add(bbToLabelNodes.get(inst.getIfFalse()));
        } else if (value instanceof InstPhi) {
            // Do nothing. Consider Phi as a MOV.
        } else if (value instanceof InstRet) {
            InstRet inst = (InstRet) value;
            children.add(getChildNode(inst.getRetVal(), instToDepNode));
        } else if (value instanceof InstRetVoid) {
            // Do nothing.
        }
    }

    /**
     * Look up the table of already-built nodes and find the appropriate node
     * (ValueNode or Register Node) of a child. May create constant nodes.
     */
    private static <T> IRTreeNode<T> getChildNode(Value value,
            Map<Instruction, IRTreeNode<T>> instToDepNode) {
        if (value instanceof Instruction) {
            Instruction inst = (Instruction) value;
            IRTreeNode<T> node = instToDepNode.get(inst);
            if (node == null) {
                throw new RuntimeException(String.format(
                        "BUG: instToDepNode is not well-computed. Child: %s",
                        IdentifiedHelper.repr(value)));
            }
            return node;
        } else {
            if (value instanceof IntConstant) {
                ValueNode<T> constNode = new ValueNode<T>(value);
                return constNode;
            } else {
                throw new RuntimeException("Unimplemented constant type ");
            }
        }
    }

    /**
     * Test if an instruction is uniquely used by another instruction in the
     * same basic block.
     * 
     * @param inst
     *            the instruction
     * @param bb
     *            the basic block. Must contain inst.
     * @return true if inst is uniquely used by another instruction in bb.
     */
    private static boolean uniquelyLocallyUsed(Instruction inst, BasicBlock bb) {
        return inst.getUsedBy().size() == 1
                && bb.getInsts().contains(inst.getUsedBy().get(0).getDst());
    }

    /**
     * Test if an instruction actually has a value (i.e. not a pure side-effect
     * instruction). STORE, BRANCH, RET, etc. does not have a value.
     * 
     * TODO: Consider pushing back to the Value class.
     * 
     * @param inst
     * @return
     */
    private static boolean hasValue(Instruction inst) {
        return inst.getType() != null;
    }

    /**
     * Test if an instruction has side effect. CALL, NEW, ALLOCA, LOAD, STORE,
     * etc. have side effect and must be kept in place.
     * 
     * TODO: Consider pushing back to the Value class.
     * 
     * @param value
     *            The SSA Value
     * @return true if the instruction has side effect.
     */
    private static boolean hasSideEffect(Value value) {
        return false;
    }

    private static List<BasicBlock> bbDFS(BasicBlock entry) {
        List<BasicBlock> result = new ArrayList<BasicBlock>();
        Deque<BasicBlock> stack = new ArrayDeque<BasicBlock>();

        stack.push(entry);

        while (!stack.isEmpty()) {
            BasicBlock top = stack.pop();
            if (result.contains(top)) {
                continue;
            }
            result.add(top);

            Instruction lastInst = top.getInsts()
                    .get(top.getInsts().size() - 1);

            if (lastInst instanceof InstBranch) {
                InstBranch inst = (InstBranch) lastInst;
                stack.push(inst.getTarget());
            } else if (lastInst instanceof InstBranch2) {
                InstBranch2 inst = (InstBranch2) lastInst;
                stack.push(inst.getIfFalse());
                stack.push(inst.getIfTrue());
            }
        }

        return result;
    }

    /**
     * Pretty print an IR tree.
     * 
     * @param tree
     *            the IR tree.
     * @return
     */
    public static <T> void prettyPrintIRTree(IRTreeNode<T> tree) {
        prettyPrintIRTree(tree, 0);
    }

    private static <T> void prettyPrintIRTree(IRTreeNode<T> node, int indent) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            line.append("    ");
        }
        line.append(node.getOpCode()).append("(")
                .append(OpCode.getOpName(node.getOpCode())).append(") ")
                .append(IdentifiedHelper.repr(node));

        if (node instanceof ValueNode) {
            line.append(" = ").append(((ValueNode<T>) node).getValue());
        } else if (node instanceof RegisterNode) {
            line.append(" ------------> ").append(
                    ((RegisterNode<T>) node).getValueNode().getValue());
        }

        System.out.println(line);

        for (IRTreeNode<T> child : node.getChildren()) {
            prettyPrintIRTree(child, indent + 1);
        }

    }
}
