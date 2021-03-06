package com.jetbrains.edu.learning;

import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.UserDataHolder;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.newproject.ui.ValidationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main interface responsible for course project language settings such as JDK or interpreter
 *
 * @param <Settings> container type holds project settings state
 */
public abstract class LanguageSettings<Settings> {

  protected List<SettingsChangeListener> myListeners = new ArrayList<>();

  /**
   * Returns list of UI components that allows user to select course project settings such as project JDK or interpreter.
   *
   * @param course course of creating project
   * @param context used as cache. If provided, must have "session"-scope. Session could be one dialog or wizard.
   * @return list of UI components with project settings
   *
   * @see PyLanguageSettings
   */
  @NotNull
  public List<LabeledComponent<JComponent>> getLanguageSettingsComponents(@NotNull Course course, @Nullable UserDataHolder context) {
    return Collections.emptyList();
  }

  public void addSettingsChangeListener(@NotNull SettingsChangeListener listener) {
    myListeners.add(listener);
  }

  @Nullable
  public ValidationMessage validate(@Nullable Course course, @Nullable String courseLocation) {
    return null;
  }

  /**
   * Returns project settings associated with state of language settings UI component.
   * It should be passed into project generator to set chosen settings in course project.
   *
   * @return project settings object
   */
  @NotNull
  public abstract Settings getSettings();

  /**
   * Returns string representations of all possible language versions to be shown to a user
   */
  @NotNull
  public List<String> getLanguageVersions() {
    return Collections.emptyList();
  }

  protected void notifyListeners() {
    for (SettingsChangeListener listener : myListeners) {
      listener.settingsChanged();
    }
  }

  public interface SettingsChangeListener {
    void settingsChanged();
  }
}
