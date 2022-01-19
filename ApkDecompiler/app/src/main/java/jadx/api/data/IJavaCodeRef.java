package jadx.api.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IJavaCodeRef extends Comparable<IJavaCodeRef> {

	CodeRefType getAttachType();

	int getIndex();

	@Override
	default int compareTo(@NotNull IJavaCodeRef o) {
		return Integer.compare(getIndex(), o.getIndex());
	}
}
