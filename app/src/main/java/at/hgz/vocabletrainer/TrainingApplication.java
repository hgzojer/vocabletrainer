package at.hgz.vocabletrainer;

import android.app.Application;
import android.util.SparseArray;

public class TrainingApplication extends Application {
	
	private static int nextId = 0;
	
	private static SparseArray<State> states = new SparseArray<State>();

	public static State getState(int id) {
		if (id < 0) {
			throw new RuntimeException("getState() - id=" + id);
		}
		State state = states.get(id);
		if (state == null) {
			state = new State(id);
			states.put(id, state);
		}
		return state;
	}
	
	public static int getNextId() {
		return nextId++;
	}
	
	public static void removeState(int id) {
		states.remove(id);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}