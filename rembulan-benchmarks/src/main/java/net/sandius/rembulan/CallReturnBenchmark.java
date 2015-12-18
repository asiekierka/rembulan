package net.sandius.rembulan;

import net.sandius.rembulan.core.FixedSizeRegisters;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.ReturnTarget;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(5)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class CallReturnBenchmark {

	public static void assertEquals(Object actual, Object expected) {
		if (actual == null) {
			if (expected != null) {
				throw new AssertionError("Expected " + expected + ", got null");
			}
		}
		else {
			if (!actual.equals(expected)) {
				throw new AssertionError("Expected " + expected + ", got " + actual);
			}
		}
	}

	public static abstract class JavaFunc {
		public abstract Object[] call(Object[] args);
	}

	public static class JavaFuncImpl extends JavaFunc {

		private final Long n;

		public JavaFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public Object[] call(Object[] args) {
			JavaFunc f = (JavaFunc) args[0];
			long l = ((Number) args[1]).longValue();

			if (l > 0) {
				Object[] result = f.call(new Object[] { f, l - 1 });
				Number m = (Number) result[0];

				return new Object[] { m.longValue() + 1 };
			}
			else {
				return new Object[] { n };
			}
		}
	}

	@Benchmark
	public void bmk_0_javaCall(Blackhole bh) {
		JavaFunc f = new JavaFuncImpl(100);

		Object[] result = f.call(new Object[] { f, 20 });

		assertEquals(result[0], 120L);
	}

	public static abstract class ViewFunc {
		public abstract void call(ObjectStack.View self, ObjectStack.View ret);
	}

	public static class ViewFuncImpl extends ViewFunc {

		private final Long n;

		public ViewFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public void call(ObjectStack.View self, ObjectStack.View ret) {
			Object r_0, r_1, r_2;
			int top;

			// load registers
			r_0 = self.get(0);
			r_1 = self.get(1);
			r_2 = self.get(2);
			top = self.getTop();

			ViewFunc f = (ViewFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				ObjectStack.View callSelf = self.from(1);
				ObjectStack.View callRet = self.from(0);

				callSelf.set(0, r_1);
				callSelf.set(1, r_2);
				callSelf.setTop(2);

				f.call(callSelf, callRet);

				r_0 = self.get(0);
				r_1 = self.get(1);
				r_2 = self.get(2);
				top = self.getTop();

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				ret.set(0, r_0);
				ret.setTop(1);
			}
			else {
				ret.set(0, n);
				ret.setTop(1);
			}
		}

	}

	@Benchmark
	public void bmk_1_sharedStackCall(RegistersBenchmark.ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		ViewFunc f = new ViewFuncImpl(100);
		ObjectStack.View root = os.rootView();
		root.set(0, f);
		root.set(1, 20);
		root.setTop(2);

		f.call(root, root);

		assertEquals(os.get(0), 120L);
	}

	public static abstract class DirectFunc {
		public abstract void call(ObjectStack objectStack, int base, int ret);
	}

	public static class DirectFuncImpl extends DirectFunc {

		private final Long n;

		public DirectFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public void call(ObjectStack objectStack, int base, int ret) {
			Object r_0, r_1, r_2;
			int top;

			r_0 = objectStack.get(base + 0);
			r_1 = objectStack.get(base + 1);
			r_2 = objectStack.get(base + 2);
			top = objectStack.getTop() - base;

			DirectFunc f = (DirectFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				objectStack.set(base + 1, r_1);
				objectStack.set(base + 2, r_2);
				objectStack.setTop(base + 3);

				f.call(objectStack, base + 1, base);

				r_0 = objectStack.get(base + 0);
				r_1 = objectStack.get(base + 1);
				r_2 = objectStack.get(base + 2);
				top = objectStack.getTop() - base;

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				objectStack.set(ret + 0, r_0);
				objectStack.setTop(ret + 1);
			}
			else {
				objectStack.set(ret + 0, n);
				objectStack.setTop(ret + 1);
			}

		}

	}

	@Benchmark
	public void bmk_2_directCall(RegistersBenchmark.ObjectStackHolder osh, Blackhole bh) {
		ObjectStack os = osh.objectStack;
		DirectFunc f = new DirectFuncImpl(100);
		os.set(0, f);
		os.set(1, 20);
		os.setTop(2);

		f.call(os, 0, 0);

		assertEquals(os.get(0), 120L);
	}

	public static abstract class AllocFunc {
		public abstract FixedSizeRegisters newRegisters();
		public abstract void call(FixedSizeRegisters self, ReturnTarget ret);
	}

	public class AllocFuncImpl extends AllocFunc {

		private final Long n;

		public AllocFuncImpl(long n) {
			this.n = n;
		}

		@Override
		public FixedSizeRegisters newRegisters() {
			return new FixedSizeRegisters(3);
		}

		@Override
		public void call(FixedSizeRegisters self, ReturnTarget ret) {
			Object r_0, r_1, r_2;
			int top;

			r_0 = self.get(0);
			r_1 = self.get(1);
			r_2 = self.get(2);
			top = self.getTop();

			AllocFunc f = (AllocFunc) r_0;
			long l = ((Number) r_1).longValue();

			if (l > 0) {

				r_2 = l - 1;
				r_1 = r_0;

				FixedSizeRegisters callSelf = f.newRegisters();
				callSelf.push(r_1);
				callSelf.push(r_2);

				f.call(callSelf, self.returnTargetFrom(0));

				r_0 = self.get(0);
				r_1 = self.get(1);
				r_2 = self.get(2);
				top = self.getTop();

				Number m = (Number) r_0;

				r_0 = m.longValue() + 1;

				ret.begin();
				ret.push(r_0);
				ret.end();
			}
			else {
				ret.begin();
				ret.push(n);
				ret.end();
			}

		}

	}

	@Benchmark
	public void bmk_3_allocRegistersCall(Blackhole bh) {
		AllocFunc f = new AllocFuncImpl(100);

		FixedSizeRegisters out = new FixedSizeRegisters(1);

		FixedSizeRegisters regs = f.newRegisters();
		regs.push(f);
		regs.push(20);

		f.call(regs, out.returnTargetFrom(0));

		assertEquals(out.get(0), 120L);
	}


}
