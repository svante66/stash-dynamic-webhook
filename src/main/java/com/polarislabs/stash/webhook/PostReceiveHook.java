package com.polarislabs.stash.webhook;

import com.atlassian.stash.hook.repository.AsyncPostReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.common.base.Strings;
import com.polarislabs.stash.webhook.service.ConcreteHttpClientFactory;
import com.polarislabs.stash.webhook.service.HttpClientFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 * 
 * @author Bryan Varner (bvarner)
 */
public class PostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private HttpClientFactory factory;
    
    /**
     * Field name for the Regex Patterns
     */
    public static final String REGEX = "regexPatterns";
    
    /**
     *
     * Field name for the ignore certs property
     */
    public static final String IGNORE_CERTS = "ignoreCerts";
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PostReceiveHook.class);

    public PostReceiveHook() {
        factory = new ConcreteHttpClientFactory();
    }

    @Override
    public void postReceive(@Nonnull RepositoryHookContext ctx, @Nonnull Collection<RefChange> changes) {
        // Parse the rules and replacement expressions.
        String[] rules = ctx.getSettings().getString(REGEX).split("\r\n|\r|\n");
        String[][] replacements = new String[rules.length][2];
        for (int i = 0; i < rules.length; i++) {
            String[] ruleSplit = rules[i].split("\\s+");

            replacements[i][0] = ruleSplit[0];
            replacements[i][1] = ruleSplit[1];
        }
        boolean ignoreCerts = ctx.getSettings().getBoolean(IGNORE_CERTS, false);

        Set<String> remoteUrls = new HashSet<String>();
        Pattern[] patterns = new Pattern[replacements.length];
        for (int i = 0; i < replacements.length; i++) {
            patterns[i] = Pattern.compile(replacements[i][0]);
        }

        for (RefChange change : changes) {
            LOGGER.debug("Check ref: " + change.getRefId());

            // If the ref Id matches any of the replacements, build the URL and add
            // it to the remoteUrls to invoke.
            // if change.getRefId().
            for (int i = 0; i < patterns.length; i++) {
                Matcher m1 = patterns[i].matcher(change.getRefId());
                if (m1.find()) {
                    LOGGER.debug("Ref: " + change.getRefId() + " MATCH: " + replacements[i][0]);
                    
                    // We need to url_encode the param values after a ? if there is one.
                    
                    String[] remoteParts = m1.replaceAll(replacements[i][1]).split("\\?|&");
                    StringBuilder sb = new StringBuilder(remoteParts[0]);
                    for (int j = 1; j < remoteParts.length; j++) {
                        // Add the separator.
                        sb.append( j == 1 ? "?" : "&");
                        
                        // Parse the = from the parameter so we can urlencode the values.
                        String paramName = remoteParts[j].substring(0, remoteParts[j].indexOf('='));
                        String paramValue = remoteParts[j].substring(remoteParts[j].indexOf('=') + 1);

                        try {
                            paramValue = URLEncoder.encode(paramValue, "UTF-8");
                        } catch (UnsupportedEncodingException ex) {}
                        sb.append(paramName).append("=").append(paramValue);
                    }
                    
                    remoteUrls.add(sb.toString());
                } else {
                    LOGGER.debug("Ref: " + change.getRefId() + " NO MATCH: " + replacements[i][0]);
                }
            }
        }

        for (String url : remoteUrls) {
            HttpClient client = null;
            try {
                client = factory.getHttpClient(url.startsWith("https"), ignoreCerts);

                HttpResponse response = client.execute(new HttpGet(url));
                LOGGER.debug("Retrieve response code: " + response.getStatusLine().getStatusCode());
            } catch (Exception e) {
                LOGGER.error("Error retrieving url '" + url + "'", e);
            } finally {
                if (client != null) {
                    client.getConnectionManager().shutdown();
                    LOGGER.debug("Successfully shutdown connection");
                }
            }
        }
    }

    @Override
    public void validate(@Nonnull Settings settings,
            @Nonnull SettingsValidationErrors errors,
            @Nonnull Repository repository) {

        final String regex = settings.getString(REGEX);
        if (Strings.isNullOrEmpty(regex)) {
            errors.addFieldError(REGEX, "At least one ruleset is required.");
        }
    }
}
