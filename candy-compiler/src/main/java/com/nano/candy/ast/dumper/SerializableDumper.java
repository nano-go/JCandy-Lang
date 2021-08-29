package com.nano.candy.ast.dumper;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Stmt;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Position;
import com.nano.common.text.StringUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;

public abstract class SerializableDumper implements AstDumper {
	
	/**
	 * This class represents a AST node.
	 */
	protected static class TreeNode implements Iterable<Map.Entry<String, Object>> {
		private LinkedHashMap<String, Object> attrs;
		private String nodeName;
		private Position pos;

		public TreeNode(String nodeName, Position pos) {
			this.nodeName = nodeName;
			this.pos = pos;
			this.attrs = new LinkedHashMap<>();
		}
		
		public String getNodeName() {
			return nodeName;
		}
		
		public Position getPos() {
			return pos;
		}
		
		private TreeNode put(String key, Object value) {
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
	
	/**
	 * Convert a syntanx tree node into a TreeNode object.
	 */
	private static TreeNode convert(ASTreeNode node) {
		Class<?> clazz = node.getClass();
		Field[] fields = clazz.getFields();
		TreeNode treeNode = new TreeNode(getNodeName(clazz), node.pos);
		for (Field f : fields) {
			if (!f.isAccessible()) {
				f.setAccessible(true);
			}	
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers)) {
				continue;
			}
			String name;
			if (f.isAnnotationPresent(FieldName.class)) {
				FieldName fieldName = f.getAnnotation(FieldName.class);
				name = fieldName.value();
			} else if (Modifier.isPublic(modifiers)) {
				name = f.getName();
			} else continue;
			Object fieldVal = toValue(node, f);
			if (fieldVal != null)
				treeNode.put(name, fieldVal);	
		}
		return treeNode;
	}
	
	private static Object toValue(ASTreeNode node, Field field) {
		try {
			Object obj = field.get(node);
			return convert(obj);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	private static Object convert(Object obj) {
		if (obj instanceof ASTreeNode) {
			return convert((ASTreeNode)obj);
		} else if (obj instanceof List) {
			List list = (List) obj;
			ArrayList<Object> res = new ArrayList<>(list.size());
			for (Object element : list) {
				res.add(convert((element)));
			}
			return res;
		} else if (obj instanceof Optional) {
			Optional op = (Optional) obj;
			if (op.isPresent()) {
				return convert(op.get());
			}
			return null;
		} else if (obj.getClass().isArray()){
			Object[] arr = (Object[]) obj;
			Object[] res = new Object[arr.length];
			for (int i = 0; i < arr.length; i++) {
				res[i] = convert(arr[i]);
			}
			return res;
		}
		return obj;
	}

	private static String getNodeName(Class<?> clazz) {
		if (clazz.isAnnotationPresent(NodeName.class)) {
			NodeName nodeName = clazz.getAnnotation(NodeName.class) ;
			String name = nodeName.value() ;
			if (!StringUtils.isEmpty(name)) {
				return name ;
			}
		}
		return clazz.getSimpleName() ;
	}
	
	@Override
	public void dump(DumperOptions options, ASTreeNode node) {
		try {
			options.os.write(accept(node).getBytes());
			options.os.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	protected String accept(Object obj) {
		obj = convert(obj);
		if (obj instanceof TreeNode) {
			return serialize((TreeNode)obj);
		} else if (obj instanceof List) {
			return serialize((List)obj);
		} else if (obj instanceof Position) {
			return serialize((Position)obj);
		} else if (obj instanceof String) {
			return serialize((String)obj);
		} else if (obj instanceof TokenKind) {
			return serialize((TokenKind)obj);
		} else if (obj instanceof Expr.Argument) {
			return serialize((Expr.Argument)obj);
		} else if (obj instanceof Stmt.Parameters) {
			return serialize((Stmt.Parameters)obj);
		}
		if (obj.getClass().isArray()) {
			return serialize((Object[])obj);
		}
		return obj.toString();
	}

	/**
	 * This returned value can't be ignored.
	 */
	protected abstract String serialize(TreeNode treeNode);
	protected abstract String serialize(TokenKind tk);
	protected abstract String serialize(Object[] array);
	protected abstract String serialize(List list);
	protected abstract String serialize(String str);
	protected abstract String serialize(Position pos);
	protected abstract String serialize(Stmt.Parameters obj);
	protected abstract String serialize(Expr.Argument obj);
}
