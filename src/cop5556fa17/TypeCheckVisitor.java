package cop5556fa17;

import java.net.URL;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}
		
		//global symbol table
		SymbolTable symTable = new SymbolTable();

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		
		if(declaration_Variable.e!=null) 
			declaration_Variable.e.visit(this, arg);

		if(symTable.lookupType(declaration_Variable.name) != null)
			throw new SemanticException(declaration_Variable.firstToken, "Name already present in symbol table");
		
		symTable.insert(declaration_Variable.name, declaration_Variable);
		declaration_Variable.nodeType = TypeUtils.getType(declaration_Variable.type);
		
		if(declaration_Variable.e != null && 
				declaration_Variable.nodeType != declaration_Variable.e.nodeType)
			throw new SemanticException(declaration_Variable.firstToken, 
					"Types do not match "+declaration_Variable.nodeType+" "+declaration_Variable.e.nodeType);
		return declaration_Variable.type;
			
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		
		if(expression_Binary.e0!=null) 
			expression_Binary.e0.visit(this, arg);
		if(expression_Binary.e1!=null) 
			expression_Binary.e1.visit(this, arg);
		
		if(expression_Binary.e0 == null || expression_Binary.e1 == null)
			throw new SemanticException(expression_Binary.firstToken, 
					"Encountered null expressions expr0:"+expression_Binary.e0+" expr1:"+expression_Binary.e1);
		
		Kind op = expression_Binary.op;
		if (op == Kind.OP_EQ || op == Kind.OP_NEQ)
			expression_Binary.nodeType = Type.BOOLEAN;
		else if ((op == Kind.OP_GE || op == Kind.OP_GT ||
				op == Kind.OP_LT || op == Kind.OP_LE) && expression_Binary.e0.nodeType == Type.INTEGER)
			expression_Binary.nodeType = Type.BOOLEAN;
		else if ((op == Kind.OP_AND || op == Kind.OP_OR) && 
				(expression_Binary.e0.nodeType == Type.INTEGER ||
				expression_Binary.e0.nodeType == Type.BOOLEAN))
			expression_Binary.nodeType = expression_Binary.e0.nodeType;
		else if ((op == Kind.OP_DIV || op == Kind.OP_MINUS || op == Kind.OP_MOD ||
				op == Kind.OP_PLUS || op == Kind.OP_POWER || op == Kind.OP_TIMES) &&
				expression_Binary.e0.nodeType == Type.INTEGER)
			expression_Binary.nodeType = Type.INTEGER;
		else
			expression_Binary.nodeType = null;
		
		if(expression_Binary.e0.nodeType != expression_Binary.e1.nodeType ||
				expression_Binary.nodeType == null)
			throw new SemanticException(expression_Binary.firstToken, 
					"Encountered type mismatch between expressions or null type");				
		
		return expression_Binary.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		
		if(expression_Unary.e!=null) 
			expression_Unary.e.visit(this, arg);

		Type t = expression_Unary.e.nodeType;
		if (expression_Unary.op == Kind.OP_EXCL && 
				(t == Type.BOOLEAN || t == Type.INTEGER))
			expression_Unary.nodeType = t;
		else if ((expression_Unary.op == Kind.OP_PLUS || expression_Unary.op == Kind.OP_MINUS) &&
				t == Type.INTEGER)
			expression_Unary.nodeType = Type.INTEGER;
		else
			throw new SemanticException(expression_Unary.firstToken, 
					"Expected type Integer or Boolean but encountered Null");
		
		return expression_Unary.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		
		if(index.e0!=null) index.e0.visit(this, arg);
		if(index.e1!=null) index.e1.visit(this, arg);
		
		if(index.e0 ==  null || index.e1 == null)
			throw new SemanticException(index.firstToken,"Encountered null expression - expr0:"+index.e0+" expr1:"+index.e1);
		if (index.e0.nodeType != Type.INTEGER || index.e1.nodeType != Type.INTEGER)
			throw new SemanticException(index.firstToken, "Expression types are required to be INTEGER");
		index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r 
				&& index.e1.firstToken.kind == Kind.KW_a));
		
		return index.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		
		if(expression_PixelSelector.index!=null) 
			expression_PixelSelector.index.visit(this, arg);

		Type name = symTable.lookupType(expression_PixelSelector.name);
		if (name == Type.IMAGE)
			expression_PixelSelector.nodeType = Type.INTEGER;
		else if (expression_PixelSelector.index == null)
			expression_PixelSelector.nodeType = name;
		else
			throw new SemanticException(expression_PixelSelector.firstToken, 
					"Encountered type Null");
		
		return expression_PixelSelector.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		
		if(expression_Conditional.condition!=null) 
			expression_Conditional.condition.visit(this, arg);
		if(expression_Conditional.trueExpression!=null) 
			expression_Conditional.trueExpression.visit(this, arg);
		if(expression_Conditional.falseExpression!=null) 
			expression_Conditional.falseExpression.visit(this, arg);
		
		if(expression_Conditional.condition == null || expression_Conditional.trueExpression == null ||
				expression_Conditional.falseExpression == null)
			throw new SemanticException(expression_Conditional.firstToken, "Encountered null expressions");
		if(expression_Conditional.condition.nodeType != Type.BOOLEAN ||
				expression_Conditional.trueExpression.nodeType != expression_Conditional.falseExpression.nodeType)
			throw new SemanticException(expression_Conditional.firstToken, "Type mismatch of expressions");
		
		expression_Conditional.nodeType = expression_Conditional.trueExpression.nodeType;
		
		return expression_Conditional.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		
		if(declaration_Image.xSize!=null) 
			declaration_Image.xSize.visit(this, arg);
		if(declaration_Image.ySize!=null) 
			declaration_Image.ySize.visit(this, arg);
		if(declaration_Image.source!=null) 
			declaration_Image.source.visit(this, arg);
		
		if(symTable.lookupType(declaration_Image.name) != null)
			throw new SemanticException(declaration_Image.firstToken, "Name already present in symbol table");
		
		symTable.insert(declaration_Image.name, declaration_Image);
		declaration_Image.nodeType = Type.IMAGE;

		if(declaration_Image.xSize != null) {
			if(declaration_Image.ySize == null ||
				(declaration_Image.xSize.nodeType != Type.INTEGER || declaration_Image.ySize.nodeType != Type.INTEGER))
			throw new SemanticException(declaration_Image.firstToken, "Expected Integer types for xsize and ysize");
		}
		return declaration_Image.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		
		boolean url = true;
		try {
			URL u = new URL(source_StringLiteral.fileOrUrl);
			u.toURI();
		} catch(Exception e){
			url = false;
		}
		source_StringLiteral.nodeType = url ? Type.URL:Type.FILE;
		
		return source_StringLiteral.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		
		if(source_CommandLineParam.paramNum!=null) 
			source_CommandLineParam.paramNum.visit(this, arg);

		if(source_CommandLineParam.paramNum == null)
			throw new SemanticException(source_CommandLineParam.firstToken, "Encountered null expression");
		//source_CommandLineParam.nodeType = source_CommandLineParam.paramNum.nodeType;
		source_CommandLineParam.nodeType = null;
		if(source_CommandLineParam.paramNum.nodeType != Type.INTEGER)
			throw new SemanticException(source_CommandLineParam.paramNum.firstToken, 
					"Expected type Integer but encountered "+source_CommandLineParam.paramNum.nodeType);
		
		return source_CommandLineParam.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		
		source_Ident.nodeType = symTable.lookupType(source_Ident.name);
		if(source_Ident.nodeType != Type.FILE && source_Ident.nodeType != Type.URL)
			throw new SemanticException(source_Ident.firstToken, 
					"Expected type URL or FILE but received "+source_Ident.nodeType);
		
		return source_Ident.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		
		if(declaration_SourceSink.source!=null) 
			declaration_SourceSink.source.visit(this, arg);
		
		if(symTable.lookupType(declaration_SourceSink.name) != null)
			throw new SemanticException(declaration_SourceSink.firstToken, "Name already present in symbol table");
		
		symTable.insert(declaration_SourceSink.name, declaration_SourceSink);
		declaration_SourceSink.nodeType = TypeUtils.getType(declaration_SourceSink.type);
		
		if(declaration_SourceSink.source.nodeType != null &&
				declaration_SourceSink.source.nodeType != declaration_SourceSink.nodeType)
			throw new SemanticException(declaration_SourceSink.firstToken, "Type mismatch "+declaration_SourceSink.source.nodeType+
					" "+declaration_SourceSink.nodeType);
		
		return declaration_SourceSink.type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		
		expression_IntLit.nodeType = Type.INTEGER;
		
		return expression_IntLit.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		
		if(expression_FunctionAppWithExprArg.arg!=null) 
			expression_FunctionAppWithExprArg.arg.visit(this, arg);
		
		if(expression_FunctionAppWithExprArg.arg.nodeType != Type.INTEGER)
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, 
					"Expected expression type to be INTEGER");
		
		expression_FunctionAppWithExprArg.nodeType = Type.INTEGER;
		
		return expression_FunctionAppWithExprArg.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		
		if(expression_FunctionAppWithIndexArg.arg!=null) 
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
		
		expression_FunctionAppWithIndexArg.nodeType = Type.INTEGER;
		
		return expression_FunctionAppWithIndexArg.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		
		expression_PredefinedName.nodeType = Type.INTEGER;
		
		return expression_PredefinedName.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		
		if(statement_Out.sink!=null) 
			statement_Out.sink.visit(this, arg);
	
		Declaration nameDec = symTable.lookupDec(statement_Out.name);
		statement_Out.setDec(nameDec);
		
		if(statement_Out.sink == null)
			throw new SemanticException(statement_Out.firstToken, "Encountered sink as null");
		
		if (nameDec == null || !((nameDec.nodeType == Type.INTEGER || nameDec.nodeType == Type.BOOLEAN) 
				&& statement_Out.sink.nodeType == Type.SCREEN) && !(nameDec.nodeType == Type.IMAGE && 
				(statement_Out.sink.nodeType == Type.FILE || statement_Out.sink.nodeType == Type.SCREEN)))
			throw new SemanticException(statement_Out.firstToken, "Encountered unexpected types");
		
		return statement_Out.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		
		if(statement_In.source!=null) 
			statement_In.source.visit(this, arg);
		
		Declaration namedec = symTable.lookupDec(statement_In.name);
		statement_In.setDec(namedec);
		// correction
		/* Type nameType = symTable.lookupType(statement_In.name);
		if(namedec == null || nameType == null || statement_In.source.nodeType == null)
			throw new SemanticException(statement_In.firstToken, "Encountered null - name.Declaration:"+namedec+
					" name.Type:"+nameType+" source:"+statement_In.source.nodeType);
		if(nameType != statement_In.source.nodeType)
			throw new SemanticException(statement_In.firstToken, "Encountered unexpected types"); */
		
		return statement_In.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		
		if(statement_Assign.e!=null) 
			statement_Assign.e.visit(this, arg);
		if(statement_Assign.lhs!=null) 
			statement_Assign.lhs.visit(this, arg);
		
		if(statement_Assign.e == null || statement_Assign.lhs == null)
			throw new SemanticException(statement_Assign.firstToken, 
					"Encountered null for LHS or expression");
		if(statement_Assign.lhs.nodeType == Type.IMAGE 
				&& statement_Assign.e.nodeType == Type.INTEGER) {
		}else if(((statement_Assign.lhs.nodeType == Type.INTEGER || 
				statement_Assign.lhs.nodeType == Type.BOOLEAN) && 
				statement_Assign.lhs.nodeType != statement_Assign.e.nodeType))
			throw new SemanticException(statement_Assign.firstToken, 
					"Types "+statement_Assign.lhs.nodeType+" and "+statement_Assign.e.nodeType+" are not compatible");
		
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		
		return statement_Assign.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		if(lhs.index!=null) lhs.index.visit(this, arg);
		
		Declaration nameDec = symTable.lookupDec(lhs.name);
		if(nameDec == null)
			throw new SemanticException(lhs.firstToken, "Encountered null declaration in symbol table for "+lhs.name);
		lhs.declaration = nameDec;
		lhs.nodeType = lhs.declaration.nodeType;
		if(lhs.index != null)
			lhs.setCartesian(lhs.index.isCartesian());
		
		return lhs.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		
		// TODO Auto-generated method stub
		sink_SCREEN.nodeType = Type.SCREEN;
		
		return sink_SCREEN.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		
		sink_Ident.nodeType = symTable.lookupType(sink_Ident.name);
		if(sink_Ident.nodeType != Type.FILE)
			new SemanticException(sink_Ident.firstToken, 
					"Expected type FILE but encountered "+sink_Ident.nodeType);
		
		return sink_Ident.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		
		expression_BooleanLit.nodeType = Type.BOOLEAN;
		
		return expression_BooleanLit.nodeType;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		
		expression_Ident.nodeType = symTable.lookupType(expression_Ident.name);
		
		return expression_Ident.nodeType;
		//throw new UnsupportedOperationException();
	}

}
