package p;

interface I {

	void m();

}

class A implements I {
	public void m() {
	}

	public void m1() {
	}

	void test() {
		A a= new A();
		if (a instanceof I) {
			((A)a).m();
		}
	}
}
