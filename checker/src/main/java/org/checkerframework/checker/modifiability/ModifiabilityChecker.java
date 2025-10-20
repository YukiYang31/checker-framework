package org.checkerframework.checker.modifiability;


import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SuppressWarningsPrefix;


/**
* Checker that verifies whether List objects are Modifiable or Unmodifiable.
*/
@SuppressWarningsPrefix({"modifiable", "unmodifiable"})
public class ModifiabilityChecker extends BaseTypeChecker {
}