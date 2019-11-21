package jeon_3_2.compiler.hw_05.listener;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import jeon_3_2.compiler.hw_05.generated.MiniCParser;
import jeon_3_2.compiler.hw_05.generated.MiniCParser.*;
import jeon_3_2.compiler.hw_05.listener.SymbolTable.Type;

import static jeon_3_2.compiler.hw_05.listener.BytecodeGenListenerHelper.*;


//import generated.MiniCParser;
//import generated.MiniCParser.Fun_declContext;
//import generated.MiniCParser.Local_declContext;
//import generated.MiniCParser.ParamsContext;
//import generated.MiniCParser.Type_specContext;
//import generated.MiniCParser.Var_declContext;
//import listener.main.SymbolTable.Type;
//import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}

	static public class VarInfo {
		Type type;
		int id;
		int initVal;

		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}

	// Function Info
	static public class FInfo {
	    // ex) sigStr : main([Ljava/lang/String;)V
		// ex) sigStr : sum(I)I
		public String sigStr;
	}

	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function


	private int _globalVarID = 0;
    private int _localVarID = 0;
    private int _labelID = 0;
    private int _tempVarID = 0;

	SymbolTable(){
		initFunDecl();
		initFunTable();
	}

	void initFunDecl(){		// at each func decl
		_lsymtable.clear();
        _localVarID = 0;
        _labelID = 0;
        _tempVarID = 32;
	}

	void putLocalVar(String varname, Type type){
		//<Fill here>

        // 이미 존재할 경우
        if(_lsymtable.containsKey(varname)) {
            System.out.println("local 변수 "+varname+"이 이미 존재");
            return;
        }

        // 로컬 변수 해시맵에 추가. localVarID는 1씩 증가
        _lsymtable.put(varname, new VarInfo(type, _localVarID++));
	}
	void putGlobalVar(String varname, Type type){
		//<Fill here>

        // 이미 존재할 경우
        if(_gsymtable.containsKey(varname)) {
            System.out.println("global 변수 "+varname+"이 이미 존재");
            return;
        }

        // 글로벌 변수 해시맵에 추가. globalVarID는 1씩 증가
        _gsymtable.put(varname, new VarInfo(type, _globalVarID++));
	}

	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>

        // 이미 존재할 경우
        if(_lsymtable.containsKey(varname)) {
            System.out.println("local 변수 "+varname+"이 이미 존재");
            return;
        }

        // 로컬 변수 해시맵에 추가. localVarID는 1씩 증가
        _lsymtable.put(varname, new VarInfo(type, _localVarID++, initVar));
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>

        // 이미 존재할 경우
        if(_gsymtable.containsKey(varname)) {
            System.out.println("global 변수 "+varname+"이 이미 존재");
            return;
        }

        // 글로벌 변수 해시맵에 추가. globalVarID는 1씩 증가
        _gsymtable.put(varname, new VarInfo(type, _globalVarID++, initVar));
	}

	void putParams(MiniCParser.ParamsContext params) {
		for(int i = 0; i < params.param().size(); i++) {
            //<Fill here>
            ParamContext p = params.param(i);

            String type = p.type_spec().getText();
            String varname = p.IDENT().getText();

            // int or void
            if (p.getChildCount()==2) {
            	// int
            	if(type.equals("int")) {
            		_lsymtable.put(varname, new VarInfo(Type.INT, _localVarID++));
				}
            	// void
				else if(type.equals("void")) {
					_lsymtable.put(varname, new VarInfo(Type.VOID, _localVarID++));
				}
			}

            // int[]
			else {
				_lsymtable.put(varname, new VarInfo(Type.INTARRAY, _localVarID++));
			}

		}
	}

	private void initFunTable() {
	    // println 함수
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		// main 함수
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";

		// 둘다 함수 해시맵에 추가
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}

	public String getFunSpecStr(String fname) {
		// <Fill here>
		// 함수의 JVM어셈블리어 코드 반환

		FInfo fInfo = _fsymtable.get(fname);
		// 함수 해시맵에 fname 이란 함수가 존재할 경우
		if(fInfo!=null)
			return fInfo.sigStr;

        // 존재하지 않을 경우
        return "getFunSpecStr Error";
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		// 함수의 JVM어셈블리어 코드 반환

		String funStr = "";
		// 오버로딩 함수 사용
		funStr += getFunSpecStr(ctx.IDENT().getText());
		return funStr;
	}

	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";
		String rtype = "";
		String res = "";

		// <Fill here>
		argtype += getParamTypesText(ctx.params());
		rtype += getTypeText(ctx.type_spec());

		res =  fname + "(" + argtype + ")" + rtype;

		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);

		return res;
	}

	String getVarId(String name){
		// <Fill here>

		// if local variable exist
		VarInfo lvar = _lsymtable.get(name);
		if(lvar!=null)
			return String.valueOf(lvar.id);

		// if global bariable exist
		VarInfo gvar = _lsymtable.get(name);
		if(gvar!=null)
			return String.valueOf(gvar.id);

		// error
        return "get Var Id Error";
	}

	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}

		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}

		return Type.ERROR;
	}
	String newLabel() {
		return "label" + _labelID++;
	}

	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		String sname = "";
		// 오버로딩 함수 사용
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		// 오버로딩 함수 사용
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}
	
}
