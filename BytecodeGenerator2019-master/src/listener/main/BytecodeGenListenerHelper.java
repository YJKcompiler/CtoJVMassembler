package jeon_3_2.compiler.hw_05.listener;

import java.util.Hashtable;
import jeon_3_2.compiler.hw_05.generated.*;
import jeon_3_2.compiler.hw_05.generated.MiniCParser.*;

public class BytecodeGenListenerHelper {

	// <boolean functions>

	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof MiniCParser.Fun_declContext;
	}

	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}

	// global vars
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}

	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in>
		// type_spec 자식의 문자열을 직접 비교
		Type_specContext tspec = (Type_specContext) ctx.getChild(0);
		return tspec.getChild(0).getText().equals("void");
	}

	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}

	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
			// <Fill in>
		// "void" 또는 "int" 를 JVM어셈블리어로 반환
		if(typespec.VOID()!=null)
			return "V"; // void
		return "I"; // int
	}

	// params
	static String getParamName(ParamContext param) {
		// <Fill in>
		return param.IDENT().getText();
	}

	static String getParamTypesText(ParamsContext params) {
		String typeText = "";

		for(int i = 0; i < params.param().size(); i++) {
			MiniCParser.Type_specContext typespec = (MiniCParser.Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}

	static String getLocalVarName(Local_declContext local_decl) {
		// <Fill in>
		return local_decl.IDENT().getText();
	}

	static String getFunName(Fun_declContext ctx) {
		// <Fill in>
		return ctx.IDENT().getText();
	}

	// // //
	static String getFunName(ExprContext ctx) {
		// <Fill in>
		return ctx.IDENT().getText();
	}

	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() <= 5;
		// 수정
//		return ctx.getChildCount() < 5;
	}

	static String getFunProlog() {
		String prolog = "";
		prolog += ".class public Test\n";
		prolog += ".super java/lang/Object\n";
		prolog += ".method public <init>()V\n";
		prolog += "\taload_0\n";
		prolog += "\tinvokenonvirtual java/lang/Object/<init>()V\n";
		prolog += "\treturn\n";
		prolog += ".end method\n\n";
		return prolog;
	}

	static String getCurrentClassName() {
		return "Test";
	}
}
