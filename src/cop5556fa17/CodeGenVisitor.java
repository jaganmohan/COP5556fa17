package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.ImageFrame;
//import cop5556fa17.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	
	FieldVisitor fv; // visitor of fields, need to check

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectively
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		//mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		handleLocalVariables(mainStart, mainEnd);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}
	
	public void handleLocalVariables(Label mainStart, Label mainEnd) throws Exception {
		//handles all predefined local variables including args
		
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 7);
		
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 8);
		
		//mv.visitLocalVariable("DEF_X", "I", null, mainStart, mainEnd, 9);
		//mv.visitLdcInsn(Integer.valueOf(256));
		//mv.visitVarInsn(ISTORE, 9);
		
		//mv.visitLocalVariable("DEF_Y", "I", null, mainStart, mainEnd, 10);
		//mv.visitLdcInsn(Integer.valueOf(256));
		//mv.visitVarInsn(ISTORE, 10);
		
		//mv.visitLocalVariable("Z", "I", null, mainStart, mainEnd, 11);
		//mv.visitLdcInsn(Integer.valueOf(16777215));
		//mv.visitVarInsn(ISTORE, 11);
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		
		// creates a static variable
		String name = declaration_Variable.name;
		if(declaration_Variable.nodeType == Type.INTEGER)
			fv = cw.visitField(ACC_STATIC, name, "I", null, null); 
		if(declaration_Variable.nodeType == Type.BOOLEAN)
			fv = cw.visitField(ACC_STATIC, name, "Z", null, null);
		
		// evaluates expression if not null which will be put on top of stack
		if(declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			if(declaration_Variable.nodeType == Type.INTEGER)
				mv.visitFieldInsn(PUTSTATIC, className, name, "I"); 
			if(declaration_Variable.nodeType == Type.BOOLEAN)
				mv.visitFieldInsn(PUTSTATIC, className, name, "Z");
		}
		fv.visitEnd();
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
				
		// Generate code to evaluate expression 0 and 1 and place values on top of stack
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		Kind op = expression_Binary.op;
		
		e0.visit(this, arg);
		e1.visit(this, arg);
		
		// Code to perform the op - implemented only for boolean and integer
		switch(op) {
		
		case OP_AND:
			// TODO need to check for objects
			if(e0.nodeType == Type.INTEGER || e0.nodeType == Type.BOOLEAN)
				mv.visitInsn(IAND);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_OR:
			if(e0.nodeType == Type.INTEGER || e0.nodeType == Type.BOOLEAN)
				mv.visitInsn(IOR);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_EQ:
			//TODO
			if(e0.nodeType == Type.INTEGER || e0.nodeType == Type.BOOLEAN) {
				Label eq_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, eq_l1);
				mv.visitInsn(ICONST_1);
				Label eq_l2 = new Label();
				mv.visitJumpInsn(GOTO, eq_l2);
				mv.visitLabel(eq_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(eq_l2);
			} else {
				Label eq_l1 = new Label();
				mv.visitJumpInsn(IF_ACMPNE, eq_l1);
				mv.visitInsn(ICONST_1);
				Label eq_l2 = new Label();
				mv.visitJumpInsn(GOTO, eq_l2);
				mv.visitLabel(eq_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(eq_l2);
			}
			break;
			
		case OP_NEQ:
			if(e0.nodeType == Type.INTEGER && e1.nodeType == Type.INTEGER) {
				Label neq_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, neq_l1);
				mv.visitInsn(ICONST_1);
				Label neq_l2 = new Label();
				mv.visitJumpInsn(GOTO, neq_l2);
				mv.visitLabel(neq_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(neq_l2);
			}else if (e0.nodeType == Type.BOOLEAN && e1.nodeType == Type.BOOLEAN) {
				//TODO for boolean
				mv.visitInsn(IXOR);
			}else {
				//TODO for other types
				Label neq_l1 = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, neq_l1);
				mv.visitInsn(ICONST_1);
				Label neq_l2 = new Label();
				mv.visitJumpInsn(GOTO, neq_l2);
				mv.visitLabel(neq_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(neq_l2);
			}
			break;
			
		case OP_LT:
			if(e0.nodeType == Type.INTEGER) {
				Label lt_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, lt_l1);
				mv.visitInsn(ICONST_1);
				Label lt_l2 = new Label();
				mv.visitJumpInsn(GOTO, lt_l2);
				mv.visitLabel(lt_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(lt_l2);
			} else {
				throw new UnsupportedOperationException();
			}
			break;
			
		case OP_GT:
			if(e0.nodeType == Type.INTEGER) {
				Label gt_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, gt_l1);
				mv.visitInsn(ICONST_1);
				Label gt_l2 = new Label();
				mv.visitJumpInsn(GOTO, gt_l2);
				mv.visitLabel(gt_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(gt_l2);
			} else {
				throw new UnsupportedOperationException();
			}
			break;
			
		case OP_LE:
			if(e0.nodeType == Type.INTEGER) {
				Label le_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, le_l1);
				mv.visitInsn(ICONST_1);
				Label le_l2 = new Label();
				mv.visitJumpInsn(GOTO, le_l2);
				mv.visitLabel(le_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(le_l2);
			} else {
				throw new UnsupportedOperationException();
			}
			break;
			
		case OP_GE:
			if(e0.nodeType == Type.INTEGER) {
				Label ge_l1 = new Label();
				// if false jump to label ge_l1
				mv.visitJumpInsn(IF_ICMPLT, ge_l1);
				// puts const 1 on top of stack representing true
				mv.visitInsn(ICONST_1);
				Label ge_l2 = new Label();
				// if true after executing top stmts jump to label ge_l2
				mv.visitJumpInsn(GOTO, ge_l2);
				// puts const 0 on top of stack representing false
				mv.visitLabel(ge_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ge_l2);
			} else {
				throw new UnsupportedOperationException();
			}
			break;
			
		case OP_PLUS:
			if(e0.nodeType == Type.INTEGER)
				mv.visitInsn(IADD);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_MINUS:
			if(e0.nodeType == Type.INTEGER)
				mv.visitInsn(ISUB);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_TIMES:
			if(e0.nodeType == Type.INTEGER)
				mv.visitInsn(IMUL);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_DIV:
			if(e0.nodeType == Type.INTEGER)
				mv.visitInsn(IDIV);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_MOD:
			if(e0.nodeType == Type.INTEGER)
				mv.visitInsn(IREM);
			else
				throw new UnsupportedOperationException();
			break;
			
		default:
			throw new UnsupportedOperationException();
			//break;
		}
				
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getNodeType());
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		
		// visit expression value and place its value on top of stack
		Expression e = expression_Unary.e;
		e.visit(this, arg);
		Kind op = expression_Unary.op;
		
		switch(op) {
		
		case OP_PLUS:
			if(e.nodeType != Type.INTEGER)
				throw new UnsupportedOperationException();
			break;
			
		case OP_MINUS:
			if(e.nodeType == Type.INTEGER)
				mv.visitInsn(INEG);
			else
				throw new UnsupportedOperationException();
			break;
			
		case OP_EXCL:
			if(e.nodeType == Type.BOOLEAN) {
				Label excl_l1 = new Label();
				// jump to label excl_l1 if top of stack 
				// equals to false or 0(in java sense)
				mv.visitJumpInsn(IFEQ, excl_l1);
				mv.visitInsn(ICONST_0);
				Label excl_l2 = new Label();
				mv.visitJumpInsn(GOTO, excl_l2);
				mv.visitLabel(excl_l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(excl_l2);
			}else if(e.nodeType == Type.INTEGER){
				//mv.visitInsn(ICONST_M1);
				mv.visitLdcInsn(Integer.MAX_VALUE);
				mv.visitInsn(IXOR);
			} else {
				throw new UnsupportedOperationException();
			}
			break;
		default:
			throw new UnsupportedOperationException();
			//break;
		}
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getNodeType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		
		if(!index.isCartesian()) {
			mv.visitInsn(DUP2);
			//cart_x
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", 
					"(II)I", false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			//cart_y
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", 
					"(II)I", false);
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {

		//load image onto stack
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, "Ljava/awt/image/BufferedImage;");

		//visit index and check cartesian or not
		expression_PixelSelector.index.visit(this, arg);

		// get pixel and leave on TOS
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getPixel", 
				"(Ljava/awt/image/BufferedImage;II)I", false);
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
				
		expression_Conditional.condition.visit(this, arg);
		
		// top of stack will be ICONST_1 if  cond expr is true
		mv.visitInsn(ICONST_1);
		Label cond_l1 = new Label();
		mv.visitJumpInsn(IF_ICMPNE, cond_l1);
		// leaves true expression on top of stack
		expression_Conditional.trueExpression.visit(this, arg);
		Label cond_l2 = new Label();
		mv.visitJumpInsn(GOTO, cond_l2);
		mv.visitLabel(cond_l1);
		// leaves false expression on top of stack
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(cond_l2);

//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getNodeType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		// creates a static variable
		String name = declaration_Image.name;
		//if(declaration_Variable.nodeType == Type.INTEGER)
		fv = cw.visitField(ACC_STATIC, name, "Ljava/awt/image/BufferedImage;", null, null);
		
		// puts source string on TOS
		if(declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);

			if(declaration_Image.xSize != null) {
				// Need to check if we need to convert to integer again
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}

			// read the image 
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", 
					"(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
		} else {
			if(declaration_Image.xSize != null) {
				// Need to check if we need to convert to integer again
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}else {
				//mv.visitVarInsn(ILOAD, 9);
				//mv.visitVarInsn(ILOAD, 10);
				mv.visitLdcInsn(256);
				mv.visitLdcInsn(256);
			}
			//call makeImage to create an image
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", 
					"(II)Ljava/awt/image/BufferedImage;", false);
		}
		
		mv.visitFieldInsn(PUTSTATIC, className, name, "Ljava/awt/image/BufferedImage;"); 

		fv.visitEnd();
		return null;
		//throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
		//throw new UnsupportedOperationException();
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		
		int arg_slot = 0;
		mv.visitVarInsn(ALOAD, arg_slot);
		
		// put value of expression on top of stack representing index
		source_CommandLineParam.paramNum.visit(this, arg);
		
		// push args[index] on top of stack
		mv.visitInsn(AALOAD);
		
		//CodeGenUtils.genLogTOS(GRADE, mv, source_CommandLineParam.getNodeType());
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		String name = declaration_SourceSink.name;
		fv = cw.visitField(ACC_STATIC, name, "Ljava/lang/String;", null, null);
		
		if(declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, name, "Ljava/lang/String;");
		}
		
		fv.visitEnd();
		return null;
		//throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
				
		mv.visitLdcInsn(Integer.valueOf(expression_IntLit.value));

//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {

		// put expression value on top of stack
		expression_FunctionAppWithExprArg.arg.visit(this, arg);

		// call function from RuntimeFunctions
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "abs", 
					"(I)I", false);
		}else {
			throw new UnsupportedOperationException();
		}

		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		
		Index idx = expression_FunctionAppWithIndexArg.arg;
		idx.e0.visit(this, arg);
		idx.e1.visit(this, arg);
		
		Kind fn = expression_FunctionAppWithIndexArg.function;
		switch(fn) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", 
					"(II)I", false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", 
					"(II)I", false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", 
					"(II)I", false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", 
					"(II)I", false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		
		switch(expression_PredefinedName.kind) {
		case KW_x:
			mv.visitVarInsn(ILOAD, 1);
			break;
		case KW_y:
			mv.visitVarInsn(ILOAD, 2);
			break;
		case KW_X:
			mv.visitVarInsn(ILOAD, 3);
			//mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", 
			//		"(Ljava/awt/image/BufferedImage;)I", false);
			break;
		case KW_Y:
			mv.visitVarInsn(ILOAD, 4);
			//mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", 
			//		"(Ljava/awt/image/BufferedImage;)I", false);
			break;
		case KW_r:
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", 
					"(II)I", false);
			break;
		case KW_a:
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", 
					"(II)I", false);
			break;
		case KW_R:
			mv.visitVarInsn(ILOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", 
					"(II)I", false);
			break;
		case KW_A:
			mv.visitLdcInsn(ICONST_0);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", 
					"(II)I", false);
			break;
		case KW_DEF_X:
			mv.visitLdcInsn(256);
			break;
		case KW_DEF_Y:
			mv.visitLdcInsn(256);
			break;
		case KW_Z:
			mv.visitLdcInsn(16777215);
			break;
		default:
			throw new UnsupportedOperationException("CodeGenVisitor::visitExpression_PredefinedName");
		}
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_PredefinedName.getNodeType());

		return null;
		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		
		Type decType = statement_Out.getDec().nodeType;
		// Get the field value on top of the stack
		if(decType == Type.INTEGER)
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
		else if(decType == Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
		else if(decType == Type.IMAGE)
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Ljava/awt/image/BufferedImage;");
		else
			throw new UnsupportedOperationException("CodeGenVisitor::visitStatement_Out");
		
		CodeGenUtils.genLogTOS(GRADE, mv, decType);

		if(decType == Type.INTEGER || decType == Type.BOOLEAN) {
			// Print to the console			
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			
			if(decType == Type.INTEGER)
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
			else if(decType == Type.BOOLEAN)
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		} else if(decType == Type.IMAGE) {
			statement_Out.sink.visit(this, arg);
		}
		
		// TODO HW6 remaining cases
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {

		// visiting source to get value on top of stack
		statement_In.source.visit(this, arg);
		// converting to correct type
		if(statement_In.getDec().nodeType == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", 
					"(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		} else if(statement_In.getDec().nodeType == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", 
					"(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		} else if(statement_In.getDec().nodeType == Type.IMAGE) {
			//TODO HW6
			Declaration_Image declaration_Image = (Declaration_Image)statement_In.getDec();
			if(declaration_Image.xSize != null) {
				// Need to check if we need to convert to integer again
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}

			// read the image 
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", 
					"(Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/awt/image/BufferedImage;", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
		}
		
		return null;
		
		//throw new UnsupportedOperationException();
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		//TODO  (see comment)
		
		if(statement_Assign.lhs.nodeType == Type.INTEGER || 
				statement_Assign.lhs.nodeType == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		} else if (statement_Assign.lhs.nodeType == Type.IMAGE) {
			
			// Initialize X and Y
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", 
					"(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 3);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", 
					"(Ljava/awt/image/BufferedImage;)I", false);
			mv.visitVarInsn(ISTORE, 4);
			
			// loop over all pixels of the image
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			Label l3 = new Label();
			mv.visitJumpInsn(GOTO, l3);
			Label l4 = new Label();
			mv.visitLabel(l4);
			//TODO
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			// End TODO
			mv.visitIincInsn(1, 1);
			mv.visitLabel(l3);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPLT, l4);
			mv.visitIincInsn(2, 1);
			mv.visitLabel(l1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPLT, l2);
		} else {
			throw new UnsupportedOperationException();
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		if(lhs.nodeType == Type.INTEGER) {			
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		} else if(lhs.nodeType == Type.BOOLEAN) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		} else if(lhs.nodeType == Type.IMAGE) {
			// load image reference
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, "Ljava/awt/image/BufferedImage;");
			// sets a pixel location on top of the stack
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			//if(lhs.index != null)
			//	lhs.index.visit(this, arg);
			// storing pixel at (x,y) represents a pixel location on top of the stack
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "setPixel", 
					"(ILjava/awt/image/BufferedImage;II)V", false);
		} else {
			throw new UnsupportedOperationException();
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		
		// write the image to screen
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "makeFrame", 
				"(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
		// remove frame from top of stack
		mv.visitInsn(POP);
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {

		// load file to be written to
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, "Ljava/lang/String;");
		// write the image to file
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "write", 
				"(Ljava/awt/image/BufferedImage;Ljava/lang/String;)V", false);

		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
				
		mv.visitLdcInsn(expression_BooleanLit.value);
		
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO for other types
				
		if(expression_Ident.nodeType == Type.INTEGER)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		else if(expression_Ident.nodeType == Type.BOOLEAN)
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		else
			throw new UnsupportedOperationException();

//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getNodeType());
		return null;
	}
	
}
