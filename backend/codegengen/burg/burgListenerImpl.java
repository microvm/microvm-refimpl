package burg;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import burg.Burg.CCTOperand;
import burg.Burg.MCOp;
import burg.Burg.MCRule;
import burg.Burg.NonTerminal;
import burg.Burg.OpdIntImmediate;
import burg.Burg.OpdRegister;
import burg.Burg.Rule;
import burg.Burg.Terminal;
import burg.Burg.TreeNode;
import burg.burgParser.McOperandContext;
import burg.burgParser.NodeContext;

public class burgListenerImpl extends burgBaseListener {
    
    @Override public void exitTargetDecl(@NotNull burgParser.TargetDeclContext ctx) {
        Burg.targetName = ctx.string().getText();
    }
    
    List<MCRule> mcEmissionRules = new ArrayList<MCRule>();
    
    @Override public void enterTreerule(@NotNull burgParser.TreeruleContext ctx) {
        if (!mcEmissionRules.isEmpty())
            mcEmissionRules = new ArrayList<MCRule>();
    }
    
    @Override public void exitTreerule(@NotNull burgParser.TreeruleContext ctx) { 
        NonTerminal lhs = (NonTerminal) Burg.findOrCreateNonTerm(ctx.NONTERM().toString());
        TreeNode rhs = getTreeNode(ctx.node());
        int cost = Integer.parseInt(ctx.DIGITS().toString());
        
        Rule rule = new Rule(lhs, rhs, cost);
        
        if (!mcEmissionRules.isEmpty())
            rule.setMCEmissionRules(mcEmissionRules);
        
        
        
        Burg.newRule(rule);
    }
    
    private TreeNode getTreeNode(NodeContext ctx) {
        if (ctx.NONTERM() != null)
            return Burg.findOrCreateNonTerm(ctx.NONTERM().toString());
        
        Terminal t = (Terminal) Burg.findOrCreateTerm(ctx.TERM().toString());
        for (NodeContext c : ctx.node()) {
            t.children.add(getTreeNode(c));
        }
        return t;
    }
    
    @Override public void exitMcode(@NotNull burgParser.McodeContext ctx) {
        String opName = ctx.mcOp().getText();
        
        MCOp op;
        if (!Burg.mcOps.containsKey(opName)) {
            op = new MCOp();
            op.name = opName;
            op.operands = ctx.mcOperand().size();
            
            Burg.mcOps.put(opName, op);
        } else op = Burg.mcOps.get(opName);
        
        List<CCTOperand> operands = new ArrayList<CCTOperand>();
        for (burgParser.McOperandContext operandCtx : ctx.mcOperand()) {
            operands.add(getOperand(operandCtx));
        }
        
        MCRule r = new MCRule(op, operands);
        mcEmissionRules.add(r);
    }

    private CCTOperand getOperand(McOperandContext operandCtx) {
        CCTOperand ret = null;
        if (operandCtx instanceof burgParser.McOpdNodeChildContext) {
            List<CCTOperand> indices = new ArrayList<CCTOperand>();
            
            for (burgParser.IndexContext i : ((burgParser.McOpdNodeChildContext) operandCtx).multiIndex().index()) {
                indices.add(getIndex(i));
            }
            
            ret = new Burg.OpdNode(indices);
        } else if (operandCtx instanceof burgParser.McOpdNodeFuncContext) {
            String funcName = ((burgParser.McOpdNodeFuncContext) operandCtx).funcCall().getText();
            if (((burgParser.McOpdNodeFuncContext) operandCtx).funcCallRcv() == null) {
                ret = new Burg.OpdNodeFunc(funcName);
            } else {
                String receiverName = ((burgParser.McOpdNodeFuncContext) operandCtx).funcCallRcv().getText();
                ret = new Burg.OpdNodeFunc(funcName, receiverName);
            }
        } else if (operandCtx instanceof burgParser.McOpdImmContext) {
            if (((burgParser.McOpdImmContext) operandCtx).mcImmediate().mcIntImmediate() != null)
                ret = new Burg.OpdIntImmediate(Long.parseLong(((burgParser.McOpdImmContext) operandCtx).mcImmediate().mcIntImmediate().getText()));
            else Burg.error("not support fpimm yet");
        } else if (operandCtx instanceof burgParser.McOpdRegContext) {
            burgParser.McRegContext mcRegCtx = ((burgParser.McOpdRegContext) operandCtx).mcReg();
            if (mcRegCtx instanceof burgParser.McOpdResRegContext) {
                ret = Burg.OpdRegister.findOrCreate("res_reg", OpdRegister.RES_REG);
            }
            else if (mcRegCtx instanceof burgParser.McOpdRetRegContext) {
                OpdRegister tRet = OpdRegister.findOrCreate("ret_reg", OpdRegister.RET_REG);
                CCTOperand index = getIndex(((burgParser.McOpdRetRegContext) mcRegCtx).index());
                tRet.setIndex(index);
                ret = tRet;
            }
            else if (mcRegCtx instanceof burgParser.McOpdParamRegContext) {
                OpdRegister tRet = OpdRegister.findOrCreate("param_reg", OpdRegister.PARAM_REG);
                CCTOperand index = getIndex(((burgParser.McOpdParamRegContext) mcRegCtx).index());
                tRet.setIndex(index);
                ret = tRet;
            }
            else if (mcRegCtx instanceof burgParser.McOpdMachineRegContext) {
                ret = OpdRegister.findOrCreate(mcRegCtx.getText(), OpdRegister.MACHINE_REG);
            }
        }
        
        return ret;
    }
    
    private CCTOperand getIndex(burgParser.IndexContext i) {
        if (i.DIGITS() != null)
            return new OpdIntImmediate(Integer.parseInt(i.DIGITS().getText()));
        else return getOperand(i.mcOperand());
    }
}
