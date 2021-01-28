package com.nano.candy.ast.printer;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Position;
import java.util.List;

public abstract class SerializablePrinter extends AstPrinter {

	@Override
	public String toString(ASTreeNode node) {
		return serialize(Convertor.convert(node));
	}
	
	protected String accept(Object obj) {
		if (obj instanceof TreeNode) {
			return serialize((TreeNode)obj) ;
		} else if (obj instanceof List) {
			return serialize((List)obj) ;
		} else if (obj instanceof Position) {
			return serialize((Position)obj) ;
		} else if (obj instanceof String) {
			return serialize((String)obj) ;
		} else if (obj instanceof TokenKind) {
			return serialize((TokenKind)obj) ;
		}
		if (obj.getClass().isArray()) {
			return serialize((Object[])obj) ;
		}
		return obj.toString() ;
	}

	protected abstract String serialize(TokenKind tk) ;
	protected abstract String serialize(Object[] array) ;
	protected abstract String serialize(List list) ;
	protected abstract String serialize(String str) ;
	protected abstract String serialize(Position pos) ;
	protected abstract String serialize(TreeNode treeNode) ;
}
