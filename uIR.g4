/*
 * defines uVM IR text form
 */

grammar uIR;

ir
    :   metaData*
    ;

metaData
    :   typeDef
    |   funcSigDef
    |   constDef
    |   globalDef
    |   funcDecl
    |   funcDef
    ;

typeDef
    :   '.typedef' IDENTIFIER '=' typeConstructor
    ;

funcSigDef
    :   '.funcsig' IDENTIFIER '=' funcSigConstructor
    ;

constDef
    :   '.const' IDENTIFIER '<' type '>' '=' constExpr
    ;
    
globalDef
    :   '.global' IDENTIFIER '<' type '>'
    ;

funcDecl
    :   '.funcdecl' IDENTIFIER '<' funcSig '>'
    ;
    
funcDef
    :   '.funcdef' IDENTIFIER '<' funcSig '>' funcBody
    ;

type
    :   IDENTIFIER          # ReferencedType
    |   typeConstructor     # InLineType
    ;

typeConstructor
    :   'int' '<' intLiteral '>'            # IntType
    |   'float'                             # FloatType
    |   'double'                            # DoubleType
    |   'ref' '<' type '>'                  # RefType
    |   'iref' '<' type '>'                 # IRefType
    |   'weakref' '<' type '>'              # WeakRefType
    |   'struct' '<' type* '>'              # StructType
    |   'array' '<' type intLiteral '>'     # ArrayType
    |   'hybrid' '<' type type '>'          # HybridType
    |   'void'                              # VoidType
    |   'func' '<' funcSig '>'              # FuncType
    |   'thread'                            # ThreadType
    |   'stack'                             # StackType
    |   'tagref64'                          # TagRef64Type
    ;

funcSig
    :   IDENTIFIER          # ReferencedFuncSig
    |   funcSigConstructor  # InLineFuncSig
    ;

funcSigConstructor
    :   type '(' type* ')'
    ;

constant
    :   IDENTIFIER          # ReferencedConst
    |   constExpr           # InLineConst
    ;

constExpr
    :   intLiteral          # IntConst
    |   fpLiteral           # FPConst
    |   '{' constant* '}'   # StructConst
    |   'NULL'              # NullConst
    ;

funcBody
    :   '{' basicBlock+ '}'
    ;

basicBlock
    :   entryBlock regularBlock*
    ;

entryBlock
    :   label? inst+
    ;

regularBlock
    :   label inst+
    ;

label
    :   IDENTIFIER ':'
    ;

inst
    :   IDENTIFIER '=' instBody             # NamedInstruction
    |   instBody                            # AnonymousInstruction
    ;

instBody
    :   'PARAM' intLiteral                      # InstParam

    // Integer/FP Arithmetic
    |   BINOPS '<' type '>' value value         # InstBinOp

    // Integer/FP Comparison
    |   CMPOPS '<' type '>' value value         # InstCmp

    // Conversions
    |   CONVOPS  '<' type type '>' value            # InstConversion
    
    // Select
    |   'SELECT' '<' type '>' value value value     # InstSelect

    // Intra-function Control Flow
    |   'BRANCH' IDENTIFIER                         # InstBranch
    |   'BRANCH2' value IDENTIFIER IDENTIFIER       # InstBranch2
    |   'SWITCH' '<' type '>' value IDENTIFIER '{'
            (value ':' IDENTIFIER ';')* '}'         # InstSwitch
    |   'PHI' '<' type '>' '{'
            (IDENTIFIER ':' value ';')* '}'         # InstPhi

    // Inter-function Control Flow
    |   'CALL' funcCallBody keepAlive?              # InstCall
    |   'INVOKE' funcCallBody IDENTIFIER IDENTIFIER keepAlive? # InstInvoke
    |   'TAILCALL' funcCallBody                     # InstTailCall

    |   'RET' '<' type '>' value                    # InstRet
    |   'RETVOID'                                   # InstRetVoid
    |   'THROW' value                               # InstThrow
    |   'LANDINGPAD'                                # InstLandingPad

    // Aggregate Operations
    |   'EXTRACTVALUE' '<' type intLiteral '>' value        # InstExtractValue
    |   'INSERTVALUE' '<' type intLiteral '>' value value   # InstInsertValue

    // Memory Operations
    |   'NEW'           '<' type '>'                # InstNew
    |   'NEWHYBRID'     '<' type '>' value          # InstNewHybrid
    |   'ALLOCA'        '<' type '>'                # InstAlloca
    |   'ALLOCAHYBRID'  '<' type '>' value          # InstAllocaHybrid
    
    |   'GETIREF'       '<' type '>' value              # InstGetIRef

    |   'GETFIELDIREF'  '<' type intLiteral '>' value   # InstGetFieldIRef
    |   'GETELEMIREF'   '<' type '>' value value        # InstGetElemIRef
    |   'SHIFTIREF'     '<' type '>' value value        # InstShiftIRef
    |   'GETFIXEDPARTIREF'  '<' type '>' value          # InstGetFixedPartIRef
    |   'GETVARPARTIREF'    '<' type '>' value          # InstGetVarPartIRef
    
    |   'LOAD' ATOMICORD? '<' type '>' value           # InstLoad
    |   'STORE' ATOMICORD? '<' type '>' value value    # InstStore
    |   'CMPXCHG' ATOMICORD ATOMICORD
                    '<' type '>' value value value      # InstCmpXChg
    |   'ATOMICRMW' ATOMICORD? ATOMICRMWOP
                '<' type '>' value value                # InstAtomicRMW

    |   'FENCE' ATOMICORD                              # InstFence

    // Trap
    |   'TRAP' '<' type '>'
            IDENTIFIER IDENTIFIER keepAlive             # InstTrap
    |   'WATCHPOINT' intLiteral '<' type '>'
            IDENTIFIER IDENTIFIER IDENTIFIER keepAlive  # InstWatchPoint

    // Foreign Function Interface
    |   'CCALL' CALLCONV funcCallBody                   # InstCCall

    // Thread and Stack Operations
    |   'NEWSTACK'  funcCallBody                        # InstNewStack

    // Intrinsic Functions
    |   'ICCALL' IDENTIFIER args keepAlive?      # InstCall
    |   'IINVOKE' IDENTIFIER args
            IDENTIFIER IDENTIFIER keepAlive?            # InstInvoke
    ;

funcCallBody
    :   '<' funcSig '>' value args
    ;

args
    :   '(' value* ')'
    ;

keepAlive
    :   'KEEPALIVE' '(' value* ')'
    ;

CALLCONV : 'DEFAULT' ;

BINOPS : IBINOPS | FBINOPS ;

IBINOPS
    : 'ADD'
    | 'SUB'
    | 'MUL'
    | 'UDIV'
    | 'SDIV'
    | 'UREM'
    | 'SREM'
    | 'SHL'
    | 'LSHR'
    | 'ASHR'
    | 'AND'
    | 'OR'
    | 'XOR'
    ;
    
FBINOPS
    : 'FADD' | 'FSUB' | 'FMUL' | 'FDIV' | 'FREM'
    ;

CMPOPS : ICMPOPS | FCMPOPS ;

ICMPOPS
    : 'EQ'
    | 'NE'
    | 'SGT'
    | 'SLT'
    | 'SGE'
    | 'SLE'
    | 'UGT'
    | 'ULT'
    | 'UGE'
    | 'ULE'
    ;

FCMPOPS
    : 'FTRUE' | 'FFALSE' 
    | 'FUNO' | 'FUEQ' | 'FUNE' | 'FUGT' | 'FULT' | 'FUGE' | 'FULE'
    | 'FORD' | 'FOEQ' | 'FONE' | 'FOGT' | 'FOLT' | 'FOGE' | 'FOLE'
    ;
    
CONVOPS
    : 'TRUNC' | 'ZEXT' | 'SEXT' | 'FPTRUNC' | 'FPEXT'
    | 'FPTOUI' | 'FPTOSI' | 'UITOFP' | 'SITOFP' | 'BITCAST'
    | 'REFCAST' | 'IREFCAST'
    ;

ATOMICORD
    : 'NOT_ATOMIC' | 'UNORDERED' | 'MONOTONIC' | 'ACQUIRE' | 'RELEASE'
    | 'ACQ_REL' | 'SQL_CST'
    ;

ATOMICRMWOP
    : 'XCHG' | 'ADD' | 'SUB' | 'AND' | 'NAND' | 'OR' | 'XOR'
    | 'MAX' | 'MIN' | 'UMAX' | 'UMIN'
    ;
value
    :   IDENTIFIER      # ReferencedValue
    |   constExpr       # InlineConstValue
    ;

intLiteral
    :   INT_DEC     # DecIntLiteral
    |   INT_OCT     # OctIntLiteral
    |   INT_HEX     # HexIntLiteral
    ;

fpLiteral
    :   FP_NUM
    ;

// LEXER

INT_DEC
    :   ('+'|'-')? DIGIT_NON_ZERO DIGIT*
    ;
    
INT_OCT
    :   ('+'|'-')? '0' DIGIT*
    ;

INT_HEX
    :   ('+'|'-')? '0x' HEX_DIGIT+
    ;
    
FP_NUM
    :   ('+'|'-')? DIGIT+ '.' DIGIT+ ('e' ('+'|'-')? DIGIT+)?
    ;

IDENTIFIER
    :   GLOBAL_ID_PREFIX IDCHAR+
    |   LOCAL_ID_PREFIX IDCHAR+
    ;

fragment
DIGIT
    :   [0-9]
    ;

fragment
DIGIT_NON_ZERO
    :   [1-9]
    ;

fragment
HEX_DIGIT
    :   [0-9a-fA-F]
    ;

fragment
GLOBAL_ID_PREFIX: '@';

fragment
LOCAL_ID_PREFIX: '%';

fragment
IDCHAR
    :   [a-z]
    |   [A-Z]
    |   [0-9]
    |   '-'
    |   '_'
    |   '.'
    ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;
