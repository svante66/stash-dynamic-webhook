package com.polarislabs.stash.webhook;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

/**
 * Test case for the PostReceiveHook class.
 * 
 * @author Peter Leibiger (kuhnroyal)
 * @author Michael Irwin (mikesir87)
 * @author Bryan Varner (bvarner)
 */
public class PostReceiveHookTest {

  private static final String REGEX_TEST = ""
          + ".*/origin/(release/.*)\\/.* http://ci.ajenkinsbox.com/job/foo/buildWithParameters?branch=$1&environment=qa" + "\n"
          + ".*/origin/(dev)\\/.* http://ci.ajenkinsbox.com/job/foo/buildWithParameters?branch=$1&environment=dev" + "\n"
          + "";

  private PostReceiveHook hook;
  private SettingsValidationErrors errors;
  private Settings settings;
  private Repository repo;
  
  /**
   * Setup tasks
   */
  @Before
  public void setup() throws Exception {
    hook = new PostReceiveHook();
    settings = mock(Settings.class);
    errors = mock(SettingsValidationErrors.class);
    repo = mock(Repository.class);

    when(settings.getString(PostReceiveHook.REGEX))
      .thenReturn(REGEX_TEST);
  }

  /**
   * Validate that an error is added when the regex parameter is null
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenRegexNull() throws Exception {
    when(settings.getString(PostReceiveHook.REGEX)).thenReturn(null);
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(PostReceiveHook.REGEX), anyString());
  }

  /**
   * Validate that an error is added when the regex is empty
   * @throws Exception
   */
  @Test
  public void shouldAddErrorWhenRegexEmpty() throws Exception {
    when(settings.getString(PostReceiveHook.REGEX)).thenReturn("");
    hook.validate(settings, errors, repo);
    verify(errors).addFieldError(eq(PostReceiveHook.REGEX), anyString());
  }

}
