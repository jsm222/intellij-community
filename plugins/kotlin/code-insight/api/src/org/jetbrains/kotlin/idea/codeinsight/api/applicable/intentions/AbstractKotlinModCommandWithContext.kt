// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.kotlin.idea.codeinsight.api.applicable.intentions

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.codeinsight.api.applicable.KotlinApplicableToolWithContext
import org.jetbrains.kotlin.idea.codeinsight.api.applicable.prepareContextWithAnalyze
import org.jetbrains.kotlin.idea.codeinsight.api.applicators.KotlinApplicabilityRange
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.containsInside
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import kotlin.reflect.KClass

abstract class AbstractKotlinModCommandWithContext<ELEMENT : KtElement, CONTEXT>(private val clazz: KClass<ELEMENT>) :
    PsiUpdateModCommandAction<ELEMENT>(clazz.java),
    KotlinApplicableToolWithContext<ELEMENT, CONTEXT> {


    /**
     * Checks the intention's applicability based on [isApplicableByPsi] and [KotlinApplicabilityRange].
     */
    fun isApplicableTo(element: ELEMENT, caretOffset: Int): Boolean {
        if (!isApplicableByPsi(element)) return false
        val ranges = getApplicabilityRange().getApplicabilityRanges(element)
        if (ranges.isEmpty()) return false

        // A KotlinApplicabilityRange should be relative to the element, while `caretOffset` is absolute.
        val relativeCaretOffset = caretOffset - element.startOffset
        return ranges.any { it.containsOffset(relativeCaretOffset) }
    }

    protected open val isKotlinOnlyIntention: Boolean = true

    /**
     * Override if the action should be available on library sources.
     * It means that it won't modify the code of the current file e.g., it implements the interface in project code or change some settings
     */
    protected open fun checkFile(file: PsiFile): Boolean {
        return BaseIntentionAction.canModify(file)
    }

    fun getTarget(offset: Int, file: PsiFile): ELEMENT? {
        if (!checkFile(file)) return null

        val leaf1 = file.findElementAt(offset)
        val leaf2 = file.findElementAt(offset - 1)
        val commonParent = if (leaf1 != null && leaf2 != null) PsiTreeUtil.findCommonParent(leaf1, leaf2) else null

        var elementsToCheck: Sequence<PsiElement> = emptySequence()
        if (leaf1 != null) elementsToCheck += leaf1.parentsWithSelf.takeWhile { it != commonParent }
        if (leaf2 != null) elementsToCheck += leaf2.parentsWithSelf.takeWhile { it != commonParent }
        if (commonParent != null && commonParent !is PsiFile) elementsToCheck += commonParent.parentsWithSelf

        for (element in elementsToCheck) {
            @Suppress("UNCHECKED_CAST")
            if (clazz.isInstance(element)) {
                ProgressManager.checkCanceled()
                if (isApplicableTo(element as ELEMENT, offset)) {
                    return element
                }
                if (visitTargetTypeOnlyOnce()) {
                    return null
                }
            }
            if (element.textRange.containsInside(offset) && skipProcessingFurtherElementsAfter(element)) break
        }
        return null
    }

    fun getTarget(editor: Editor, file: PsiFile): ELEMENT? {
        if (isKotlinOnlyIntention && file !is KtFile) return null

        val offset = editor.caretModel.offset
        return getTarget(offset, file)
    }

    /** Whether to skip looking for targets after having processed the given element, which contains the cursor. */
    protected open fun skipProcessingFurtherElementsAfter(element: PsiElement): Boolean = element is KtBlockExpression

    protected open fun visitTargetTypeOnlyOnce(): Boolean = false

    override fun getPresentation(context: ActionContext, element: ELEMENT): Presentation? {
        if (!isApplicableTo(element, context.offset)) return null
        val analysisContext = prepareContextWithAnalyze(element) ?: return null
        return Presentation.of(getActionName(element, analysisContext))
    }

    final override fun invoke(context: ActionContext, element: ELEMENT, updater: ModPsiUpdater) {
        val analyzeContext = analyze(element) { prepareContext(element) } ?: return
        apply(element, analyzeContext, context.project, updater)
    }

    /**
     * Applies a fix to [element] using information from [context]. [apply] should not use the Analysis API due to performance concerns, as
     * [apply] is usually executed on the EDT. Any information that needs to come from the Analysis API should be supplied via
     * [prepareContext]. [apply] is executed in a write action if [element] is physical and [shouldApplyInWriteAction] returns `true`.
     */
    open fun apply(element: ELEMENT, context: CONTEXT, project: Project, updater: ModPsiUpdater?) {
        apply(element, context, project, editor = null)
    }

    override fun apply(element: ELEMENT, context: CONTEXT, project: Project, editor: Editor?) {
    }

}
