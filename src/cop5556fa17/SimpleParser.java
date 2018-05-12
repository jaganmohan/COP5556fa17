package cop5556fa17;

import cop5556fa17.Scanner.*;
import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

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

	SimpleParser(Scanner scanner) {
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
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}
	
	void source() throws SyntaxException {
		if(isTokenKind(OP_AT)){
			t = getNextToken();
			expression();
		}else if(isTokenKind(STRING_LITERAL)
				|| isTokenKind(IDENTIFIER)){
			t = getNextToken();
		}else{
			throw new SyntaxException(t,"Encountered unexpected token in ::source "+t.getText()+" at position "+t.pos);
		}
	}
	
	void variableDecl() throws SyntaxException {
		//TODO implement this
		match(IDENTIFIER);
		if(isTokenKind(OP_ASSIGN)){
			t = getNextToken();
			expression();
		}
	}
	
	void sourceSinkDecl() throws SyntaxException{
		match(IDENTIFIER);
		match(OP_ASSIGN);
		source();
	}
	
	void imageDecl() throws SyntaxException {
		//match(KW_image);
		if(isTokenKind(LSQUARE)){
			t = getNextToken();
			expression();
			if(isTokenKind(COMMA)){
				t = getNextToken();
				expression();
				match(RSQUARE);
			}else{
				throw new SyntaxException(t,"Encountered unexpected token in ::imageDecl "+t.getText()+" at position "+t.pos);
			}
		}
		match(IDENTIFIER);
		if(isTokenKind(OP_LARROW)){
			t = getNextToken();
			source();
		}
	}
	
	void declaration() throws SyntaxException {
		//TODO implement this
		switch(t.kind){
		case KW_int:
		case KW_boolean:
			t = getNextToken();
			variableDecl();
			break;
		case KW_url:
		case KW_file:
			t = getNextToken();
			sourceSinkDecl();
			break;
		case KW_image:
			t = getNextToken();
			imageDecl();
			break;
		default:
			throw new SyntaxException(t,"Encountered unexpected token in ::declaration "+t.getText()+" at position "+t.pos);
		}
	}
	
	void statement() throws SyntaxException {
		match(IDENTIFIER);
		if(isTokenKind(OP_LARROW)){
			//ImageInStatement
			t = getNextToken();
			source();
		}else if(isTokenKind(OP_RARROW)){
			//ImageOutStatement
			//IDENTIFIER must be file
			t = getNextToken();
			if(isTokenKind(IDENTIFIER) 
					|| isTokenKind(KW_SCREEN)){
				t = getNextToken();
			}else
				throw new SyntaxException(t, "Encountered unexpected token in ImageOutStatement "+t.getText()+" at position "+t.pos);
		}else{
			//AssignmentStatement
			if(isTokenKind(LSQUARE)){
				t = getNextToken();
				match(LSQUARE);
				if(isTokenKind(KW_x)){
					t = getNextToken();
					match(COMMA);
					match(KW_y);
				}else if(isTokenKind(KW_r)){
					t = getNextToken();
					match(COMMA);
					match(KW_A);
				}else{
					throw new SyntaxException(t, "Encountered unexpected token in AssignmentStatement "+t.getText()+" at position "+t.pos);
				}
				match(RSQUARE);
				match(RSQUARE);
			}
			match(OP_ASSIGN);
			expression();
		}
	}

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 *  
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		//TODO  implement this
		match(IDENTIFIER);
		while(!isTokenKind(EOF)){
			if(isTokenKind(KW_int) || isTokenKind(KW_boolean)||
					isTokenKind(KW_url)|| isTokenKind(KW_file)|| isTokenKind(KW_image)){
				declaration();
			}else if(t.kind == IDENTIFIER){
				statement();
			}else{
				throw new SyntaxException(t, "Encountered unexpected token in ::program "+t.getText()+" at position "+t.pos);
			}
			match(SEMI);
		}
	}
	
	void primary() throws SyntaxException {
		switch(t.kind){
		case INTEGER_LITERAL:
			t = getNextToken();
			break;
		case BOOLEAN_LITERAL:
			t = getNextToken();
			break;
		case LPAREN:
			t = getNextToken();
			expression();
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
			t = getNextToken();
			switch(t.kind){
			case LPAREN:
				t = getNextToken();
				expression();
				match(RPAREN);
				break;
			case LSQUARE:
				t = getNextToken();
				//selector
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
				break;
			default:
				throw new SyntaxException(t,"Encountered unexpected token while parsing FunctionApplication "+t.getText()+" at position "+t.pos);
			}
			break;
		default:
			throw new SyntaxException(t,"Encountered unexpected token in ::primary "+t.getText()+" at position "+t.pos);
		}
	}
	
	void unaryExpression() throws SyntaxException {
		switch(t.kind){
		
		case OP_PLUS:
		case OP_MINUS:
		case OP_EXCL:
			t = getNextToken();
			unaryExpression();
			break;
		case IDENTIFIER:
			t = getNextToken();
			//IdentOrPixelSelectorExpression 
			if(isTokenKind(LSQUARE)){
				t = getNextToken();
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
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
			t = getNextToken();
			break;
		default:
			//primary
			primary();
			break;
		}
	}
	
	void multExpression() throws SyntaxException {
		unaryExpression();
		while(isTokenKind(OP_TIMES) || isTokenKind(OP_DIV)
				|| isTokenKind(OP_MOD)){
			t = getNextToken();
			unaryExpression();
		}
	}
	
	void addExpression() throws SyntaxException {
		multExpression();
		while(isTokenKind(OP_PLUS) 
				|| isTokenKind(OP_MINUS)){
			t = getNextToken();
			multExpression();
		}
	}
	
	void relExpression() throws SyntaxException {
		addExpression();
		while(isTokenKind(OP_LT) || isTokenKind(OP_GT) 
				|| isTokenKind(OP_LE) || isTokenKind(OP_GE)){
			t = getNextToken();
			addExpression();
		}
	}

	void eqExpression() throws SyntaxException {
		relExpression();
		while(isTokenKind(OP_EQ) || isTokenKind(OP_NEQ)){
			t = getNextToken();
			relExpression();
		}
	}
	
	void andExpression() throws SyntaxException {
		eqExpression();
		while(isTokenKind(OP_AND)){
			t = getNextToken();
			eqExpression();
		}
	}
	
	void orExpression() throws SyntaxException {
		andExpression();
		while(isTokenKind(OP_OR)){
			t = getNextToken();
			andExpression();
		}
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {
		//TODO implement this.
		orExpression();
		if(isTokenKind(OP_Q)){
			t = getNextToken();
			expression();
			match(OP_COLON);
			expression();
		}	
	}
	
	private Token getNextToken() throws UnsupportedOperationException {
		if(scanner.hasTokens())
			return scanner.nextToken();
		throw new UnsupportedOperationException("[ERROR] No more tokens found");
	}
	
	private boolean isTokenKind(Kind tokenType){
		if (t.kind == tokenType)
			return true;
		return false;
	}

	private Token match(Kind tokenType) throws SyntaxException {
		if(t.kind == tokenType) {
			t = getNextToken();
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
