// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.vfs.newvfs.persistent.dev.enumerator;

import com.intellij.openapi.vfs.newvfs.persistent.dev.intmultimaps.NonParallelNonPersistentIntToMultiIntMap;
import org.jetbrains.annotations.NotNull;
import org.junit.AssumptionViolatedException;

import java.io.IOException;
import java.nio.file.Path;

public class NonParallelNonPersistentInToMultiIntMapTest extends IntToMultiIntMapTestBase<NonParallelNonPersistentIntToMultiIntMap> {

  @Override
  protected NonParallelNonPersistentIntToMultiIntMap create(@NotNull Path tempDir) {
    return new NonParallelNonPersistentIntToMultiIntMap();
  }

  @Override
  void ZERO_IS_PROHIBITED_KEY() throws IOException {
    throw new AssumptionViolatedException("NonParallelNonPersistentIntToMultiIntMap is implemented it differently");
  }

  @Override
  void ZERO_IS_PROHIBITED_VALUE() throws IOException {
    throw new AssumptionViolatedException("NonParallelNonPersistentIntToMultiIntMap is implemented it differently");
  }
}
