package com.nano.candy.ast.printer;
import com.nano.candy.utils.Position;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;

public class TreeNode implements Iterable<Map.Entry<String, Object>> {
	
	private LinkedHashMap<String, Object> attrs;
	private String nodeName;
	private Position pos;

	public TreeNode(String nodeName, Position pos) {
		this.nodeName = nodeName;
		this.pos = pos;
		this.attrs = new LinkedHashMap<>();
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public Position getPos() {
		return pos;
	}
	
	public TreeNode put(String key, Object value) {
		attrs.put(key, value);
		return this;
	}
	
	public Object get(String key) {
		return attrs.get(key);
	}
	
	@Override
	public Iterator<Map.Entry<String, Object>> iterator() {
		return attrs.entrySet().iterator();
	}

	@Override
	public Spliterator<Map.Entry<String, Object>> spliterator() {
		return attrs.entrySet().spliterator();
	}
	
	 
}
