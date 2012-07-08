package p;

public class C extends D {

	@Override
	public void m() {
	}

	public void m1() {
	}

	void test() {
		C o = new C();
		if (o instanceof C) {
			((C) o).m();
		}
	}
}
