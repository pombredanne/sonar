/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.scan.filesystem;

import org.sonar.api.BatchExtension;

import java.io.File;

/**
 * Extension point to exclude some files from project scan. Some use-cases :
 * <ul>
 *   <li>exclude the files that are older than x days</li>
 *   <li>exclude the files which names start with Generated</li>
 * </ul>
 * @since 3.5
 * @deprecated in 4.0. Replaced by {@link InputFileFilter}.
 */
@Deprecated
public interface FileSystemFilter extends BatchExtension {

  /**
   * Plugins must not implement this interface. It is provided at runtime.
   */
  public interface Context {
    ModuleFileSystem fileSystem();

    FileType type();

    File relativeDir();

    /**
     * File path relative to source directory. Never return null.
     */
    String relativePath();

    /**
     * Absolute file path. Directory separator is slash, even on windows. Never return null.
     */
    String canonicalPath();
  }

  boolean accept(File file, Context context);
}
