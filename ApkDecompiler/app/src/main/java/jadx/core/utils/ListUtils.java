package jadx.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.nodes.BlockNode;

public class ListUtils {

	public static <T> boolean isSingleElement(@Nullable List<T> list, T obj) {
		if (list == null || list.size() != 1) {
			return false;
		}
		return Objects.equals(list.get(0), obj);
	}

	public static <T> boolean unorderedEquals(List<T> first, List<T> second) {
		if (first.size() != second.size()) {
			return false;
		}
		return first.containsAll(second);
	}

	public static <T, R> List<R> map(Collection<T> list, Function<T, R> mapFunc) {
		if (list == null || list.isEmpty()) {
			return Collections.emptyList();
		}
		List<R> result = new ArrayList<>(list.size());
		for (T t : list) {
			result.add(mapFunc.apply(t));
		}
		return result;
	}

	public static <T> T first(List<T> list) {
		return list.get(0);
	}

	public static <T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}

	public static List<BlockNode> distinctList(List<BlockNode> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	/**
	 * Replace old element to new one.
	 * Support null and empty immutable list (created by Collections.emptyList())
	 */
	public static <T> List<T> safeReplace(List<T> list, T oldObj, T newObj) {
		if (list == null || list.isEmpty()) {
			// immutable empty list
			List<T> newList = new ArrayList<>(1);
			newList.add(newObj);
			return newList;
		}
		list.remove(oldObj);
		list.add(newObj);
		return list;
	}
}
