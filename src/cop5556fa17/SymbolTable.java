package cop5556fa17;

import java.util.HashMap;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.Declaration;

//TODO implement this
public class SymbolTable {
	
	HashMap<String, Declaration> table = new HashMap<String, Declaration>();
	
	public SymbolTable() {
		
	}
	
	public Type lookupType(String name) {
		if(table.containsKey(name))
			return table.get(name).nodeType;
		else
			return null;
	}
	
	public Declaration lookupDec(String name) {
		if(table.containsKey(name))
			return table.get(name);
		else
			return null;
	}
	
	public void insert(String name, Declaration dec) {
		table.put(name, dec);
	}
	
}
