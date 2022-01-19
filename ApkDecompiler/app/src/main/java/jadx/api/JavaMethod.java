package jadx.api;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;

import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.MethodOverrideAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.Utils;

public final class JavaMethod implements JavaNode {
	private final MethodNode mth;
	private final JavaClass parent;

	JavaMethod(JavaClass cls, MethodNode m) {
		this.parent = cls;
		this.mth = m;
	}

	@Override
	public String getName() {
		return mth.getAlias();
	}

	@Override
	public String getFullName() {
		return mth.getMethodInfo().getFullName();
	}

	@Override
	public JavaClass getDeclaringClass() {
		return parent;
	}

	@Override
	public JavaClass getTopParentClass() {
		return parent.getTopParentClass();
	}

	public AccessInfo getAccessFlags() {
		return mth.getAccessFlags();
	}

	public List<ArgType> getArguments() {
		List<ArgType> infoArgTypes = mth.getMethodInfo().getArgumentsTypes();
		if (infoArgTypes.isEmpty()) {
			return Collections.emptyList();
		}
		List<ArgType> arguments = mth.getArgTypes();
		return Utils.collectionMap(arguments,
				type -> ArgType.tryToResolveClassAlias(mth.root(), type));
	}

	public ArgType getReturnType() {
		ArgType retType = mth.getReturnType();
		return ArgType.tryToResolveClassAlias(mth.root(), retType);
	}

	@Override
	public List<JavaNode> getUseIn() {
		return getDeclaringClass().getRootDecompiler().convertNodes(mth.getUseIn());
	}

	public List<JavaMethod> getOverrideRelatedMethods() {
		MethodOverrideAttr ovrdAttr = mth.get(AType.METHOD_OVERRIDE);
		if (ovrdAttr == null) {
			return Collections.emptyList();
		}
		JadxDecompiler decompiler = getDeclaringClass().getRootDecompiler();
		return ovrdAttr.getRelatedMthNodes().stream()
				.map(m -> ((JavaMethod) decompiler.convertNode(m)))
				.collect(Collectors.toList());
	}

	public boolean isConstructor() {
		return mth.getMethodInfo().isConstructor();
	}

	public boolean isClassInit() {
		return mth.getMethodInfo().isClassInit();
	}

	@Override
	public int getDecompiledLine() {
		return mth.getDecompiledLine();
	}

	@Override
	public int getDefPos() {
		return mth.getDefPosition();
	}

	@Override
	public void removeAlias() {
		this.mth.getMethodInfo().removeAlias();
	}

	/**
	 * Internal API. Not Stable!
	 */
	@ApiStatus.Internal
	public MethodNode getMethodNode() {
		return mth;
	}

	@Override
	public int hashCode() {
		return mth.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof JavaMethod && mth.equals(((JavaMethod) o).mth);
	}

	@Override
	public String toString() {
		return mth.toString();
	}
}
