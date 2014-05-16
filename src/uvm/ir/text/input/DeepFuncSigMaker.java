package uvm.ir.text.input;

import parser.uIRParser.FuncSigConstructorContext;
import parser.uIRParser.ReferencedFuncSigContext;
import uvm.FunctionSignature;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Similar to DeepTypeMaker, but works on ".funcsig" definitions.
 */
class DeepFuncSigMaker extends ShallowFuncSigMaker {

    DeepFuncSigMaker(RecursiveBundleBuilder recursiveBundleBuilder) {
        super(recursiveBundleBuilder);
    }

    @Override
    public FunctionSignature visitReferencedFuncSig(
            ReferencedFuncSigContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        FunctionSignature sig = rbb.bundle.getFuncSigNs().getByName(name);

        if (sig == null) {
            ParserHelper.parseError(ctx, "Undefined sig " + name);
        }

        return sig;
    }

    @Override
    public FunctionSignature visitFuncSigConstructor(
            FuncSigConstructorContext ctx) {
        FunctionSignature sig = super.visitFuncSigConstructor(ctx);
        rbb.typeAndSigPopulator.visitFuncSigConstructor(sig, ctx);
        return sig;
    }
}