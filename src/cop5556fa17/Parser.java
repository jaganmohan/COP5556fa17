package cop5556fa17;

import cop5556fa17.Scanner.*;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

import static cop5556fa17.Scanner.Kind.*;

import java.util.ArrayList;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		// gives EOF as token for empty input
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	Source source() throws SyntaxException {
		Source s = null;
		if(isTokenKind(OP_AT)){
			Token firstToken = t;
			consume();
			Expression paramNum = expression();
			s = new Source_CommandLineParam(firstToken, paramNum);
		}else if(isTokenKind(STRING_LITERAL)){
			s = new Source_StringLiteral(t, t.getText());
			consume();
		}else if(isTokenKind(IDENTIFIER)){
			s = new Source_Ident(t, t);
			consume();
		}else{
			throw new SyntaxException(t,"Encountered unexpected token in ::source "+t.getText()+" at position "+t.pos);
		}
		return s;
	}
	
	Declaration_Variable variableDecl() throws SyntaxException {
		//TODO implement this
		Token firstToken = t;
		Token type = t; //varType
		consume();
		Token name = t; //IDENTIFIER
		match(IDENTIFIER);
		Expression e = null;
		if(isTokenKind(OP_ASSIGN)){
			consume();
			e = expression(); //expression
		}
		return new Declaration_Variable(firstToken, type, name, e);
	}
	
	Declaration_SourceSink sourceSinkDecl() throws SyntaxException{
		Token firstToken = t;
		Token type = t; //SourceSinkType
		consume();
		Token name = t; //IDENTIFIER
		match(IDENTIFIER);
		match(OP_ASSIGN);
		Source s = source(); //source
		return new Declaration_SourceSink(firstToken, type, name, s);
	}
	
	Declaration_Image imageDecl() throws SyntaxException {
		//match(KW_image);
		Token firstToken = t;
		consume();
		Expression xSize = null, ySize = null;
		if(isTokenKind(LSQUARE)){
			consume();
			xSize = expression();
			if(isTokenKind(COMMA)){
				consume();
				ySize = expression();
				match(RSQUARE);
			}else{
				throw new SyntaxException(t,"Encountered unexpected token in ::imageDecl "+t.getText()+" at position "+t.pos);
			}
		}
		Token name = t;
		match(IDENTIFIER);
		Source s = null;
		if(isTokenKind(OP_LARROW)){
			consume();
			s = source();
		}
		return new Declaration_Image(firstToken, xSize, ySize, name, s);
	}
	
	Declaration declaration() throws SyntaxException {
		//TODO implement this
		Declaration dec = null;
		switch(t.kind){
		case KW_int:
		case KW_boolean:
			dec = variableDecl();
			break;
		case KW_url:
		case KW_file:
			dec = sourceSinkDecl();
			break;
		case KW_image:
			dec = imageDecl();
			break;
		default:
			throw new SyntaxException(t,"Encountered unexpected token in ::declaration "+t.getText()+" at position "+t.pos);
		}
		return dec;
	}
	
	Statement statement() throws SyntaxException {
		Statement stmt = null;
		Token firstToken = t;
		Token name = t;
		match(IDENTIFIER);
		if(isTokenKind(OP_LARROW)){
			//ImageInStatement
			consume();
			Source s = source();
			stmt = new Statement_In(firstToken, name, s);
		}else if(isTokenKind(OP_RARROW)){
			//ImageOutStatement
			//IDENTIFIER must be file
			consume();
			Sink s = null;
			if(isTokenKind(IDENTIFIER)){
				s = new Sink_Ident(t, t);
				consume();
			}else if(isTokenKind(KW_SCREEN)){
				s = new Sink_SCREEN(t);
				consume();
			}else
				throw new SyntaxException(t, "Encountered unexpected token in ImageOutStatement "+t.getText()+" at position "+t.pos);
			stmt = new Statement_Out(firstToken, name, s);
		}else{
			//AssignmentStatement
			LHS lhs = null;
			Index idx = null;
			if(isTokenKind(LSQUARE)){
				consume();
				match(LSQUARE);
				if(isTokenKind(KW_x)){
					Token firstTokenIdx = t;
					Expression e0 = new Expression_PredefinedName(t, t.kind);
					consume();
					match(COMMA);
					Expression e1 = new Expression_PredefinedName(t, t.kind);
					match(KW_y);
					idx = new Index(firstTokenIdx, e0, e1);
				}else if(isTokenKind(KW_r)){
					Expression e0 = new Expression_PredefinedName(t, t.kind);
					Token firstTokenIdx = t;
					consume();
					match(COMMA);
					Expression e1 = new Expression_PredefinedName(t, t.kind);
					match(KW_a);
					//match(KW_A);
					idx = new Index(firstTokenIdx, e0, e1);
				}else{
					throw new SyntaxException(t, "Encountered unexpected token in AssignmentStatement "+t.getText()+" at position "+t.pos);
				}
				match(RSQUARE);
				match(RSQUARE);
				lhs = new LHS(firstToken, name, idx);
			}
			lhs = new LHS(firstToken, name, idx);
			match(OP_ASSIGN);
			Expression e = expression();
			stmt = new Statement_Assign(firstToken, lhs, e);
		}
		return stmt;
	}

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 *  
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		ArrayList<ASTNode> decsAndStatements = 
				new ArrayList<ASTNode>();
		Token firstToken = t;
		Token name = t;
		match(IDENTIFIER);
		//consume();
		while(!isTokenKind(EOF)){
			if(isTokenKind(KW_int) || isTokenKind(KW_boolean)||
					isTokenKind(KW_url)|| isTokenKind(KW_file)|| isTokenKind(KW_image)){
				decsAndStatements.add(declaration());
			}else if(t.kind == IDENTIFIER){
				decsAndStatements.add(statement());
			}else{
				throw new SyntaxException(t, "Encountered unexpected token in ::program "+t.getText()+" at position "+t.pos);
			}
			match(SEMI);
		}
		return new Program(firstToken, name, decsAndStatements);
	}
	
	Expression primary() throws SyntaxException {
		Expression primExpr = null;
		Token firstToken = t;
		switch(t.kind){
		case INTEGER_LITERAL:
			primExpr = new Expression_IntLit(firstToken, t.intVal());
			consume();
			break;
		case BOOLEAN_LITERAL:
			primExpr = new Expression_BooleanLit(firstToken, Boolean.valueOf(t.getText()));
			consume();
			break;
		case LPAREN:
			consume();
			primExpr = expression();
			match(RPAREN);
			break;
		case KW_sin:
		case KW_cos:
		case KW_atan:
		case KW_abs:
		case KW_cart_x:
		case KW_cart_y:
		case KW_polar_a:
		case KW_polar_r:
			// FunctionApplication
			consume();
			switch(t.kind){
			case LPAREN:
				consume();
				Expression arg = expression();
				match(RPAREN);
				primExpr = new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, arg);
				break;
			case LSQUARE:
				consume(); //LSQUARE
				Index idx = selector();
				match(RSQUARE);
				primExpr = new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, idx);
				break;
			default:
				throw new SyntaxException(t,"Encountered unexpected token while parsing FunctionApplication "+t.getText()+" at position "+t.pos);
			}
			break;
		default:
			throw new SyntaxException(t,"Encountered unexpected token in ::primary "+t.getText()+" at position "+t.pos);
		}
		return primExpr;
	}
	
	Index selector() throws SyntaxException{
		Token firstTokenIdx = t;
		Expression e0 = expression();
		match(COMMA);
		Expression e1 = expression();
		return new Index(firstTokenIdx, e0, e1);
	}
	
	Expression unaryExpression() throws SyntaxException {
		Expression exprUnary = null;
		Token firstToken = t;
		switch(t.kind){
		case OP_PLUS:
		case OP_MINUS:
		case OP_EXCL:
			Token op = t;
			consume();
			Expression e = unaryExpression();
			exprUnary = new Expression_Unary(firstToken, op, e);
			break;
		case IDENTIFIER:
			Token name = t;
			exprUnary = new Expression_Ident(firstToken, name);
			consume();
			//IdentOrPixelSelectorExpression 
			if(isTokenKind(LSQUARE)){
				consume(); //LSQUARE
				Index idx = selector();
				match(RSQUARE);
				exprUnary = new Expression_PixelSelector(firstToken, name, idx);
			}
			break;
		case KW_x:
		case KW_y:
		case KW_r:
		case KW_a:
		case KW_X:
		case KW_Y: 
		case KW_Z:
		case KW_A:
		case KW_R:
		case KW_DEF_X:
		case KW_DEF_Y:
			// UnaryExpressionNotPlusOrMinus
			exprUnary = new Expression_PredefinedName(firstToken, t.kind);
			consume();
			break;
		default:
			//primary
			exprUnary = primary();
			break;
		}
		return exprUnary;
	}
	
	Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = unaryExpression(), e1;
		while(isTokenKind(OP_TIMES) || isTokenKind(OP_DIV)
				|| isTokenKind(OP_MOD)){
			Token op = t;
			consume();
			e1 = unaryExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = multExpression(), e1;
		while(isTokenKind(OP_PLUS) 
				|| isTokenKind(OP_MINUS)){
			Token op = t;
			consume();
			e1 = multExpression();
			e0 = new Expression_Binary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression relExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = addExpression(), e1;
		while(isTokenKind(OP_LT) || isTokenKind(OP_GT) 
				|| isTokenKind(OP_LE) || isTokenKind(OP_GE)){
			Token op = t;
			consume();
			e1 = addExpression();
			e0 = new Expression_Binary(first, e0, op, e1);
		}
		return e0;
	}

	Expression eqExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = relExpression(), e1;
		while(isTokenKind(OP_EQ) || isTokenKind(OP_NEQ)){
			Token op = t;
			consume();
			e1 = relExpression();
			e0 = new Expression_Binary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression andExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = eqExpression(), e1;
		while(isTokenKind(OP_AND)){
			Token op = t;
			consume();
			e1 = eqExpression();
			e0 = new Expression_Binary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression orExpression() throws SyntaxException {
		Token first = t;
		Expression e0 = andExpression(), e1;
		while(isTokenKind(OP_OR)){
			Token op = t;
			consume();
			e1 = andExpression();
			e0 = new Expression_Binary(first, e0, op, e1);
		}
		return e0;
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Token first = t;
		Expression e0 = orExpression();
		if(isTokenKind(OP_Q)){
			consume();
			Expression e1 = expression(), e2;
			match(OP_COLON);
			e2 = expression();
			e0 = new Expression_Conditional(first, e0, e1, e2);
		}
		return e0;
	}
	
	private void consume() throws SyntaxException {
		if(scanner.hasTokens())
			t = scanner.nextToken();
		else
			throw new SyntaxException(t,"[ERROR] No more tokens found");
	}
	
	private boolean isTokenKind(Kind tokenType){
		if (t.kind == tokenType)
			return true;
		return false;
	}

	private Token match(Kind tokenType) throws SyntaxException {
		if(t.kind == tokenType) {
			consume();
			return t;
		}
		String msg = "Expected "+tokenType+" at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, msg);
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
