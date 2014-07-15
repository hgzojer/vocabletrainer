package at.hgz.vocabletrainer;

import java.util.List;

import at.hgz.vocabletrainer.db.Dictionary;
import at.hgz.vocabletrainer.db.Vocable;
import at.hgz.vocabletrainer.set.TrainingElem;

public class State {
	
	private Dictionary dictionary;
	private List<Vocable> vocables;
	
	private List<TrainingElem> list;
	private TrainingElem vocable;
	private int right;
	private int wrong;
	private int todo;
	private boolean needInit = true;
	
	private int direction;
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		if (this.dictionary != dictionary) {
			this.dictionary = dictionary;
			needInit = true;
			list = null;
		}
	}

	public List<Vocable> getVocables() {
		return vocables;
	}

	public void setVocables(List<Vocable> vocables) {
		this.vocables = vocables;
	}

	public void incRight() {
		right++;
	}

	public void incWrong() {
		wrong++;
	}

	public void decTodo() {
		todo--;
	}

	public TrainingElem getVocable() {
		return vocable;
	}

	public void setVocable(TrainingElem vocable) {
		this.vocable = vocable;
	}

	public int getTodo() {
		return todo;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getWrong() {
		return wrong;
	}

	public void setWrong(int wrong) {
		this.wrong = wrong;
	}

	public void setTodo(int todo) {
		this.todo = todo;
	}

	public List<TrainingElem> getList() {
		return list;
	}

	public void setList(List<TrainingElem> list) {
		this.list = list;
	}

	public boolean isNeedInit() {
		return needInit;
	}

	public void setNeedInit(boolean needInit) {
		this.needInit = needInit;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
}
