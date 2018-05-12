/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, /*LITERAL, KEYWORD, SEPARATOR, OPERATOR,*/
		INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, LF/* \n */, CR/* \r */, EOF;
		
		/*Kind(String text, int len){
			this.text = text;
			this.len = len;
		}
		
		final String text;
		final int len;*/
	}
	
	// Need to revisit and check
	public static enum State {
		START, IN_DIGIT, IN_IDENTIFIER, IN_STRING, IN_COMMENT, END, AFTER_MINUS,
		AFTER_EQ, AFTER_EXCL, AFTER_LT, AFTER_GT, AFTER_POW, AFTER_DIV, AFTER_ESC;
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}
	

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  

	private int lines;

	private int cols;
	
	private int pos;
	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		cols = 1;
		lines = 1;
	}
	
	/**
	 * 
	 * @param pos
	 * @return
	 */
	private void skipSpace(){
		while(Character.isWhitespace(chars[pos])){
			if(chars[pos]=='\r'&& chars[pos+1]=='\n' || 
					chars[pos] == '\n' || chars[pos] == '\r'){
				if(chars[pos]=='\r'&& chars[pos+1]=='\n') pos++;
				lines++;
				cols=0;
			}
			pos++;cols++;
		}
	}
	
	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int startPos = 0;
		int line = 1;
		int posInLine = 1;
		//tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		//================= Implementation Start ============//
		int inp_len = chars.length, ch=-1;
		ParseChar c = null;
		State state = State.START;
		while(pos < inp_len || !c.used){ //if c.used pos++;
			if(c == null || c.used){
				if(state==State.START)skipSpace();
				c = new ParseChar(chars[pos], pos++,cols++);
				ch = c.ch;
			}
			if(state == State.START){
				c.used = true;
				startPos = c.pos;
				posInLine = c.col;
			}
			line = lines;

			switch(state){
			case START:{	
				switch(ch){
				////////// Separator Begin //////////
				case ',':
					tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case ';':
					tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '(':
					tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case ')':
					tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '[':
					tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case ']':
					tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
					state = State.START;
					break;
					////////// Separator End /////////
					////////// Operator Begin /////////
				case '|':
					tokens.add(new Token(Kind.OP_OR, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '@':
					tokens.add(new Token(Kind.OP_AT, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '?':
					tokens.add(new Token(Kind.OP_Q, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case ':':
					tokens.add(new Token(Kind.OP_COLON, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '&':
					tokens.add(new Token(Kind.OP_AND, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '+':
					tokens.add(new Token(Kind.OP_PLUS, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '%':
					tokens.add(new Token(Kind.OP_MOD, startPos, 1, line, posInLine));
					state = State.START;
					break;
				case '-':
					state = State.AFTER_MINUS;
					break;
				case '=':
					state = State.AFTER_EQ;
					break;
				case '!':
					state = State.AFTER_EXCL;
					break;
				case '<':
					state =  State.AFTER_LT;
					break;
				case '>':
					state = State.AFTER_GT;
					break;
				case '*':
					state = State.AFTER_POW;
					break;
				case '/':
					state = State.AFTER_DIV;
					break;
					////////// Operator End ///////////
				case '"':
					state = State.IN_STRING;
					break;
				default:
					if(ch == EOFchar){
						state = State.END;
					}else if(Character.isJavaIdentifierStart(ch)){
						state = State.IN_IDENTIFIER;
						//c.used = false;
					}else if(Character.isDigit(ch)){
						if(ch == '0'){
							tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, 1, line, posInLine));
							state = State.START;
						}else
							state = State.IN_DIGIT;
						//c.used = false;
					}else if(ch == '\n' || ch == '\r'){
						if(ch=='\r'&& chars[pos]=='\n') pos++;
						lines++;
						cols=1;
						state = State.START;
					}else if(!Character.isLetterOrDigit(ch) && ch != '$' &&
							ch != '_' && !Character.isWhitespace(ch)){
						throw new LexicalException("Illegal character found while parsing "+(char)ch
								+" at pos "+c.pos+"\n", c.pos);
					}else{
						state = State.START;
					}
					break;
				}
			}break;
			case IN_COMMENT:{
				//TODO revisit handling of new lines
				if(ch == '\n' || ch == '\r'){
					//End of comment token
					c.used = false;
					state = State.START;
				}else if(ch == EOFchar){
					state = State.END;
				}
			}break;
			case IN_STRING:{
				switch(ch){
				case '"':
					String t = new String(chars, startPos, c.pos-startPos+1);
					tokens.add(new Token(Kind.STRING_LITERAL, startPos, t.length(), line, posInLine));
					state = State.START;
					break;
				case '\\':
					state = State.AFTER_ESC;
					break;
				default:
					//TODO throw LexicalException
					if(ch == '\n' || ch == '\r' || ch == EOFchar)
						throw new LexicalException("Unexpected line terminator or EOF while parsing string literal at position "+c.pos+"\n", c.pos);
					break;
				}
			}break;
			case IN_DIGIT:{
					if(!Character.isDigit(ch)){
						String d = new String(chars, startPos, c.pos-startPos);
						try{
						Integer.parseInt(d);
						}catch(NumberFormatException e){
							throw new LexicalException("Number out of bounds of Integer data type - "+d+"\n", c.pos);
						}
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, c.pos-startPos, line, posInLine));
						c.used = false;
						state = State.START;
					}
				//}
			}break;
			case IN_IDENTIFIER:{
				if((!Character.isLetterOrDigit(ch) && ch != '$' &&
						ch != '_') || pos == inp_len){
					String t = new String(chars, startPos, c.pos-startPos);
					switch(t){
					///////////// Keyword Begin /////////////
					case "x":
						tokens.add(new Token(Kind.KW_x, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "X":
						tokens.add(new Token(Kind.KW_X, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "y":
						tokens.add(new Token(Kind.KW_y, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "Y":
						tokens.add(new Token(Kind.KW_Y, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "r":
						tokens.add(new Token(Kind.KW_r, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "R":
						tokens.add(new Token(Kind.KW_R, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "a":
						tokens.add(new Token(Kind.KW_a, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "A":
						tokens.add(new Token(Kind.KW_A, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "Z":
						tokens.add(new Token(Kind.KW_Z, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "DEF_X":
						tokens.add(new Token(Kind.KW_DEF_X, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "DEF_Y":
						tokens.add(new Token(Kind.KW_DEF_Y, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "SCREEN":
						tokens.add(new Token(Kind.KW_SCREEN, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "cart_x":
						tokens.add(new Token(Kind.KW_cart_x, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "cart_y":
						tokens.add(new Token(Kind.KW_cart_y, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "polar_a":
						tokens.add(new Token(Kind.KW_polar_a, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "polar_r":
						tokens.add(new Token(Kind.KW_polar_r, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "abs":
						tokens.add(new Token(Kind.KW_abs, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "sin":
						tokens.add(new Token(Kind.KW_sin, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "cos":
						tokens.add(new Token(Kind.KW_cos, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "atan":
						tokens.add(new Token(Kind.KW_atan, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "log":
						tokens.add(new Token(Kind.KW_log, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "image":
						tokens.add(new Token(Kind.KW_image, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "int":
						tokens.add(new Token(Kind.KW_int, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "boolean":
						tokens.add(new Token(Kind.KW_boolean, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "url":
						tokens.add(new Token(Kind.KW_url, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "file":
						tokens.add(new Token(Kind.KW_file, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
						//////////// Keyword End ///////////
						////////// Boolean Literal /////////
					case "true":
						tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					case "false":
						tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
						////////// Boolean Literal /////////
					default:
						tokens.add(new Token(Kind.IDENTIFIER, startPos, t.length(), line, posInLine));
						state = State.START;
						break;
					}
					c.used = false;
				}
			}break;
			case AFTER_ESC:{
				switch(ch){
				case 'b':
					state = State.IN_STRING;
					break;
				case 't':
					state = State.IN_STRING;
					break;
				case 'n':
					state = State.IN_STRING;
					break;
				case 'f':
					state = State.IN_STRING;
					break;
				case 'r':
					state = State.IN_STRING;
					break;
				case '"':
					state = State.IN_STRING;
					break;
				case '\'':
					state = State.IN_STRING;
					break;
				case '\\':
					state = State.IN_STRING;
					break;
				default:
					//TODO throw LexicalException
					throw new LexicalException("Unexpected escape character "+(char)ch+" found while parsing at pos "+c.pos+"\n", c.pos);
					//break;
				}
			}break;
			case AFTER_DIV:{
				switch(ch){
				case '/':
					state = State.IN_COMMENT;
					break;
				default:
					tokens.add(new Token(Kind.OP_DIV, startPos, 1, line, posInLine));
					state = State.START;
					c.used = false;
					break;
				}
			}break;
			case AFTER_EQ:{
				switch(ch){
				case '=':
					tokens.add(new Token(Kind.OP_EQ, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1, line, posInLine));
					state = State.START;
					c.used = false;
					break;
				}
			}break;
			case AFTER_EXCL:{
				switch(ch){
				case '=':
					tokens.add(new Token(Kind.OP_NEQ, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_EXCL, startPos, 1, line, posInLine));
					c.used = false;
					state = State.START;
					break;
				}
			}break;
			case AFTER_GT:{
				switch(ch){
				case '=':
					tokens.add(new Token(Kind.OP_GE, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_GT, startPos, 1, line, posInLine));
					state =  State.START;
					c.used = false;
					break;
				}
			}break;
			case AFTER_LT:{
				switch(ch){
				case '=':
					tokens.add(new Token(Kind.OP_LE, startPos, 2, line, posInLine));
					state = State.START;
					break;
				case '-':
					tokens.add(new Token(Kind.OP_LARROW, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_LT, startPos, 1, line, posInLine));
					state = State.START;
					c.used = false;
					break;
				}
			}break;
			case AFTER_MINUS:{
				switch(ch){
				case '>':
					tokens.add(new Token(Kind.OP_RARROW, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_MINUS, startPos, 1, line, posInLine));
					state = State.START;
					c.used = false;
					break;
				}
			}break;
			case AFTER_POW:{
				switch(ch){
				case '*':
					tokens.add(new Token(Kind.OP_POWER, startPos, 2, line, posInLine));
					state = State.START;
					break;
				default:
					tokens.add(new Token(Kind.OP_TIMES, startPos, 1, line, posInLine));
					state = State.START;
					c.used = false;
					break;
				}
			}break;
			default:
				break;
			}
			if(state == State.END){
				tokens.add(new Token(Kind.EOF, c.pos, 0, line, posInLine));
				return this;
			}
		}
		return this;
	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}
	
	class ParseChar{
		public final int ch, pos, col;
		public boolean used = true;
		public ParseChar(int ch, int pos, int col){
			this.ch = ch;
			this.pos = pos;
			this.col = col;
		}
	}

}
