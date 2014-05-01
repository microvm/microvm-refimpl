package parser;

import org.antlr.v4.runtime.tree.ParseTree;

import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncDeclContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.FuncSigDefContext;
import parser.uIRParser.GlobalDefContext;
import parser.uIRParser.IntLiteralContext;
import parser.uIRParser.TypeDefContext;
import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.ssavalue.Constant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
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

        // The first pass, create a stub for all .typedef and .funcsig.
        uIRBaseVisitor<Void> makeDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                handleTypeDef(ctx);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                handleFuncSigDef(ctx);
                return null;
            }
        };
        makeDeclaredTypes.visit(pt);

        // The second pass, populate its dependencies.
        uIRBaseVisitor<Void> populateDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                populateTypeDef(ctx);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                populateFuncSigDef(ctx);
                return null;
            }

        };
        populateDeclaredTypes.visit(pt);

        // Visit all global constant definitions, global data definitions,
        // function definitions and function declarations. They all depend on
        // types.
        uIRBaseVisitor<Void> otherDefHandler = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                handleConstDefShallow(ctx);
                return null;
            }

            @Override
            public Void visitGlobalDef(GlobalDefContext ctx) {
                handleGlobalData(ctx);
                return null;
            }

            @Override
            public Void visitFuncDef(FuncDefContext ctx) {
                handleFuncDefShallow(ctx);
                return null;
            }

            @Override
            public Void visitFuncDecl(FuncDeclContext ctx) {
                handleFuncDecl(ctx);
                return null;
            }
        };
        otherDefHandler.visit(pt);

        // Re-visit all global constant definitions to populate nested
        // constants. In this case they are all Struct constants.
        uIRBaseVisitor<Void> populateConstDef = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                populateConstDef(ctx);
                return null;
            }
        };
        populateConstDef.visit(pt);
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

    private void handleTypeDef(TypeDefContext ctx) {
        Type type = shallowTypeMaker.visit(ctx.typeConstructor());
        String name = ctx.IDENTIFIER().getText();
        type.setName(name);
        bundle.bind(type.getID(), name);
    }

    private void handleFuncSigDef(FuncSigDefContext ctx) {
        FunctionSignature sig = shallowFuncSigMaker.visit(ctx
                .funcSigConstructor());
        String name = ctx.IDENTIFIER().getText();
        sig.setName(name);
        bundle.bind(sig.getID(), name);
    }

    private void populateTypeDef(TypeDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        final Type oldType = bundle.getTypeByName(name);

        new PopulateDeclaredTypeAndSig(RecursiveBundleBuilder.this, oldType)
                .visit(ctx.typeConstructor());
    }

    private void populateFuncSigDef(FuncSigDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        FunctionSignature sig = bundle.getFuncSigByName(name);

        typeAndSigPopulator.visitFuncSigConstructor(sig,
                ctx.funcSigConstructor());
    }

    // Associated classes for type/funcSig creation and population.
    // They are externalised for their sizes.

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
    private Constant handleConstDefShallow(ConstDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (!name.startsWith("@")) {
            throw new ASTParsingException("Local identifier " + name
                    + " found. Expect Galobal identifier.");
        }

        Type type = deepTypeMaker.visit(ctx.type());
        Constant constant = new ShallowConstMaker(this, type).visit(ctx
                .constExpr());

        constant.setName(name);

        bundle.bind(constant.getID(), name);

        return constant;
    }

    private void populateConstDef(ConstDefContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Constant constant = bundle.getConstantByName(name);
        new PopulateDeclaredConst(this, constant).visit(ctx);
    }

    // The same shallow-populator-deep pattern as types.
    ConstPopulator constPopulator = new ConstPopulator(this);

    // Global data

    private GlobalData makeGlobalData(GlobalDefContext ctx) {
        GlobalData globalData = new GlobalData();
        int id = makeID();
        globalData.setID(id);
        String name = assertGlobal(ctx.IDENTIFIER().getText());
        globalData.setName(name);

        Type type = deepTypeMaker.visit(ctx.type());
        globalData.setType(type);

        bundle.registerGlobalData(id, name, globalData);
        return globalData;
    }

    private GlobalDataConstant makeGlobalDataConstant(GlobalData globalData) {
        GlobalDataConstant constant = new GlobalDataConstant();
        constant.setID(globalData.getID());
        constant.setName(globalData.getName());
        constant.setGlobalData(globalData);

        // Since the GlobalData and the GlobalDataConstant always have the same
        // ID and name, there is no need to re-bind the ID to the name.
        bundle.registerConstant(constant.getID(), null, constant);

        return constant;
    }

    private void handleGlobalData(GlobalDefContext ctx) {
        GlobalData globalData = makeGlobalData(ctx);
        makeGlobalDataConstant(globalData);
    }

    // Functions

    private Function declareFunction(String name, FunctionSignature sig) {
        Function function = new Function();
        int id = makeID();
        function.setID(id);
        function.setName(name);
        function.setSig(sig);

        bundle.registerFunc(id, name, function);

        return function;
    }

    private FunctionConstant makeFunctionConstant(Function function) {
        FunctionConstant constant = new FunctionConstant();
        constant.setID(function.getID());
        constant.setName(function.getName());
        constant.setFunction(function);

        // Since the GlobalData and the GlobalDataConstant always have the same
        // ID and name, there is no need to re-bind the ID to the name.
        bundle.registerConstant(constant.getID(), null, constant);

        return constant;
    }

    /**
     * Only shallowly handle a function definition, i.e. treat it as a function
     * declaration. The function body will be constructed in the next phase.
     */
    private void handleFuncDefShallow(FuncDefContext ctx) {
        // If already declared, do not declare again.
        // Potential error: an erroneous µVM-IR program may contain multiple
        // definitions of the same function with different signatures. We do not
        // check this error.

        String name = assertGlobal(ctx.IDENTIFIER().getText());

        // If already declared, do not declare again.
        // Potential error: an erroneous µVM-IR program may contain multiple
        // definitions of the same function with different signatures. We do not
        // check this error.

        if (bundle.getFuncByName(name) != null) {
            return;
        }

        FunctionSignature sig = deepFuncSigMaker.visit(ctx.funcSig());

        Function function = declareFunction(name, sig);
        makeFunctionConstant(function);
    }

    private void handleFuncDecl(FuncDeclContext ctx) {
        String name = assertGlobal(ctx.IDENTIFIER().getText());

        // It does not make sense to declare a function multiple times.

        if (bundle.getFuncByName(name) != null) {
            throw new ASTParsingException("Function " + name
                    + " declared multiple times.");
        }

        FunctionSignature sig = deepFuncSigMaker.visit(ctx.funcSig());

        Function function = declareFunction(name, sig);
        makeFunctionConstant(function);
    }

    // Utility methods

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
