package org.checkerframework.checker.modifiability;


import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationMirrorSet;


/**
* Annotated type factory for the Modifiability Checker.
*/
public class ModifiabilityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {


    private final AnnotationMirror UNMODIFIABLE;
    private final AnnotationMirrorSet setOfUnmodifiable;


    public ModifiabilityAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        this.UNMODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Unmodifiable.class);
        this.setOfUnmodifiable = AnnotationMirrorSet.singleton(UNMODIFIABLE);
        postInit();
    }


    @Override
    protected Set<AnnotationMirror> getEnumConstructorQualifiers() {
        return setOfUnmodifiable;
        }
}