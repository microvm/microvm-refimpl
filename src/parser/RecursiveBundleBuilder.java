package parser;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FloatTypeContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.FuncSigDefContext;
import parser.uIRParser.InLineFuncSigContext;
import parser.uIRParser.InLineTypeContext;
import parser.uIRParser.IntLiteralContext;
import parser.uIRParser.IntTypeContext;
import parser.uIRParser.ReferencedFuncSigContext;
import parser.uIRParser.ReferencedTypeContext;
import parser.uIRParser.TypeContext;
import parser.uIRParser.TypeDefContext;
import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.ssavalue.Constant;
import uvm.type.Type;

/**
 * RecursiveUIRBuilder builds a uvm Bundle from a uir parse tree.
 * <p>
 * Not thread safe. Don't use from multiple threads.
 */
public class RecursiveBundleBuilder {

    // Interface for the user.

    /**
     * The result bundle.
     */
    Bundle bundle;

    public RecursiveBundleBuilder() {
        bundle = new Bundle();
    }

    /**
     * @return The result bundle. Should only be called after building.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Build the bundle.
     * 
     * @param pt
     *            The ParseTree object of the root, i.e. the result of
     *            parser.ir()
     */
    public void build(ParseTree pt) {
        // Build types in two passes.

        // // The first pass, create a stub for all .typedef and .funcsig.
        uIRBaseVisitor<Void> makeDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                Type type = shallowTypeMaker.visit(ctx.typeConstructor());
                String name = ctx.IDENTIFIER().getText();
                type.setName(name);
                bundle.bind(type.getID(), name);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                FunctionSignature sig = shallowFuncSigMaker.visit(ctx
                        .funcSigConstructor());
                String name = ctx.IDENTIFIER().getText();
                sig.setName(name);
                bundle.bind(sig.getID(), name);
                return null;
            }
        };
        makeDeclaredTypes.visit(pt);

        // // The second pass, populate its dependencies.
        uIRBaseVisitor<Void> populateDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                String name = ctx.IDENTIFIER().getText();
                final Type oldType = bundle.getTypeByName(name);

                new PopulateDeclaredTypeAndSig(RecursiveBundleBuilder.this,
                        oldType).visit(ctx.typeConstructor());

                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                String name = ctx.IDENTIFIER().getText();
                FunctionSignature sig = bundle.getFuncSigByName(name);

                typeAndSigPopulator.visitFuncSigConstructor(sig,
                        ctx.funcSigConstructor());

                return null;
            }

        };
        populateDeclaredTypes.visit(pt);

        // Visit all global constant definitions. Skip local definitions in this
        // step.
        uIRBaseVisitor<Void> globalConstDefVisitor = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                handleGlobalConstDef(ctx);
                return null;
            }

            @Override
            public Void visitFuncDef(FuncDefContext ctx) {
                return null; // Skip functions to skip local ".const"
            }
        };
        globalConstDefVisitor.visit(pt);
    }

    // ID facilities

    private int nextId = 1;

    /**
     * Create a new globally unique ID.
     * <p>
     * TODO: Consider moving to a more "global" place. Candidate: MicroVM
     * 
     * @return A unique ID for everything: types, signatures, functions,
     *         instructions, ...
     */
    int makeID() {
        int thisId = nextId++;
        return thisId;
    }

    // Types and function signatures

    ShallowTypeMaker shallowTypeMaker = new ShallowTypeMaker(this);
    ShallowFuncSigMaker shallowFuncSigMaker = new ShallowFuncSigMaker(this);

    TypeAndSigPopulator typeAndSigPopulator = new TypeAndSigPopulator(this);

    DeepTypeMaker deepTypeMaker = new DeepTypeMaker(this);
    DeepFuncSigMaker deepFuncSigMaker = new DeepFuncSigMaker(this);

    // Literal parsers.
    // Literals may and may not be Constants.
    // The int type also takes a literal as a parameter.

    IntLiteralParser intLiteralParser = new IntLiteralParser();
    FPLiteralParser fpLiteralParser = new FPLiteralParser();

    /**
     * A convenient method that handles the cast.
     */
    long intLitToLong(IntLiteralContext ctx) {
        return (long) intLiteralParser.visit(ctx);
    }

    /**
     * A convenient method that handles the cast.
     */
    int intLitToInt(IntLiteralContext ctx) {
        return (int) ((long) intLiteralParser.visit(ctx));
    }

    // Constant processors

    /**
     * A general function to handle ".const" definitions, global or local.
     */
    private Constant handleConstDef(ConstDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Type type = deepTypeMaker.visit(ctx.type());
        Constant constant = new ConstExprHandler(this, type).visit(ctx
                .constExpr());

        constant.setName(name);

        return constant;
    }

    public Constant handleGlobalConstDef(ConstDefContext ctx) {
        Constant constant = handleConstDef(ctx);

        String name = constant.getName();
        if (!name.startsWith("@")) {
            throw new ASTParsingException("Local identifier " + name
                    + " found. Expect Galobal identifier.");
        }

        bundle.bind(constant.getID(), name);

        return constant;
    }

    // Functions and instructions.

    /**
     * Assert if an identifier is global
     * 
     * @param id
     *            An id.
     * @return The id parameter itself
     * @throws ASTParsingException
     *             Thrown if the ID is not global.
     */
    static String assertGlobal(String id) throws ASTParsingException {
        if (id.charAt(0) != '@') {
            throw new ASTParsingException("Met identifier " + id
                    + " while expecting a global identifier");
        }
        return id;
    }

}
