package p;

public class C implements I {
	public void m() {
	}

	public void m1() {
	}

	void test() {
		C o = new C();
		if (o instanceof I) {
			((C) o).m();
		}
	}
}
