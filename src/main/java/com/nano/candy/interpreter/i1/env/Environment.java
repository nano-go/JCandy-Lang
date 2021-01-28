package com.nano.candy.interpreter.i1.env;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.utils.Position;
import java.util.HashMap;
import java.util.Objects;

public class Environment {

	private Position currentPosition ;
	private Scope scope ;
	private GlobalScope global ;
	private HashMap<Expr, Integer> distances ;
	
	public Environment() {
		this.global = new GlobalScope() ;
		this.scope = global ;
		this.distances = new HashMap<>() ;
	}
	
	public HashMap<Expr, Integer> setDistances(HashMap<Expr, Integer> distances) {
		this.distances.putAll(distances) ;
		return this.distances ;
	}
	
	public Scope enterScope() {
		return enterScope(new CommonScope(scope));
	}
	
	public Scope enterScope(Scope scope) {
		setScope(scope);
		return scope;
	}
	
	public Scope exitScope() {
		setScope(scope.getOutterScope());
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}
	
	public Scope getScopeAt(Expr expr) {
		if (!this.distances.containsKey(expr)) {
			return global ;
		}
		int distance = this.distances.get(expr) ;
		return getScopeAt(distance);
	}
	
	public Scope getScopeAt(int distance) {
		Scope scope = this.scope ;
		for (int i = 0; i < distance; i ++) {
			scope = scope.getOutterScope() ;
		}
		return scope ;
	}
	
	public int getDistance(Expr expr) {
		return distances.get(expr);
	}

	/**
	 * Syncronizes the position of the given tree node for reporting errors.
	 */
	public void syncLocation(ASTreeNode node) {
		syncLocation(node.pos) ;
	}

	/**
	 * Syncronizes the position for reporting errors.
	 */
	public void syncLocation(Position pos) {
		currentPosition = Objects.requireNonNull(pos) ;
	}

	public Position getCurrentLocation() {
		return currentPosition ;
	}
}
