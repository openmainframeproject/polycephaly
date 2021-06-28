package org.omp.polycephaly;

public class HelloWorld {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello World!!! (main)");
	}
	public boolean sayHelloWorld() {
		// TODO Auto-generated method stub
        System.out.println("Hello World! (stdout)");
        System.err.println("Hello World! (stderr)");
        return true;
	}
}