package adif_relevamientos.com.ar.utils;

public class Barrier {
	public synchronized void block() throws InterruptedException {
		wait();
	}

	public synchronized void release() throws InterruptedException {
		notify();
	}

	public synchronized void releaseAll() throws InterruptedException {
		notifyAll();
	}
}
