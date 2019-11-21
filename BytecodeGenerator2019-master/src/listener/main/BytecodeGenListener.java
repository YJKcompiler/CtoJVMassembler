package jeon_3_2.compiler.hw_05.listener;

import jeon_3_2.compiler.hw_05.generated.MiniCBaseListener;
import jeon_3_2.compiler.hw_05.generated.MiniCParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import jeon_3_2.compiler.hw_05.generated.MiniCBaseListener.*;
import jeon_3_2.compiler.hw_05.generated.MiniCParser.*;
//import generated.MiniCBaseListener;
//import generated.MiniCParser;
//import generated.MiniCParser.ExprContext;
//import generated.MiniCParser.Fun_declContext;
//import generated.MiniCParser.Local_declContext;
//import generated.MiniCParser.ParamsContext;
//import generated.MiniCParser.ProgramContext;
//import generated.MiniCParser.StmtContext;
//import generated.MiniCParser.Type_specContext;
//import generated.MiniCParser.Var_declContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import static jeon_3_2.compiler.hw_05.listener.SymbolTable.*;
import static jeon_3_2.compiler.hw_05.listener.BytecodeGenListenerHelper.*;

//import static listener.main.BytecodeGenListenerHelper.*;
//import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }

    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    SymbolTable symbolTable = new SymbolTable();

    int tab = 0;
    int label = 0;

    // program	: decl+

    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
        symbolTable.initFunDecl();

        String fname = getFunName(ctx);
        ParamsContext params;

        if (fname.equals("main")) {
            symbolTable.putLocalVar("args", Type.INTARRAY);
        } else {
            // 함수이름 해시맵에 put
            symbolTable.putFunSpecStr(ctx);
            params = (MiniCParser.ParamsContext) ctx.getChild(3);

            // 로컬 변수 해시맵에 parameter들 put
            symbolTable.putParams(params);
        }
    }


    // var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
    @Override
    public void enterVar_decl(MiniCParser.Var_declContext ctx) {
        String varName = ctx.IDENT().getText();

        if (isArrayDecl(ctx)) {
            symbolTable.putGlobalVar(varName, Type.INTARRAY);
        } else if (isDeclWithInit(ctx)) {
            symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
        } else { // simple decl
            symbolTable.putGlobalVar(varName, Type.INT);
        }
    }


    @Override
    public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
        if (isArrayDecl(ctx)) {
            symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
        } else if (isDeclWithInit(ctx)) {
            symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
        } else { // simple decl
            symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
        }
    }


    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        String classProlog = getFunProlog();

        String fun_decl = "", var_decl = "";

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (isFunDecl(ctx, i))
                fun_decl += newTexts.get(ctx.decl(i));
            else
                var_decl += newTexts.get(ctx.decl(i));
        }

        newTexts.put(ctx, classProlog + var_decl + fun_decl);

        System.out.println(newTexts.get(ctx));
    }


    // decl	: var_decl | fun_decl
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        String decl = "";
        if (ctx.getChildCount() == 1) {
            if (ctx.var_decl() != null)                //var_decl
                decl += newTexts.get(ctx.var_decl());
            else                            //fun_decl
                decl += newTexts.get(ctx.fun_decl());
        }
        newTexts.put(ctx, decl);
    }

    // stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        String stmt = "";
        if (ctx.getChildCount() > 0) {
            if (ctx.expr_stmt() != null)                // expr_stmt
                stmt += newTexts.get(ctx.expr_stmt());
            else if (ctx.compound_stmt() != null)    // compound_stmt
                stmt += newTexts.get(ctx.compound_stmt());
                // <(0) Fill here>
            else if (ctx.if_stmt() != null)
                stmt += newTexts.get(ctx.if_stmt());
            else if (ctx.while_stmt() != null)
                stmt += newTexts.get(ctx.while_stmt());
            else
                stmt += newTexts.get(ctx.return_stmt());
        }
        newTexts.put(ctx, stmt);
    }

    // expr_stmt	: expr ';'
    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        String stmt = "";
        if (ctx.getChildCount() == 2) {
            stmt += newTexts.get(ctx.expr());    // expr
        }
        newTexts.put(ctx, stmt);
    }


    // while_stmt	: WHILE '(' expr ')' stmt
    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        // <(1) Fill here!>
        String while_stmt = "";

        String condExpr = newTexts.get(ctx.expr());
        String thenStmt = newTexts.get(ctx.stmt());

        String lLoop = symbolTable.newLabel();
        String lend = symbolTable.newLabel();


        while_stmt += lLoop +":" + "\n"
                +condExpr + "\n"
                + "ifeq " + lend + "\n"
                + thenStmt + "\n"
                + "goto " + lLoop +"\n"
                + lend + ":" + "\n";

        newTexts.put(ctx, while_stmt);
    }


    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        // <(2) Fill here!>
        String fun_decl = "";
        fun_decl += funcHeader(ctx, getFunName(ctx));
        fun_decl += newTexts.get(ctx.compound_stmt());

        // void함수일 경우 코드에 return이 있어도 없어도 되지만
        // JVM 어셈블리어는 return이 무조건 있어야함
        if(isVoidF(ctx))
            fun_decl += "return\n\n";

        // ".end method" 앞의 /t 지워주기 (exitCompound_stmt() 에서 붙어서 옴)
        fun_decl = fun_decl.substring(0, fun_decl.length() - 1);
        fun_decl += ".end method\n\n";
        newTexts.put(ctx, fun_decl);
    }


    private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) {
        return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"
                + "\t" + ".limit stack " + getStackSize(ctx) + "\n"
                + "\t" + ".limit locals " + getLocalVarSize(ctx) + "\n";
    }


    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String varName = ctx.IDENT().getText();
        String varDecl = "";

        if (isDeclWithInit(ctx)) {
            varDecl += "putfield " + varName + "\n";
            // v. initialization => Later! skip now..:
        }
        newTexts.put(ctx, varDecl);
    }


    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String varDecl = "";

        if (isDeclWithInit(ctx)) {
            String vId = symbolTable.getVarId(ctx);
            varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
                    + "istore " + vId + "\n";
        }

        newTexts.put(ctx, varDecl);
    }


    // compound_stmt	: '{' local_decl* stmt* '}'
    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        // <(3) Fill here>

        String compound_stmt = "";

        // 자식들 붙이기
        for (int i = 0; i < ctx.local_decl().size(); i++)
            compound_stmt += newTexts.get(ctx.local_decl(i));
        for (int i = 0; i < ctx.stmt().size(); i++)
            compound_stmt += newTexts.get(ctx.stmt(i));

        // 첫줄 \t
        compound_stmt = "\t" + compound_stmt;

        // 블록 내부는 빈칸 띄우기
        compound_stmt = compound_stmt.replaceAll("\n", "\n\t");
        newTexts.put(ctx, compound_stmt);
    }

    // if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        String stmt = "";
        String condExpr = newTexts.get(ctx.expr());
        String thenStmt = newTexts.get(ctx.stmt(0));

        String lend = symbolTable.newLabel();
        String lelse = symbolTable.newLabel();


        if (noElse(ctx)) {
            stmt += condExpr + "\n"
                    + "ifeq " + lend + "\n"
                    + thenStmt + "\n"
                    + lend + ":" + "\n";
        } else {
            String elseStmt = newTexts.get(ctx.stmt(1));
            stmt += condExpr + "\n"
                    + "ifeq " + lelse + "\n"
                    + thenStmt + "\n"
                    + "goto " + lend + "\n"
                    + lelse + ": " + elseStmt + "\n"
                    + lend + ":" + "\n";
        }

        newTexts.put(ctx, stmt);
    }


    // return_stmt	: RETURN ';' | RETURN expr ';'
    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        String return_stmt = "";

        // void함수의 Return은 exitFun_decl()에서 처리
        // void함수일 경우 코드에 return이 있어도 없어도 되지만
        // JVM 어셈블리어는 return이 무조건 있어야하므로 여기서 안함
		if(isIntReturn(ctx)) {
            return_stmt += newTexts.get(ctx.expr());
            return_stmt += "ireturn\n";
        }

        newTexts.put(ctx, return_stmt);
    }


    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        String expr = "";

        if (ctx.getChildCount() <= 0) {
            newTexts.put(ctx, "");
            return;
        }

        if (ctx.getChildCount() == 1) { // IDENT | LITERAL
            if (ctx.IDENT() != null) {
                String idName = ctx.IDENT().getText();
                if (symbolTable.getVarType(idName) == Type.INT) {
                    expr += "iload " + symbolTable.getVarId(idName) + " \n";
                }
                //else	// Type int array => Later! skip now..
                //	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
            } else if (ctx.LITERAL() != null) {
                String literalStr = ctx.LITERAL().getText();
                expr += "ldc " + literalStr + " \n";
            }
        } else if (ctx.getChildCount() == 2) { // UnaryOperation
			// null 에러로 인한 수정
            expr = handleUnaryExpr(ctx, expr);
            // 기존 코드 : expr = handleUnaryExpr(ctx, newTexts.get(ctx) + expr);
        } else if (ctx.getChildCount() == 3) {
            if (ctx.getChild(0).getText().equals("(")) {        // '(' expr ')'
                expr = newTexts.get(ctx.expr(0));

            } else if (ctx.getChild(1).getText().equals("=")) {    // IDENT '=' expr
                expr = newTexts.get(ctx.expr(0))
                        + "istore " + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";

            } else {                                            // binary operation
                expr = handleBinExpr(ctx, expr);

            }
        }
        // IDENT '(' args ')' |  IDENT '[' expr ']'
        else if (ctx.getChildCount() == 4) {
            if (ctx.args() != null) {        // function calls
                expr = handleFunCall(ctx, expr);
            } else { // expr
                // Arrays: TODO
            }
        }
        // IDENT '[' expr ']' '=' expr
        else { // Arrays: TODO			*/
        }
        newTexts.put(ctx, expr);
    }


    private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
        String l1 = symbolTable.newLabel();
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        // 다시 store 해야할 때 사용
        String varId = "";

        expr += newTexts.get(ctx.expr(0));

        switch (ctx.getChild(0).getText()) {
            case "-":
                expr += "           ineg \n";
                break;
            case "--":
                varId = expr.split(" ")[1];
                expr += "ldc 1" + "\n"
                        + "isub" + "\n"
                        + "istore "+varId +"\n";
                break;
            case "++":
                varId = expr.split(" ")[1];
                expr += "ldc 1" + "\n"
                        + "iadd" + "\n"
                        + "istore "+varId +"\n";
                break;
            case "!":
                expr += "ifeq " + l2 + "\n"
                        + l1 + ": " + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "ldc 1" + "\n"
                        + lend + ": " + "\n";
                break;
        }
        return expr;
    }


    private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        expr += newTexts.get(ctx.expr(0));
        expr += newTexts.get(ctx.expr(1));

        switch (ctx.getChild(1).getText()) {
            case "*":
                expr += "imul \n";
                break;
            case "/":
                expr += "idiv \n";
                break;
            case "%":
                expr += "irem \n";
                break;
            case "+":        // expr(0) expr(1) iadd
                expr += "iadd \n";
                break;
            case "-":
                expr += "isub \n";
                break;

            case "==":
                expr += "isub " + "\n"
                        + "ifeq "+l2 + "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "ldc 1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "!=":
                expr += "isub " + "\n"
                        + "ifne "+l2+ "\n"
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": "+"\nldc 1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "<=":
                // <(5) Fill here>
                expr += "isub " + "\n"
                        + "ifle "+l2+ "\n"  // a-b <= 0 이면 점프
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "\nldc 1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "<":
                // <(6) Fill here>
                expr += "isub " + "\n"
                        + "iflt " + l2 + "\n"   // a-b < 0 이면 점프
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "\nldc 1" + "\n"
                        + lend + ": " + "\n";
                break;

            case ">=":
                // <(7) Fill here>
                expr += "isub " + "\n"
                        + "ifge " +l2+ "\n"   // a-b >= 0 이면 점프
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "\nldc 1" + "\n"
                        + lend + ": " + "\n";

                break;

            case ">":
                // <(8) Fill here>
                expr += "isub " + "\n"
                        + "ifgt " +l2+ "\n"   // a-b > 0 이면 점프
                        + "ldc 0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": " + "\nldc 1" + "\n"
                        + lend + ": " + "\n";
                break;

            case "and":
                expr += "ifne " + lend + "\n"
                        + "pop" + "\n" + "ldc 0" + "\n"
                        + lend + ": " + "\n";
                break;
            case "or":
                // <(9) Fill here>
                expr += "ifeq " + lend + "\n"
                        + "pop" + "\n" + "ldc 1" + "\n"
                        + lend + ": " + "\n";
                break;

        }
        return expr;
    }

    private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
        String fname = getFunName(ctx);

        if (fname.equals("_print")) {        // System.out.println
            expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
                    + newTexts.get(ctx.args())
                    + "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
        } else {
            expr = newTexts.get(ctx.args())
                    + "invokestatic " + getCurrentClassName() + "/" + symbolTable.getFunSpecStr(fname) + "\n";
        }

        return expr;

    }

    // args	: expr (',' expr)* | ;
    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {

        String argsStr = "\n";

        for (int i = 0; i < ctx.expr().size(); i++) {
            argsStr += newTexts.get(ctx.expr(i));
        }
        newTexts.put(ctx, argsStr);
    }

}
