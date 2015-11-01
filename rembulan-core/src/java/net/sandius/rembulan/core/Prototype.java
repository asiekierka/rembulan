package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.GenericBuilder;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;

public class Prototype {

	// TODO: split into required and optional debug part

	/* constants used by the function */
	private final ReadOnlyArray<Object> consts;

	private final IntVector code;

	/* functions defined inside the function */
	private final ReadOnlyArray<Prototype> p;

	/* map from opcodes to source lines */
	private final IntVector lineinfo;

	/* information about local variables */
	private final ReadOnlyArray<LocalVariable> locvars;

	/* upvalue information */
	private final ReadOnlyArray<Upvalue.Desc> upvalues;

	private final String source;

	private final int linedefined;

	private final int lastlinedefined;

	private final int numparams;

	private final boolean is_vararg;

	private final int maxstacksize;

	public Prototype(
			ReadOnlyArray<Object> consts,
			IntVector code,
			ReadOnlyArray<Prototype> p,
			IntVector lineinfo,
			ReadOnlyArray<LocalVariable> locvars,
			ReadOnlyArray<Upvalue.Desc> upvalues,
			String source,
			int linedefined,
			int lastlinedefined,
			int numparams,
			boolean is_vararg,
			int maxstacksize) {

		Check.notNull(consts);
		Check.notNull(code);
		Check.notNull(p);
		// lineinfo may be null
		Check.notNull(locvars);
		Check.notNull(upvalues);
		Check.notNull(source);

		this.consts = consts;
		this.code = code;
		this.p = p;
		this.lineinfo = lineinfo;
		this.locvars = locvars;
		this.upvalues = upvalues;
		this.source = source;
		this.linedefined = linedefined;
		this.lastlinedefined = lastlinedefined;
		this.numparams = numparams;
		this.is_vararg = is_vararg;
		this.maxstacksize = maxstacksize;

		for (Object o : this.consts) {
			if (!isValidConstant(o)) {
				throw new IllegalArgumentException("Not a valid constant: " + o);
			}
		}
	}

	public static boolean isValidConstant(Object o) {
		LuaType tpe = LuaType.typeOf(o);
		switch (tpe) {
			case NIL:
			case BOOLEAN:
			case NUMBER:
			case STRING:
				return true;
			default:
				return false;
		}
	}

//	public static Prototype newEmptyPrototype(int n_upvalues) {
//		return new Prototype(
//				new ReadOnlyArray<Object>(new Object[0]),
//				IntVector.wrap(new int[0]),
//				new ReadOnlyArray<Prototype>(new Prototype[0]),
//				IntVector.wrap(new int[0]),
//				new ReadOnlyArray<LocalVariable>(new LocalVariable[0]),
//				new ReadOnlyArray<Upvalue.Desc>(new Upvalue.Desc[n_upvalues]),
//				null,
//				0, 0, 0, false, 0);
//	}

//	public static Prototype newEmptyPrototype() {
//		return Prototype.newEmptyPrototype(0);
//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Prototype prototype = (Prototype) o;

		if (linedefined != prototype.linedefined) return false;
		if (lastlinedefined != prototype.lastlinedefined) return false;
		if (numparams != prototype.numparams) return false;
		if (is_vararg != prototype.is_vararg) return false;
		if (maxstacksize != prototype.maxstacksize) return false;
		if (!consts.equals(prototype.consts)) return false;
		if (!code.equals(prototype.code)) return false;
		if (!p.equals(prototype.p)) return false;
		if (lineinfo != null ? !lineinfo.equals(prototype.lineinfo) : prototype.lineinfo != null)
			return false;
		if (!locvars.equals(prototype.locvars)) return false;
		if (!upvalues.equals(prototype.upvalues)) return false;
		return source.equals(prototype.source);
	}

	@Override
	public int hashCode() {
		int result = consts.hashCode();
		result = 31 * result + code.hashCode();
		result = 31 * result + p.hashCode();
		result = 31 * result + (lineinfo != null ? lineinfo.hashCode() : 0);
		result = 31 * result + locvars.hashCode();
		result = 31 * result + upvalues.hashCode();
		result = 31 * result + source.hashCode();
		result = 31 * result + linedefined;
		result = 31 * result + lastlinedefined;
		result = 31 * result + numparams;
		result = 31 * result + (is_vararg ? 1 : 0);
		result = 31 * result + maxstacksize;
		return result;
	}

	public ReadOnlyArray<Object> getConstants() {
		return consts;
	}

	public IntVector getCode() {
		return code;
	}

	public ReadOnlyArray<Prototype> getNestedPrototypes() {
		return p;
	}

	/** Get the name of a local variable.
	 *
	 * @param number the local variable number to look up
	 * @param pc the program counter
	 * @return the name, or null if not found
	 */
	public String getLocalVariableName(int number, int pc) {
		for (int i = 0; i < locvars.size() && locvars.get(i).beginPC <= pc; i++) {
			if (pc < locvars.get(i).endPC) {  // is variable active?
				number--;
				if (number == 0) {
					return locvars.get(i).variableName;
				}
			}
		}
		return null;  // not found
	}

	public ReadOnlyArray<LocalVariable> getLocalVariables() {
		return locvars;
	}

	public ReadOnlyArray<Upvalue.Desc> getUpValueDescriptions() {
		return upvalues;
	}

	public boolean hasUpValues() {
		return !upvalues.isEmpty();
	}

	public boolean hasLineInfo() {
		return lineinfo != null;
	}

	public int getLineAtPC(int pc) {
		return hasLineInfo() ? pc >= 0 && pc < lineinfo.length() ? lineinfo.get(pc) : -1 : -1;
	}

	public int getBeginLine() {
		return linedefined;
	}

	public int getEndLine() {
		return lastlinedefined;
	}

	public int getNumberOfParameters() {
		return numparams;
	}

	public boolean isVararg() {
		return is_vararg;
	}

	public int getMaximumStackSize() {
		return maxstacksize;
	}

	public String toString() {
		return source + ":" + linedefined + "-" + lastlinedefined;
	}

	public String getSource() {
		return source;
	}

	public String getShortSource() {
//		String name = source.tojstring();
		String name = source;
        if (name.startsWith("@") || name.startsWith("=")) {
			name = name.substring(1);
		}
		else if (name.startsWith("\033")) {
			name = "binary string";
		}
        return name;
	}

	public static class Builder implements GenericBuilder<Prototype> {
		public final ArrayList<Object> constants;
		public IntVector code;
		public final ArrayList<Prototype> p;

		public IntVector lineinfo;

		public final ArrayList<LocalVariable> locvars;
		public final ArrayList<Upvalue.Desc.Builder> upvalues;

		public String source;
		public int linedefined;
		public int lastlinedefined;
		public int numparams;
		public boolean is_vararg;
		public int maxstacksize;

		public Builder() {
			this.constants = new ArrayList<Object>();
			this.code = null;
			this.p = new ArrayList<Prototype>();
			this.lineinfo = null;
			this.locvars = new ArrayList<LocalVariable>();
			this.upvalues = new ArrayList<Upvalue.Desc.Builder>();
		}

		// FIXME: ugly! is there a point in having this even?
		public Builder(Prototype proto) {
			this.constants = new ArrayList<Object>();
			for (Object c : proto.consts) {
				constants.add(c);
			}

			this.p = new ArrayList<Prototype>();
			for (Prototype pp : proto.p) {
				this.p.add(pp);
			}

			this.lineinfo = proto.lineinfo;

			this.locvars = new ArrayList<LocalVariable>();
			for (LocalVariable lv : proto.locvars) {
				this.locvars.add(lv);
			}

			this.upvalues = new ArrayList<Upvalue.Desc.Builder>();
			for (Upvalue.Desc uv : proto.upvalues) {
				this.upvalues.add(new Upvalue.Desc.Builder(uv.name, uv.inStack, uv.index));
			}

			this.code = proto.code;

			this.source = proto.source;
			this.linedefined = proto.linedefined;
			this.lastlinedefined = proto.lastlinedefined;
			this.numparams = proto.numparams;
			this.is_vararg = proto.is_vararg;
			this.maxstacksize = proto.maxstacksize;
		}

		@Override
		public Prototype build() {
			Upvalue.Desc[] uvs0 = new Upvalue.Desc[this.upvalues.size()];
			for (int i = 0; i < this.upvalues.size(); i++) {
				uvs0[i] = this.upvalues.get(i).build();
			}
			ReadOnlyArray<Upvalue.Desc> uvs = ReadOnlyArray.wrap(uvs0);

			return new Prototype(
					ReadOnlyArray.fromCollection(Object.class, constants),
					code,
					ReadOnlyArray.fromCollection(Prototype.class, p),
					lineinfo,
					ReadOnlyArray.fromCollection(LocalVariable.class, locvars),
					uvs,
					source,
					linedefined,
					lastlinedefined,
					numparams,
					is_vararg,
					maxstacksize);
		}

	}

}
