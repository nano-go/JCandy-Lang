package com.nano.candy.ast.printer;
import com.nano.candy.ast.ASTreeNode;
import com.nano.common.text.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Convertor {
	
	public static TreeNode convert(ASTreeNode node) {
		Class<?> clazz = node.getClass();
		Field[] fields = clazz.getDeclaredFields();
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
				FieldName fieldName = f.getAnnotation(FieldName.class) ;
				name = fieldName.value();
			} else if (Modifier.isPublic(modifiers)) {
				name = f.getName();
			} else continue;
			treeNode.put(name, toValue(node, f)) ;	
		}
		return treeNode ;
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
			if (op.isPresent()) return convert(op.get());
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
}
