package jadx.core.dex.visitors;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.EnumMapAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.FilledNewArrayNode;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.NewArrayNode;
import jadx.core.dex.instructions.SwitchInsn;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.LiteralArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.shrink.CodeShrinkVisitor;
import jadx.core.utils.InsnList;
import jadx.core.utils.InsnRemover;
import jadx.core.utils.InsnUtils;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxException;

@JadxVisitor(
		name = "ReSugarCode",
		desc = "Simplify synthetic or verbose code",
		runAfter = CodeShrinkVisitor.class
)
public class ReSugarCode extends AbstractVisitor {

	@Override
	public boolean visit(ClassNode cls) throws JadxException {
		initClsEnumMap(cls);
		return true;
	}

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		int k = 0;
		while (true) {
			boolean changed = false;
			InsnRemover remover = new InsnRemover(mth);
			for (BlockNode block : mth.getBasicBlocks()) {
				remover.setBlock(block);
				List<InsnNode> instructions = block.getInstructions();
				int size = instructions.size();
				for (int i = 0; i < size; i++) {
					changed |= process(mth, instructions, i, remover);
				}
				remover.perform();
			}
			if (changed) {
				CodeShrinkVisitor.shrinkMethod(mth);
			} else {
				break;
			}
			if (k++ > 100) {
				mth.addWarnComment("Reached limit for ReSugarCode iterations");
				break;
			}
		}
	}

	private static boolean process(MethodNode mth, List<InsnNode> instructions, int i, InsnRemover remover) {
		InsnNode insn = instructions.get(i);
		if (insn.contains(AFlag.REMOVE)) {
			return false;
		}
		switch (insn.getType()) {
			case NEW_ARRAY:
				return processNewArray(mth, (NewArrayNode) insn, instructions, i, remover);

			case SWITCH:
				return processEnumSwitch(mth, (SwitchInsn) insn);

			default:
				return false;
		}
	}

	/**
	 * Replace new-array and sequence of array-put to new filled-array instruction.
	 */
	private static boolean processNewArray(MethodNode mth, NewArrayNode newArrayInsn,
			List<InsnNode> instructions, int i, InsnRemover remover) {
		Object arrayLenConst = InsnUtils.getConstValueByArg(mth.root(), newArrayInsn.getArg(0));
		if (!(arrayLenConst instanceof LiteralArg)) {
			return false;
		}
		int len = (int) ((LiteralArg) arrayLenConst).getLiteral();
		if (len == 0) {
			return false;
		}
		RegisterArg arrArg = newArrayInsn.getResult();
		List<RegisterArg> useList = arrArg.getSVar().getUseList();
		if (useList.size() < len) {
			return false;
		}
		List<InsnNode> arrPuts = useList.stream()
				.map(InsnArg::getParentInsn)
				.filter(Objects::nonNull)
				.filter(insn -> insn.getType() == InsnType.APUT)
				.sorted(Comparator.comparingLong(insn -> {
					Object constVal = InsnUtils.getConstValueByArg(mth.root(), insn.getArg(1));
					if (constVal instanceof LiteralArg) {
						return ((LiteralArg) constVal).getLiteral();
					}
					return -1; // bad value, put at top to fail fast next check
				}))
				.collect(Collectors.toList());
		if (arrPuts.size() != len) {
			return false;
		}
		// expect all puts to be in same block
		if (!new HashSet<>(instructions).containsAll(arrPuts)) {
			return false;
		}

		for (int j = 0; j < len; j++) {
			InsnNode insn = arrPuts.get(j);
			if (!checkPutInsn(mth, insn, arrArg, j)) {
				return false;
			}
		}

		// checks complete, apply
		ArgType arrType = newArrayInsn.getArrayType();
		InsnNode filledArr = new FilledNewArrayNode(arrType.getArrayElement(), len);
		filledArr.setResult(arrArg.duplicate());

		for (InsnNode put : arrPuts) {
			filledArr.addArg(replaceConstInArg(mth, put.getArg(2)));
			remover.addAndUnbind(put);
		}
		remover.addAndUnbind(newArrayInsn);

		InsnNode lastPut = Utils.last(arrPuts);
		int replaceIndex = InsnList.getIndex(instructions, lastPut);
		instructions.set(replaceIndex, filledArr);
		return true;
	}

	private static InsnArg replaceConstInArg(MethodNode mth, InsnArg valueArg) {
		if (valueArg.isLiteral()) {
			FieldNode f = mth.getParentClass().getConstFieldByLiteralArg((LiteralArg) valueArg);
			if (f != null) {
				InsnNode fGet = new IndexInsnNode(InsnType.SGET, f.getFieldInfo(), 0);
				return InsnArg.wrapArg(fGet);
			}
		}
		return valueArg.duplicate();
	}

	private static boolean checkPutInsn(MethodNode mth, InsnNode insn, RegisterArg arrArg, int putIndex) {
		if (insn == null || insn.getType() != InsnType.APUT) {
			return false;
		}
		if (!arrArg.sameRegAndSVar(insn.getArg(0))) {
			return false;
		}
		InsnArg indexArg = insn.getArg(1);
		Object value = InsnUtils.getConstValueByArg(mth.root(), indexArg);
		if (value instanceof LiteralArg) {
			int index = (int) ((LiteralArg) value).getLiteral();
			return index == putIndex;
		}
		return false;
	}

	private static boolean processEnumSwitch(MethodNode mth, SwitchInsn insn) {
		InsnArg arg = insn.getArg(0);
		if (!arg.isInsnWrap()) {
			return false;
		}
		InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
		if (wrapInsn.getType() != InsnType.AGET) {
			return false;
		}
		EnumMapInfo enumMapInfo = checkEnumMapAccess(mth.root(), wrapInsn);
		if (enumMapInfo == null) {
			return false;
		}
		FieldNode enumMapField = enumMapInfo.getMapField();
		InsnArg invArg = enumMapInfo.getArg();

		EnumMapAttr.KeyValueMap valueMap = getEnumMap(mth, enumMapField);
		if (valueMap == null) {
			return false;
		}
		int caseCount = insn.getKeys().length;
		for (int i = 0; i < caseCount; i++) {
			Object key = insn.getKey(i);
			Object newKey = valueMap.get(key);
			if (newKey == null) {
				return false;
			}
		}
		// replace confirmed
		if (!insn.replaceArg(arg, invArg)) {
			return false;
		}
		for (int i = 0; i < caseCount; i++) {
			insn.modifyKey(i, valueMap.get(insn.getKey(i)));
		}
		enumMapField.add(AFlag.DONT_GENERATE);
		checkAndHideClass(enumMapField.getParentClass());
		return true;
	}

	private static void initClsEnumMap(ClassNode enumCls) {
		MethodNode clsInitMth = enumCls.getClassInitMth();
		if (clsInitMth == null || clsInitMth.isNoCode() || clsInitMth.getBasicBlocks() == null) {
			return;
		}
		EnumMapAttr mapAttr = new EnumMapAttr();
		for (BlockNode block : clsInitMth.getBasicBlocks()) {
			for (InsnNode insn : block.getInstructions()) {
				if (insn.getType() == InsnType.APUT) {
					addToEnumMap(enumCls.root(), mapAttr, insn);
				}
			}
		}
		if (!mapAttr.isEmpty()) {
			enumCls.addAttr(mapAttr);
		}
	}

	@Nullable
	private static EnumMapAttr.KeyValueMap getEnumMap(MethodNode mth, FieldNode field) {
		ClassNode syntheticClass = field.getParentClass();
		EnumMapAttr mapAttr = syntheticClass.get(AType.ENUM_MAP);
		if (mapAttr == null) {
			return null;
		}
		return mapAttr.getMap(field);
	}

	private static void addToEnumMap(RootNode root, EnumMapAttr mapAttr, InsnNode aputInsn) {
		InsnArg litArg = aputInsn.getArg(2);
		if (!litArg.isLiteral()) {
			return;
		}
		EnumMapInfo mapInfo = checkEnumMapAccess(root, aputInsn);
		if (mapInfo == null) {
			return;
		}
		InsnArg enumArg = mapInfo.getArg();
		FieldNode field = mapInfo.getMapField();
		if (field == null || !enumArg.isInsnWrap()) {
			return;
		}
		InsnNode sget = ((InsnWrapArg) enumArg).getWrapInsn();
		if (!(sget instanceof IndexInsnNode)) {
			return;
		}
		Object index = ((IndexInsnNode) sget).getIndex();
		if (!(index instanceof FieldInfo)) {
			return;
		}
		FieldNode fieldNode = root.resolveField((FieldInfo) index);
		if (fieldNode == null) {
			return;
		}
		int literal = (int) ((LiteralArg) litArg).getLiteral();
		mapAttr.add(field, literal, fieldNode);
	}

	public static EnumMapInfo checkEnumMapAccess(RootNode root, InsnNode checkInsn) {
		InsnArg sgetArg = checkInsn.getArg(0);
		InsnArg invArg = checkInsn.getArg(1);
		if (!sgetArg.isInsnWrap() || !invArg.isInsnWrap()) {
			return null;
		}
		InsnNode invInsn = ((InsnWrapArg) invArg).getWrapInsn();
		InsnNode sgetInsn = ((InsnWrapArg) sgetArg).getWrapInsn();
		if (invInsn.getType() != InsnType.INVOKE || sgetInsn.getType() != InsnType.SGET) {
			return null;
		}
		InvokeNode inv = (InvokeNode) invInsn;
		if (!inv.getCallMth().getShortId().equals("ordinal()I")) {
			return null;
		}
		ClassNode enumCls = root.resolveClass(inv.getCallMth().getDeclClass());
		if (enumCls == null || !enumCls.isEnum()) {
			return null;
		}
		Object index = ((IndexInsnNode) sgetInsn).getIndex();
		if (!(index instanceof FieldInfo)) {
			return null;
		}
		FieldNode enumMapField = root.resolveField((FieldInfo) index);
		if (enumMapField == null || !enumMapField.getAccessFlags().isSynthetic()) {
			return null;
		}
		return new EnumMapInfo(inv.getArg(0), enumMapField);
	}

	/**
	 * If all static final synthetic fields have DONT_GENERATE => hide whole class
	 */
	private static void checkAndHideClass(ClassNode cls) {
		for (FieldNode field : cls.getFields()) {
			AccessInfo af = field.getAccessFlags();
			if (af.isSynthetic() && af.isStatic() && af.isFinal()
					&& !field.contains(AFlag.DONT_GENERATE)) {
				return;
			}
		}
		cls.add(AFlag.DONT_GENERATE);
	}

	private static class EnumMapInfo {
		private final InsnArg arg;
		private final FieldNode mapField;

		public EnumMapInfo(InsnArg arg, FieldNode mapField) {
			this.arg = arg;
			this.mapField = mapField;
		}

		public InsnArg getArg() {
			return arg;
		}

		public FieldNode getMapField() {
			return mapField;
		}
	}
}
