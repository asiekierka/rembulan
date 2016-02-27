package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.types.FunctionType;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import java.util.Map;

public class CompilationContext {

	private final Map<Prototype, CompilationUnit> units;

	public CompilationContext(Map<Prototype, CompilationUnit> units) {
		this.units = Check.notNull(units);
	}

	public FunctionType typeOf(Prototype prototype) {
		return units.containsKey(prototype) ? units.get(prototype).generic().functionType() : LuaTypes.FUNCTION;
	}

}
