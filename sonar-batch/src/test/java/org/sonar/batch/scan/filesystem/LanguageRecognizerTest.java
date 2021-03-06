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
package org.sonar.batch.scan.filesystem;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LanguageRecognizerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void test_sanitizeExtension() throws Exception {
    assertThat(LanguageRecognizer.sanitizeExtension(".cbl")).isEqualTo("cbl");
    assertThat(LanguageRecognizer.sanitizeExtension(".CBL")).isEqualTo("cbl");
    assertThat(LanguageRecognizer.sanitizeExtension("CBL")).isEqualTo("cbl");
    assertThat(LanguageRecognizer.sanitizeExtension("cbl")).isEqualTo("cbl");
  }

  @Test
  public void search_by_file_extension() throws Exception {
    Language[] languages = new Language[]{new MockLanguage("java", "java", "jav"), new MockLanguage("cobol", "cbl", "cob")};
    LanguageRecognizer recognizer = new LanguageRecognizer(newProject("java"), languages);

    recognizer.start();
    assertThat(recognizer.of(temp.newFile("Foo.java"))).isEqualTo("java");
    assertThat(recognizer.of(temp.newFile("Foo.JAVA"))).isEqualTo("java");
    assertThat(recognizer.of(temp.newFile("Foo.jav"))).isEqualTo("java");
    assertThat(recognizer.of(temp.newFile("Foo.Jav"))).isEqualTo("java");

    // multi-language is not supported yet -> filter on project language
    assertThat(recognizer.of(temp.newFile("abc.cbl"))).isNull();
    assertThat(recognizer.of(temp.newFile("abc.CBL"))).isNull();
    assertThat(recognizer.of(temp.newFile("abc.php"))).isNull();
    assertThat(recognizer.of(temp.newFile("abc"))).isNull();
    recognizer.stop();
  }

  @Test
  public void fail_if_conflict_of_file_extensions() throws Exception {
    Language[] languages = new Language[]{new MockLanguage("java", "java"), new MockLanguage("java2", "java", "java2")};

    LanguageRecognizer recognizer = spy(new LanguageRecognizer(newProject("java"), languages));
    recognizer.start();

    verify(recognizer).warnConflict(eq("java"), argThat(new ArgumentMatcher<Set<String>>() {
      @Override
      public boolean matches(Object o) {
        Set<String> set = (Set<String>) o;
        return set.contains("java") && set.contains("java2") && set.size() == 2;
      }
    }));
  }


  @Test
  public void plugin_can_declare_a_file_extension_twice_for_case_sensitivity() throws Exception {
    Language[] languages = new Language[]{new MockLanguage("abap", "abap", "ABAP")};

    LanguageRecognizer recognizer = new LanguageRecognizer(newProject("abap"), languages);
    recognizer.start();
    assertThat(recognizer.of(temp.newFile("abc.abap"))).isEqualTo("abap");
  }

  @Test
  public void language_with_no_extension() throws Exception {
    // abap here is associated to files without extension
    Language[] languages = new Language[]{new MockLanguage("java", "java"), new MockLanguage("abap")};

    // files without extension are detected only on abap projects
    LanguageRecognizer recognizer = new LanguageRecognizer(newProject("java"), languages);
    recognizer.start();
    assertThat(recognizer.of(temp.newFile("abc"))).isNull();
    recognizer.stop();

    recognizer = new LanguageRecognizer(newProject("abap"), languages);
    recognizer.start();
    assertThat(recognizer.of(temp.newFile("abc"))).isEqualTo("abap");
    recognizer.stop();

  }

  private Project newProject(String language) {
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(language);
    return project;
  }


  static class MockLanguage implements Language {
    private final String key;
    private final String[] extensions;

    MockLanguage(String key, String... extensions) {
      this.key = key;
      this.extensions = extensions;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public String getName() {
      return key;
    }

    @Override
    public String[] getFileSuffixes() {
      return extensions;
    }
  }
}
